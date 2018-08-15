package com.questworld.util;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

/**
 * Allows for implementing some functions from {@link VersionAdapter}, but not
 * others. This can be used if a version only needs minor changes over a
 * previous version. The intended use is different subversions within the same
 * main version (ie <tt>"v1_12_2_r1"</tt> vs <tt>"v1_12_2_r1_spigot"</tt>)<br/>
 * <br/>
 * Implements functions in {@link VersionAdapter} to throw an
 * {@link UnsupportedOperationException}. This way, {@link MultiAdapter} will
 * fall back to using the next lowest version.
 * 
 * @see VersionAdapter
 * @see MultiAdapter
 * 
 * @author ezeiger92
 */
public abstract class PartialAdapter extends VersionAdapter {

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

	@Override
	public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		throw new UnsupportedOperationException("Adaptor does not supply \"sendTitle\"");
	}

}
