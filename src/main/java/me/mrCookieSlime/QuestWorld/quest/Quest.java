package me.mrCookieSlime.QuestWorld.quest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestStatus;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.contract.IQuestWrite;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;
import me.mrCookieSlime.QuestWorld.util.Text;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

class Quest extends Renderable implements IQuestWrite {
	
	Category category;
	int id;
	long cooldown;
	String name;
	ItemStack item;
	List<Mission> tasks = new ArrayList<>();
	
	List<String> commands = new ArrayList<>();
	List<String> world_blacklist = new ArrayList<>();
	List<ItemStack> rewards = new ArrayList<>();
	int money;
	int xp;
	int partysize;
	
	boolean disableParties, ordered, autoclaim;
	
	Quest parent;
	String permission;
	
	FileConfiguration config;
	
	// Internal
	protected Quest(Quest quest) {
		copy(quest);
	}
	
	protected void copy(Quest source) {
		category = source.category;
		id       = source.id;
		cooldown = source.cooldown;
		name     = source.name;
		item     = source.item.clone();

		tasks = new ArrayList<>();
		tasks.addAll(source.tasks);
		
		commands = new ArrayList<>();
		commands.addAll(source.commands);
		
		world_blacklist = new ArrayList<>();
		world_blacklist.addAll(source.world_blacklist);
		
		rewards = new ArrayList<>();
		rewards.addAll(source.rewards);

		money          = source.money;
		xp             = source.xp;
		partysize      = source.partysize;
		disableParties = source.disableParties;
		ordered        = source.ordered;
		autoclaim      = source.autoclaim;
		parent         = source.parent;
		permission     = source.permission;

		updateLastModified();
	}
	
	protected void copyTo(Quest dest) {
		dest.copy(this);
	}
	
	// Package
	public Quest(Category category, FileConfiguration file) {
		this.category = category;
		
		config = file;
		this.id = Integer.parseInt(file.getName().replace(".quest", "").split("-C")[0]);
		this.cooldown = config.getLong("cooldown");
		this.disableParties = config.getBoolean("disable-parties");
		this.ordered = config.getBoolean("in-order");
		this.autoclaim = config.getBoolean("auto-claim");
		this.name = Text.colorize(config.getString("name"));
		this.item = new ItemBuilder(config.getItemStack("item")).display(name).get();
		loadMissions();
		this.rewards = loadRewards();
		this.money = config.getInt("rewards.money");
		this.xp = config.getInt("rewards.xp");
		
		if (config.contains("rewards.commands")) commands = config.getStringList("rewards.commands");
		if (config.contains("world-blacklist")) world_blacklist = config.getStringList("world-blacklist");
		
		if (config.contains("min-party-size")) partysize = config.getInt("min-party-size");
		else partysize = 1;
		
		if (config.contains("permission")) this.permission = config.getString("permission");
		else this.permission = "";
		
		category.addQuest(this);
	}

	// External
	public Quest(String name, String input) {
		this.category = (Category)QuestWorld.getInstance().getCategory(Integer.parseInt(input.split(" M ")[0]));
		
		this.id = Integer.parseInt(input.split(" M ")[1]);
		config = YamlConfiguration.loadConfiguration(getFile());
		this.cooldown = -1;
		this.name = Text.colorize(name);
		this.item = new ItemBuilder(Material.BOOK_AND_QUILL).display(name).get();

		this.money = 0;
		this.xp = 0;
		this.parent = null;
		this.disableParties = false;
		this.permission = "";
		this.ordered = false;
		this.autoclaim = false;
		this.partysize = 1;
		
		category.addQuest(this);
	}
	
	public void refreshParent() {
		String parentId = config.getString("parent", null);
		if (parentId != null) {
			String[] parts = parentId.split("-C");
			Category c = (Category)QuestWorld.getInstance().getCategory(Integer.parseInt(parts[0]));
			if (c != null)
				parent = c.getQuest(Integer.parseInt(parts[1]));
		}
	}
	
	File getFile() {
		String path = QuestWorld.getInstance().getConfig().getString("save.questdata");
		return new File(path + id + "-C" + category.getID() + ".quest");
	}
	
	private Location locationHelper(ConfigurationSection loc) {
		return new Location(Bukkit.getWorld(loc.getString("world")), loc.getDouble("x"), loc.getDouble("y"),
				loc.getDouble("z"), (float)loc.getDouble("yaw"), (float)loc.getDouble("pitch"));
	}
	
