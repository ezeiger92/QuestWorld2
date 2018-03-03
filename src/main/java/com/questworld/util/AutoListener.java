package com.questworld.util;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class AutoListener implements Listener {

	protected final void register(Plugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
}
