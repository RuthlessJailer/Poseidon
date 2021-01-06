package com.ruthlessjailer.api.poseidon

import com.ruthlessjailer.api.theseus.ReflectUtil
import com.ruthlessjailer.api.theseus.multiversion.MinecraftVersion
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Biome
import org.bukkit.command.Command
import org.bukkit.command.CommandMap
import org.bukkit.command.PluginCommand
import org.bukkit.command.SimpleCommandMap
import org.bukkit.enchantments.Enchantment

const val NMS = "net.minecraft.server"
const val OBC = "org.bukkit.craftbukkit"

/**
 * @author RuthlessJailer
 */
object Spigot {

	/**
	 * Registers a command into the [Bukkit command map][CommandMap].
	 *
	 * @param command the [Command] to register
	 */
	fun registerCommand(command: Command) {
		val commandMap: CommandMap = commandMap
		commandMap.register(command.label, command)
	}

	/**
	 * Unregisters a command and all of its aliases from the [Bukkit command map][CommandMap].
	 *
	 * @param label the name of the command to unregister
	 */
	fun unregisterCommand(label: String) {
		val command: PluginCommand? = Bukkit.getPluginCommand(label)
		val commandMap: MutableMap<String, Command> = ReflectUtil.getFieldValue(SimpleCommandMap::class.java, "knownCommands", commandMap)
		commandMap.remove(label)
		if (command != null) {
			if (command.isRegistered) {
				command.unregister(ReflectUtil.getFieldValue(Command::class.java, "commandMap", command))
			}
			for (alias: String in command.aliases) {
				commandMap.remove(alias)
			}
		}
	}

	/**
	 * @return the [Bukkit command map][CommandMap].
	 */
	val commandMap: SimpleCommandMap = ReflectUtil.invokeMethod(
			getOBCClass("CraftServer"),
			"getCommandMap",
			Bukkit.getServer())

	/**
	 * [Unregisters][Spigot.unregisterEnchantment] an [Enchantment] (if it exists), and then registers it again.
	 *
	 * @param enchantment the [Enchantment] to register
	 */
	fun registerEnchantment(enchantment: Enchantment) {
		unregisterEnchantment(enchantment)
		ReflectUtil.setField(Enchantment::class.java, "acceptingNew", null, true)
		Enchantment.registerEnchantment(enchantment)
	}

	/**
	 * Unregisters an [Enchantment].
	 *
	 * @param enchantment the [Enchantment] to register
	 */
	fun unregisterEnchantment(enchantment: Enchantment) {
		val byName: MutableMap<String, Enchantment> = ReflectUtil.getFieldValue(Enchantment::class.java, "byName", null)
		byName.remove(enchantment.name)
		val byKey: MutableMap<NamespacedKey, Enchantment> = ReflectUtil.getFieldValue(Enchantment::class.java, "byKey", null)
		byKey.remove(enchantment.key)
	}

	/**
	 * Wrapper for [Class.forName] but adds [OBC] to the beginning.
	 *
	 * @param pkg the path to the class
	 *
	 * @return the found class
	 *
	 * @throws ReflectUtil.ReflectionException if the class is not found
	 */
	fun getOBCClass(pkg: String): Class<*> = ReflectUtil.getClass(OBC + "." + MinecraftVersion.SERVER_VERSION + "." + pkg)

	/**
	 * Wrapper for [Class.forName] but adds [NMS] and [MinecraftVersion.CURRENT_VERSION] to
	 * the beginning.
	 *
	 * @param pkg the path to the class
	 *
	 * @return the found class
	 *
	 * @throws ReflectUtil.ReflectionException if the class is not found
	 */
	fun getNMSClass(pkg: String): Class<*> = ReflectUtil.getClass(NMS + "." + MinecraftVersion.SERVER_VERSION + "." + pkg)

	/**
	 * Minecraft-specific version of [ReflectUtil.getEnum]. Corrects some cases to ensure backwards and forwards compatibility.
	 *
	 * @param enumType the enum to search
	 * @param name     the name of the constant. Spaces will be replaced with underscores and it will be upper cased
	 *
	 * @return the found enum value or `null`
	 */
	fun <E : Enum<E>> getEnum(enumType: Class<E>, name: String, vararg legacyNames: String): E? {
		var parsed = name.toUpperCase().replace(" ".toRegex(), "_")

		if (MinecraftVersion.atLeast(MinecraftVersion.v1_13)) {
			if (enumType == Material::class.java) {
				if (parsed == "RAW_FISH") {
					parsed = "SALMON"
				} else if (parsed == "MONSTER_EGG") {
					parsed = "ZOMBIE_SPAWN_EGG"
				}
			} else if (enumType == Biome::class.java) {
				if (parsed == "ICE_MOUNTAINS") {
					parsed = "SNOWY_TAIGA"
				}
			}
		}

		return ReflectUtil.getEnum(enumType, parsed, *legacyNames)
	}


}