	private void locationHelper(Location in, ConfigurationSection loc) {
		loc.set("world", in.getWorld().getName());
		loc.set("x", in.getX());
		loc.set("y", in.getY());
		loc.set("z", in.getZ());
		loc.set("yaw", (double)in.getYaw());
		loc.set("pitch", (double)in.getPitch());
	}

	private void loadMissions() {
		ConfigurationSection missions = config.getConfigurationSection("missions");
		if (missions == null)
			return;

		for (String key: missions.getKeys(false)) {
			ConfigurationSection mission = missions.getConfigurationSection(key);
			
			if(mission.contains("citizen")) {
				mission.set("custom_int", mission.get("custom_int", mission.get("citizen")));
				mission.set("citizen", null);
			}
			
			if(mission.contains("name")) {
				mission.set("custom_string", mission.get("custom_string", mission.get("name")));
				mission.set("name", null);
			}

			QuestChange changes = new QuestChange(this);
			
			changes.addMission(new Mission(this, key,
					MissionType.valueOf(mission.getString("type")),
					EntityType.valueOf(mission.getString("entity")),
					Text.colorize(mission.getString("custom_string")),
					mission.getItemStack("item"),
					locationHelper(mission.getConfigurationSection("location")),
					mission.getInt("amount"),
					Text.colorize(mission.getString("display-name")),
					mission.getInt("timeframe"),
					mission.getBoolean("reset-on-death"),
					mission.getInt("custom_int"),
					// not exclude = allow, what we want
					!mission.getBoolean("exclude-spawners"),
					Text.colorize(mission.getString("lore"))));
			
			if(changes.sendEvent())
				changes.apply();
		}
	}
	
