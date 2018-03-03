package com.questworld.manager;

import static com.questworld.util.json.Prop.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.questworld.QuestWorldPlugin;
import com.questworld.api.QuestWorld;
import com.questworld.api.Translation;
import com.questworld.api.contract.IPartyState;
import com.questworld.util.PlayerTools;
import com.questworld.util.json.JsonBlob;

public class Party implements IPartyState {
	private final UUID leader;
	private final ProgressTracker tracker;
	private Set<UUID> members = new HashSet<>();
	private Set<UUID> pending = new HashSet<>();

	public Party(UUID leader) {
		this.leader = leader;
		tracker = QuestWorldPlugin.instance().getImpl().getPlayerStatus(leader).getTracker();
		
		if(tracker.getPartyLeader() == null)
			tracker.setPartyLeader(leader);
		
		members.addAll(tracker.getPartyMembers());
		
		// This is actually not great. Players don't know which parties they're invited to
		//pending.addAll(tracker.getPartyPending());
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
				CLICK_RUN(p, () -> {
					if (hasInvited(p)) {
						int maxParty = QuestWorld.getPlugin().getConfig().getInt("party.max-members");
						if (getSize() >= maxParty) {
							PlayerTools.sendTranslation(p, true, Translation.PARTY_ERROR_FULL, Integer.toString(maxParty));
						}
						else playerJoin(p);
					}
				}))
			.add(" ")
			.add("DENY", DARK_RED, BOLD,
				HOVER_TEXT("Click to deny this Invitation", GRAY),
				CLICK_RUN(p, () -> {
					pending.remove(p.getUniqueId());
				}))
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
		QuestWorldPlugin.instance().getImpl().getPlayerStatus(p).getTracker().setPartyLeader(leader);
		pending.remove(p.getUniqueId());
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
		QuestWorldPlugin.instance().getImpl().getPlayerStatus(traitor.getUniqueId()).getTracker().setPartyLeader(null);
		save();
	}
	
	public void disband() {
		for (UUID member: members) {
			Player player = Bukkit.getPlayer(member);
			QuestWorldPlugin.instance().getImpl().getPlayerStatus(member).getTracker().setPartyLeader(null);
			
			if(player != null)
				PlayerTools.sendTranslation(player, true, Translation.PARTY_GROUP_DISBAND);
		}
		
		members.clear();
		tracker.setPartyLeader(null);
		save();
	}
	
	public Set<UUID> getPending() {
		return Collections.unmodifiableSet(pending);
	}

	@Override
	public int getSize() {
		// Leader is not included in "members", currently
		return members.size() + 1;
	}
	
	public void save() {
		tracker.setPartyMembers(members);
		// Invited players don't know they're invited, so this is bad
		//tracker.setPartyPending(pending);
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
