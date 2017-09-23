package me.mrCookieSlime.QuestWorld.managers;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import java.util.function.Predicate;

import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.api.Manual;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.Ticking;
import me.mrCookieSlime.QuestWorld.parties.Party;
import me.mrCookieSlime.QuestWorld.quests.Category;
import me.mrCookieSlime.QuestWorld.quests.Mission;
import me.mrCookieSlime.QuestWorld.quests.Quest;
import me.mrCookieSlime.QuestWorld.quests.QuestStatus;
import me.mrCookieSlime.QuestWorld.quests.QuestingObject;
import me.mrCookieSlime.QuestWorld.utils.PlayerTools;
import me.mrCookieSlime.QuestWorld.utils.Text;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerManager {
	
	public static Map<UUID, Quest> autoclaim = new HashMap<UUID, Quest>();
	
	private Config cfg;
	private UUID uuid;
	private QuestingObject last;

	private Map<Long, Category> activeCategories;
	private Map<Long, Quest> activeQuests;
	private Map<Long, Mission> activeMissions;
	
	private Stack<Integer> pages = new Stack<>();
	
	public PlayerManager(OfflinePlayer p) {
		this(p.getUniqueId());
	}
	
	public PlayerManager(UUID uuid) {
		this.uuid = uuid;
		this.cfg = new Config("data-storage/Quest World/" + uuid + ".yml");
		
		QuestWorld.getInstance().registerManager(this);
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
		forEachTaskOf(type, condition, 1, false);
	}
	
	public void forEachTaskOf(MissionType type, Predicate<IMission> condition, int amount, boolean overwriteProgress) {
		
		Player player = Bukkit.getPlayer(uuid);
		String worldName = player.getWorld().getName();
		
		for(Mission task : QuestWorld.getInstance().getMissionsOf(type)) {
			Quest quest = task.getQuest();
			if(quest == null)
				continue;
			
			Category category = quest.getCategory();
			//TODO This (and the null above) *shouldn't* ever happen, but there are some crazy things in this code
			// Check to make sure this ACTUALY never happens, prevent it from being possible, and remove checks
			if(category == null)
				continue;
			
			if (category.isWorldEnabled(worldName) && quest.isWorldEnabled(worldName)) {
				if (!hasCompletedTask(task) && hasUnlockedTask(task)) {
					if (getStatus(quest).equals(QuestStatus.AVAILABLE)) {
						if(condition.test(task))
							if(overwriteProgress)
								setProgress(task, amount);
							else
								addProgress(task, amount);
					}
				}
			}
		}
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
	
	public long getCompletionDate(IMission task) {
		if (!cfg.contains(task.getQuest().getCategory().getID() + "." + task.getQuest().getID() + ".mission." + task.getID() + ".complete-until")) return 0;
		return cfg.getLong(task.getQuest().getCategory().getID() + "." + task.getQuest().getID() + ".mission." + task.getID() + ".complete-until");
	}
	
	public boolean isWithinTimeframe(IMission task) {
		long date = getCompletionDate(task);
		if (date == 0) return true;
		return date > System.currentTimeMillis();
	}
	
	public boolean updateTimeframe(UUID uuid, IMission task, int amount) {
		if (task.getTimeframe() == 0) return true;
		Config cfg = QuestWorld.getInstance().getManager(Bukkit.getOfflinePlayer(uuid)).toConfig();
		Player p = Bukkit.getPlayer(uuid);
		
		// TODO This checks against the class' uuid, then assigns based on the supplied uuid. WHAT.
		if (!isWithinTimeframe(task)) {
			cfg.setValue(task.getQuest().getCategory().getID() + "." + task.getQuest().getID() + ".mission." + task.getID() + ".complete-until", null);
			cfg.setValue(task.getQuest().getCategory().getID() + "." + task.getQuest().getID() + ".mission." + task.getID() + ".progress", 0);
			if (p != null) {
				PlayerTools.sendTranslation(p, false, Translation.notify_timefail, task.getQuest().getName());
			}
			return false;
		}
		else if (getProgress(task) == 0 && amount > 0) {
			cfg.setValue(task.getQuest().getCategory().getID() + "." + task.getQuest().getID() + ".mission." + task.getID() + ".complete-until", (long) (System.currentTimeMillis() + (task.getTimeframe() * 60 * 1000)));
			if (p != null) 
				PlayerTools.sendTranslation(p, false, Translation.notify_timestart, task.getText(), Text.timeFromNum(task.getTimeframe()));
		}
		return true;
	}
	
	public void update(boolean quest_check) {
		Player p = Bukkit.getPlayer(uuid);
		
		if (p != null && quest_check) {
			for (Mission task: QuestWorld.getInstance().getTickingMissions()) {
				if (getStatus(task.getQuest()).equals(QuestStatus.AVAILABLE) && !hasCompletedTask(task) && hasUnlockedTask(task)) {
					Ticking t = (Ticking) task.getType();
					int progress = t.onTick(p, task);
					if(progress != Manual.FAIL)
						setProgress(task, progress);
				}
			}
		}
		
		for (Category category: QuestWorld.getInstance().getCategories()) {
			for (Quest quest: category.getQuests()) {
				if (getStatus(quest).equals(QuestStatus.AVAILABLE)) {
					boolean finished = quest.getMissions().size() != 0;
					for (Mission task: quest.getMissions()) {
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

	public QuestStatus getStatus(Quest quest) {
		Player p = Bukkit.getPlayer(uuid);
		if (quest.getParent() != null && !hasFinished(quest.getParent())) return QuestStatus.LOCKED;
		if (p != null && !quest.hasPermission(p)) return QuestStatus.LOCKED;
		if (quest.getPartySize() == 0 && getParty() != null) return QuestStatus.LOCKED_NO_PARTY;
		if (quest.getPartySize() > 1 && (getParty() == null || getParty().getSize() < quest.getPartySize())) return QuestStatus.LOCKED_PARTY_SIZE;
		if (!cfg.contains(quest.getCategory().getID() + "." + quest.getID() + ".status")) {
			cfg.setValue(quest.getCategory().getID() + "." + quest.getID() + ".status", QuestStatus.AVAILABLE.toString());
			return QuestStatus.AVAILABLE;
		}
		else return QuestStatus.valueOf(cfg.getString(quest.getCategory().getID() + "." + quest.getID() + ".status"));
	}

	public boolean hasFinished(Quest quest) {
		return cfg.getBoolean(quest.getCategory().getID() + "." + quest.getID() + ".finished");
	}

	public boolean hasCompletedTask(Mission task) {
		return getProgress(task) >= getTotal(task);
	}

	public boolean hasUnlockedTask(Mission task) {
		if (!task.getQuest().isOrdered()) return true;
		List<Mission> tasks = task.getQuest().getMissions();
		int index = tasks.indexOf(task) - 1;
		if (index < 0) return true;
		else return hasCompletedTask(tasks.get(index));
	}
	
	public int getProgress(IMission task) {
		Quest quest = task.getQuest();
		if (!cfg.contains(quest.getCategory().getID() + "." + quest.getID() + ".mission." + task.getID() + ".progress")) return 0;
		else return cfg.getInt(quest.getCategory().getID() + "." + quest.getID() + ".mission." + task.getID() + ".progress");
	}
	
	public int getTotal(IMission task) {
		return task.getAmount();
	}
	
	public int addProgress(IMission task, int amount) {
		int progress = getProgress(task) + amount;
		int rest = progress - getTotal(task);
		setProgress(task, rest > 0 ? task.getAmount(): progress);
		return rest;
	}

	public void setProgress(IMission task, int amount) {
		if(task.getQuest().supportsParties() && getParty() != null)
			for(UUID uuid : getParty().getPlayers())
				setSingleProgress(uuid, task, amount);
		else
			setSingleProgress(this.uuid, task, amount);
	}
	
	private void setSingleProgress(UUID uuid, IMission task, int amount) {
		amount = Math.min(amount, task.getAmount());
		if (!updateTimeframe(uuid, task, amount))
			return;
		
		QuestWorld.getInstance().getManager(uuid.toString()).toConfig()
		.setValue(task.getQuest().getCategory().getID() + "." + task.getQuest().getID() + ".mission." + task.getID() + ".progress", amount);
		
		if(amount == task.getAmount()) {
			Player player = Bukkit.getPlayer(uuid);
			if(player != null)
				sendQuestDialogue(player, task, task.getDialogue().iterator());
		}
	}

	public void sendQuestDialogue(final Player player, final IMission task, final Iterator<String> dialogue) {
		if (dialogue.hasNext()) {
			sendDialogueComponent(player, dialogue.next());
			sendDialogue(player.getUniqueId(), task, dialogue);
		}
		else {
			PlayerTools.sendTranslation(player, false, Translation.notify_completetask, task.getQuest().getName());
		}
	}
	
	private void sendDialogue(final UUID uuid, final IMission task, final Iterator<String> dialogue) {
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

			// Change for ezeiger92/QuestWorld2#43 - Remove default complete message if dialog is present
			// Previously "check !task.getType().getID().equals("ACCEPT_QUEST_FROM_NPC") && "
			// This was done to keep quests quiet when interacting with citizens
			if (player != null && task.getDialogue().isEmpty())
				PlayerTools.sendTranslation(player, false, Translation.notify_completetask, task.getQuest().getName());
		}
	}

	private void sendDialogueComponent(Player player, String line) {
		if (line.startsWith("/")) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), line.substring(1).replace("<player>", player.getName()));
		}
		else player.sendMessage(ChatColor.translateAlternateColorCodes('&', line.replace("<player>", player.getName())));
	}

	public void completeQuest(Quest quest) {
		if (quest.getCooldown() == -1) cfg.setValue(quest.getCategory().getID() + "." + quest.getID() + ".status", QuestStatus.FINISHED.toString());
		else {
			if (quest.getCooldown() == 0) cfg.setValue(quest.getCategory().getID() + "." + quest.getID() + ".status", QuestStatus.AVAILABLE.toString());
			else cfg.setValue(quest.getCategory().getID() + "." + quest.getID() + ".status", QuestStatus.ON_COOLDOWN.toString());
			
			String dateString = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date(System.currentTimeMillis() + quest.getRawCooldown()));
			
			cfg.setValue(quest.getCategory().getID() + "." + quest.getID() + ".cooldown", dateString);
			for (Mission task: quest.getMissions()) {
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
	
	public QuestingObject getLastEntry() {
		return last;
	}
	
	public void updateLastEntry(QuestingObject entry) {
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
	
	/**
	 * 
	 */
	public Collection<Category> getAvailableCategories() {
		return activeCategories.values();
	}
	
	public Collection<Quest> getAvailableQuests() {
		return activeQuests.values();
	}
	
	public Collection<Mission> getAvailableMissions() {
		return activeMissions.values();
	}
	
	public void setQuestAvailable(Quest quest, boolean state) {
		
	}
	
	public void setCategoryAvailable(Category category, boolean state) {
		
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
}
