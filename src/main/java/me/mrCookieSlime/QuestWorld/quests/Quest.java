package me.mrCookieSlime.QuestWorld.quests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.InvUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.CSCoreLibPlugin.general.Player.PlayerInventory;
import me.mrCookieSlime.CSCoreLibPlugin.general.audio.Soundboard;
import me.mrCookieSlime.QuestWorld.QuestWorld;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class Quest extends QWObject {
	
	Category category;
	int id;
	long cooldown;
	String name;
	ItemStack item;
	List<QuestMission> tasks;
	
	List<String> commands = new ArrayList<String>();
	List<String> world_blacklist = new ArrayList<String>();
	List<ItemStack> rewards;
	int money;
	int xp;
	int partysize;
	
	boolean disableParties, ordered, autoclaim;
	
	Quest parent;
	String permission;
	
	public Quest(Category category, File file) {
		this.category = category;
		
		Config cfg = new Config(file);
		this.id = Integer.parseInt(file.getName().replace(".quest", "").split("-C")[0]);
		this.cooldown = cfg.getLong("cooldown");
		this.disableParties = cfg.getBoolean("disable-parties");
		this.ordered = cfg.getBoolean("in-order");
		this.autoclaim = cfg.getBoolean("auto-claim");
		this.name = ChatColor.translateAlternateColorCodes('&', cfg.getString("name"));
		this.item = new CustomItem(cfg.getItem("item"), name);
		this.tasks = loadMissions(cfg);
		this.rewards = loadRewards(cfg);
		this.money = cfg.getInt("rewards.money");
		this.xp = cfg.getInt("rewards.xp");
		if (cfg.contains("rewards.commands")) commands = cfg.getStringList("rewards.commands");
		if (cfg.contains("world-blacklist")) world_blacklist = cfg.getStringList("world-blacklist");
		
		if (cfg.contains("min-party-size")) partysize = cfg.getInt("min-party-size");
		else partysize = 1;
		
		if (cfg.contains("permission")) this.permission = cfg.getString("permission");
		else this.permission = "";
		
		category.addQuest(this);
	}

	public Quest(String name, String input) {
		this.category = QuestWorld.getInstance().getCategory(Integer.parseInt(input.split(" M ")[0]));
		
		this.id = Integer.parseInt(input.split(" M ")[1]);
		this.cooldown = 0;
		this.name = ChatColor.translateAlternateColorCodes('&', name);
		this.item = new CustomItem(new MaterialData(Material.BOOK_AND_QUILL).toItemStack(1), name);
		
		this.tasks = new ArrayList<QuestMission>();
		this.rewards = new ArrayList<ItemStack>();
		this.money = 0;
		this.xp = 0;
		this.commands = new ArrayList<String>();
		this.world_blacklist = new ArrayList<String>();
		this.parent = null;
		this.disableParties = false;
		this.permission = "";
		this.ordered = false;
		this.autoclaim = false;
		this.partysize = 1;
		
		category.addQuest(this);
	}
	
	public void updateParent(Config cfg) {
		if (cfg.contains("parent")) {
			Category c = QuestWorld.getInstance().getCategory(Integer.parseInt(cfg.getString("parent").split("-C")[0]));
			if (c != null) parent = c.getQuest(Integer.parseInt(cfg.getString("parent").split("-C")[1]));
		}
	}

	private List<QuestMission> loadMissions(Config cfg) {
		if (!cfg.contains("missions")) return new ArrayList<QuestMission>();
		List<QuestMission> missions = new ArrayList<QuestMission>();
		for (String key: cfg.getKeys("missions")) {
			if (!cfg.contains("missions." + key + ".location.world")) {
				cfg.setValue("missions." + key + ".location", new Location(Bukkit.getWorlds().get(0), 0, 0, 0));
				cfg.save();
				missions.add(new QuestMission(this, key, MissionType.valueOf(cfg.getString("missions." + key + ".type")), EntityType.valueOf(cfg.getString("missions." + key + ".entity")), cfg.getString("missions." + key + ".name"), cfg.getItem("missions." + key + ".item"), new Location(Bukkit.getWorlds().get(0), 0, 0, 0), cfg.getInt("missions." + key + ".amount"), cfg.getString("missions." + key + ".display-name"), cfg.contains("missions." + key + ".timeframe") ? cfg.getLong("missions." + key + ".timeframe"): 0, cfg.getBoolean("missions." + key + ".reset-on-death"), cfg.getInt("missions." + key + ".citizen"), cfg.getBoolean("missions." + key + ".exclude-spawners"), cfg.getString("missions." + key + ".lore")));
			}
			else missions.add(new QuestMission(this, key, MissionType.valueOf(cfg.getString("missions." + key + ".type")), EntityType.valueOf(cfg.getString("missions." + key + ".entity")), cfg.getString("missions." + key + ".name"), cfg.getItem("missions." + key + ".item"), cfg.getLocation("missions." + key + ".location"), cfg.getInt("missions." + key + ".amount"), cfg.getString("missions." + key + ".display-name"), cfg.contains("missions." + key + ".timeframe") ? cfg.getLong("missions." + key + ".timeframe"): 0, cfg.getBoolean("missions." + key + ".reset-on-death"), cfg.getInt("missions." + key + ".citizen"), cfg.getBoolean("missions." + key + ".exclude-spawners"), cfg.getString("missions." + key + ".lore")));
			
		}
		return missions;
	}
	
	public void save() {
		Config cfg = new Config(new File("plugins/QuestWorld/quests/" + id + "-C" + category.getID() + ".quest"));
		cfg.setValue("id", id);
		cfg.setValue("category", category.getID());
		cfg.setValue("cooldown", String.valueOf(cooldown));
		cfg.setValue("name", name.replaceAll("�", "&"));
		cfg.setValue("item", new ItemStack(item));
		cfg.setValue("rewards.items", null);
		cfg.setValue("rewards.money", money);
		cfg.setValue("rewards.xp", xp);
		cfg.setValue("rewards.commands", commands);
		cfg.setValue("missions", null);
		cfg.setValue("permission", permission);
		cfg.setValue("disable-parties", disableParties);
		cfg.setValue("in-order", ordered);
		cfg.setValue("auto-claim", autoclaim);
		cfg.setValue("world-blacklist", world_blacklist);
		cfg.setValue("min-party-size", partysize);
		
		int index = 0;
		for (ItemStack reward: rewards) {
			if (reward != null) {
				cfg.setValue("rewards.items." + index, reward);
				index++;
			}
		}
		for (QuestMission mission: tasks) {
			cfg.setValue("missions." + mission.getID() + ".type", mission.getType().toString());
			cfg.setValue("missions." + mission.getID() + ".amount", mission.getAmount());
			cfg.setValue("missions." + mission.getID() + ".item", new ItemStack(mission.getItem()));
			cfg.setValue("missions." + mission.getID() + ".entity", mission.getEntity().toString());
			if (mission.getLocation() != null && mission.getLocation().getWorld() != null) cfg.setValue("missions." + mission.getID() + ".location", mission.getLocation());
			cfg.setValue("missions." + mission.getID() + ".name", mission.getEntityName());
			cfg.setValue("missions." + mission.getID() + ".display-name", mission.getCustomName());
			cfg.setValue("missions." + mission.getID() + ".timeframe", mission.getTimeframe());
			cfg.setValue("missions." + mission.getID() + ".reset-on-death", mission.resetsonDeath());
			cfg.setValue("missions." + mission.getID() + ".lore", mission.getLore());
			cfg.setValue("missions." + mission.getID() + ".citizen", mission.getCitizenID());
			cfg.setValue("missions." + mission.getID() + ".exclude-spawners", mission.acceptsSpawners());
		}
		if (parent != null) cfg.setValue("parent", String.valueOf(parent.getCategory().getID() + "-C" + parent.getID()));
		else cfg.setValue("parent", null);
		
		cfg.save();
	}

	public int getID() {
		return id;
	}
	
	public ItemStack getItem() {
		return item.clone();
	}

	public Category getCategory() {
		return category;
	}

	public QuestStatus getStatus(Player p) {
		return QuestWorld.getInstance().getManager(p).getStatus(this);
	}
	
	public List<QuestMission> getFinishedTasks(Player p) {
		List<QuestMission> list = new ArrayList<QuestMission>();
		for (QuestMission task: getMissions()) {
			if (QuestWorld.getInstance().getManager(p).hasCompletedTask(task)) list.add(task);
		}
		return list;
	}

	public String getProgress(Player p) {
		StringBuilder progress = new StringBuilder();
		float percentage = Math.round((((getFinishedTasks(p).size() * 100.0f) / getMissions().size()) * 100.0f) / 100.0f);
		
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

	public List<QuestMission> getMissions() {
		return tasks;
	}
	
	private List<ItemStack> loadRewards(Config cfg) {
		if (!cfg.contains("rewards.items.0")) return new ArrayList<ItemStack>();
		List<ItemStack> items = new ArrayList<ItemStack>();
		for (String key: cfg.getKeys("rewards.items")) {
			ItemStack item = cfg.getItem("rewards.items." + key);
			if (item != null) items.add(item);
		}
		return items;
	}
	
	public void setItemRewards(Player p) {
		rewards.clear();
		for (int i = 0; i < 9; i++) {
			ItemStack item = p.getInventory().getItem(i);
			if (item != null && item.getType() != null && item.getType() != Material.AIR) rewards.add(item.clone());
		}
	}

	public void setItem(ItemStack item) {
		this.item = new CustomItem(item, name);
	}

	public void toggleWorld(String world) {
		if (world_blacklist.contains(world)) world_blacklist.remove(world);
		else world_blacklist.add(world);
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = ChatColor.translateAlternateColorCodes('&', name);
		this.item = new CustomItem(item, name);
	}

	public List<ItemStack> getRewards() {
		return rewards;
	}
	
	public QuestMission getMission(int i) {
		return tasks.size() > i ? tasks.get(i): null;
	}
	
	public void addMission(QuestMission mission) {
		this.tasks.add(mission);
	}
	
	public void removeMission(QuestMission mission) {
		this.tasks.remove(mission);
	}
	
	public void setPartySize(int size) {
		this.partysize = size;
	}

	public long getCooldown() {
		return cooldown;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown * 60 * 1000;
	}
	
	public int getMoney() {
		return money;
	}
	
	public int getPartySize() {
		return partysize;
	}

	public int getXP() {
		return xp;
	}
	
	public void setMoney(int money) {
		this.money = money;
	}
	
	public void setXP(int xp) {
		this.xp = xp;
	}
	
	public void handoutReward(Player p) {
		for (ItemStack reward: rewards) {
			if (InvUtils.fits(p.getInventory(), reward)) p.getInventory().addItem(reward.clone());
			else p.getWorld().dropItemNaturally(p.getLocation(), reward.clone());
		}
		p.playSound(p.getLocation(), Soundboard.getLegacySounds("ENTITY_ITEM_PICKUP", "ITEM_PICKUP"), 1F, 1F);
		PlayerInventory.update(p);
		
		if (xp > 0) p.setLevel(p.getLevel() + xp);
		if (money > 0 && QuestWorld.getInstance().getEconomy() != null) QuestWorld.getInstance().getEconomy().depositPlayer(p, money);
		
		for (String command: commands) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("@p", p.getName()));
		}
		
		QuestWorld.getInstance().getManager(p).completeQuest(this);
	}

	public String getFormattedCooldown() {
		long cooldown = this.cooldown / 60 / 1000;
		StringBuilder builder = new StringBuilder();
		builder.append(cooldown / 60);
		builder.append("h ");
		builder.append(cooldown % 60);
		builder.append("m");
		return builder.toString();
	}
	
	public Quest getParent() {
		return this.parent;
	}

	public void setParent(Quest quest) {
		this.parent = quest;
	}

	public List<String> getCommands() {
		return commands;
	}

	public void removeCommand(int i) {
		commands.remove(i);
	}

	public void addCommand(String command) {
		commands.add(command);
	}

	@Override
	public String getPermission() {
		return permission;
	}

	@Override
	public void setPermission(String permission) {
		this.permission = permission;
	}

	public boolean supportsParties() {
		return !disableParties;
	}

	public void setPartySupport(boolean supportsParties) {
		this.disableParties = supportsParties;
	}

	public boolean isOrdered() {
		return ordered;
	}

	public void setOrdered(boolean ordered) {
		this.ordered = ordered;
	}

	public boolean isAutoClaiming() {
		return autoclaim;
	}

	public void setAutoClaim(boolean autoclaim) {
		this.autoclaim = autoclaim;
	}

	public boolean isWorldEnabled(String world) {
		return !world_blacklist.contains(world);
	}

}
