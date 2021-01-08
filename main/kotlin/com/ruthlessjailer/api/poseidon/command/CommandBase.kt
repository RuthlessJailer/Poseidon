package com.ruthlessjailer.api.poseidon.command

import com.ruthlessjailer.api.poseidon.Chat
import com.ruthlessjailer.api.poseidon.PluginBase
import com.ruthlessjailer.api.poseidon.Spigot
import com.ruthlessjailer.api.theseus.Checks
import com.ruthlessjailer.api.theseus.Common
import com.ruthlessjailer.api.theseus.ReflectUtil
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.*
import org.bukkit.permissions.Permissible
import java.util.*

abstract class CommandBase : Command {

	constructor(label: String) : this(label.split("\\|")[0], parseAliases(label))

	private constructor(label: String, aliases: List<String>) : super(label, "description", "", aliases) {
		Checks.verify(
				this !is CommandExecutor && this !is TabCompleter,
				String.format(
						"Do not implement %s or %s when extending %s.",
						ReflectUtil.getPath(CommandExecutor::class.java), ReflectUtil.getPath(TabCompleter::class.java), ReflectUtil.getPath(javaClass)),
				CommandException::class.java)
	}

	companion object Info {

		const val DEFAULT_PERMISSION_MESSAGE = "&cYou do not have the permission &3\${permission}&c needed to run this command!"
		const val DEFAULT_PERMISSION_SYNTAX = "\${plugin.name}.command.\${command.label}"
		const val DEFAULT_SUB_COMMAND_PERMISSION_SYNTAX = "\${permission}.\${sub.command}"
		const val DEFAULT_PLAYER_FALSE_MESSAGE = "&cThis command must be executed by a player!"

		var starPermissionSyntax = PluginBase.instance?.name?.let {
			DEFAULT_PERMISSION_SYNTAX
					.replace("\${plugin.name}", it)
					.replace("\${command.label}", "*")
		}.toString()
			protected set

		@JvmStatic
		private fun parseAliases(string: String): List<String> {
			val aliases: Array<String> = string.split("\\|").toTypedArray()
			return if (aliases.size > 1) listOf(*Common.copyToEnd(aliases, 1)) else ArrayList()
		}
	}

	//format vars

	var customPermissionSyntax: String = DEFAULT_PERMISSION_SYNTAX
			.replace("\${plugin.name}", PluginBase.getCurrentName().toLowerCase())
			.replace("\${command.label}", label)
		protected set(value) {
			field = value
					.replace("\${plugin.name}", PluginBase.getCurrentName().toLowerCase())
					.replace("\${command.label}", label)
		}

	var customSubCommandPermissionSyntax: String = DEFAULT_SUB_COMMAND_PERMISSION_SYNTAX.replace("\${permission}", customPermissionSyntax)
		protected set(value) {
			field = value.replace("\${permission}", customPermissionSyntax)
		}

	var customPermissionMessage: String = DEFAULT_PERMISSION_MESSAGE //bukkit's name permissionMessage that's why it's called customPermissionMessage
			.replace("\${permission}", customPermissionSyntax)
		protected set(value) {
			field = value.replace("\${permission}", customPermissionSyntax)
		}
		get() = TODO() //Chat.colorize(field)

//	var helpMenuFormatOverride: HelpMenuFormat = HelpMenuFormat.DEFAULT_FORMAT
//		protected set

	//settings vars

	val superior = this is SuperiorCommand

	var registered = false
		private set

	var minArgs = 0
		protected set

	var tabCompleteSubCommands = true
		protected set

	var autoCheckPermissionForSubCommands = true
		protected set

	var autoGenerateHelpMenu = true

	//abstract/overridden methods

	final override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
		Chat.debug("Commands", "Command /$label with args ${args.contentToString()} executed by ${sender.name}.")

