package me.mrCookieSlime.QuestWorld.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import me.mrCookieSlime.QuestWorld.Directories;
import me.mrCookieSlime.QuestWorld.QuestWorldPlugin;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestStatus;
import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Ticking;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.annotation.Nullable;
import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IPlayerStatus;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.event.MissionCompletedEvent;
import me.mrCookieSlime.QuestWorld.util.PlayerTools;
import me.mrCookieSlime.QuestWorld.util.Text;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerStatus implements IPlayerStatus {
	private static PlayerStatus of(OfflinePlayer player) {
		return (PlayerStatus)QuestWorld.getAPI().getPlayerStatus(player);
	}
	
	private static PlayerStatus of(UUID uuid) {
		return (PlayerStatus)QuestWorld.getAPI().getPlayerStatus(uuid);
	}
	
	private final UUID playerUUID;
	private final ProgressTracker tracker;
	
	public PlayerStatus(UUID uuid) {
		this.playerUUID = uuid;
		tracker = new ProgressTracker(uuid);
	}
	
	@Override
	public int countQuests(@Nullable ICategory root, @Nullable QuestStatus status) {
		if(root != null)
			return questsInCategory(root, status);
		
		int result = 0;
		for(ICategory category : QuestWorld.getFacade().getCategories())
			result += questsInCategory(category, status);

		return result;
	}
	
	private int questsInCategory(ICategory category, @Nullable QuestStatus status) {
		if(status == null)
			return category.getQuests().size();

		int result = 0;
		for(IQuest quest : category.getQuests())
			if(getStatus(quest) == status)
				++result;
		
		return result;
	}
	
	private static Player asOnline(UUID playerUUID) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
		if(player.isOnline())
			return (Player)player;
		
		throw new IllegalArgumentException("Player " + player.getName() + " ("+player.getUniqueId()+") is offline");
	}
	
	private static Optional<Player> ifOnline(UUID playerUUID) {
		return Optional.ofNullable(Bukkit.getPlayer(playerUUID));
	}
	
	public List<IMission> getActiveMissions(MissionType type) {
		List<IMission> result = new ArrayList<>();
		
		for(IMission task : QuestWorld.getViewer().getMissionsOf(type))
			if (isMissionActive(task))
				result.add(task);
		
		return result;
	}
	
	public void unload() {
		tracker.onSave();
	}
	
	public long getCooldownEnd(IQuest quest) {
		return tracker.getQuestRefresh(quest);
	}
	
	public boolean isWithinTimeframe(IMission task) {
		long date = tracker.getMissionEnd(task);
		if (date == 0) return true;
		return date > System.currentTimeMillis();
	}
	
	public boolean updateTimeframe(IMission task, int amount) {
		if (task.getTimeframe() == 0)
			return true;

		if (!isWithinTimeframe(task)) {
			tracker.setMissionEnd(task, null);
			tracker.setMissionProgress(task, 0);
			ifOnline(playerUUID).ifPresent(player ->
				PlayerTools.sendTranslation(player, false, Translation.NOTIFY_TIME_FAIL,
						task.getQuest().getName(),
						task.getText(),
						getProgress(task)+"/"+task.getAmount())
			);
			return false;
		}
		else if (getProgress(task) == 0 && amount > 0) {
			tracker.setMissionEnd(task, System.currentTimeMillis() + task.getTimeframe() * 60L * 1000L);
			
			ifOnline(playerUUID).ifPresent(player ->
				PlayerTools.sendTranslation(player, false, Translation.NOTIFY_TIME_START, task.getText(), Text.timeFromNum(task.getTimeframe()))
			);
		}
		return true;
	}
	
	@Override
	public boolean isMissionActive(IMission mission) {
		return getStatus(mission.getQuest()).equals(QuestStatus.AVAILABLE)
				&& !hasCompletedTask(mission)
				&& hasUnlockedTask(mission);
	}
	
	@Override
	public void update() {
		update(false);
	}
	
	public void update(boolean quest_check) {
		Player p = asOnline(playerUUID);
		
		if (quest_check)
			for (IMission mission: QuestWorld.getViewer().getTickingMissions())
				if (isMissionActive(mission))
					((Ticking) mission.getType()).onTick(p, new MissionSet.Result(mission, this));
		
		for (ICategory category: QuestWorld.getFacade().getCategories()) {
			for (IQuest quest: category.getQuests()) {
				if (getStatus(quest).equals(QuestStatus.AVAILABLE)) {
					boolean finished = quest.getMissions().size() != 0;
					for (IMission task: quest.getMissions()) {
						updateTimeframe(task, 0);
						if (!hasCompletedTask(task))
							finished = false;
					}
					
					if (finished) {
						tracker.setQuestFinished(quest, true);
						
						if (!quest.getAutoClaimed())
							tracker.setQuestStatus(quest, QuestStatus.REWARD_CLAIMABLE);
						else
							quest.completeFor(p);
					}
				}
				else if(getStatus(quest).equals(QuestStatus.ON_COOLDOWN))
					if(tracker.getQuestRefresh(quest) <= System.currentTimeMillis())
						tracker.setQuestStatus(quest, QuestStatus.AVAILABLE);
			}
		}
	}

	@Override
	public QuestStatus getStatus(IQuest quest) {
		Player p = asOnline(playerUUID);
		String worldName = p.getWorld().getName();

		if (!PlayerTools.checkPermission(p, quest.getPermission())) return QuestStatus.LOCKED_NO_PERM;
		if (quest.getParent() != null && !hasFinished(quest.getParent())) return QuestStatus.LOCKED_PARENT;
		if (!quest.getWorldEnabled(worldName) || !quest.getCategory().isWorldEnabled(worldName)) return QuestStatus.LOCKED_WORLD;
		
		Party party = (Party)QuestWorld.getParty(p);
		int partySize = party != null ? party.getSize() : 0;
		
		if (quest.getPartySize() == 0 && partySize > 0) return QuestStatus.LOCKED_NO_PARTY;
		if (quest.getPartySize() > 1 && partySize < quest.getPartySize()) return QuestStatus.LOCKED_PARTY_SIZE;
		
		return tracker.getQuestStatus(quest);
	}

	@Override
	public boolean hasFinished(IQuest quest) {
		return tracker.isQuestFinished(quest);
	}

	@Override
	public boolean hasCompletedTask(IMission task) {
		return getProgress(task) >= task.getAmount();
	}

	@Override
	public boolean hasUnlockedTask(IMission task) {
		if (!task.getQuest().getOrdered()) return true;
		
		List<? extends IMission> tasks = task.getQuest().getOrderedMissions();
		int index = tasks.indexOf(task) - 1;
		if (index < 0 || hasCompletedTask(task)) return true;
		else return !inDialogue && hasCompletedTask(tasks.get(index));
	}
	
	@Override
	public int getProgress(IMission task) {
		int progress = tracker.getMissionProgress(task);
		
		return Math.min(progress, task.getAmount());
	}
	
	@Override
	public int getProgress(IQuest quest) {
		int progress = 0;
		for(IMission task : quest.getMissions())
			if(hasCompletedTask(task))
				++progress;
		
		return progress;
	}
	
	@Override
	public int getProgress(ICategory category) {
		int progress = 0;
		for(IQuest quest : category.getQuests())
			if(hasFinished(quest))
				++progress;
		
		return progress;
	}
	
	@Override
	public String progressString(IQuest quest) {
		int progress = 0;
		for(IMission mission : quest.getMissions())
			if(hasCompletedTask(mission))
				++progress;
		
		int amount = quest.getMissions().size();
		
		return Text.progressBar(progress, amount, null);
	}
	
	@Override
	public String progressString() {
		int done = 0;
		int total = 0;

		for (ICategory category: QuestWorld.getFacade().getCategories())  {
			total += category.getQuests().size();
			
			for (IQuest quest: category.getQuests())
				if (hasFinished(quest))
					++done;
		}
		
		return Text.progressBar(done, total, null);
	}
	
	
	public void addProgress(IMission task, int amount) {
		int newProgress = Math.max(getProgress(task) + amount, 0);
		setProgress(task, Math.min(task.getAmount(), newProgress));
	}

	public void setProgress(IMission task, int amount) {
		Party party = (Party)QuestWorld.getParty(playerUUID);
		if(task.getQuest().supportsParties() && party != null)
			for(UUID memberUuid: party.getGroupUUIDs())
				of(memberUuid).setSingleProgress(task, amount);
		else
			setSingleProgress(task, amount);
	}
	
	private void setSingleProgress(IMission task, int amount) {
		amount = Math.min(amount, task.getAmount());
		if (!updateTimeframe(task, amount))
			return;
		
		tracker.setMissionProgress(task, amount);
		
		if(amount == task.getAmount()) {
			Bukkit.getPluginManager().callEvent(new MissionCompletedEvent(task));
			sendDialogue(playerUUID, task, task.getDialogue().iterator());
		}
	}
	
	protected boolean inDialogue = false;
	
	public static void sendDialogue(UUID uuid, IMission task, Iterator<String> dialogue) {
		of(uuid).inDialogue = false;
		ifOnline(uuid).ifPresent(player -> {
			String line;
			
			// Grab a line if we can
			// Otherwise if there was no dialogue, use the completion placeholder
			// If there are no lines, and there was dialogue, we're clearly done so return
			// Refactor for ezeiger92/QuestWorld2#57
			boolean hasNext = dialogue.hasNext();
			if(hasNext)
				line = dialogue.next();
			else if(task.getDialogue().isEmpty())
				line = "*";
			else 
				return;
			
			if(line.equals("*"))
				// Change for ezeiger92/QuestWorld2#43 - Remove default complete message if dialog is present
				// Previously "check !task.getType().getID().equals("ACCEPT_QUEST_FROM_NPC") && "
				// This was done to keep quests quiet when interacting with citizens
				PlayerTools.sendTranslation(player, false, Translation.NOTIFY_COMPLETED,
						task.getQuest().getName(),
						task.getText());
			else
				sendDialogueComponent(player, line);
			
			if(hasNext) {
				of(uuid).inDialogue = true;
				Bukkit.getScheduler().scheduleSyncDelayedTask(QuestWorld.getPlugin(),
						() -> sendDialogue(uuid, task, dialogue), 70L);
			}
		});
	}

	private static void sendDialogueComponent(Player player, String line) {
		line = line.replace("@p", player.getName());

		// TODO: remove
		line = line.replace("<player>", player.getName());
		
		if(line.startsWith("/"))
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), line.substring(1));

		else {
			line = Text.deserializeNewline(Text.colorize(QuestWorld.getPlugin().getConfig().getString("dialogue.prefix"))) + line;
			
			player.sendMessage(line);
		}
	}

	public void completeQuest(IQuest quest) {
		if (quest.getRawCooldown() < 0)
			tracker.setQuestStatus(quest, QuestStatus.FINISHED);
		
		else {
			if (quest.getRawCooldown() == 0)
				tracker.setQuestStatus(quest, QuestStatus.AVAILABLE);
			
			else {
				tracker.setQuestStatus(quest, QuestStatus.ON_COOLDOWN);
				tracker.setQuestRefresh(quest, System.currentTimeMillis() + quest.getRawCooldown());
			}
			
			for (IMission task: quest.getMissions()) {
				 setProgress(task, 0);
			 }
		}
	}
	
	public ProgressTracker getTracker() {
		return tracker;
	}
	

	public static void clearAllCategoryData(ICategory category) {
		clearDataImpl(category);
	}
	
	// Right, so this function USED to loop through every file in data-storage/Quest World on
	// the main thread. W H A T
	public static void clearAllQuestData(IQuest quest) {
		clearDataImpl(quest);
	}
	
	public static void clearAllMissionData(IMission mission) {
		clearDataImpl(mission);
	}
	
	private static void clearDataImpl(Object object) {
		Consumer<ProgressTracker> callback;
		
		if(object instanceof IQuest)
			callback = tracker -> tracker.clearQuest((IQuest)object);
		else if(object instanceof ICategory)
			callback = tracker -> tracker.clearCategory((ICategory)object);
		else if(object instanceof IMission)
			callback = tracker -> tracker.clearMission((IMission)object);
		else {
			throw new IllegalArgumentException("clearData called with: " + object.getClass().getSimpleName());
		}
		
		Bukkit.getScheduler().runTaskAsynchronously(QuestWorld.getPlugin(), () -> {
			// First: clear all the quest data on a new thread
			for (File file: Directories.listFiles(QuestWorldPlugin.instance().getDataFolders().playerdata, (file, name) -> name.endsWith(".yml"))) {
				String uuid = file.getName().substring(0, file.getName().length() - 4);
				try {
					ProgressTracker t = new ProgressTracker(UUID.fromString(uuid));
					callback.accept(t);
					t.onSave();
				}
				// File name was not 
				catch(IllegalArgumentException e) {}
			}

			// Second: go back to the main thread and make sure all player managers know what happened
			Bukkit.getScheduler().callSyncMethod(QuestWorld.getPlugin(), () -> {
				for(Player player : Bukkit.getOnlinePlayers())
					callback.accept(of(player).getTracker());
				
				return false;
			});
		});
	}

	@Override
	public boolean hasDeathEvent(IMission mission) {
		return ifOnline(playerUUID).map(player -> {
			IQuest quest = mission.getQuest();
			String playerWorld = player.getWorld().getName();
			
			return getStatus(quest).equals(QuestStatus.AVAILABLE)
					&& mission.getDeathReset()
					&& quest.getWorldEnabled(playerWorld)
					&& quest.getCategory().isWorldEnabled(playerWorld);
		})
		.orElse(false);
	}
}
