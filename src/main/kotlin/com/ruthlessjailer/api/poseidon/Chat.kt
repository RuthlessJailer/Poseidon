package com.ruthlessjailer.api.poseidon

import com.ruthlessjailer.api.theseus.Chat
import com.ruthlessjailer.api.theseus.Common
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.util.*

/**
 * @author RuthlessJailer
 */
object Chat {

	var debugMode = false

	fun sendf(who: CommandSender, format: String, vararg objects: Any?) {
		who.sendMessage(colorize(String.format(format, *objects)))
	}

	fun send(who: CommandSender, vararg what: String?) {
		for (s in what) {
			who.sendMessage(colorize(s))
		}
	}

	fun send(who: CommandSender, what: Collection<String?>) {
		for (s in what) {
			who.sendMessage(colorize(s))
		}
	}

	fun send(what: String, vararg who: CommandSender) {
		for (sender in who) {
			send(sender, what)
		}
	}

	fun send(what: Collection<String?>, vararg who: CommandSender) {
		for (sender in who) {
			send(sender, what)
		}
	}

	fun send(what: String, who: Collection<CommandSender>) {
		for (sender in who) {
			send(sender, what)
		}
	}

	fun sendf(who: Collection<CommandSender>, format: String, vararg objects: Any?) {
		for (sender in who) {
			sendf(sender, String.format(format, *objects))
		}
	}

	fun send(what: Collection<String?>, who: Collection<CommandSender>) {
		for (sender in who) {
			send(sender, what)
		}
	}

	fun broadcast(vararg announcement: String?) {
		for (s in announcement) {
			for (player in Bukkit.getOnlinePlayers()) {
				send(player, s)
			}
			send(Bukkit.getConsoleSender(), s)
		}
	}

	fun broadcastf(format: String, vararg objects: Any?) {
		broadcast(String.format(format, *objects))
	}

	fun broadcast(announcement: Collection<String?>) {
		for (s in announcement) {
			for (player in Bukkit.getOnlinePlayers()) {
				send(player, announcement)
			}
			send(Bukkit.getConsoleSender(), s)
		}
	}

	fun colorize(string: String?): String {
		return ChatColor.translateAlternateColorCodes('&', getString(string))
	}

	fun colorize(vararg strings: String): Array<String?> {
		TODO()
//		return Arrays.stream(strings).map(Function<String, String?> { obj: String -> obj.colorize() }).collect(Collectors.toList()).toTypedArray()
	}

	fun colorize(strings: Collection<String>): List<String> {
		TODO()
//		return strings.stream().map(obj: String -> obj.colorize() ).collect(Collectors.toList())
	}

	fun stripColors(string: String?): String {
		return getString(string).replace(Regex("([&${ChatColor.COLOR_CHAR}])([0-9a-fk-or])"), "")
	}

	fun stripColors(vararg strings: String): Array<String?> {
		TODO()
//		return Arrays.stream(strings).map(Function<String, String?> { obj: String -> obj.stripColors() }).collect(Collectors.toList()).toTypedArray()
	}

	/**
	 * Prints a debug message.
	 * Will only trigger if [Chat.isDebugMode].
	 *
	 *
	 * Format: `"[00:00:00 INFO]: [DEBUG] [PREFIX] message"`
	 * If prefix is null or empty: `"[00:00:00 INFO]: [DEBUG] [|] message"`
	 */
	fun debug(prefix: String, vararg messages: String?) {
		val parsed = getString(prefix)
		if (debugMode) {
			for (message in messages) {
				val text = String.format("[DEBUG] [%s] %s", if (parsed.isEmpty()) "|" else parsed, message)
				if (PluginBase.log != null) {
					PluginBase.log.info(text)
				} else {
					println(StringBuilder(text).insert(7, ":"))
				}
			}
		}
	}

	/**
	 * Prints a debug message.
	 * Will only trigger if [Chat.isDebugMode].
	 *
	 *
	 * Format: `"[00:00:00 INFO]: [DEBUG] [PREFIX] message"`
	 * If prefix is null or empty: `"[00:00:00 INFO]: [DEBUG] [|] message"`
	 */
	fun debug(prefix: String, vararg objects: Any?) {
		if (debugMode) {
			for (message in Common.convert(
					listOf(objects)
										  ) { o: Any? -> if (o != null && o.javaClass.isArray) Arrays.toString(o as Array<Any?>?) else o.toString() }) {
				val text = String.format("[DEBUG] [%s] %s", if (prefix.isEmpty()) "|" else prefix, message)
				if (PluginBase.log != null) {
					PluginBase.log.info(text)
				} else {
					println(StringBuilder(text).insert(7, ":"))
				}
			}
		}
	}


	fun debugf(prefix: String, format: String, vararg objects: Any?) {
		if (debugMode) {
			for (message in Common.convert(
					listOf(*objects)
										  ) { o: Any? -> if (o != null && o.javaClass.isArray) Arrays.toString(o as Array<Any?>?) else o.toString() }) {
				val text = String.format("[DEBUG] [%s] %s", if (prefix.isEmpty()) "|" else prefix, String.format(format, *objects))
				if (PluginBase.log != null) {
					PluginBase.log.info(text)
				} else {
					println(StringBuilder(text).insert(7, ":"))
				}
			}
		}
	}

