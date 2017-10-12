package me.mrCookieSlime.QuestWorld.manager;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.contract.IRenderable;
import me.mrCookieSlime.QuestWorld.party.Party;
import me.mrCookieSlime.QuestWorld.api.Manual;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestStatus;
import me.mrCookieSlime.QuestWorld.api.Ticking;
import me.mrCookieSlime.QuestWorld.util.PlayerTools;
import me.mrCookieSlime.QuestWorld.util.Text;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Player;

public class PlayerManager {
	
	public static Map<UUID, IQuest> autoclaim = new HashMap<>();
	
	private Config cfg;
	private UUID uuid;
	private IRenderable last;
	
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
		forEachTaskOf(type, (m,i) -> condition.test(m) ? 1 : Manual.FAIL, false);
	}
	
	public void forEachTaskOf(MissionType type, BiFunction<IMission, Integer, Integer> condition, boolean overwriteProgress) {
		
		Player player = Bukkit.getPlayer(uuid);
		String worldName = player.getWorld().getName();
		
		for(IMission task : QuestWorld.getInstance().getMissionsOf(type)) {
			IQuest quest = task.getQuest();	
			ICategory category = quest.getCategory();
			
			if (category.isWorldEnabled(worldName) && quest.isWorldEnabled(worldName)) {
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
		save();
		QuestWorld.getInstance().unregisterManager(this);
	}

	public void save() {
		cfg.save();
	}

	public UUID getUUID() {
		return uuid;
	}
	
	public long getCooldownEnd(IQuest quest) {
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
	
	public static boolean updateTimeframe(UUID uuid, IMission task, int amount) {
		if (task.getTimeframe() == 0) return true;
		PlayerManager manager = QuestWorld.getInstance().getManager(Bukkit.getOfflinePlayer(uuid));
		Config cfg = manager.toConfig();
		Player p = Bukkit.getPlayer(uuid);

		if (!manager.isWithinTimeframe(task)) {
			cfg.setValue(task.getQuest().getCategory().getID() + "." + task.getQuest().getID() + ".mission." + task.getID() + ".complete-until", null);
			cfg.setValue(task.getQuest().getCategory().getID() + "." + task.getQuest().getID() + ".mission." + task.getID() + ".progress", 0);
			if (p != null) {
				PlayerTools.sendTranslation(p, false, Translation.NOTIFY_TIME_FAIL, task.getQuest().getName());
			}
			return false;
		}
		else if (manager.getProgress(task) == 0 && amount > 0) {
			cfg.setValue(task.getQuest().getCategory().getID() + "." + task.getQuest().getID() + ".mission." + task.getID() + ".complete-until", (long) (System.currentTimeMillis() + (task.getTimeframe() * 60 * 1000)));
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

	public QuestStatus getStatus(IQuest quest) {
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

	public boolean hasFinished(IQuest quest) {
		return cfg.getBoolean(quest.getCategory().getID() + "." + quest.getID() + ".finished");
	}

	public boolean hasCompletedTask(IMission task) {
		return getProgress(task) >= getTotal(task);
	}

	public boolean hasUnlockedTask(IMission task) {
		if (!task.getQuest().isOrdered()) return true;
		
		List<? extends IMission> tasks = task.getQuest().getMissions();
		int index = tasks.indexOf(task) - 1;
		if (index < 0) return true;
		else return hasCompletedTask(tasks.get(index));
	}
	
	public int getProgress(IMission task) {
		IQuest quest = task.getQuest();
		int progress = cfg.getInt(quest.getCategory().getID() + "." + quest.getID() + ".mission." + task.getID() + ".progress");
		
		return Math.min(progress, task.getAmount());
	}
	
	
	//TODO Better place for these
	public String progressString(IMission task) {
		int progress = getProgress(task);
		int amount = task.getAmount();
		
		return Text.progressBar(
				progress,
				amount,
				task.getType().progressString(progress / (float)amount, progress, amount));
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
	
	private static void setSingleProgress(UUID uuid, IMission task, int amount) {
		amount = Math.min(amount, task.getAmount());
		if (!updateTimeframe(uuid, task, amount))
			return;
		
		QuestWorld.getInstance().getManager(uuid).toConfig()
		.setValue(task.getQuest().getCategory().getID() + "." + task.getQuest().getID() + ".mission." + task.getID() + ".progress", amount);
		
		if(amount == task.getAmount()) {
			Player player = Bukkit.getPlayer(uuid);
			if(player != null)
				sendQuestDialogue(player, task, task.getDialogue().iterator());
		}
	}

	public static void sendQuestDialogue(final Player player, final IMission task, final Iterator<String> dialogue) {
		if (dialogue.hasNext()) {
			sendDialogueComponent(player, dialogue.next());
			sendDialogue(player.getUniqueId(), task, dialogue);
		}
		else {
			PlayerTools.sendTranslation(player, false, Translation.NOTIFY_COMPLETED, task.getQuest().getName());
		}
	}
	
	private static void sendDialogue(final UUID uuid, final IMission task, final Iterator<String> dialogue) {
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
				PlayerTools.sendTranslation(player, false, Translation.NOTIFY_COMPLETED, task.getQuest().getName());
		}
	}

	private static void sendDialogueComponent(Player player, String line) {
		if (line.startsWith("/")) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), line.substring(1).replace("<player>", player.getName()));
		}
		else player.sendMessage(ChatColor.translateAlternateColorCodes('&', line.replace("<player>", player.getName())));
	}

	public void completeQuest(IQuest quest) {
		if (quest.getCooldown() == -1) cfg.setValue(quest.getCategory().getID() + "." + quest.getID() + ".status", QuestStatus.FINISHED.toString());
		else {
			if (quest.getCooldown() == 0) cfg.setValue(quest.getCategory().getID() + "." + quest.getID() + ".status", QuestStatus.AVAILABLE.toString());
			else cfg.setValue(quest.getCategory().getID() + "." + quest.getID() + ".status", QuestStatus.ON_COOLDOWN.toString());
			
			String dateString = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date(System.currentTimeMillis() + quest.getRawCooldown()));
			
			cfg.setValue(quest.getCategory().getID() + "." + quest.getID() + ".cooldown", dateString);
			for (IMission task: quest.getMissions()) {
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
	
	public IRenderable getLastEntry() {
		return last;
	}
	
	public void updateLastEntry(IRenderable entry) {
		this.last = entry;
	}
	
	public int getLevel() {
		return 0;
	}
	
	public String getProgress() {
		StringBuilder progress = new StringBuilder();
		int done = 0, total = 0;
		for (ICategory category: QuestWorld.getInstance().getCategories())  {
			for (IQuest quest: category.getQuests()) {
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
	
	public void clearQuestData(IQuest quest) {
		cfg.setValue(quest.getCategory().getID() + "." + quest.getID(), null);
	}
	
	// Right, so this function USED to loop through every file in data-storage/Quest World on
	// the main thread. W H A T
	public static void clearAllQuestData(IQuest quest) {
		Bukkit.getScheduler().runTaskAsynchronously(QuestWorld.getInstance(), () -> {
			// First: clear all the quest data on a new thread
			for (File file: new File("data-storage/Quest World").listFiles()) {
				Config cfg = new Config(file);

				cfg.setValue(quest.getCategory().getID() + "." + quest.getID(), null);
				cfg.save();
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
