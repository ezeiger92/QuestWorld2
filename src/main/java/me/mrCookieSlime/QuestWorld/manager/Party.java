package me.mrCookieSlime.QuestWorld.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static me.mrCookieSlime.QuestWorld.util.json.Prop.*;

import me.mrCookieSlime.QuestWorld.QuestWorldPlugin;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.contract.IPartyState;
import me.mrCookieSlime.QuestWorld.util.PlayerTools;
import me.mrCookieSlime.QuestWorld.util.json.JsonBlob;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Party implements IPartyState {
	private final UUID leader;
	private final ProgressTracker tracker;
	private Set<UUID> members = new HashSet<>();
	private Set<UUID> pending = new HashSet<>();

	public Party(UUID leader) {
		this.leader = leader;
		tracker = QuestWorldPlugin.getImpl().getPlayerStatus(leader).getTracker();
		
		if(tracker.getPartyLeader() == null)
			tracker.setPartyLeader(leader);
		
		members.addAll(tracker.getPartyMembers());
		pending.addAll(tracker.getPartyPending());
	}

	protected Party(Party source) {
		leader = source.leader;
		tracker = source.tracker;
		members.addAll(source.members);
		pending.addAll(source.pending);
	}
	
	private static Set<Player> uuidToPlayer(Set<UUID> in) {
		return in.stream().map(Bukkit::getPlayer)
				.filter(onlineMember -> onlineMember != null).collect(Collectors.toSet());
	}
	
	public UUID getLeaderUUID() {
		return leader;
	}
	
	public Set<UUID> getGroupUUIDs() {
		Set<UUID> result = new HashSet<>(members.size() + 1);
		
		result.add(leader);
		result.addAll(members);
		
		return result;
	}
	
	@Override
	public Set<OfflinePlayer> getGroup() {
		return getGroupUUIDs().stream().map(Bukkit::getOfflinePlayer).collect(Collectors.toSet());
	}
	
	@Override
	public Set<OfflinePlayer> getMembers() {
		return members.stream().map(Bukkit::getOfflinePlayer).collect(Collectors.toSet());
	}
	
	@Override
	public OfflinePlayer getLeader() {
		return Bukkit.getOfflinePlayer(leader);
	}
	
	@Override
	public void invitePlayer(Player p) {
		
		PlayerTools.sendTranslation(p, true, Translation.PARTY_PLAYER_INVITED, Bukkit.getOfflinePlayer(leader).getName());
		
		PlayerTools.tellraw(p, new JsonBlob("ACCEPT", GREEN, BOLD,
				HOVER_TEXT("Click to accept this Invitation", GRAY),
				CLICK_RUN("/quests accept " + leader))
			.add(" ")
			.add("DENY", DARK_RED, BOLD,
				HOVER_TEXT("Click to deny this Invitation", GRAY))
			.toString());
		
		pending.add(p.getUniqueId());
		save();
	}
	
	@Override
	public void playerJoin(Player p) {
		for (OfflinePlayer member : getGroup())
			if (member.isOnline()) 
				PlayerTools.sendTranslation((Player)member, true, Translation.PARTY_PLAYER_JOINED, p.getName());
		
		members.add(p.getUniqueId());
		PlayerTools.sendTranslation(p, true, Translation.PARTY_GROUP_JOIN, p.getName(), Bukkit.getOfflinePlayer(leader).getName());
		QuestWorldPlugin.getImpl().getPlayerStatus(p).getTracker().setPartyLeader(leader);
		pending.remove(p);
		save();
	}
	
	@Override
	public void playerLeave(OfflinePlayer traitor, LeaveReason reason) {
		Set<UUID> group = getGroupUUIDs();
		
		if(!group.contains(traitor.getUniqueId()))
			return;
		
		Set<Player> remainingOnlineGroup = uuidToPlayer(group);
		
		switch(reason) {
		case ABANDON:
			for(Player p : remainingOnlineGroup) 
				PlayerTools.sendTranslation(p, true, Translation.PARTY_GROUP_ABANDON, traitor.getName());
			break;
			
		case DISCONNECT:
			for(Player p : remainingOnlineGroup) 
				PlayerTools.sendTranslation(p, true, Translation.PARTY_GROUP_ABANDON, traitor.getName());
			break;
			
		case KICKED:
			if(traitor.isOnline())
				PlayerTools.sendTranslation((Player)traitor, true, Translation.PARTY_PLAYER_KICKED, traitor.getName());
			
			for(Player p : remainingOnlineGroup) 
				PlayerTools.sendTranslation(p, true, Translation.PARTY_GROUP_KICK, traitor.getName());
			break;
		}
		
		members.remove(traitor.getUniqueId());
		QuestWorldPlugin.getImpl().getPlayerStatus(traitor.getUniqueId()).getTracker().setPartyLeader(null);
		save();
	}
	
	public void disband() {
		for (UUID member: members) {
			Player player = Bukkit.getPlayer(member);
			QuestWorldPlugin.getImpl().getPlayerStatus(member).getTracker().setPartyLeader(null);
			
			
			if(player != null)
				PlayerTools.sendTranslation(player, true, Translation.PARTY_GROUP_DISBAND);
		}
		
		members.clear();
		tracker.setPartyLeader(null);
		save();
	}
	
	public List<UUID> getPending() {
		return new ArrayList<>(pending);
	}

	@Override
	public int getSize() {
		// Leader is not included in "members", currently
		return members.size() + 1;
	}
	
	public void save() {
		tracker.setPartyMembers(members);
		tracker.setPartyPending(pending);
	}

	@Override
	public boolean isLeader(OfflinePlayer player) {
		return player.getUniqueId().equals(leader);
	}
	
	@Override
	public boolean hasInvited(OfflinePlayer player) {
		return pending.contains(player.getUniqueId());
	}
}
