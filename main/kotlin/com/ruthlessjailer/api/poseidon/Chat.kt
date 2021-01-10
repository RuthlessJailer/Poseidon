package com.ruthlessjailer.api.poseidon

import com.ruthlessjailer.api.theseus.Common
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * @author RuthlessJailer
 */
object Chat {

	var debugMode = false

	fun send(what: String, vararg who: CommandSender) = who.forEach { to -> send(to, what) }
	fun send(what: String, who: Collection<CommandSender>) = who.forEach { to -> send(to, what) }
	fun send(what: Collection<String?>, vararg who: CommandSender) = who.forEach { to -> send(to, what) }
	fun send(what: Collection<String?>, who: Collection<CommandSender>) = who.forEach { to -> send(what, to) }
	fun send(who: CommandSender, vararg what: String?) = what.forEach { who.sendMessage(colorize(it)) }
	fun send(who: CommandSender, what: Collection<String?>) = what.forEach { who.sendMessage(colorize(it)) }
	fun sendf(who: CommandSender, format: String, vararg objects: Any?) = who.sendMessage(colorize(String.format(format, *objects)))
	fun sendf(who: Collection<CommandSender>, format: String, vararg objects: Any?) = who.forEach { to -> sendf(to, String.format(format, *objects)) }

	fun broadcast(vararg announcement: String?) {
		for (s in announcement) {
			for (player in Bukkit.getOnlinePlayers()) {
				send(player, s)
			}
			send(Bukkit.getConsoleSender(), s)
		}
	}

	fun broadcastf(format: String, vararg objects: Any?) = broadcast(String.format(format, *objects))

	fun broadcast(announcement: Collection<String?>) {
		for (s in announcement) {
			for (player in Bukkit.getOnlinePlayers()) {
				send(player, announcement)
			}
			send(Bukkit.getConsoleSender(), s)
		}
	}

	fun colorize(string: String?): String = ChatColor.translateAlternateColorCodes('&', string ?: "")
	fun colorize(stream: Stream<String>): List<String> = stream.map(::colorize).collect(Collectors.toList())
	fun colorize(strings: Collection<String>): List<String> = colorize(strings.stream())
	fun colorize(vararg strings: String): Array<String> = colorize(Arrays.stream(strings)).toTypedArray()

	fun strip(string: String?): String = string?.replace(Regex("([&${ChatColor.COLOR_CHAR}])([0-9a-fk-or])"), "") ?: ""
	fun strip(stream: Stream<String>): List<String> = stream.map(::strip).collect(Collectors.toList())
	fun strip(strings: Collection<String>): List<String> = strip(strings.stream())
	fun strip(vararg strings: String): Array<String> = strip(Arrays.stream(strings)).toTypedArray()

	fun debug(prefix: String, vararg messages: String?) {
		val parsed = getString(prefix)
		if (debugMode) {
			for (message in messages) {
				val text = String.format("[DEBUG] [%s] %s", if (parsed.isEmpty()) "|" else parsed, message)
				PluginBase.log.info(text)
			}
		}
	}

	fun debug(prefix: String, vararg objects: Any?) {
		if (debugMode) {
			for (message in Common.convert(
					listOf(objects)
										  ) { o: Any? -> if (o != null && o.javaClass.isArray) Arrays.toString(o as Array<Any?>?) else o.toString() }) {
				val text = String.format("[DEBUG] [%s] %s", if (prefix.isEmpty()) "|" else prefix, message)
				PluginBase.log.info(text)
			}
		}
	}

	fun debugf(prefix: String, format: String, vararg objects: Any?) {
		if (debugMode) {
			for (message in Common.convert(
					listOf(*objects)
										  ) { o: Any? -> if (o != null && o.javaClass.isArray) Arrays.toString(o as Array<Any?>?) else o.toString() }) {
				val text = String.format("[DEBUG] [%s] %s", if (prefix.isEmpty()) "|" else prefix, String.format(format, *objects))
				PluginBase.log.info(text)
			}
		}
	}

	fun info(vararg messages: String) = messages.forEach { PluginBase.log.info(it) }
	fun infof(format: String, vararg objects: Any?) = PluginBase.log.info(String.format(format, *objects))
	fun warning(vararg messages: String) = messages.forEach { PluginBase.log.warning(it) }
	fun warningf(format: String, vararg objects: Any?) = PluginBase.log.warning(String.format(format, *objects))
	fun severe(vararg messages: String) = messages.forEach { PluginBase.log.severe(it) }
	fun severef(format: String, vararg objects: Any?) = PluginBase.log.severe(String.format(format, *objects))


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