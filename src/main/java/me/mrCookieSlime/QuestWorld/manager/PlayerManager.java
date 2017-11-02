package me.mrCookieSlime.QuestWorld.manager;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Manual;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestStatus;
import me.mrCookieSlime.QuestWorld.api.Ticking;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.contract.IStateful;
import me.mrCookieSlime.QuestWorld.party.Party;
import me.mrCookieSlime.QuestWorld.util.PlayerTools;
import me.mrCookieSlime.QuestWorld.util.Text;

import org.bukkit.Bukkit;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Player;

public class PlayerManager {
	
	private UUID uuid;
	private IStateful last;
	
	private Stack<Integer> pages = new Stack<>();
	
	private final ProgressTracker tracker;
	
	public PlayerManager(AnimalTamer p) {
		this(p.getUniqueId());
	}
	
	public PlayerManager(UUID uuid) {
		this.uuid = uuid;
		tracker = new ProgressTracker(uuid);
	}
	
	public void putPage(int pageNum) {
		pages.add(pageNum);
	}
	
	public int popPage() {
		if(pages.isEmpty())
			return 0;
		
		return pages.pop();
	}
	
	public void clearPages() {
		pages.clear();
	}
	
	public void forEachTaskOf(MissionType type, Predicate<IMission> condition) {
		forEachTaskOf(type, (m,i) -> condition.test(m) ? 1 : Manual.FAIL, false);
	}
	
	public void forEachTaskOf(MissionType type, BiFunction<IMission, Integer, Integer> condition, boolean overwriteProgress) {
		
		Player player = Bukkit.getPlayer(uuid);
		String worldName = player.getWorld().getName();
		
		for(IMission task : QuestWorld.getInstance().getMissionsOf(type)) {
			IQuest quest = task.getQuest();	
			
			if (quest.getCategory().isWorldEnabled(worldName) && quest.isWorldEnabled(worldName)) {
				if (!hasCompletedTask(task) && hasUnlockedTask(task)) {
					if (getStatus(quest).equals(QuestStatus.AVAILABLE)) {
						int progress = overwriteProgress ? 0 : getProgress(task);
						int result = condition.apply(task, task.getAmount() - progress);
						if(result != Manual.FAIL)
							setProgress(task, result + progress);
					}
				}
			}
		}
	}
	
	public void unload() {
		tracker.save();
		QuestWorld.getInstance().unregisterManager(this);
	}

	public UUID getUUID() {
		return uuid;
	}
	
	public long getCooldownEnd(IQuest quest) {
		return tracker.getQuestRefresh(quest);
	}
	
	public boolean isWithinTimeframe(IMission task) {
		long date = tracker.getMissionCompleted(task);
		if (date == 0) return true;
		return date > System.currentTimeMillis();
	}
	
	public static boolean updateTimeframe(UUID uuid, IMission task, int amount) {
		if (task.getTimeframe() == 0) return true;
		PlayerManager manager = QuestWorld.getInstance().getManager(uuid);
		Player p = Bukkit.getPlayer(uuid);

		if (!manager.isWithinTimeframe(task)) {
			manager.tracker.setMissionCompleted(task, null);
			manager.tracker.setMissionProgress(task, 0);
			if (p != null) {
				PlayerTools.sendTranslation(p, false, Translation.NOTIFY_TIME_FAIL, task.getQuest().getName());
			}
			return false;
		}
		else if (manager.getProgress(task) == 0 && amount > 0) {
			manager.tracker.setMissionCompleted(task, System.currentTimeMillis() + task.getTimeframe() * 60 * 1000);
			if (p != null) 
				PlayerTools.sendTranslation(p, false, Translation.NOTIFY_TIME_START, task.getText(), Text.timeFromNum(task.getTimeframe()));
		}
		return true;
	}
	
	public void update(boolean quest_check) {
		Player p = Bukkit.getPlayer(uuid);
		
		if (p != null && quest_check) {
			for (IMission task: QuestWorld.getInstance().getTickingMissions()) {
				if (getStatus(task.getQuest()).equals(QuestStatus.AVAILABLE) && !hasCompletedTask(task) && hasUnlockedTask(task)) {
					Ticking t = (Ticking) task.getType();
					int progress = t.onTick(p, task);
					if(progress != Manual.FAIL)
						setProgress(task, progress);
				}
			}
		}
		
		for (ICategory category: QuestWorld.getInstance().getCategories()) {
			for (IQuest quest: category.getQuests()) {
				if (getStatus(quest).equals(QuestStatus.AVAILABLE)) {
					boolean finished = quest.getMissions().size() != 0;
					for (IMission task: quest.getMissions()) {
						updateTimeframe(this.uuid, task, 0);
						if (!hasCompletedTask(task)) finished = false;
					}
					
					if (finished) {
						tracker.setQuestFinished(quest, true);
						
						if (!quest.isAutoClaiming() || p == null)
							tracker.setQuestStatus(quest, QuestStatus.REWARD_CLAIMABLE);
						else
							quest.handoutReward(p);
					}
				}
				else if(getStatus(quest).equals(QuestStatus.ON_COOLDOWN))
					if(tracker.getQuestRefresh(quest) <= System.currentTimeMillis())
						tracker.setQuestStatus(quest, QuestStatus.AVAILABLE);
			}
		}
	}

	public QuestStatus getStatus(IQuest quest) {
		Player p = Bukkit.getPlayer(uuid);
		if (quest.getParent() != null && !hasFinished(quest.getParent())) return QuestStatus.LOCKED;
		if (p != null && !PlayerTools.checkPermission(p, quest.getPermission())) return QuestStatus.LOCKED;
		if (quest.getPartySize() == 0 && getParty() != null) return QuestStatus.LOCKED_NO_PARTY;
		if (quest.getPartySize() > 1 && (getParty() == null || getParty().getSize() < quest.getPartySize())) return QuestStatus.LOCKED_PARTY_SIZE;
		
		return tracker.getQuestStatus(quest);
	}

