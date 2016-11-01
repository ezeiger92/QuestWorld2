package me.mrCookieSlime.QuestWorld.quests;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Variable;
import me.mrCookieSlime.CSCoreLibPlugin.general.Clock;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.quests.MissionType.SubmissionType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

public class QuestManager {
	
	public static Map<UUID, Quest> autoclaim = new HashMap<UUID, Quest>();
	
	private Config cfg;
	private UUID uuid;
	private QWObject last;

	public static Set<QuestMission> ticking_tasks = new HashSet<QuestMission>();
	public static Set<QuestMission> block_breaking_tasks = new HashSet<QuestMission>();
	public static Set<QuestMission> citizen_tasks = new HashSet<QuestMission>();
	
	public QuestManager(OfflinePlayer p) {
		this.uuid = p.getUniqueId();
		this.cfg = new Config("data-storage/Quest World/" + uuid + ".yml");
		
		QuestWorld.getInstance().registerManager(this);
	}
	
	public QuestManager(UUID uuid) {
		this.uuid = uuid;
		this.cfg = new Config("data-storage/Quest World/" + uuid + ".yml");
		
		QuestWorld.getInstance().registerManager(this);
	}
	
	public void unload() {
		save();
		QuestWorld.getInstance().unregisterManager(this);
	}

	public void save() {
		cfg.save();
	}

	public UUID getUUID() {
		return uuid;
	}
	
