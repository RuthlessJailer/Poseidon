package com.ruthlessjailer.api.poseidon.example

import com.ruthlessjailer.api.poseidon.Chat
import com.ruthlessjailer.api.poseidon.PluginBase

/**
 * @author RuthlessJailer
 */
class Poseidon : PluginBase() {

	override fun onStart() {
		Chat.debugMode = true

		registerCommands(TestCommand)
		println("YEETED")
	}

}