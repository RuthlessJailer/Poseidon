package com.ruthlessjailer.api.poseidon.command

import com.ruthlessjailer.api.poseidon.PluginBase
import com.ruthlessjailer.api.poseidon.task.manager.TaskManager
import org.bukkit.command.CommandSender


/**
 * @author RuthlessJailer
 */
open class MainCommand : CommandBase(PluginBase.getCurrentName().toLowerCase()), SuperiorCommand {

	open fun reload(sender: CommandSender) {
		TaskManager.async.later {
			PluginBase.instance.reloadConfigs()
			send(sender, "Reloaded configs.")
		}
	}

}