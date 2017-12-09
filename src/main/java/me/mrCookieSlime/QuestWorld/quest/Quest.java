package me.mrCookieSlime.QuestWorld.quest;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import me.mrCookieSlime.QuestWorld.QuestWorldPlugin;
import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.contract.IQuestState;
import me.mrCookieSlime.QuestWorld.api.event.CancellableEvent;
import me.mrCookieSlime.QuestWorld.api.event.QuestCompleteEvent;
import me.mrCookieSlime.QuestWorld.util.Text;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

class Quest extends UniqueObject implements IQuestState {
	
	WeakReference<Category> category;
	int id;
	long cooldown;
	String name;
	ItemStack item = new ItemStack(Material.BOOK_AND_QUILL);
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
	
	WeakReference<Quest> parent = new WeakReference<>(null);
	String permission;
	
	YamlConfiguration config;
	
	// Internal
	protected Quest(Quest quest) {
		copy(quest);
	}
	
	protected void copy(Quest source) {
		category = source.category;
		id       = source.id;
		config   = YamlConfiguration.loadConfiguration(Facade.fileFor(this));
		cooldown = source.cooldown;
		name     = source.name;
		item     = source.item.clone();

		tasks.clear();
		tasks.addAll(source.tasks);
		commands.clear();
		commands.addAll(source.commands);
		world_blacklist.clear();
		world_blacklist.addAll(source.world_blacklist);
		rewards.clear();
		rewards.addAll(source.rewards);

		money          = source.money;
		xp             = source.xp;
		partysize      = source.partysize;
		partySupport   = source.partySupport;
		ordered        = source.ordered;
		autoclaim      = source.autoclaim;
		parent         = source.parent;
		permission     = source.permission;
	}
	
	protected void copyTo(Quest dest) {
		dest.copy(this);
	}
	
	private long fromMaybeString(Object o) {
		if(o instanceof Long || o instanceof Integer)
			return ((Number)o).longValue();
		if(o instanceof String)
			return Long.valueOf((String)o);
		
		throw new IllegalArgumentException("Expected (Long) Integer or String, got " + o.getClass().getSimpleName());
	}
	
	// Package
	Quest(int id, YamlConfiguration file, Category category) {
		this.category = new WeakReference<>(category);
		this.id = id;
		
		config = file;
		cooldown     = fromMaybeString(config.get("cooldown"));
		partySupport = !config.getBoolean("disable-parties");
		ordered      = config.getBoolean("in-order");
		autoclaim    = config.getBoolean("auto-claim");
		name         = config.getString("name");
		item         = config.getItemStack("item", item);
		
		rewards = loadRewards();
		money   = config.getInt("rewards.money");
		xp      = config.getInt("rewards.xp");
		
		commands        = config.getStringList("rewards.commands");
		world_blacklist = config.getStringList("world-blacklist");
		
		partysize  = config.getInt("min-party-size", 1);
		permission = config.getString("permission", "");
		
		loadMissions();
	}

	// External
	public Quest(String name, int id, Category category) {
		this.id = id;
		this.category = new WeakReference<>(category);
		this.name = name;
		
		config = YamlConfiguration.loadConfiguration(Facade.fileFor(this));
		cooldown = -1;

		money = 0;
		xp = 0;
		partySupport = true;
		permission = "";
		ordered = false;
		autoclaim = false;
		partysize = 1;
	}
	
	public void refreshParent() {
		parent = new WeakReference<>(
				Facade.questOfString(config.getString("parent", null)));
	}
	
	private void loadMissions() {
		ConfigurationSection missions = config.getConfigurationSection("missions");
		if (missions == null)
			return;
		
		ArrayList<Mission> arr = new ArrayList<>();

		for (String key: missions.getKeys(false)) {
			// TODO mess
			//QuestState changes = new QuestState(this);
			Map<String, Object> data = missions.getConfigurationSection(key).getValues(false);
			// getValues wont recurse through sections, so we have to manually map to... map
			data.put("location", ((ConfigurationSection)data.get("location")).getValues(false));
			
			data.put("index", Integer.valueOf(key));
			data.put("quest", this);
			
			Mission m = new Mission(data);
			m.sanitize();
			arr.add(m);
		}
		
		QuestState state = getState();
		for(Mission m : arr)
			state.directAddMission(m);
		state.apply();
	}
	
