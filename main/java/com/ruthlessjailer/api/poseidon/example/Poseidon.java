package com.ruthlessjailer.api.poseidon.example;

import com.ruthlessjailer.api.poseidon.PluginBase;

/**
 * @author RuthlessJailer
 */
public class Poseidon extends PluginBase {

	@Override
	public void onStart() {
		registerCommands(new TestCommand());
	}
}
