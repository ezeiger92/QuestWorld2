package com.questworld.api;

import org.bukkit.entity.Player;

import com.questworld.api.contract.MissionEntry;

/**
 * Modifier for {@link MissionType} that adds a ticking progress checker.
 * 
 * @see Decaying
 * @see Manual
 * 
 * @author Erik Zeiger
 */
public interface Ticking extends Manual {
	/**
	 * Modifies mission progress on player request.
	 * <p> The default behavior is to call the code provided for
	 * {@link Manual#onManual onManual}.
	 * 
	 * @param player The player sending the request
	 * @param entry A MissionEntry that provides direct progress manipulation
	 */
	default void onTick(Player player, MissionEntry entry) {
		onManual(player, entry);
	}
}
