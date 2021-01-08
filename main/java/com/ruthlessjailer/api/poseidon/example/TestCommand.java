package com.ruthlessjailer.api.poseidon.example;

import com.ruthlessjailer.api.poseidon.command.CommandBase;
import com.ruthlessjailer.api.poseidon.command.SubCommand;
import com.ruthlessjailer.api.poseidon.command.SuperiorCommand;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

/**
 * @author RuthlessJailer
 */
public class TestCommand extends CommandBase implements SuperiorCommand {
	public TestCommand() {
		super("test2");
	}

	@SubCommand(format = "%enum %enum %enum")
	private void yeet(final CommandSender sender, final String[] args, final Material material, final ChatColor chatColor, final GameMode gameMode) {
		sendf(sender, "%s, %s, %s", material, chatColor, gameMode);
	}

}
