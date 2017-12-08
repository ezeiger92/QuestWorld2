package me.mrCookieSlime.QuestWorld.api.contract;

import java.util.List;

import org.bukkit.OfflinePlayer;

public interface IParty extends IStateful {
	OfflinePlayer getLeader();
	List<OfflinePlayer> getPlayers();
	List<OfflinePlayer> getPending();
	public int getSize();
	
	boolean isLeader(OfflinePlayer player);
	boolean hasInvited(OfflinePlayer p);
}
