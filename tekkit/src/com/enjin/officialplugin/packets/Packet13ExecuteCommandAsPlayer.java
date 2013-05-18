package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.threaded.CommandExecuter;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class Packet13ExecuteCommandAsPlayer {
	
	public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
		try {
			String name = PacketUtilities.readString(in);
			String command = PacketUtilities.readString(in);
			Player p = Bukkit.getPlayerExact(name);
			//TODO: Add offline player support here
			if(p == null) {
				plugin.debug("Failed executing command \"" + command + "\" as player " + name + ". Player isn't online.");
				return;
			}
			plugin.debug("Executing command \"" + command + "\" as player " + name + ".");
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new CommandExecuter(p, command));
			//Bukkit.getServer().dispatchCommand(p, command);
		} catch (Throwable t) {
			Bukkit.getLogger().warning("Failed to dispatch command via 0x13, " + t.getMessage());
			t.printStackTrace();
		}
	}
}