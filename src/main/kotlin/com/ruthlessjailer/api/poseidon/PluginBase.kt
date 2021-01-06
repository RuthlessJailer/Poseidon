package com.ruthlessjailer.api.poseidon

import com.ruthlessjailer.api.poseidon.command.CommandBase
import com.ruthlessjailer.api.poseidon.command.SubCommandException
import com.ruthlessjailer.api.poseidon.command.parser.*
import com.ruthlessjailer.api.theseus.ReflectUtil
import com.ruthlessjailer.api.theseus.multiversion.MinecraftVersion
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger


abstract class PluginBase : JavaPlugin(), Listener {

	companion object Info {
		@Volatile
		@JvmStatic
		var instance: PluginBase? = null
			get() = if (field == null) getPlugin(PluginBase::class.java) else field
			private set

		@Volatile
		lateinit var log: Logger
			private set

		@Volatile
		lateinit var thread: Thread
			private set

		protected var enabled = false
			private set

		fun getCurrentName(): String = instance?.name.toString()

		fun isMainThread(): Boolean = thread == Thread.currentThread()

		fun catchError(exception: Exception): Nothing = catchError(exception, false)

		fun catchError(exception: Exception, disable: Boolean): Nothing {
			log(
					"&c------------------- The plugin has encountered an error. --------------------",
					"&cPlease send the latest log &8(located in /logs/latest.log) &cto the developer.",
					"&cHere is some information about the server: ",
					"&c \tVersion: &b" + instance?.description?.name + " v" + instance?.description?.version,
					"&c \tSpigot: &1" + Bukkit.getServer().version + "&c",
					"&c \tBukkit: &5" + Bukkit.getServer().bukkitVersion,
					"&c \tCraftBukkit: &a" + MinecraftVersion.SERVER_VERSION,
					"&c \tJava: &6" + System.getProperty("java.version"))

			log("&c-----------------------------------------------------------------------------")

			when (exception) {
				is SubCommandException -> log("&cYou have failed to properly use the sub-command api. Please refer to the documentation/errors and check your methods.")
				is ReflectUtil.ReflectionException -> log("&cReflection error; your server version is either too old or not yet supported.")
				is ClassNotFoundException -> log("&4Shading or dependency error. Make sure you have all the plugins needed installed.")
			}

			log("&c-----------------------------------------------------------------------------")
			exception.printStackTrace()
			log("&c-----------------------------------------------------------------------------")

			if (disable) {
				log("&4Fatal error: disabling...")
				instance?.isEnabled = false
				log("&c-----------------------------------------------------------------------------")
			}

			throw RuntimeException()
		}

		private fun log(vararg string: String) = Chat.send(Bukkit.getConsoleSender(), *string)
	}

	final override fun onEnable() {
		if (!dataFolder.exists()) { //create plugin folder
			debug("Attempting to create plugin folder.")
			if (dataFolder.mkdirs()) {
				debug("Created folder ${dataFolder.name}.")
			} else {
				Chat.warning("Unable to create folder ${dataFolder.name}.")
			}
		}

		registerEvents(this)

		//register default argument parsers
		IntParser().register()
		DoubleParser().register()
		StringParser().register()
		OfflinePlayerParser().register()
		PlayerParser().register()
		

		try {
			onStart()
		} catch (e: Exception) {
			Chat.warning("Error while enabling.")
			catchError(e)
		}

		enabled = true
	}

	final override fun onDisable() {

		//TODO

		try {
			onStop()
		} catch (e: Exception) {
			Chat.warning("Error while disabling.")
			catchError(e)
		} finally {
			instance = null
			enabled = false
		}
	}

	final override fun onLoad() {
		if (!Bukkit.isPrimaryThread()) {
			catchError(IllegalStateException("Async plugin load."))
		}

		instance = this
		log = this.logger
		thread = Thread.currentThread()

		try {
			beforeStart()
		} catch (e: Exception) {
			Chat.warning("Error while loading.")
			catchError(e)
		}
	}

	protected fun registerEvents(vararg listeners: Listener) {
		for (listener in listeners) {
			server.pluginManager.registerEvents(listener, this)
		}
	}

	protected fun registerCommands(vararg commands: CommandBase) {
		for (command in commands) {
			command.register()
		}
	}

	/**
	 * Called in [onLoad].
	 */
	open fun beforeStart() {}

	/**
	 * Called in [onEnable].
	 */
	open fun onStart() {}

	/**
	 * Called in [onDisable].
	 */
	open fun onStop() {}


	private fun debug(vararg string: String) {
		Chat.debug("Plugin", string)
	}

	private fun debugf(format: String, vararg objects: Any?) {
		Chat.debugf("Plugin", format, objects)
	}

}