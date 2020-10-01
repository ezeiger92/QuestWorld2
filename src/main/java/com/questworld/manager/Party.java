package com.questworld.manager;

import static com.questworld.util.json.Prop.BOLD;
import static com.questworld.util.json.Prop.CLICK_RUN;
import static com.questworld.util.json.Prop.DARK_RED;
import static com.questworld.util.json.Prop.GRAY;
import static com.questworld.util.json.Prop.GREEN;
import static com.questworld.util.json.Prop.HOVER_TEXT;

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
import com.questworld.api.lang.PartyReplacements;
import com.questworld.api.lang.PlayerReplacements;
import com.questworld.util.PlayerTools;
import com.questworld.util.json.JsonBlob;

public class Party implements IPartyState {
	private final UUID leader;
	private final ProgressTracker tracker;
	private Set<UUID> members = new HashSet<>();
	private Set<UUID> pending = new HashSet<>();

	public Party(UUID leader) {
		this.leader = leader;
		tracker = QuestWorldPlugin.getAPI().getPlayerStatus(leader).getTracker();

		if (tracker.getPartyLeader() == null)
			tracker.setPartyLeader(leader);

		members.addAll(tracker.getPartyMembers());

		// This is actually not great. Players don't know which parties they're invited
		// to
		// pending.addAll(tracker.getPartyPending());
	}

	protected Party(Party source) {
		leader = source.leader;
		tracker = source.tracker;
		members.addAll(source.members);
		pending.addAll(source.pending);
	}

	private static Set<Player> uuidToPlayer(Set<UUID> in) {
		return in.stream().map(Bukkit::getPlayer).filter(onlineMember -> onlineMember != null)
				.collect(Collectors.toSet());
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
	public Set<OfflinePlayer> getFullGroup() {
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

		PlayerTools.sendTranslation(p, true, Translation.PARTY_PLAYER_INVITED,
				new PartyReplacements(this));
		
		PlayerTools.tellraw(p, JsonBlob.fromLegacy(QuestWorld.translate(p, Translation.PARTY_ACCEPT_TEXT), GREEN, BOLD,
				HOVER_TEXT(JsonBlob.fromLegacy(QuestWorld.translate(p, Translation.PARTY_ACCEPT_HOVER), GRAY)),
				CLICK_RUN(p, () -> {
					if (hasInvited(p)) {
						int maxParty = QuestWorld.getPlugin().getConfig().getInt("party.max-members");
						if (getSize() >= maxParty) {
							PlayerTools.sendTranslation(p, true, Translation.PARTY_ERROR_FULL,
									new PartyReplacements(this));
						}
						else
							playerJoin(p);
					}
				})).add(" ").addLegacy(QuestWorld.translate(p, Translation.PARTY_DENY_TEXT), DARK_RED, BOLD,
						HOVER_TEXT(JsonBlob.fromLegacy(QuestWorld.translate(p, Translation.PARTY_DENY_HOVER), GRAY)), 
						CLICK_RUN(p, () -> {
							pending.remove(p.getUniqueId());
						})).toString());

		pending.add(p.getUniqueId());
		save();
	}

	@Override
	public void playerJoin(Player p) {
		for (OfflinePlayer member : getFullGroup())
			if (member.isOnline())
				PlayerTools.sendTranslation((Player) member, true, Translation.PARTY_GROUP_JOIN, new PartyReplacements(this), new PlayerReplacements(p));

		members.add(p.getUniqueId());
		PlayerTools.sendTranslation(p, true, Translation.PARTY_PLAYER_JOINED, new PartyReplacements(this), new PlayerReplacements(p));
		QuestWorldPlugin.getAPI().getPlayerStatus(p).getTracker().setPartyLeader(leader);
		pending.remove(p.getUniqueId());
		save();
	}

	@Override
	public void playerLeave(OfflinePlayer traitor, LeaveReason reason) {
		Set<UUID> group = getGroupUUIDs();

		if (!group.contains(traitor.getUniqueId()))
			return;

		Set<Player> remainingOnlineGroup = uuidToPlayer(group);

		switch (reason) {
			case ABANDON:
				for (Player p : remainingOnlineGroup)
					PlayerTools.sendTranslation(p, true, Translation.PARTY_GROUP_ABANDON, new PartyReplacements(this), new PlayerReplacements(traitor));
				break;

			case DISCONNECT:
				for (Player p : remainingOnlineGroup)
					PlayerTools.sendTranslation(p, true, Translation.PARTY_GROUP_ABANDON, new PartyReplacements(this), new PlayerReplacements(traitor));
				break;

			case KICKED:
				if (traitor.isOnline())
					PlayerTools.sendTranslation((Player) traitor, true, Translation.PARTY_PLAYER_KICKED, new PartyReplacements(this), new PlayerReplacements(traitor));

				for (Player p : remainingOnlineGroup)
					PlayerTools.sendTranslation(p, true, Translation.PARTY_GROUP_KICK, new PartyReplacements(this), new PlayerReplacements(traitor));
				break;
		}

		members.remove(traitor.getUniqueId());
		QuestWorldPlugin.getAPI().getPlayerStatus(traitor.getUniqueId()).getTracker().setPartyLeader(null);
		save();
	}

	public void disband() {
		for (UUID member : members) {
			Player player = Bukkit.getPlayer(member);
			QuestWorldPlugin.getAPI().getPlayerStatus(member).getTracker().setPartyLeader(null);

			if (player != null)
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
		// tracker.setPartyPending(pending);
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