	public void save() {
		config.set("id", id);
		config.set("category", category.getID());
		config.set("cooldown", String.valueOf(cooldown));
		config.set("name", Text.escape(name));
		config.set("item", new ItemStack(item));
		config.set("rewards.items", null);
		config.set("rewards.money", money);
		config.set("rewards.xp", xp);
		config.set("rewards.commands", commands);
		config.set("missions", null);
		config.set("permission", permission);
		config.set("disable-parties", disableParties);
		config.set("in-order", ordered);
		config.set("auto-claim", autoclaim);
		config.set("world-blacklist", world_blacklist);
		config.set("min-party-size", partysize);
		
		int index = 0;
		for (ItemStack reward: rewards) {
			if (reward != null) {
				config.set("rewards.items." + index, reward);
				index++;
			}
		}
		for (IMission mission: tasks) {
			config.set("missions." + mission.getID() + ".type", mission.getType().toString());
			config.set("missions." + mission.getID() + ".amount", mission.getAmount());
			config.set("missions." + mission.getID() + ".item", new ItemStack(mission.getMissionItem()));
			config.set("missions." + mission.getID() + ".entity", mission.getEntity().toString());
			// TODO is this check still needed?
			if (mission.getLocation() != null && mission.getLocation().getWorld() != null)
				locationHelper(mission.getLocation(), config.createSection("location"));
			config.set("missions." + mission.getID() + ".display-name", Text.escape(mission.getDisplayName()));
			config.set("missions." + mission.getID() + ".timeframe", mission.getTimeframe());
			config.set("missions." + mission.getID() + ".reset-on-death", mission.resetsonDeath());
			config.set("missions." + mission.getID() + ".lore", Text.escape(mission.getDescription()));
			// Formerly ".citizen"
			config.set("missions." + mission.getID() + ".custom_int", mission.getCustomInt());
			// Formerly ".name"
			config.set("missions." + mission.getID() + ".custom_string", Text.escape(mission.getCustomString()));
			
			config.set("missions." + mission.getID() + ".exclude-spawners", !mission.acceptsSpawners());
		}
		if (parent != null) config.set("parent", String.valueOf(parent.getCategory().getID() + "-C" + parent.getID()));
		else config.set("parent", null);
		
		try {
			config.save(getFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	public int countFinishedTasks(Player p) {
		int i = 0;
		for (IMission task: getMissions())
			if (QuestWorld.getInstance().getManager(p).hasCompletedTask(task))
				++i;
		return i;
	}

	public List<Mission> getMissions() {
		return tasks;
	}
	
	private List<ItemStack> loadRewards() {
		List<ItemStack> items = new ArrayList<>();
		ConfigurationSection rewards = config.getConfigurationSection("rewards.items");
		if(rewards == null)
			return items;
		
		for(String key : rewards.getKeys(false))
			items.add(rewards.getItemStack(key));
		
		return items;
	}
	
	public void setItemRewards(Player p) {
		updateLastModified();
		rewards.clear();
		for (int i = 0; i < 9; i++) {
			ItemStack item = p.getInventory().getItem(i);
			if (item != null && item.getType() != null && item.getType() != Material.AIR) rewards.add(item.clone());
		}
	}

	public void setItem(ItemStack item) {
		updateLastModified();
		this.item = new ItemBuilder(item).display(name).get();
	}

	public void toggleWorld(String world) {
		updateLastModified();
		if (world_blacklist.contains(world)) world_blacklist.remove(world);
		else world_blacklist.add(world);
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		updateLastModified();
		this.name = Text.colorize(name);
		ItemBuilder.edit(this.item).display(name);
	}

	public List<ItemStack> getRewards() {
		return rewards;
	}
	
	public IMission getMission(int i) {
		return tasks.size() > i ? tasks.get(i): null;
	}
	
	public void addMission(IMission mission) {
		updateLastModified();
		if(mission instanceof MissionChange)
			tasks.add(((MissionChange)mission).getSource());
		else
			tasks.add((Mission)mission);
	}
	
	public void removeMission(IMission mission) {
		updateLastModified();
		if(mission instanceof MissionChange)
			tasks.remove(((MissionChange)mission).getSource());
		else
			tasks.remove((Mission)mission);
	}
	
	public void setPartySize(int size) {
		updateLastModified();
		partysize = size;
	}

	public long getRawCooldown() {
		return cooldown;
	}
	
	public void setRawCooldown(long cooldown) {
		updateLastModified();
		this.cooldown = cooldown;
	}
	
	public long getCooldown() {
		return cooldown / 60 / 1000;
	}

	public void setCooldown(long cooldown) {
		updateLastModified();
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
		updateLastModified();
		this.money = money;
	}
	
	public void setXP(int xp) {
		updateLastModified();
		this.xp = xp;
	}
	
	public void handoutReward(Player p) {
		ItemStack[] itemReward = rewards.toArray(new ItemStack[0]);
		for(ItemStack item : p.getInventory().addItem(itemReward).values())
			p.getWorld().dropItemNaturally(p.getLocation(), item);
		
		QuestWorld.getSounds().QuestReward().playTo(p);
		
		if (xp > 0) p.setLevel(p.getLevel() + xp);
		if (money > 0 && QuestWorld.getInstance().getEconomy() != null) QuestWorld.getInstance().getEconomy().get().depositPlayer(p, money);
		
		for (String command: commands) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("@p", p.getName()));
		}
		
		QuestWorld.getInstance().getManager(p).completeQuest(this);
	}

	public String getFormattedCooldown() {
		long cooldown = getCooldown();
		if(cooldown == -1)
			return "Single Use";
		
		if(cooldown == 0)
			return "Repeating";

		return Text.timeFromNum(cooldown);
	}
	
	public IQuest getParent() {
		return this.parent;
	}

	@Override
	public void setParent(IQuest quest) {
		updateLastModified();
		if(quest instanceof QuestChange)
			parent = ((QuestChange)quest).getSource();
		else
			parent = (Quest)quest;
	}

	public List<String> getCommands() {
		return commands;
	}

	public void removeCommand(int i) {
		updateLastModified();
		commands.remove(i);
	}

	public void addCommand(String command) {
		updateLastModified();
		commands.add(command);
	}

	@Override
	public String getPermission() {
		return permission;
	}

	@Override
	public void setPermission(String permission) {
		updateLastModified();
		this.permission = permission;
	}

	public boolean supportsParties() {
		return !disableParties;
	}

	public void setPartySupport(boolean supportsParties) {
		updateLastModified();
		this.disableParties = supportsParties;
	}

	public boolean isOrdered() {
		return ordered;
	}

	public void setOrdered(boolean ordered) {
		updateLastModified();
		this.ordered = ordered;
	}

	public boolean isAutoClaiming() {
		return autoclaim;
	}

	public void setAutoClaim(boolean autoclaim) {
		updateLastModified();
		this.autoclaim = autoclaim;
	}

	public boolean isWorldEnabled(String world) {
		return !world_blacklist.contains(world);
	}

	@Override
	public boolean isValid() {
		return category.isValid() && (category.getQuest(id) != null);
	}
	
	@Override
	public QuestChange getWriter() {
		return new QuestChange(this);
	}

	@Override
	public boolean apply() {
		return true;
	}

	@Override
	public boolean discard() {
		return false;
	}

	@Override
	public IQuest getSource() {
		return this;
	}

	@Override
	public boolean hasChange(Member field) {
		return true;
	}
}
