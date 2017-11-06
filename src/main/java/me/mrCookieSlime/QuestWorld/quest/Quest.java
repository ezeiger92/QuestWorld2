package me.mrCookieSlime.QuestWorld.quest;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.contract.IQuestState;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;
import me.mrCookieSlime.QuestWorld.util.Text;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

class Quest extends Renderable implements IQuestState {
	
	WeakReference<Category> category;
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
	
	WeakReference<Quest> parent;
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
	Quest(int id, YamlConfiguration file, Category category) {
		this.category = new WeakReference<>(category);
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
		this.category = new WeakReference<>(category);
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
			
			Category c = (Category)QuestWorld.get().getCategory(parts[1]);
			if (c != null)
				parent = new WeakReference<>(c.getQuest(parts[0]));
		}
	}
	
	File getFile() {
		return new File(QuestWorld.getPath("data.questing"), id + "-C" + getCategory().getID() + ".quest");
	}
	
	private void loadMissions() {
		ConfigurationSection missions = config.getConfigurationSection("missions");
		if (missions == null)
			return;

		for (String key: missions.getKeys(false)) {
			// TODO mess
			QuestState changes = new QuestState(this);
			Map<String, Object> data = missions.getConfigurationSection(key).getValues(false);
			// getValues wont recurse through sections, so we have to manually map to... map
			data.put("location", ((ConfigurationSection)data.get("location")).getValues(false));
			
			data.put("menu_index", Integer.valueOf(key));
			data.put("quest", this);
				
			Mission m = new Mission(data);
			changes.addMission(m);
			changes.apply();
		}
	}
	
	public void save() {
		config.set("id", id);
		config.set("category", getCategory().getID());
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
		
		config.set("rewards.items", rewards);
		
		/*int index = 0;
		for (ItemStack reward: rewards) {
			if (reward != null) {
				config.set("rewards.items." + index, reward);
				index++;
			}
		}*/
		for (Mission mission: tasks) {
			Map<String, Object> data = mission.serialize();
			// TODO keep a quest id
			data.remove("quest");
			config.set("missions." + mission.getIndex(), data);
			//mission.save(config.createSection("missions." + mission.getID()));
		}
		Quest parent = getParent();
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
		return category.get();
	}

	public List<Mission> getMissions() {
		return tasks;
	}
	
	private List<ItemStack> loadRewards() {
		@SuppressWarnings("unchecked")
		List<ItemStack> newItems = (List<ItemStack>) config.getList("rewards.items");
		
		if(newItems != null)
			return newItems;

		List<ItemStack> oldItems = new ArrayList<>();
		
		ConfigurationSection rewards = config.getConfigurationSection("rewards.items");
		if(rewards == null)
			return oldItems;
		
		for(String key : rewards.getKeys(false))
			oldItems.add(rewards.getItemStack(key));
		
		return oldItems;
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
		if(mission instanceof MissionState)
			tasks.add(((MissionState)mission).getSource());
		else
			tasks.add((Mission)mission);
	}
	
	public void removeMission(IMission mission) {
		updateLastModified();
		if(mission instanceof MissionState)
			tasks.remove(((MissionState)mission).getSource());
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
		ItemStack[] itemReward = rewards.toArray(new ItemStack[rewards.size()]);
		for(ItemStack item : p.getInventory().addItem(itemReward).values())
			p.getWorld().dropItemNaturally(p.getLocation(), item);
		
		QuestWorld.getSounds().QUEST_REWARD.playTo(p);
		
		if (xp > 0) p.setLevel(p.getLevel() + xp);
		if (money > 0 && QuestWorld.get().getEconomy() != null) QuestWorld.get().getEconomy().get().depositPlayer(p, money);
		
		for (String command: commands) 
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("@p", p.getName()));
	}

	public String getFormattedCooldown() {
		long cooldown = getCooldown();
		if(cooldown == -1)
			return "Single Use";
		
		if(cooldown == 0)
			return "Repeating";

		return Text.timeFromNum(cooldown);
	}
	
	public Quest getParent() {
		return parent.get();
	}

	@Override
	public void setParent(IQuest quest) {
		updateLastModified();
		Quest parent;
		if(quest instanceof QuestState)
			parent = ((QuestState)quest).getSource();
		else
			parent = (Quest)quest;
		
		this.parent = new WeakReference<>(parent);
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
	public QuestState getState() {
		return new QuestState(this);
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
	
	Quest(Map<String, Object> data) {
		loadMap(data);
	}
	
	public Map<String, Object> serialize() {
		return null;
	}
	
	private void loadMap(Map<String, Object> data) {
		
	}
}
