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
	
	boolean partySupport;
	boolean ordered;
	boolean autoclaim;
	
	Quest parent;
	String permission;
	
	YamlConfiguration config;
	
	// Internal
	protected Quest(Quest quest) {
		copy(quest);
	}
	
	protected void copy(Quest source) {
		category = source.category;
		id       = source.id;
		config   = YamlConfiguration.loadConfiguration(getFile());
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
		partySupport   = source.partySupport;
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
	public Quest(int id, YamlConfiguration file, Category category) {
		this.category = category;
		this.id = id;
		
		config = file;
		cooldown     = config.getLong("cooldown");
		partySupport = !config.getBoolean("disable-parties");
		ordered      = config.getBoolean("in-order");
		autoclaim    = config.getBoolean("auto-claim");
		name         = Text.colorize(config.getString("name"));
		item         = new ItemBuilder(config.getItemStack("item")).display(name).get();
		
		loadMissions();
		
		rewards = loadRewards();
		money   = config.getInt("rewards.money");
		xp      = config.getInt("rewards.xp");
		
		commands        = config.getStringList("rewards.commands");
		world_blacklist = config.getStringList("world-blacklist");
		
		partysize  = config.getInt("min-party-size", 1);
		permission = config.getString("permission", "");
		
		category.addQuest(this);
	}

	// External
	public Quest(String name, int id, Category category) {
		this.id = id;
		this.category = category;
		this.name = Text.colorize(name);
		
		config = YamlConfiguration.loadConfiguration(getFile());
		cooldown = -1;
		item = new ItemBuilder(Material.BOOK_AND_QUILL).display(name).get();

		money = 0;
		xp = 0;
		parent = null;
		partySupport = true;
		permission = "";
		ordered = false;
		autoclaim = false;
		partysize = 1;
		
		category.addQuest(this);
	}
	
	public void refreshParent() {
		String parentId = config.getString("parent", null);
		if (parentId != null) {
			int[] parts = RenderableFacade.splitQuestString(parentId);
			
			Category c = (Category)QuestWorld.getInstance().getCategory(parts[1]);
			if (c != null)
				parent = c.getQuest(parts[0]);
		}
	}
	
	File getFile() {
		return new File(QuestWorld.getPath("data.questing"), id + "-C" + category.getID() + ".quest");
	}
	
	private Location locationHelper(ConfigurationSection loc) {
		/*World w = Bukkit.getWorld(loc.getString("world"));
		double x = loc.getDouble("x");
		double y = loc.getDouble("y");
		double z = loc.getDouble("z");
		float yaw = (float)loc.getDouble("yaw");
		float pitch = (float)loc.getDouble("pitch");*/
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
		// TODO: rename to partySupport
		config.set("disable-parties", !partySupport);
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
		for (Mission mission: tasks) {
			config.set("missions." + mission.getID() + ".type", mission.getType().toString());
			config.set("missions." + mission.getID() + ".amount", mission.getAmount());
			config.set("missions." + mission.getID() + ".item", new ItemStack(mission.getMissionItem()));
			config.set("missions." + mission.getID() + ".entity", mission.getEntity().toString());
			// TODO is this check still needed?
			if (mission.getLocation() != null && mission.getLocation().getWorld() != null)
				locationHelper(mission.getLocation(), config.createSection("missions." + mission.getID() + ".location"));
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
		for (Mission task: getMissions())
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
	
	public Mission getMission(int i) {
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
		
		QuestWorld.getSounds().QUEST_REWARD.playTo(p);
		
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
		return partySupport;
	}

	public void setPartySupport(boolean partySupport) {
		updateLastModified();
		this.partySupport = partySupport;
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
	public QuestChange getState() {
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
