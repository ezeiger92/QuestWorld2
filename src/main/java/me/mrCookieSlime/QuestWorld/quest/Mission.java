package me.mrCookieSlime.QuestWorld.quest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

class Mission extends Renderable implements IMissionWrite {

	Quest quest;
	MissionType type;
	ItemStack item;
	int amount;
	int menuIndex;
	EntityType entity;
	Location location;
	String customString;
	String displayName;
	int timeframe;
	boolean deathReset;
	String description;
	int customInt;
	boolean spawnersAllowed;
	
	ArrayList<String> dialogue = new ArrayList<>();
	
	// External
	public Mission(int menuIndex, Quest quest) {
		loadDefaults();
		this.menuIndex = menuIndex;
		this.quest = quest;
	}

	// Package
	Mission(Map<String, Object> data) {
		loadMap(data);
		loadDialogue();
		
		// Repair any quests that would have been broken by updates, namely location quests
		MissionChange changes = new MissionChange(this);
		type.attemptUpgrade(changes);
		changes.apply();
	}
	
	protected void loadDefaults() {
		loadMap(new HashMap<>());
	}
	
	protected void copy(Mission source) {
		loadMap(source.serialize());
		quest = source.quest;

		dialogue = new ArrayList<>();
		dialogue.addAll(source.dialogue);
		
		updateLastModified();
	}
	
	protected void copyTo(Mission dest) {
		dest.copy(this);
	}
	
	File getDialogueFile() {
		return new File(QuestWorld.getPath("data.dialogue"), quest.getCategory().getID()
				+ "+" + quest.getID() + "+" + getID() + ".txt");
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

	public int getID() {
		return menuIndex;
	}
	
	public Quest getQuest() {
		return quest;
	}

	public int getAmount() {
		return amount;
	}
	
	public String getText() {
		if (getDisplayName().length() > 0)
			return getDisplayName();

		return type.userDescription(this);
	}
	
	public ItemStack getMissionItem() {
		return item;
	}

	public ItemStack getDisplayItem() {
		return type.userDisplayItem(this).clone();
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
	
	private static Location locationHelper(Map<String, Object> data) {
		String defaultWorld = Bukkit.getWorlds().get(0).getName();
		return new Location(
				Bukkit.getWorld((String)data.getOrDefault("world", defaultWorld)),
				(Double)data.getOrDefault("x", 0.0),
				(Double)data.getOrDefault("y", 64.0),
				(Double)data.getOrDefault("z", 0.0),
				((Double)data.getOrDefault("yaw", 0.0)).floatValue(),
				((Double)data.getOrDefault("pitch", 0.0)).floatValue());
	}
	
	private static Map<String, Object> locationHelper(Location location) {
		HashMap<String, Object> result = new HashMap<>();
		
		result.put("world", location.getWorld().getName());
		result.put("x", location.getX());
		result.put("y", location.getY());
		result.put("z", location.getZ());
		result.put("yaw", (double)location.getYaw());
		result.put("pitch", (double)location.getPitch());
		
		return result;
	}

	public Map<String, Object> serialize() {
		HashMap<String, Object> result = new HashMap<>();

		result.put("unique",   (int)getUnique());
		result.put("quest",    quest);
		result.put("type",     type.toString());
		result.put("item",     item);
		result.put("amount",   amount);
		result.put("entity",   entity.toString());
		result.put("location", locationHelper(location));
		result.put("menu_index",    menuIndex);
		result.put("custom_string", Text.escape(customString));
		result.put("display-name",  Text.escape(displayName));
		result.put("timeframe",     timeframe);
		result.put("deathReset",    deathReset);
		result.put("lore",          Text.escape(description));
		result.put("custom_int",    customInt);
		result.put("exclude-spawners", !spawnersAllowed);
		
		return result;
	}

	@SuppressWarnings("unchecked")
	private void loadMap(Map<String, Object> data) {
		setUnique((Integer)data.getOrDefault("unique", (int)getUnique()));
		
		quest    = (Quest)data.get("quest");
		type     = QuestWorld.getMissionType((String)data.getOrDefault("type", "SUBMIT"));
		item     = (ItemStack)data.getOrDefault("item", new ItemStack(Material.STONE));
		amount   = (Integer)data.getOrDefault("amount", 1);
		entity   = EntityType.PLAYER;
		try { entity = EntityType.valueOf((String)data.get("entity")); }
		catch(Exception e) {}
		location = locationHelper((Map<String, Object>)data.get("location"));
		menuIndex    = (Integer)data.getOrDefault("menu_index", -1);
		// Chain to handle old name
		customString = (String)data.getOrDefault("name", "");
		customString = Text.colorize((String)data.getOrDefault("custom_string", customString));
		displayName  = Text.colorize((String)data.getOrDefault("display-name", ""));
		timeframe    = (Integer)data.getOrDefault("timeframe", 0);
		deathReset   = (Boolean)data.getOrDefault("reset-on-death", false);
		description  = Text.colorize((String)data.getOrDefault("lore", "Hey there! Do this Quest."));
		// Chain to handle old name
		customInt    = (Integer)data.getOrDefault("citizen", 0);
		customInt    = (Integer)data.getOrDefault("custom_int", customInt);
		spawnersAllowed = !(Boolean)data.getOrDefault("exclude-spawners", false);
	}
}
