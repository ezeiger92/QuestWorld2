package me.mrCookieSlime.QuestWorld.api;

/**
 * Possible quest statuses.
 * 
 * @author mrCookieSlime
 */
public enum QuestStatus {
	/**
	 * The parent quest has not been completed or the player lacks the
	 * required permission.
	 */
	LOCKED,
	
	/**
	 * The player is part of a Party, but the quest forbids parties.
	 */
	LOCKED_NO_PARTY,
	
	/**
	 * The player is not part of a party or the players party is too small.
	 */
	LOCKED_PARTY_SIZE,
	
	/**
	 * The quest is active.
	 */
	AVAILABLE,
	
	/**
	 * The quests has been completed, but rewards have not been claimed.
	 */
	REWARD_CLAIMABLE,
	
	/**
	 * The quest is on cooldown after all rewards have been claimed.
	 */
	ON_COOLDOWN,
	
	/**
	 * The quest was <tt>"Single-Use"</tt> and has been completed.
	 */
	FINISHED,
	
	/**
	 * Something bad happened.
	 */
	@Deprecated
	UNKNOWN,
}
