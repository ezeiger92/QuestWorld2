package me.mrCookieSlime.QuestWorld.quest;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionState;
import me.mrCookieSlime.QuestWorld.manager.ProgressTracker;
import me.mrCookieSlime.QuestWorld.util.Text;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

class Mission extends Renderable implements IMissionState {
	private WeakReference<Quest> quest;
	private int         amount = 1;
	private int         customInt = 0;
	private String      customString = "";
	private boolean     deathReset = false;
	private String      description = "Hey there! Do this Quest.";
	private ArrayList<String> dialogue = new ArrayList<>();
	private String      displayName = "";
	private EntityType  entity = EntityType.PLAYER;
	private ItemStack   item = new ItemStack(Material.STONE);
	private int         index = -1;
	private Location    location = Bukkit.getWorlds().get(0).getSpawnLocation();
	private boolean     spawnerSupport = true;
	private int         timeframe = 0;
	private MissionType type = QuestWorld.getMissionType("SUBMIT");

	public Mission(int menuIndex, Quest quest) {
		this.index = menuIndex;
		this.quest = new WeakReference<>(quest);
	}

	public Mission(Map<String, Object> data) {
		loadMap(data);
		ProgressTracker.loadDialogue(this);
	}
	
	protected Mission(Mission source) {
		copy(source);
	}
	
	public void sanitize() {
		// Repair any quests that would have been broken by updates, namely location quests
		MissionState changes = new MissionState(this);
		type.attemptUpgrade(changes);
		changes.apply();
	}

	//// IMission
	@Override
	public int getAmount() {
		return amount;
	}

	@Override
	public int getCustomInt() {
		return customInt;
	}
	
	@Override
	public String getCustomString() {
		return customString;
	}

	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public ArrayList<String> getDialogue() {
		return new ArrayList<>(dialogue);
	}
	
	@Override
	public String getDisplayName() {
		return displayName;
	}
	
	@Override
	public EntityType getEntity() {
		return entity;
	}

	@Override
	public int getIndex() {
		// TODO: return unique;
		return index;
	}

	@Override
	public Location getLocation() {
		return location;
	}
	
	@Override
	public ItemStack getItem() {
		return item.clone();
	}
	
	@Override
	public Quest getQuest() {
		return quest.get();
	}
	
	@Override
	public boolean getSpawnerSupport() {
		return spawnerSupport;
	}
	
	@Override
	public int getTimeframe() {
		return timeframe;
	}

	@Override
	public MissionType getType() {
		return type;
	}

	@Override
	public boolean getDeathReset() {
		return deathReset;
	}

	@Override
	public ItemStack getDisplayItem() {
		return type.userDisplayItem(this).clone();
	}
	
	@Override
	public MissionState getState() {
		return new MissionState(this);
	}
	
	@Override
	public String getText() {
		if (getDisplayName().length() > 0)
			return getDisplayName();

		return type.userDescription(this);
	}

	public HashMap<String, Object> serialize() {
		HashMap<String, Object> result = new HashMap<>(20);

		result.put("unique",   (int)getUnique());
		result.put("quest",    getQuest());
		result.put("type",     type.toString());
		result.put("item",     item);
		result.put("amount",   amount);
		result.put("entity",   entity.toString());
		result.put("location", locationHelper(location));
		result.put("index",    index);
		result.put("custom_string",  Text.escape(customString));
		result.put("display-name",   Text.escape(displayName));
		result.put("timeframe",      timeframe);
		result.put("reset-on-death", deathReset);
		result.put("lore",           Text.escape(description));
		result.put("custom_int",     customInt);
		result.put("exclude-spawners", !spawnerSupport);
		
		return result;
	}

	@Override
	public void setAmount(int amount) {
		this.amount = amount;
	}

	@Override
	public void setCustomInt(int val) {
		customInt = val;
	}
	
	@Override
	public void setCustomString(String customString) {
		this.customString = customString;
	}
	
	@Override
	public void setDeathReset(boolean deathReset) {
		this.deathReset = deathReset;
	}
	
	@Override
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public void setDialogue(List<String> dialogue) {
		this.dialogue.clear();
		this.dialogue.addAll(dialogue);
	}

	@Override
	public void setDisplayName(String name) {
		displayName = name;
	}
	
	@Override
	public void setEntity(EntityType entity) {
		this.entity = entity;
	}
	
	@Override
	public void setItem(ItemStack item) {
		this.item = item.clone();
		this.item.setAmount(1);
	}
	
	@Override
	public void setLocation(Location loc) {
		this.location = loc.clone();
	}

	@Override
	public void setSpawnerSupport(boolean acceptsSpawners) {
		spawnerSupport = acceptsSpawners;
	}

	@Override
	public void setType(MissionType type) {
		this.type = type;
	}
	
	@Override
	public void setTimeframe(int timeframe) {
		this.timeframe = timeframe;
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
	public Mission getSource() {
		return this;
	}

	@Override
	public boolean hasChange(Member field) {
		return true;
	}
	
	@Override
	protected void updateLastModified() {
		// TODO Remove if/when quests get their own files
		getQuest().updateLastModified();
	}
	
	protected void copy(Mission source) {
		setUnique(source.getUnique());
		quest = source.quest;
		spawnerSupport = source.spawnerSupport;
		amount = source.amount;
		customString = source.customString;
		description = source.description;
		displayName = source.displayName;
		entity = source.entity;
		index = source.index;
		location = source.location;
		item = source.item;
		timeframe = source.timeframe;
		type = source.type;
		deathReset = source.deathReset;
		dialogue = source.getDialogue();
	}
	
	protected void copyTo(Mission dest) {
		dest.copy(this);
	}
	
	protected void loadDefaults() {
		copy(new Mission(index, getQuest()));
	}

	@SuppressWarnings("unchecked")
	private void loadMap(Map<String, Object> data) {
		setUnique((Integer)data.getOrDefault("unique", (int)getUnique()));
		
		quest    = new WeakReference<>((Quest)data.get("quest"));
		type     = QuestWorld.getMissionType((String)data.getOrDefault("type", type.toString()));
		item     = (ItemStack)data.getOrDefault("item", item);
		amount   = (Integer)data.getOrDefault("amount", amount);
		try { entity = EntityType.valueOf((String)data.get("entity")); }
		catch(Exception e) {}
		location = locationHelper((Map<String, Object>)data.get("location"));
		index    = (Integer)data.getOrDefault("index", index);
		// Chain to handle old name
		customString = (String)data.getOrDefault("name", customString);
		customString = Text.colorize((String)data.getOrDefault("custom_string", customString));
		displayName  = Text.colorize((String)data.getOrDefault("display-name", displayName));
		timeframe    = (Integer)data.getOrDefault("timeframe", timeframe);
		deathReset   = (Boolean)data.getOrDefault("reset-on-death", deathReset);
		description  = Text.colorize((String)data.getOrDefault("lore", description));
		// Chain to handle old name
		customInt    = (Integer)data.getOrDefault("citizen", customInt);
		customInt    = (Integer)data.getOrDefault("custom_int", customInt);
		spawnerSupport = !(Boolean)data.getOrDefault("exclude-spawners", !spawnerSupport);
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
	
	private static HashMap<String, Object> locationHelper(Location location) {
		HashMap<String, Object> result = new HashMap<>();
		
		result.put("world", location.getWorld().getName());
		result.put("x", location.getX());
		result.put("y", location.getY());
		result.put("z", location.getZ());
		result.put("yaw", (double)location.getYaw());
		result.put("pitch", (double)location.getPitch());
		
		return result;
	}
}
