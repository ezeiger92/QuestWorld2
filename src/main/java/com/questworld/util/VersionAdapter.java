package com.questworld.util;

import java.util.Locale;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

import com.questworld.api.annotation.Mutable;

public abstract class VersionAdapter implements Comparable<VersionAdapter> {
	// "V1_12_2_R1_SPIGOT", for example
	protected abstract String forVersion();
	
	public abstract void makeSpawnEgg(@Mutable ItemStack result, EntityType mob);
	public abstract void makePlayerHead(@Mutable ItemStack result, OfflinePlayer player);
	public abstract ShapelessRecipe shapelessRecipe(String recipeName, ItemStack output);
	public abstract void sendActionbar(Player player, String message);
	
	private static String processVersion(String in) {
		in = in.toUpperCase(Locale.ENGLISH).replace('.', '_');
		
		if(in.startsWith("V"))
			in = in.substring(1);
		
		return in;
	}
	
	private static int pseudoVersion(String serverKind) {
		switch(serverKind) {
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
		String[] ourParts = toString().split("_");
		String[] theirParts = other.toString().split("_");
		
		int length = Math.min(ourParts.length, theirParts.length);
		
		for(int i = 0; i < length; ++i) {
			int ourSubver = 0;
			int theirSubver = 0;
			
			if(ourParts[i].startsWith("R")) {
				ourParts[i] = ourParts[i].substring(1);
				ourSubver = -1;
			}
			
			if(theirParts[i].startsWith("R")) {
				theirParts[i] = theirParts[i].substring(1);
				theirSubver = -1;
			}
			
			// Given strings:
			// V1_12_2
			// V1_12_R1
			if(ourSubver != theirSubver)
				// Flipped compare because we want 0, -1
				return Integer.compare(theirSubver, ourSubver);
			
			try {
				ourSubver = Integer.parseInt(ourParts[i]);
			}
			catch(NumberFormatException e) {}
			
			try {
				theirSubver = Integer.parseInt(theirParts[i]);
			}
			catch(NumberFormatException e) {}
			
			if(ourSubver != theirSubver)
				// Flipped compare because we want 3, 2, 1
				return Integer.compare(theirSubver, ourSubver);
			else if(ourSubver <= 0) {
				return Integer.compare(pseudoVersion(theirParts[i]), pseudoVersion(ourParts[i]));
			}
			// Else equal parts, continue
		}
		
		return Integer.compare(theirParts.length, ourParts.length);
	}
	
	@Override
	public String toString() {
		return processVersion(forVersion());
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof VersionAdapter)
			return toString().equals(((VersionAdapter)other).toString());
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
