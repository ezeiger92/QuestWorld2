package me.mrCookieSlime.QuestWorld.party;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.manager.PlayerManager;
import me.mrCookieSlime.QuestWorld.util.PlayerTools;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.google.gson.JsonObject;

public class Party {
	
	UUID leader;
	PlayerManager manager;
	ArrayList<UUID> members = new ArrayList<>();
	ArrayList<UUID> pending = new ArrayList<>();

	public Party(UUID uuid) {
		this.leader = uuid;
		this.manager = PlayerManager.of(uuid);
		
		members.addAll(manager.getTracker().getPartyMembers());
		pending.addAll(manager.getTracker().getPartyPending());
	}

	
	public static Party create(Player p) {
		PlayerManager.of(p).getTracker().setPartyLeader(p.getUniqueId());
		return new Party(p.getUniqueId());
	}
	
	public void invitePlayer(Player p) throws Exception {
		PlayerTools.sendTranslation(p, true, Translation.PARTY_GROUP_INVITE, Bukkit.getOfflinePlayer(leader).getName());
		
		JsonObject accept = new JsonObject();
		accept.addProperty("text", "ACCEPT");
		accept.addProperty("color", "green");
		accept.addProperty("bold", true);
		{
			JsonObject clickEvent = new JsonObject();
			clickEvent.addProperty("action", "run_command");
			clickEvent.addProperty("value", "/quests accept " + leader);
			
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
		
		pending.add(p.getUniqueId());
		save();
	}
	
	public void kickPlayer(OfflinePlayer target) {
		//OfflinePlayer target = null;
		boolean valid = false;
		List<Player> existingParty = new ArrayList<Player>();
		
		for (UUID member: getPlayers()) {
			OfflinePlayer p = Bukkit.getOfflinePlayer(member);
			if (p.isOnline())
				existingParty.add((Player)p);
			
			if(p.getUniqueId().equals(target.getUniqueId()))
				valid = true;
		}
		
		if(valid) {
			for(Player p : existingParty) {
				PlayerTools.sendTranslation(p, true, Translation.PARTY_PLAYER_KICK, target.getName());
			}
			
			members.remove(target.getUniqueId());
			PlayerManager.of(target.getUniqueId()).getTracker().setPartyLeader(null);
			save();
		}
	}
	
	public void playerJoin(Player p) {
		for (UUID member: getPlayers()) {
			Player player = Bukkit.getPlayer(member);
			if (player != null) 
				PlayerTools.sendTranslation(player, true, Translation.PARTY_PLAYER_JOIN, p.getName());
		}
		
		this.members.add(p.getUniqueId());
		PlayerTools.sendTranslation(p, true, Translation.PARTY_GROUP_JOIN, p.getName(), Bukkit.getOfflinePlayer(leader).getName());
		PlayerManager.of(p).getTracker().setPartyLeader(leader);
		if (pending.contains(p.getUniqueId()))
			pending.remove(p.getUniqueId());
		save();
	}
	
	public void playerLeave(Player player) {
		
	}
	
	public void abandon() {
		for (UUID member: members)
			PlayerManager.of(member).getTracker().setPartyLeader(null);
		
		members.clear();
		manager.getTracker().setPartyLeader(null);
		save();
	}

	public List<UUID> getPlayers() {
		List<UUID> players = new ArrayList<UUID>();
		players.add(leader);
		players.addAll(members);
		return players;
	}

	public int getSize() {
		return getPlayers().size();
	}
	
	public void save() {
		manager.getTracker().setPartyMembers(members);
		manager.getTracker().setPartyPending(pending);
	}

	public boolean isLeader(OfflinePlayer player) {
		return player.getUniqueId().equals(leader);
	}

	
	public boolean hasInvited(OfflinePlayer p) {
		return pending.contains(p.getUniqueId());
	}
}
