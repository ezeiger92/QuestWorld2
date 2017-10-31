package me.mrCookieSlime.QuestWorld.quest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.SinglePrompt;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionWrite;
import me.mrCookieSlime.QuestWorld.api.menu.QuestBook;
import me.mrCookieSlime.QuestWorld.util.PlayerTools;
import me.mrCookieSlime.QuestWorld.util.Text;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

class Mission extends Renderable implements IMissionWrite, ConfigurationSerializable {
	
	Quest quest;
	MissionType type;
	ItemStack item;
	int amount;
	int id;
	EntityType entity;
	Location location;
	String customString;
	String displayName;
	int timeframe;
	boolean deathReset;
	String description;
	int customInt;
	boolean spawnersAllowed;
	
	ArrayList<String> dialogue = new ArrayList<String>();
	
	// External
	public Mission(int id, Quest quest) {
		this.id = id;
		this.quest = quest;
		type = QuestWorld.getMissionType("SUBMIT");
		loadDefaults();
	}

	// Package
	Mission(int id, ConfigurationSection config, Quest quest) {
		this.id = id;
		this.quest = quest;
		
		if(config.contains("citizen")) {
			config.set("custom_int", config.get("custom_int", config.get("citizen")));
			config.set("citizen", null);
		}
		
		if(config.contains("name")) {
			config.set("custom_string", config.get("custom_string", config.get("name")));
			config.set("name", null);
		}
		
		type         = QuestWorld.getMissionType(config.getString("type"));
		entity       = EntityType.valueOf(config.getString("entity"));
		customString = Text.colorize(config.getString("custom_string", ""));
		item         = config.getItemStack("item");
		location     = locationHelper(config.getConfigurationSection("location"));
		amount       = config.getInt("amount");
		displayName  = Text.colorize(config.getString("display-name", ""));
		timeframe    = config.getInt("timeframe");
		deathReset   = config.getBoolean("reset-on-death");
		customInt    = config.getInt("custom_int");
		
		// not exclude = allow, what we want
		spawnersAllowed = !config.getBoolean("exclude-spawners");
		description     = Text.colorize(config.getString("lore", ""));
		loadDialogue();
		
		// Repair any quests that would have been broken by updates, namely location quests
		MissionChange changes = new MissionChange(this);
		type.attemptUpgrade(changes);
		changes.apply();
	}
	
	File getDialogueFile() {
		return new File(QuestWorld.getPath("data.dialogue"), quest.getCategory().getID()
				+ "+" + quest.getID() + "+" + getID() + ".txt");
	}
	