	public boolean hasFinished(IQuest quest) {
		return tracker.isQuestFinished(quest);
	}

	public boolean hasCompletedTask(IMission task) {
		return getProgress(task) >= task.getAmount();
	}

	public boolean hasUnlockedTask(IMission task) {
		if (!task.getQuest().isOrdered()) return true;
		
		List<? extends IMission> tasks = task.getQuest().getMissions();
		int index = tasks.indexOf(task) - 1;
		if (index < 0) return true;
		else return hasCompletedTask(tasks.get(index));
	}
	
	public int getProgress(IMission task) {
		int progress = tracker.getMissionProgress(task);
		
		return Math.min(progress, task.getAmount());
	}
	
	public String progressString(IQuest quest) {
		int progress = 0;
		for(IMission mission : quest.getMissions())
			if(hasCompletedTask(mission))
				++progress;
		
		int amount = quest.getMissions().size();
		
		return Text.progressBar(
				progress,
				amount,
				null);
	}
	
	public void addProgress(IMission task, int amount) {
		int newProgress = Math.max(getProgress(task) + amount, 0);
		setProgress(task, Math.min(task.getAmount(), newProgress));
	}

	public void setProgress(IMission task, int amount) {
		if(task.getQuest().supportsParties() && getParty() != null)
			for(UUID uuid : getParty().getPlayers())
				setSingleProgress(uuid, task, amount);
		else
			setSingleProgress(this.uuid, task, amount);
	}
	
	private static void setSingleProgress(UUID uuid, IMission task, int amount) {
		amount = Math.min(amount, task.getAmount());
		if (!updateTimeframe(uuid, task, amount))
			return;
		
		QuestWorld.getInstance().getManager(uuid).tracker.setMissionProgress(task, amount);
		
		if(amount == task.getAmount()) {
			Player player = Bukkit.getPlayer(uuid);
			if(player != null)
				sendDialogue(player, task, task.getDialogue().iterator());
		}
	}
	
	
	public static void sendDialogue(final Player player, final IMission task, final Iterator<String> dialogue) {
		if(!player.isOnline())
			return;
		
		String line;
		
		// Grab a line if we can
		// Otherwise if there was no dialogue, use the completion placeholder
		// If there are no lines, and there was dialogue, we're clearly done so return
		// Refactor for ezeiger92/QuestWorld2#57
		if(dialogue.hasNext())
			line = dialogue.next();
		else if(task.getDialogue().isEmpty())
			line = "*";
		else
			return;
		
		if(line.equals("*"))
			// Change for ezeiger92/QuestWorld2#43 - Remove default complete message if dialog is present
			// Previously "check !task.getType().getID().equals("ACCEPT_QUEST_FROM_NPC") && "
			// This was done to keep quests quiet when interacting with citizens
			PlayerTools.sendTranslation(player, false, Translation.NOTIFY_COMPLETED, task.getQuest().getName());
		else
			sendDialogueComponent(player, line);
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(QuestWorld.getInstance(),
				() -> sendDialogue(player, task, dialogue), 70L);
	}

	private static void sendDialogueComponent(Player player, String line) {
		if(line.startsWith("/"))
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), line.substring(1).replace("<player>", player.getName()));

		else {
			line = QuestWorld.getInstance().getConfig().getString("dialogue.prefix") + line;
			
			player.sendMessage(Text.colorize(line.replace("<player>", player.getName())));
		}
	}

	public void completeQuest(IQuest quest) {
		if (quest.getCooldown() == -1)
			tracker.setQuestStatus(quest, QuestStatus.FINISHED);
		
		else {
			if (quest.getCooldown() == 0)
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
	
	public Party getParty() {
		UUID leader = tracker.getPartyLeader();
		if(leader != null)
			return new Party(leader);
		return null;
	}
	
	public ProgressTracker getTracker() {
		return tracker;
	}
	
	public IStateful getLastEntry() {
		return last;
	}
	
	public void setLastEntry(IStateful entry) {
		this.last = entry;
	}
	
	@Deprecated
	public String getProgress() {
		int done = 0;
		int total = 0;

		for (ICategory category: QuestWorld.getInstance().getCategories())  {
			total += category.getQuests().size();
			
			for (IQuest quest: category.getQuests())
				if (hasFinished(quest))
					++done;
		}
		
		return Text.progressBar(done, total, null);
	}
	
	public void clearQuestData(IQuest quest) {
		tracker.clearQuest(quest);
	}
	
	// Right, so this function USED to loop through every file in data-storage/Quest World on
	// the main thread. W H A T
	public static void clearAllQuestData(IQuest quest) {
		Bukkit.getScheduler().runTaskAsynchronously(QuestWorld.getInstance(), () -> {
			// First: clear all the quest data on a new thread
			File path = QuestWorld.getPath("data.player");
			
			for (File file: path.listFiles()) {
				String uuid = file.getName().substring(0, file.getName().length() - 4);
				ProgressTracker t = new ProgressTracker(UUID.fromString(uuid));
				t.clearQuest(quest);
				t.save();
			}

			// Second: go back to the main thread and make sure all player managers know what happened
			Bukkit.getScheduler().callSyncMethod(QuestWorld.getInstance(), () -> {
				for(AnimalTamer player : Bukkit.getOnlinePlayers())
					QuestWorld.getInstance().getManager(player.getUniqueId()).clearQuestData(quest);
				
				return false;
			});
		});
	}
}