	public void save() {
		config.set("id", id);
		config.set("category", getCategory().getID());
		config.set("cooldown", cooldown);
		config.set("name", name);
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
		
		for (Mission mission: tasks) {
			Map<String, Object> data = mission.serialize();
			// TODO keep a quest id
			data.remove("quest");
			config.set("missions." + mission.getIndex(), data);
			//mission.save(config.createSection("missions." + mission.getID()));
		}

		config.set("parent", Facade.stringOfQuest(getParent()));
		
		try {
			config.save(Facade.fileFor(this));
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
		return Collections.unmodifiableList(tasks);
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
		rewards.clear();
		for (int i = 0; i < 9; i++) {
			ItemStack item = p.getInventory().getItem(i);
			if (item != null && item.getType() != null && item.getType() != Material.AIR) rewards.add(item.clone());
		}
	}

	public void setItem(ItemStack item) {
		this.item = item.clone();
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
		this.name = name;
	}

	public List<ItemStack> getRewards() {
		return rewards;
	}
	
	public Mission getMission(int i) {
		return tasks.size() > i ? tasks.get(i): null;
	}
	
	public void addMission(int index) {
		tasks.add(getCategory().getFacade().createMission(index, getSource()));
	}
	
	public void directAddMission(Mission m) {
		tasks.add(m);
	}
	
	public void removeMission(IMission mission) {
		tasks.remove(((Mission)mission).getSource());
	}
	
	public void setPartySize(int size) {
		partysize = size;
	}

	public long getRawCooldown() {
		return cooldown;
	}
	
	public void setRawCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
	
	public long getCooldown() {
		return cooldown / 60 / 1000;
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
	
	@Override
	public void clearAllUserData() {
		getCategory().getFacade().clearAllUserData(getSource());
	}
	
	@Override
	public boolean completeFor(Player p) {
		if(CancellableEvent.send(new QuestCompleteEvent(getSource(), p))) {
			handoutReward(p);
			QuestWorldPlugin.getImpl().getPlayerStatus(p).completeQuest(this);
			return true;
		}
		
		return false;
	}
	
	private void handoutReward(Player p) {
		ItemStack[] itemReward = rewards.toArray(new ItemStack[rewards.size()]);
		for(ItemStack item : p.getInventory().addItem(itemReward).values())
			p.getWorld().dropItemNaturally(p.getLocation(), item);
		
		QuestWorld.getSounds().QUEST_REWARD.playTo(p);
		
		if (xp > 0) p.setLevel(p.getLevel() + xp);
		if (money > 0 && QuestWorld.getEconomy() != null) QuestWorld.getEconomy().depositPlayer(p, money);
		
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
		parent = new WeakReference<>(quest != null ? ((Quest)quest).getSource() : null);
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
		return partySupport;
	}

	public void setPartySupport(boolean partySupport) {
		this.partySupport = partySupport;
	}

	public boolean getOrdered() {
		return ordered;
	}

	public void setOrdered(boolean ordered) {
		this.ordered = ordered;
	}

	public boolean getAutoClaimed() {
		return autoclaim;
	}

	public void setAutoClaim(boolean autoclaim) {
		this.autoclaim = autoclaim;
	}

	public boolean getWorldEnabled(String world) {
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
	public Quest getSource() {
		return this;
	}

	@Override
	public boolean hasChange(Member field) {
		return true;
	}
	
	@Deprecated
	Quest(Map<String, Object> data) {
		loadMap(data);
	}
	
	@Deprecated
	public Map<String, Object> serialize() {
		return null;
	}
	
	@Deprecated
	private void loadMap(Map<String, Object> data) {
		
	}
}