	fun info(vararg messages: String) {
		for (message in messages) {
			if (PluginBase.log != null) {
				PluginBase.log.info(message)
			} else {
				println("[INFO]: $message")
			}
		}
	}

	fun infof(format: String, vararg objects: Any?) {
		if (PluginBase.log != null) {
			PluginBase.log.info(String.format(format, *objects))
		} else {
			System.err.println("[INFO]: " + String.format(format, *objects))
		}
	}

	fun warning(vararg messages: String) {
		for (message in messages) {
			if (PluginBase.log != null) {
				PluginBase.log.warning(message)
			} else {
				println("[WARN]: $message")
			}
		}
	}

	fun warningf(format: String, vararg objects: Any?) {
		if (PluginBase.log != null) {
			PluginBase.log.warning(String.format(format, *objects))
		} else {
			System.err.println("[WARN]: " + String.format(format, *objects))
		}
	}

	fun severe(vararg messages: String) {
		for (message in messages) {
			if (PluginBase.log != null) {
				PluginBase.log.severe(message)
			} else {
				System.err.println("[ERROR]: $message")
			}
		}
	}

	fun severef(format: String, vararg objects: Any?) {
		if (PluginBase.log != null) {
			PluginBase.log.severe(String.format(format, *objects))
		} else {
			System.err.println("[ERROR]: " + String.format(format, *objects))
		}
	}

	/*private static String consoleColorize(final String string) {
		return ConsoleColor.translateAlternateColorCodes('&', getString(string));
	}

	public static String[] consoleColorize(final String... strings) {
		if (strings == null) { return new String[]{}; }
		return Arrays.stream(strings).map(Chat::consoleColorize).collect(Collectors.toList()).toArray(new String[strings.length]);
	}*/

	/*private static String consoleColorize(final String string) {
		return ConsoleColor.translateAlternateColorCodes('&', getString(string));
	}

	public static String[] consoleColorize(final String... strings) {
		if (strings == null) { return new String[]{}; }
		return Arrays.stream(strings).map(Chat::consoleColorize).collect(Collectors.toList()).toArray(new String[strings.length]);
	}*/
	fun bungeeColorize(string: String?): String {
		return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', getString(string))
	}

	fun bungeeColorize(vararg strings: String): Array<String?> {
		TODO()
//		return Arrays.stream(strings).map(Function<String, String?> { obj: String -> obj.bungeeColorize() }).collect(Collectors.toList()).toTypedArray()
	}

	/*@AllArgsConstructor
	public enum ConsoleColor {//TODO: add JANSI compatibility

		CODES("r?0426153f"),
		RESET("\u001B[0m"),//r
		UNKNOWN("\u001B[7m"),//?
		BLACK("\u001B[30m"),//0
		RED("\u001B[31m"),//4
		GREEN("\u001B[32m"),//2
		GOLD("\u001B[33m"),//6
		BLUE("\u001B[34m"),//1
		PURPLE("\u001B[35m"),//5
		AQUA("\u001B[36m"),//3
		WHITE("\u001B[37m");//f

		private final String value;

		public static String translateAlternateColorCodes(final char altColorChar, final String textToTranslate) {
			final StringBuilder sb = new StringBuilder(textToTranslate);
			for (int i = 0; i < sb.length(); i++) {
				if (sb.charAt(i) == altColorChar && ConsoleColor.CODES.toString().indexOf(sb.charAt(i + 1)) != -1) {
					sb.replace(i, i + 2, ConsoleColor.values()[ConsoleColor.CODES.toString()
																				 .indexOf(
																						 sb.charAt(i + 1))].toString());
				}
			}
			return sb.toString();
		}


		@Override
		public String toString() {
			return this.value;
		}
	}*/

	/*@AllArgsConstructor
	public enum ConsoleColor {//TODO: add JANSI compatibility

		CODES("r?0426153f"),
		RESET("\u001B[0m"),//r
		UNKNOWN("\u001B[7m"),//?
		BLACK("\u001B[30m"),//0
		RED("\u001B[31m"),//4
		GREEN("\u001B[32m"),//2
		GOLD("\u001B[33m"),//6
		BLUE("\u001B[34m"),//1
		PURPLE("\u001B[35m"),//5
		AQUA("\u001B[36m"),//3
		WHITE("\u001B[37m");//f

		private final String value;

		public static String translateAlternateColorCodes(final char altColorChar, final String textToTranslate) {
			final StringBuilder sb = new StringBuilder(textToTranslate);
			for (int i = 0; i < sb.length(); i++) {
				if (sb.charAt(i) == altColorChar && ConsoleColor.CODES.toString().indexOf(sb.charAt(i + 1)) != -1) {
					sb.replace(i, i + 2, ConsoleColor.values()[ConsoleColor.CODES.toString()
																				 .indexOf(
																						 sb.charAt(i + 1))].toString());
				}
			}
			return sb.toString();
		}


		@Override
		public String toString() {
			return this.value;
		}
	}*/
	private fun getString(string: String?): String = string ?: ""


}