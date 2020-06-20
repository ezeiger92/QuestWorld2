package com.questworld.util.adapter;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.questworld.api.annotation.Mutable;
import com.questworld.util.Version;

public abstract class TypedAdapter extends VersionAdapter {
	public TypedAdapter(Version version) {
		super(version);
	}
	
	public TypedAdapter() {
		super();
	}
	
	public abstract void makePlayerHead(@Mutable ItemStack result, OfflinePlayer player);
	public abstract void makeSpawnEgg(@Mutable ItemStack result, EntityType mob);
	public abstract void sendActionbar(Player player, String message);
	public abstract void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut);
}