		try {
			if (!hasPermission(sender, customPermissionSyntax)) {
				Chat.send(sender, customPermissionMessage)
				return false
			}

			if (!(autoGenerateHelpMenu && args.isNotEmpty() && args[0].equals("help", ignoreCase = true))) { //don't run on help command
				onExecute(sender, args, commandLabel)
			}

			if (superior) {
				SubCommandManager.executeFor(this as SuperiorCommand, sender, args)
			}
		} catch (ignored: CommandException) {
		} catch (e: Exception) {
			PluginBase.catchError(e)
		}

		return true
	}

	final override fun tabComplete(sender: CommandSender, alias: String, args: Array<String>): List<String> = tabComplete(sender, alias, args, null)
	final override fun tabComplete(sender: CommandSender, alias: String, args: Array<String>, location: Location?): List<String> =
			if (superior && tabCompleteSubCommands) {
//				SubCommandManager.tabCompleteFor(this, sender, args)
				onTabComplete(sender, alias, args, location)
			} else {
				onTabComplete(sender, alias, args, location)
			}

	protected open fun onExecute(sender: CommandSender, args: Array<out String>, commandLabel: String) {}
	protected open fun onTabComplete(sender: CommandSender, alias: String, args: Array<String>, location: Location?): List<String> = ArrayList()

	//public methods

	fun register() {
		Checks.verify(!registered, "Command is already registered.", CommandException::class.java)
		val currentCommand = Bukkit.getPluginCommand(label)
		if (currentCommand != null) {
			val plugin = currentCommand.plugin.name
			if (plugin != PluginBase.getCurrentName()) {
				Chat.warning("Plugin $plugin is already using command /$label! Stealing...")
			}
			Spigot.unregisterCommand(label)
			Chat.debug("Commands", "Muahahahaha! Stole command /$label from plugin $plugin!")
		}
		Spigot.registerCommand(this)
		if (superior) {
			SubCommandManager.register((this as SuperiorCommand))
//			SubCommandManager.generateHelpMenu(this, helpMenuFormatOverride)
		}
		registered = true
	}

	fun unregister() {
		Checks.verify(registered, "Command is already unregistered.", CommandException::class.java)
		Spigot.unregisterCommand(label)
		registered = false
	}

	//util methods

	/**
	 * Joins the provided array.
	 *
	 * @param startIndex the starting index, inclusive
	 * @param args       the [String][] to parse
	 *
	 * @return the [joined][joinToString] [String], [copied][Common.copyFromStart] from the `startIndex`
	 */
	protected fun joinArgs(startIndex: Int, args: Array<String>): String {
		return Common.copyFromStart(args, startIndex).joinToString(" ")
	}

	protected fun send(sender: CommandSender, vararg messages: String) {
		Chat.send(sender, *messages)
	}

	protected fun sendf(sender: CommandSender, message: String, vararg parameters: Any?) {
		Chat.sendf(sender, message, *parameters)
	}

	protected fun broadcast(vararg messages: String) {
		Chat.broadcast(*messages)
	}

	protected fun broadcastf(messages: String, vararg parameters: Any?) {
		Chat.broadcastf(messages, *parameters)
	}

	/**
	 * Checks if given [Permissible] [is op][Permissible.isOp], has the [star permission][CommandBase.starPermissionSyntax], has the
	 * [command&#39;s permission][CommandBase.customPermissionSyntax], or has the given permission.
	 *
	 * @param permissible the [Permissible] to check
	 * @param permission  the permission to check (last resort)
	 *
	 * @return `true` if the [Permissible] [is op][Permissible.isOp], has the [star permission][CommandBase.starPermissionSyntax],
	 * has the [command&#39;s permission][CommandBase.customPermissionSyntax], or has the given permission; `false` if either argument is null or the
	 * given requirements are not met.
	 *
	 * @see Common.hasPermission
	 */
	fun hasPermission(permissible: Permissible?, permission: String?): Boolean {
		return if (permissible == null) false
		else permissible.isOp ||
			 permissible.hasPermission(starPermissionSyntax) ||
			 permissible.hasPermission(customPermissionSyntax) ||
			 (permission != null && permissible.hasPermission(permission))
	}

}