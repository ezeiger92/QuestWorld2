package com.questworld.api;

import org.bukkit.entity.Player;

import com.questworld.api.contract.MissionEntry;

/**
 * Modifier for {@link MissionType} enables manual check within the quest book.
 * 
 * @see Decaying
 * @see Ticking
 * 
 * @author Erik Zeiger
 */
public interface Manual {
	/**
	 * Modifies mission progress on player request.
	 * 
	 * @param player The player sending the request
	 * @param entry A MissionEntry that provides direct progress manipulation
	 */
	void onManual(Player player, MissionEntry entry);

	/**
	 * Sets the button label inside the quest book for this type of mission.
	 * <p>
	 * The default is <tt>"&r> Click for manual check"</tt>. This will be moved into
	 * a translation in the future.
	 * 
	 * @return
	 */
	// TODO: Move to translation, along with all overrides
	default String getLabel() {
		return "&r> Click for manual check";
	};
}
