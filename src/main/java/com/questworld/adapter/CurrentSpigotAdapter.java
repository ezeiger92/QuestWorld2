package com.questworld.adapter;

import org.bukkit.entity.Player;

import com.questworld.util.PartialAdapter;

// The only spigot-specific method is sendActionbar, everything else should fall through to CurrentAdapter
public class CurrentSpigotAdapter extends PartialAdapter {
	public CurrentSpigotAdapter() throws ClassNotFoundException {
		// Ensure we're running spigot
		Class.forName("org.spigotmc.SpigotConfig");
	}
	
	@Override
	protected String forVersion() {
		return "v1_12_r1_SPIGOT";
	}
	
	@Override
	public void sendActionbar(Player player, String message) {
		 player.spigot().sendMessage(
				net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
				net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message));
	}
}
