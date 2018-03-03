package com.questworld.api;

import org.bukkit.event.entity.PlayerDeathEvent;

import com.questworld.api.contract.MissionEntry;

/**
 * Modifier for {@link MissionType} that allows custom behavior on death.
 * 
 * @see Manual
 * @see Ticking
 * 
 * @author Erik Zeiger
 */
public interface Decaying {
	/**
	 * Modifies a missions progress on player death.
	 * <p> The default behavior simply sets progress to <tt>0</tt>.
	 * 
	 * @param event The player death
	 * @param entry A MissionEntry that provides direct progress manipulation
	 */
	default void onDeath(PlayerDeathEvent event, MissionEntry entry) {
		entry.setProgress(0);
		return;
	}
}