	void save(ConfigurationSection config) {
		config.set("type", getType().toString());
		config.set("amount", getAmount());
		config.set("item", getMissionItem().clone());
		config.set("entity", getEntity().toString());
		// TODO is this check still needed?
		if (getLocation() != null && getLocation().getWorld() != null)
			locationHelper(getLocation(), config.createSection("location"));
		config.set("display-name", Text.escape(getDisplayName()));
		config.set("timeframe", getTimeframe());
		config.set("reset-on-death", resetsonDeath());
		config.set("lore", Text.escape(getDescription()));
		// Formerly ".citizen"
		config.set("custom_int", getCustomInt());
		// Formerly ".name"
		config.set("custom_string", Text.escape(getCustomString()));
		
		config.set("exclude-spawners", !acceptsSpawners());
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
	
	private void loadDialogue() {
		File file = getDialogueFile();
		if (file.exists()) {
			try {
				dialogue.addAll(Files.readAllLines(file.toPath(), Charset.forName("UTF-8")));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void loadDefaults() {
		item = new ItemStack(Material.STONE);
		amount = 1;
		entity = EntityType.PLAYER;
		customString = "";
		location = new Location(Bukkit.getWorlds().get(0), 0, 64, 0);
		displayName = null;
		timeframe = 0;
		deathReset = false;
		customInt = 0;
		spawnersAllowed = true;
		description = "Hey there! Do this Quest.";
	}
	
	protected Mission(Mission source) {
		copy(source);
	}
	
	protected void copy(Mission source) {
		quest = source.quest;
		type = source.type;
		item = source.item.clone();
		amount = source.amount;
		id = source.id;
		entity = source.entity;
		location = source.location.clone();
		customString = source.customString;
		displayName = source.displayName;
		timeframe = source.timeframe;
		deathReset = source.deathReset;
		description = source.description;
		customInt = source.customInt;
		spawnersAllowed = source.spawnersAllowed;

		dialogue = new ArrayList<String>();
		dialogue.addAll(source.dialogue);
		
		updateLastModified();
	}
	
	protected void copyTo(Mission dest) {
		dest.copy(this);
	}

	public int getID() {
		return id;
	}
	
	public Quest getQuest() {
		return quest;
	}

	public int getAmount() {
		return amount;
	}
	
	public String getText() {
		if (getDisplayName() != null)
			return getDisplayName();

		return type.userDescription(this);
	}
	
	public ItemStack getMissionItem() {
		return item;
	}

	public ItemStack getDisplayItem() {
		return type.userDisplayItem(this);
	}
	
	public void setItem(ItemStack item) {
		this.item = item.clone();
		this.item.setAmount(1);
	}
	
	public EntityType getEntity() {
		return entity;
	}
	
	public void setEntity(EntityType entity) {
		updateLastModified();
		this.entity = entity;
	}
	
	public void setCustomString(String customString) {
		updateLastModified();
		this.customString = customString;
	}

	public MissionType getType() {
		return type;
	}

	public void setType(MissionType type) {
		updateLastModified();
		this.type = type;
		// TODO: Something like this
		// type.attemptUpgrade(this);
	}

	public void setAmount(int amount) {
		updateLastModified();
		this.amount = amount;
	}
	
	public String getCustomString() {
		return customString;
	}

	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location loc) {
		updateLastModified();
		this.location = loc.clone();
	}

	public void setLocation(Player p) {
		setLocation(p.getLocation().getBlock().getLocation());
	}

	@Override
	public String getPermission() {
		return null;
	}
	
	public void setupDialogue(Player p) {
		this.dialogue = new ArrayList<String>();
		addDialogueLine(p);
	}
	
	private void addDialogueLine(Player p) {
		PlayerTools.promptInput(p, new SinglePrompt(
				PlayerTools.makeTranslation(true, Translation.MISSION_DIALOG_ADD),
				(c,s) -> {
					Player p2 = (Player) c.getForWhom();
					if (s.equalsIgnoreCase("exit()")) {
						File file = getDialogueFile();
						if(file.exists())
							file.delete();
						
						updateLastModified();
						PlayerTools.sendTranslation(p2, true, Translation.MISSION_DIALOG_SET, file.getName());
						QuestBook.openQuestMissionEditor(p2, this);

						try {
							// The only downside to this is system-specific newlines
							Files.write(file.toPath(), dialogue, Charset.forName("UTF-8"));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					else {
						dialogue.add(s);
						addDialogueLine(p2);
						QuestWorld.getSounds().DIALOG_ADD.playTo(p2);
					}
					return true;
				}
		));
	}

	public List<String> getDialogue() {
		return dialogue;
	}

	public void setDisplayName(String name) {
		updateLastModified();
		displayName = name;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public int getTimeframe() {
		return timeframe;
	}
	
	public boolean hasTimeframe() {
		return timeframe > 0;
	}
	
	public void setTimeframe(int timeframe) {
		updateLastModified();
		this.timeframe = timeframe;
	}

	public boolean resetsonDeath() {
		return deathReset;
	}
	
	public void setDeathReset(boolean deathReset) {
		updateLastModified();
		this.deathReset = deathReset;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		updateLastModified();
		this.description = description;
	}

	public void setCustomInt(int val) {
		updateLastModified();
		customInt = val;
	}

	public int getCustomInt() {
		return customInt;
	}

	public boolean acceptsSpawners() {
		return spawnersAllowed;
	}

	public void setSpawnerSupport(boolean acceptsSpawners) {
		updateLastModified();
		spawnersAllowed = acceptsSpawners;
	}
	
	@Override
	public void updateLastModified() {
		// TODO Really take a look at this whole system again
		((Quest)quest).updateLastModified();
	}
	
	@Override
	public MissionChange getState() {
		return new MissionChange(this);
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
	public IMission getSource() {
		return this;
	}

	@Override
	public boolean hasChange(Member field) {
		return true;
	}

	@Override
	public Map<String, Object> serialize() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static Mission deserialize(Map<String, Object> data) {
		return null;
	}
}
