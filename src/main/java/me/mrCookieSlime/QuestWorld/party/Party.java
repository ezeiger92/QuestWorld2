package me.mrCookieSlime.QuestWorld.party;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.manager.PlayerManager;
import me.mrCookieSlime.QuestWorld.util.PlayerTools;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.google.gson.JsonObject;

public class Party {
	
	UUID leader;
	Set<UUID> members;
	PlayerManager manager;
	Set<UUID> pending;

	public Party(UUID uuid) {
		this.leader = uuid;
		this.manager = QuestWorld.getInstance().getManager(Bukkit.getOfflinePlayer(uuid));
		members = new HashSet<UUID>();
		pending = new HashSet<UUID>();
		if (manager.toConfig().contains("party.members")) {
			for (String member: manager.toConfig().getStringList("party.members")) {
				members.add(UUID.fromString(member));
			}
		}
		if (manager.toConfig().contains("party.pending-requests")) {
			for (String request: manager.toConfig().getStringList("party.pending-requests")) {
				pending.add(UUID.fromString(request));
			}
		}
	}

	
	public static Party create(Player p) {
		QuestWorld.getInstance().getManager(p).toConfig().set("party.associated", p.getUniqueId().toString());
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
	
	public void kickPlayer(String name) {
		OfflinePlayer target = null;
		List<Player> existingParty = new ArrayList<Player>();
		
		for (UUID member: getPlayers()) {
			Player p = Bukkit.getPlayer(member);
			if (p != null) {
				existingParty.add(p);
				if(p.getName().equalsIgnoreCase(name))
					target = p;
			}
		}
		
		if(target != null) {
			for(Player p : existingParty) {
				PlayerTools.sendTranslation(p, true, Translation.PARTY_PLAYER_KICK, name);
			}
			
			members.remove(target.getUniqueId());
			QuestWorld.getInstance().getManager(target).toConfig().set("party.associated", null);
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
		QuestWorld.getInstance().getManager(p).toConfig().set("party.associated", leader.toString());
		if (pending.contains(p.getUniqueId())) pending.remove(p.getUniqueId());
		save();
	}
	
	public void playerLeave(Player player) {
		
	}
	
	public void abandon() {
		for (UUID member: members)
			QuestWorld.getInstance().getManager(Bukkit.getOfflinePlayer(member)).toConfig().set("party.associated", null);
		
		members.clear();
		manager.toConfig().set("party.associated", null);
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
		List<String> list = new ArrayList<String>();
		for (UUID member: members) {
			list.add(member.toString());
		}
		manager.toConfig().set("party.members", list);
		
		List<String> invitations = new ArrayList<String>();
		for (UUID p: pending) {
			invitations.add(p.toString());
		}
		manager.toConfig().set("party.pending-requests", invitations);
	}

	public boolean isLeader(OfflinePlayer player) {
		return player.getUniqueId().equals(leader);
	}

	
	public boolean hasInvited(OfflinePlayer p) {
		return pending.contains(p.getUniqueId());
	}
}
