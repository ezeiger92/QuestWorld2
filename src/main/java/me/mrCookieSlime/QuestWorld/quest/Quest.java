package me.mrCookieSlime.QuestWorld.quest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestStatus;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.contract.IQuestWrite;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;
import me.mrCookieSlime.QuestWorld.util.Text;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
	
	public Quest(Category category, File file) {
		this.category = category;
		
		Config cfg = new Config(file);
		this.id = Integer.parseInt(file.getName().replace(".quest", "").split("-C")[0]);
		this.cooldown = cfg.getLong("cooldown");
		this.disableParties = cfg.getBoolean("disable-parties");
		this.ordered = cfg.getBoolean("in-order");
		this.autoclaim = cfg.getBoolean("auto-claim");
		this.name = Text.colorize(cfg.getString("name"));
		this.item = new ItemBuilder(cfg.getItem("item")).display(name).get();
		loadMissions(cfg);
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
		this.category = (Category)QuestWorld.getInstance().getCategory(Integer.parseInt(input.split(" M ")[0]));
		
		this.id = Integer.parseInt(input.split(" M ")[1]);
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
	
	public void updateParent(Config cfg) {
		if (cfg.contains("parent")) {
			Category c = (Category)QuestWorld.getInstance().getCategory(Integer.parseInt(cfg.getString("parent").split("-C")[0]));
			if (c != null) parent = c.getQuest(Integer.parseInt(cfg.getString("parent").split("-C")[1]));
		}
	}

	private void loadMissions(Config cfg) {
		if (!cfg.contains("missions"))
			return;

		for (String key: cfg.getKeys("missions")) {
			String prefix = "missions." + key;
			
			if(cfg.contains(prefix + ".citizen")) {
				int customInt = cfg.getInt(prefix + ".citizen");
				cfg.setValue(prefix + ".citizen", null);
				cfg.setValue(prefix + ".custom_int", customInt);
				cfg.save();
			}
			
			if(cfg.contains(prefix + ".name")) {
				String customString = cfg.getString(prefix + ".name");
				cfg.setValue(prefix + ".name", null);
				cfg.setValue(prefix + ".custom_string", customString);
				cfg.save();
			}

			QuestChange changes = new QuestChange(this);
			
			changes.addMission(new Mission(this, key,
					MissionType.valueOf(cfg.getString(prefix + ".type")),
					EntityType.valueOf(cfg.getString(prefix + ".entity")),
					Text.colorize(cfg.getString(prefix + ".custom_string")),
					cfg.getItem(prefix + ".item"),
					cfg.getLocation(prefix + ".location"),
					cfg.getInt(prefix + ".amount"),
					Text.colorize(cfg.getString(prefix + ".display-name")),
					cfg.contains(prefix + ".timeframe") ? cfg.getInt(prefix + ".timeframe"): 0,
					cfg.getBoolean(prefix + ".reset-on-death"),
					cfg.getInt(prefix + ".custom_int"),
					// not exclude = allow, what we want
					!cfg.getBoolean(prefix + ".exclude-spawners"),
					Text.colorize(cfg.getString(prefix + ".lore"))));
			
			if(changes.sendEvent())
				changes.apply();
		}
	}
	
	public void save() {
		Config cfg = new Config(new File("plugins/QuestWorld/quests/" + id + "-C" + category.getID() + ".quest"));
		cfg.setValue("id", id);
		cfg.setValue("category", category.getID());
		cfg.setValue("cooldown", String.valueOf(cooldown));
		cfg.setValue("name", Text.escape(name));
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
		for (IMission mission: tasks) {
			cfg.setValue("missions." + mission.getID() + ".type", mission.getType().toString());
			cfg.setValue("missions." + mission.getID() + ".amount", mission.getAmount());
			cfg.setValue("missions." + mission.getID() + ".item", new ItemStack(mission.getMissionItem()));
			cfg.setValue("missions." + mission.getID() + ".entity", mission.getEntity().toString());
			if (mission.getLocation() != null && mission.getLocation().getWorld() != null) cfg.setValue("missions." + mission.getID() + ".location", mission.getLocation());
			cfg.setValue("missions." + mission.getID() + ".display-name", Text.escape(mission.getDisplayName()));
			cfg.setValue("missions." + mission.getID() + ".timeframe", mission.getTimeframe());
			cfg.setValue("missions." + mission.getID() + ".reset-on-death", mission.resetsonDeath());
			cfg.setValue("missions." + mission.getID() + ".lore", Text.escape(mission.getDescription()));
			// Formerly ".citizen"
			cfg.setValue("missions." + mission.getID() + ".custom_int", mission.getCustomInt());
			// Formerly ".name"
			cfg.setValue("missions." + mission.getID() + ".custom_string", Text.escape(mission.getCustomString()));
			
			cfg.setValue("missions." + mission.getID() + ".exclude-spawners", !mission.acceptsSpawners());
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
