package com.ruthlessjailer.api.poseidon.example

import com.ruthlessjailer.api.poseidon.command.CommandBase
import com.ruthlessjailer.api.poseidon.command.SubCommand
import com.ruthlessjailer.api.poseidon.command.SuperiorCommand
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender

/**
 * @author RuthlessJailer
 */
object TestCommand : CommandBase("test2"), SuperiorCommand {

	override fun onExecute(sender: CommandSender, args: Array<out String>, commandLabel: String) {
		//nothing is here
	}

	@SubCommand("add %int")
	fun add(sender: CommandSender, args: Array<String>, int: Int) {
		send(sender, "&3$int &9+ &3$int &9= &6${int + int}")
	}

	@SubCommand("material|mat|m %enum", [Material::class])
	fun material(sender: CommandSender, args: Array<String>, material: Material) {
		println(material.createBlockData().asString)
	}

	@SubCommand("player|pl|p %offline")
	fun player(sender: CommandSender, args: Array<String>, offline: OfflinePlayer) {
		println(offline.name)
	}

	@SubCommand("yeet|xd")
	fun args(sender: CommandSender, args: Array<String>) {
		println(args[0])
	}


}