	public long getCooldownEnd(Quest quest) {
		if (!cfg.contains(quest.getCategory().getID() + "." + quest.getID() + ".cooldown")) return -1;
		try {
			return new SimpleDateFormat("yyyy-MM-dd-HH-mm").parse(cfg.getString(quest.getCategory().getID() + "." + quest.getID() + ".cooldown")).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public long getCompletionDate(QuestMission task) {
		if (!cfg.contains(task.getQuest().getCategory().getID() + "." + task.getQuest().getID() + ".mission." + task.getID() + ".complete-until")) return 0;
		return cfg.getLong(task.getQuest().getCategory().getID() + "." + task.getQuest().getID() + ".mission." + task.getID() + ".complete-until");
	}
	
	public boolean isWithinTimeframe(QuestMission task) {
		long date = getCompletionDate(task);
		if (date == 0) return true;
		return date > System.currentTimeMillis();
	}
	
	public boolean updateTimeframe(UUID uuid, QuestMission task, int amount) {
		if (task.getTimeframe() == 0) return true;
		Config cfg = QuestWorld.getInstance().getManager(Bukkit.getOfflinePlayer(uuid)).toConfig();
		Player p = Bukkit.getPlayer(uuid);
		if (!isWithinTimeframe(task)) {
			cfg.setValue(task.getQuest().getCategory().getID() + "." + task.getQuest().getID() + ".mission." + task.getID() + ".complete-until", null);
			cfg.setValue(task.getQuest().getCategory().getID() + "." + task.getQuest().getID() + ".mission." + task.getID() + ".progress", 0);
			if (p != null) {
				QuestWorld.getInstance().getLocalization().sendTranslation(p, "notifications.task-failed-timeframe", false, new Variable("<Quest>", task.getQuest().getName()));
			}
			return false;
		}
		else if (getProgress(task) == 0 && amount > 0) {
			cfg.setValue(task.getQuest().getCategory().getID() + "." + task.getQuest().getID() + ".mission." + task.getID() + ".complete-until", (long) (System.currentTimeMillis() + (task.getTimeframe() * 60 * 1000)));
			if (p != null) QuestWorld.getInstance().getLocalization().sendTranslation(p, "notifications.task-timeframe-started", false, new Variable("<Objective>", task.getText()), new Variable("<Timeframe>", (task.getTimeframe() / 60) + "h " + (task.getTimeframe() % 60) + "m"));
		}
		return true;
	}
	
	public void update(boolean quest_check) {
		Player p = Bukkit.getPlayer(uuid);
		
		if (p != null && quest_check) {
			for (QuestMission task: getTickingTasks()) {
				if (getStatus(task.getQuest()).equals(QuestStatus.AVAILABLE) && !hasCompletedTask(task) && hasUnlockedTask(task)) {
					if (task.getType().getID().equals("PLAY_TIME")) setProgress(task, p.getStatistic(Statistic.PLAY_ONE_TICK) / 20 / 60);
					else if (task.getType().getID().equals("REACH_LOCATION")) {
						if (task.getLocation().getWorld().getName().equals(p.getWorld().getName()) && task.getLocation().distance(p.getLocation()) < task.getAmount()) {
							// Normally expecting "getAmount" to complete task, "getAmount" in this case is the search radius
							// Just set the task to done (because it is) rather than increment by 1
							setProgress(task, task.getAmount());
						}
					}
				}
			}
		}
		
		for (Category category: QuestWorld.getInstance().getCategories()) {
			for (Quest quest: category.getQuests()) {
				if (getStatus(quest).equals(QuestStatus.AVAILABLE)) {
					boolean finished = quest.getMissions().size() != 0;
					for (QuestMission task: quest.getMissions()) {
						updateTimeframe(this.uuid, task, 0);
						if (!hasCompletedTask(task)) finished = false;
					}
					
					if (finished) {
						cfg.setValue(quest.getCategory().getID() + "." + quest.getID() + ".finished", true);
						
						if (!quest.isAutoClaiming()) cfg.setValue(quest.getCategory().getID() + "." + quest.getID() + ".status", QuestStatus.REWARD_CLAIMABLE.toString());
						else {
							if (p == null) cfg.setValue(quest.getCategory().getID() + "." + quest.getID() + ".status", QuestStatus.REWARD_CLAIMABLE.toString());
							else quest.handoutReward(p);
						}
					}
				}
				else if (getStatus(quest).equals(QuestStatus.ON_COOLDOWN)) {
					try {
						if (!new SimpleDateFormat("yyyy-MM-dd-HH-mm").parse(cfg.getString(quest.getCategory().getID() + "." + quest.getID() + ".cooldown")).after(new Date())) {
							 cfg.setValue(quest.getCategory().getID() + "." + quest.getID() + ".status", QuestStatus.AVAILABLE.toString());
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private Set<QuestMission> getTickingTasks() {
		return ticking_tasks;
	}
	
	public static Set<QuestMission> getCitizenTasks() {
		return citizen_tasks;
	}

	public QuestStatus getStatus(Quest quest) {
		Player p = Bukkit.getPlayer(uuid);
		if (quest.getParent() != null && !hasFinished(quest.getParent())) return QuestStatus.LOCKED;
		if (p != null && !quest.hasPermission(p)) return QuestStatus.LOCKED;
		if (quest.getPartySize() == 0 && getParty() != null) return QuestStatus.LOCKED_NO_PARTY;
		if (quest.getPartySize() > 1 && getParty() != null && getParty().getSize() < quest.getPartySize()) return QuestStatus.LOCKED_PARTY_SIZE;
		if (!cfg.contains(quest.getCategory().getID() + "." + quest.getID() + ".status")) {
			cfg.setValue(quest.getCategory().getID() + "." + quest.getID() + ".status", QuestStatus.AVAILABLE.toString());
			return QuestStatus.AVAILABLE;
		}
		else return QuestStatus.valueOf(cfg.getString(quest.getCategory().getID() + "." + quest.getID() + ".status"));
	}

	public boolean hasFinished(Quest quest) {
		return cfg.getBoolean(quest.getCategory().getID() + "." + quest.getID() + ".finished");
	}

	public boolean hasCompletedTask(QuestMission task) {
		return getProgress(task) >= getTotal(task);
	}

	public boolean hasUnlockedTask(QuestMission task) {
		if (!task.getQuest().isOrdered()) return true;
		List<QuestMission> tasks = task.getQuest().getMissions();
		int index = tasks.indexOf(task) - 1;
		if (index < 0) return true;
		else return hasCompletedTask(tasks.get(index));
	}
	
	public int getProgress(QuestMission task) {
		Quest quest = task.getQuest();
		if (!cfg.contains(quest.getCategory().getID() + "." + quest.getID() + ".mission." + task.getID() + ".progress")) return 0;
		else return cfg.getInt(quest.getCategory().getID() + "." + quest.getID() + ".mission." + task.getID() + ".progress");
	}
	
	public int getTotal(QuestMission task) {
		if(task.getType().getSubmissionType() == SubmissionType.LOCATION)
			return 1;
		
		return task.getAmount();
	}
	
	public int addProgress(QuestMission task, int amount) {
		int progress = getProgress(task) + amount;
		int rest = progress - getTotal(task);
		setProgress(task, rest > 0 ? task.getAmount(): progress);
		return rest;
	}

	public void setProgress(QuestMission task, int amount) {
		if (!updateTimeframe(this.uuid, task, amount)) return;
		cfg.setValue(task.getQuest().getCategory().getID() + "." + task.getQuest().getID() + ".mission." + task.getID() + ".progress", amount > task.getAmount() ? task.getAmount(): amount);
		
		if (amount >= task.getAmount()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				sendQuestDialogue(player, task, task.getDialogue().iterator());
			}
		}
		
		if (!task.getType().getID().equals("ACCEPT_QUEST_FROM_NPC") && task.getQuest().supportsParties()) {
			Party party = getParty();
			if (party != null) {
				for (UUID uuid: party.getPlayers()) {
					if (!uuid.equals(this.uuid)) {
						updateTimeframe(uuid, task, amount);
						if (amount >= task.getAmount()) {
							Player player = Bukkit.getPlayer(uuid);
							if (player != null) QuestWorld.getInstance().getLocalization().sendTranslation(player, "notifications.task-completed", false, new Variable("<Quest>", task.getQuest().getName()));
						}
						QuestWorld.getInstance().getManager(Bukkit.getOfflinePlayer(uuid)).toConfig().setValue(task.getQuest().getCategory().getID() + "." + task.getQuest().getID() + ".mission." + task.getID() + ".progress", amount);
					}
				}
			}
		}
	}

	public void sendQuestDialogue(final Player player, final QuestMission task, final Iterator<String> dialogue) {
		if (dialogue.hasNext()) {
			sendDialogueComponent(player, dialogue.next());
			sendDialogue(player.getUniqueId(), task, dialogue);
		}
		else {
			QuestWorld.getInstance().getLocalization().sendTranslation(player, "notifications.task-completed", false, new Variable("<Quest>", task.getQuest().getName()));
		}
	}
	
	private void sendDialogue(final UUID uuid, final QuestMission task, final Iterator<String> dialogue) {
		if (dialogue.hasNext()) {
			final String line = dialogue.next();
			Bukkit.getScheduler().scheduleSyncDelayedTask(QuestWorld.getInstance(), new Runnable() {
				
				@Override
				public void run() {
					Player player = Bukkit.getPlayer(uuid);
					if (player != null) {
						sendDialogueComponent(player, line);
						sendDialogue(uuid, task, dialogue);
					}
				}
			}, 70L);
		}
		else {
			Player player = Bukkit.getPlayer(uuid);
			if (!task.getType().getID().equals("ACCEPT_QUEST_FROM_NPC") && player != null) QuestWorld.getInstance().getLocalization().sendTranslation(player, "notifications.task-completed", false, new Variable("<Quest>", task.getQuest().getName()));
		}
	}

	private void sendDialogueComponent(Player player, String line) {
		if (line.startsWith("/")) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), line.substring(1).replace("<player>", player.getName()));
		}
		else player.sendMessage(ChatColor.translateAlternateColorCodes('&', line.replace("<player>", player.getName())));
	}

	public void completeQuest(Quest quest) {
		if (quest.getCooldown() == 0) cfg.setValue(quest.getCategory().getID() + "." + quest.getID() + ".status", QuestStatus.FINISHED.toString());
		else {
			cfg.setValue(quest.getCategory().getID() + "." + quest.getID() + ".status", QuestStatus.ON_COOLDOWN.toString());
			cfg.setValue(quest.getCategory().getID() + "." + quest.getID() + ".cooldown", Clock.format(new Date(System.currentTimeMillis() + quest.getCooldown())));
			for (QuestMission task: quest.getMissions()) {
				 setProgress(task, 0);
			 }
		}
	}
	
	public Party getParty() {
		if (!cfg.contains("party.associated")) return null;
		else return new Party(UUID.fromString(cfg.getString("party.associated")));
	}

	public Config toConfig() {
		return cfg;
	}
	
	public QWObject getLastEntry() {
		return last;
	}
	
	public void updateLastEntry(QWObject entry) {
		this.last = entry;
	}
	
	public int getLevel() {
		return 0;
	}
	
	public String getProgress() {
		StringBuilder progress = new StringBuilder();
		int done = 0, total = 0;
		for (Category category: QuestWorld.getInstance().getCategories())  {
			for (Quest quest: category.getQuests()) {
				if (hasFinished(quest)) done++;
				total++;
			}
		}
		float percentage = Math.round((((done * 100.0f) / total) * 100.0f) / 100.0f);
		
		if (percentage < 16.0F) progress.append("&4");
		else if (percentage < 32.0F) progress.append("&c");
		else if (percentage < 48.0F) progress.append("&6");
		else if (percentage < 64.0F) progress.append("&e");
		else if (percentage < 80.0F) progress.append("&2");
		else progress = progress.append("&a");
		
		int rest = 20;
		for (int i = (int) percentage; i >= 5; i = i - 5) {
			progress.append(":");
			rest--;
		}
		
		progress.append("&7");
		
		for (int i = 0; i < rest; i++) {
			progress.append(":");
		}
		
		progress.append(" - " + percentage + "%");
		
		return ChatColor.translateAlternateColorCodes('&', progress.toString());
	}
	
	public void clearQuestData(Quest quest) {
		cfg.setValue(quest.getCategory().getID() + "." + quest.getID(), null);
	}
	
	public static void clearAllQuestData(Quest quest) {
		for (File file: new File("data-storage/Quest World").listFiles()) {
			String uuid = file.getName().replace(".yml", "");
			if (QuestWorld.getInstance().isManagerLoaded(uuid)) {
				QuestWorld.getInstance().getManager(uuid).clearQuestData(quest);
			}
			else {
				Config cfg = new Config(file);

				cfg.setValue(quest.getCategory().getID() + "." + quest.getID(), null);
				cfg.save();
			}
		}
	}
	
	public static void updateTickingTasks() {
		Set<QuestMission> ticking = new HashSet<QuestMission>();
		Set<QuestMission> blockbreaking = new HashSet<QuestMission>();
		Set<QuestMission> citizens = new HashSet<QuestMission>();
		
		for (Category category: QuestWorld.getInstance().getCategories()) {
			for (Quest quest: category.getQuests()) {
				for (QuestMission task: quest.getMissions()) {
					if (task.getType().isTicker()) ticking.add(task);
					if (task.getType().getID().equals("MINE_BLOCK")) blockbreaking.add(task);
					if (task.getType().getSubmissionType().toString().startsWith("CITIZENS_")) citizens.add(task);
				}
			}
		}
		
		ticking_tasks = ticking;
		block_breaking_tasks = blockbreaking;
		citizen_tasks = citizens;
	}

}
