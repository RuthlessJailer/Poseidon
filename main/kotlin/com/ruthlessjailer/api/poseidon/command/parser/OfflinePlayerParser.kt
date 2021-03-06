package com.ruthlessjailer.api.poseidon.command.parser

import com.ruthlessjailer.api.theseus.Common
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

/**
 * @author RuthlessJailer
 */
class OfflinePlayerParser : ArgumentParser<OfflinePlayer>(Regex("%ofp"), OfflinePlayer::class.java) {
	override fun isValid(argument: String): Boolean = true

	override fun parse(argument: String): OfflinePlayer = Bukkit.getOfflinePlayer(argument)

	override fun possibilities(): List<String> = Common.getPlayerNames()


}