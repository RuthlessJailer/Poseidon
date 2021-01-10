package com.ruthlessjailer.api.poseidon.example;

import com.ruthlessjailer.api.poseidon.Chat;
import com.ruthlessjailer.api.poseidon.PluginBase;

/**
 * @author RuthlessJailer
 */
public class Poseidon extends PluginBase {

	@Override
	public void onStart() {
		Chat.INSTANCE.setDebugMode(true);
		registerCommands(new TestCommand());
	}

}
