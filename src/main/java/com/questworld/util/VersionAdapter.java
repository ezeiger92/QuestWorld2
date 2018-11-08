package com.questworld.util;

import java.util.Locale;

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
	protected abstract String forVersion();

	public abstract void makeSpawnEgg(@Mutable ItemStack result, EntityType mob);

	public abstract void makePlayerHead(@Mutable ItemStack result, OfflinePlayer player);

	public abstract ShapelessRecipe shapelessRecipe(String recipeName, ItemStack output);

	public abstract void sendActionbar(Player player, String message);
	
	public abstract void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut);

	private static final String processVersion(String in) {
		in = in.toUpperCase(Locale.ENGLISH).replace('.', '_');

		if (in.startsWith("V"))
			in = in.substring(1);

		return in;
	}

	private static final int apiLevel(String serverKind) {
		switch (serverKind) {
			case "TACO":
			case "TACOSPIGOT":
				return 3;

			case "PAPER":
			case "PAPERSPIGOT":
				return 2;

			case "SPIGOT":
				return 1;

			default:
				return 0;
		}
	}

	@Override
	public final int compareTo(VersionAdapter other) {
		String[] ourParts = processVersion(forVersion()).split("_");
		String[] theirParts = processVersion(other.forVersion()).split("_");

		int length = Math.min(ourParts.length, theirParts.length);

		for (int i = 0; i < length; ++i) {
			int ourSubver = 0;
			int theirSubver = 0;

			if (ourParts[i].startsWith("R")) {
				ourParts[i] = ourParts[i].substring(1);
				ourSubver = -1;
			}

			if (theirParts[i].startsWith("R")) {
				theirParts[i] = theirParts[i].substring(1);
				theirSubver = -1;
			}

			if (ourSubver != theirSubver) {
				return Integer.compare(theirSubver, ourSubver);
			}

			try {
				ourSubver = Integer.parseInt(ourParts[i]);
			}
			catch (NumberFormatException e) {}

			try {
				theirSubver = Integer.parseInt(theirParts[i]);
			}
			catch (NumberFormatException e) {}

			if (ourSubver != theirSubver) {
				return Integer.compare(theirSubver, ourSubver);
			}
			else if (ourSubver <= 0) {
				return Integer.compare(apiLevel(theirParts[i]), apiLevel(ourParts[i]));
			}
		}

		return Integer.compare(theirParts.length, ourParts.length);
	}

	@Override
	public String toString() {
		return processVersion(forVersion());
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof VersionAdapter)
			return toString().equals(((VersionAdapter) other).toString());

		return false;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
