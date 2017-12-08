package me.mrCookieSlime.QuestWorld.manager;

import java.util.ArrayList;
import java.util.List;

import me.mrCookieSlime.QuestWorld.QuestWorldPlugin;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.contract.IPartyState;
import me.mrCookieSlime.QuestWorld.util.PlayerTools;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.google.gson.JsonObject;

public class Party implements IPartyState {
	
	OfflinePlayer leader;
	PlayerStatus manager;
	ArrayList<OfflinePlayer> members = new ArrayList<>();
	ArrayList<OfflinePlayer> pending = new ArrayList<>();

	public Party(OfflinePlayer leader) {
		this.leader = leader;
		this.manager = QuestWorldPlugin.getImpl().getPlayerStatus(leader);
		
		members.addAll(manager.getTracker().getPartyMembers());
		pending.addAll(manager.getTracker().getPartyPending());
	}

	protected Party(Party source) {
		leader = source.leader;
		manager = source.manager;
		members.addAll(source.members);
		pending.addAll(source.pending);
	}
	
	public static Party create(Player p) {
		QuestWorldPlugin.getImpl().getPlayerStatus(p).getTracker().setPartyLeader(p);
		return new Party(p);
	}
	
	public void invitePlayer(Player p) throws Exception {
		PlayerTools.sendTranslation(p, true, Translation.PARTY_PLAYER_INVITED, leader.getName());
		
		JsonObject accept = new JsonObject();
		accept.addProperty("text", "ACCEPT");
		accept.addProperty("color", "green");
		accept.addProperty("bold", true);
		{
			JsonObject clickEvent = new JsonObject();
			clickEvent.addProperty("action", "run_command");
			clickEvent.addProperty("value", "/quests accept " + leader.getUniqueId());
			
			accept.add("clickEvent", clickEvent);
		}
		{
			JsonObject hoverEvent = new JsonObject();
			hoverEvent.addProperty("action", "show_text");
			{
				JsonObject hoverText = new JsonObject();
				hoverText.addProperty("text", "Click to deny this Invitation");
				hoverText.addProperty("color", "gray");
				
				hoverEvent.add("value", hoverText);
			}
			
			accept.add("hoverEvent", hoverEvent);
		}
		PlayerTools.tellraw(p, accept.toString());
		
		pending.add(p);
		save();
	}
	
	public void playerJoin(Player p) {
		for (OfflinePlayer player: getPlayers()) {
			if (player.isOnline()) 
				PlayerTools.sendTranslation((Player)player, true, Translation.PARTY_PLAYER_JOINED, p.getName());
		}
		
		members.add(p);
		PlayerTools.sendTranslation(p, true, Translation.PARTY_GROUP_JOIN, p.getName(), leader.getName());
		QuestWorldPlugin.getImpl().getPlayerStatus(p).getTracker().setPartyLeader(leader);
		pending.remove(p);
		save();
	}
	
	public enum LeaveReason {
		ABANDON,
		DISCONNECT,
		KICKED,
	}
	
	public void playerLeave(OfflinePlayer player, LeaveReason reason) {
		boolean valid = false;
		List<Player> existingParty = new ArrayList<Player>();
		
		for (OfflinePlayer p : getPlayers()) {
			if(p.getUniqueId().equals(player.getUniqueId()))
				valid = true;
			else if (p.isOnline())
				existingParty.add((Player)p);
		}
		
		if(!valid)
			return;
		
		switch(reason) {
		case ABANDON:
			for(Player p : existingParty) 
				PlayerTools.sendTranslation(p, true, Translation.PARTY_GROUP_ABANDON, player.getName());
			break;
			
		case DISCONNECT:
			for(Player p : existingParty) 
				PlayerTools.sendTranslation(p, true, Translation.PARTY_GROUP_ABANDON, player.getName());
			break;
			
		case KICKED:
			if(player.isOnline())
				PlayerTools.sendTranslation((Player)player, true, Translation.PARTY_PLAYER_KICKED, player.getName());
			
			for(Player p : existingParty) 
				PlayerTools.sendTranslation(p, true, Translation.PARTY_GROUP_KICK, player.getName());
			break;
		}
		
		members.remove(player);
		QuestWorldPlugin.getImpl().getPlayerStatus(player).getTracker().setPartyLeader(null);
		save();
	}
	
	public void disband() {
		for (OfflinePlayer member: members) {
			QuestWorldPlugin.getImpl().getPlayerStatus(member).getTracker().setPartyLeader(null);
			
			if(member.isOnline())
				PlayerTools.sendTranslation((Player)member, true, Translation.PARTY_GROUP_DISBAND);
		}
		
		members.clear();
		manager.getTracker().setPartyLeader(null);
		save();
	}
	
	@Override
	public OfflinePlayer getLeader() {
		return leader;
	}

	@Override
	public List<OfflinePlayer> getPlayers() {
		List<OfflinePlayer> players = new ArrayList<>();
		players.addAll(members);
		players.add(leader);
		return players;
	}
	
	@Override
	public List<OfflinePlayer> getPending() {
		return new ArrayList<>(pending);
	}

	@Override
	public int getSize() {
		// Leader is not included in "members", currently
		return members.size() + 1;
	}
	
	public void save() {
		manager.getTracker().setPartyMembers(members);
		manager.getTracker().setPartyPending(pending);
	}

	@Override
	public boolean isLeader(OfflinePlayer player) {
		return player.getUniqueId().equals(leader);
	}
	
	@Override
	public boolean hasInvited(OfflinePlayer p) {
		return pending.contains(p);
	}
}
