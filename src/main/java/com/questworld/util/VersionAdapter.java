package com.questworld.util;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

import com.questworld.api.annotation.Mutable;

/**
 * Provides an interface for common operations that require different code in
 * different server versions. Please note: This interface may expand at any
 * time. New methods will be marked deprecated and given a safe, dummy
 * implementation. The next minor API version will change them into abstract
 * methods.<br/>
 * <br/>
 * Originally created to handle Spigot's method of sending actionbar messages,
 * this can be used to support legacy server versions to a degree.
 * 
 * @see PartialAdapter
 * 
 * @author ezeiger92
 */
public abstract class VersionAdapter implements Comparable<VersionAdapter> {
	
	public abstract void makeSpawnEgg(@Mutable ItemStack result, EntityType mob);

	public abstract void makePlayerHead(@Mutable ItemStack result, OfflinePlayer player);

	public abstract ShapelessRecipe shapelessRecipe(String recipeName, ItemStack output);

	public abstract void sendActionbar(Player player, String message);

	public abstract void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut);
	
	public abstract void setItemDamage(@Mutable ItemStack result, int damage);
	
	private Version version;
	
	public VersionAdapter(Version version) {
		this.version = version;
	}
	
	public Version getVersion() {
		return version;
	}
	
	protected void setVersion(Version version) {
		this.version = version;
	}

	@Override
	public final int compareTo(VersionAdapter other) {
		return version.compareTo(other.version);
	}

	@Override
	public String toString() {
		return version.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof VersionAdapter)
			return version.equals(((VersionAdapter)other).version);

		return false;
	}

	@Override
	public int hashCode() {
		return ("va-" + toString()).hashCode();
	}
}
