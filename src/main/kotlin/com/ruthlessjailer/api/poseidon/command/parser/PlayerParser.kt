package com.ruthlessjailer.api.poseidon.command.parser

import com.ruthlessjailer.api.theseus.Common
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * @author RuthlessJailer
 */
class PlayerParser : ArgumentParser<Player>(type = Player::class.java, format = "player") {
	override fun isValid(argument: String): Boolean = Common.getPlayerNames().contains(argument)

	override fun parse(argument: String): Player? = Bukkit.getPlayer(argument)

	override fun possibilities(): List<String> = Common.getPlayerNames()


}