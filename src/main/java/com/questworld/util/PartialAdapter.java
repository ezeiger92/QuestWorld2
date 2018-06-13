package com.questworld.util;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

public class PartialAdapter extends VersionAdapter {

	@Override
	protected String forVersion() {
		throw new UnsupportedOperationException("Adaptor does not supply \"forVersion\"");
	}

	@Override
	public void makeSpawnEgg(ItemStack result, EntityType mob) {
		throw new UnsupportedOperationException("Adaptor does not supply \"makeSpawnEgg\"");

	}

	@Override
	public void makePlayerHead(ItemStack result, OfflinePlayer player) {
		throw new UnsupportedOperationException("Adaptor does not supply \"makePlayerHead\"");

	}

	@Override
	public ShapelessRecipe shapelessRecipe(String recipeName, ItemStack output) {
		throw new UnsupportedOperationException("Adaptor does not supply \"shapelessRecipe\"");
	}

	@Override
	public void sendActionbar(Player player, String message) {
		throw new UnsupportedOperationException("Adaptor does not supply \"sendActionbar\"");
	}

}
