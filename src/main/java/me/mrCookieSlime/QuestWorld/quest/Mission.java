package me.mrCookieSlime.QuestWorld.quest;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionState;
import me.mrCookieSlime.QuestWorld.util.Text;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

class Mission extends Renderable implements IMissionState {

	private boolean spawnersAllowed;
	private int amount;
	private int customInt;
	private String customString;
	private String description;
	private ArrayList<String> dialogue = new ArrayList<>();
	private String displayName;
	private EntityType entity;
	private int index;
	private Location location;
	private ItemStack item;
	private WeakReference<Quest> quest;
	private int timeframe;
	private MissionType type;
	private boolean deathReset;

	public Mission(int menuIndex, Quest quest) {
		loadDefaults();
		this.index = menuIndex;
		this.quest = new WeakReference<>(quest);
	}

	public Mission(Map<String, Object> data) {
		loadMap(data);
		loadDialogue();
		
		// Repair any quests that would have been broken by updates, namely location quests
		MissionState changes = new MissionState(this);
		type.attemptUpgrade(changes);
		changes.apply();
	}

	@Override
	public boolean acceptsSpawners() {
		return spawnersAllowed;
	}

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
	public ItemStack getMissionItem() {
		return item.clone();
	}
	
	@Override
	public Quest getQuest() {
		return quest.get();
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
	public boolean resetsonDeath() {
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
	
	@Deprecated
	@Override
	public String getDialogueFilename() {
		return getDialogueFile().getName();
	}

	public HashMap<String, Object> serialize() {
		HashMap<String, Object> result = new HashMap<>();

		result.put("unique",   (int)getUnique());
		result.put("quest",    getQuest());
		result.put("type",     type.toString());
		result.put("item",     item);
		result.put("amount",   amount);
		result.put("entity",   entity.toString());
		result.put("location", locationHelper(location));
		result.put("index",     index);
		result.put("custom_string",  Text.escape(customString));
		result.put("display-name",   Text.escape(displayName));
		result.put("timeframe",      timeframe);
		result.put("reset-on-death", deathReset);
		result.put("lore",           Text.escape(description));
		result.put("custom_int",     customInt);
		result.put("exclude-spawners", !spawnersAllowed);
		
		return result;
	}

	@Override
	public void setAmount(int amount) {
		updateLastModified();
		this.amount = amount;
	}

	@Override
	public void setCustomInt(int val) {
		updateLastModified();
		customInt = val;
	}
	
	@Override
	public void setCustomString(String customString) {
		updateLastModified();
		this.customString = customString;
	}
	
	@Override
	public void setDeathReset(boolean deathReset) {
		updateLastModified();
		this.deathReset = deathReset;
	}
	
	@Override
	public void setDescription(String description) {
		updateLastModified();
		this.description = description;
	}
	
	@Override
	public void setDialogue(List<String> dialogue) {
		this.dialogue.clear();
		this.dialogue.addAll(dialogue);
	}

	@Override
	public void setDisplayName(String name) {
		updateLastModified();
		displayName = name;
	}
	
	@Override
	public void setEntity(EntityType entity) {
		updateLastModified();
		this.entity = entity;
	}
	
	@Override
	public void setItem(ItemStack item) {
		this.item = item.clone();
		this.item.setAmount(1);
	}
	
	@Override
	public void setLocation(Location loc) {
		updateLastModified();
		this.location = loc.clone();
	}

	@Override
	public void setSpawnerSupport(boolean acceptsSpawners) {
		updateLastModified();
		spawnersAllowed = acceptsSpawners;
	}

	@Override
	public void setType(MissionType type) {
		updateLastModified();
		this.type = type;
		// TODO: Something like this
		// type.attemptUpgrade(this);
	}
	
	@Override
	public void setTimeframe(int timeframe) {
		updateLastModified();
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
	public IMission getSource() {
		return this;
	}

	@Override
	public boolean hasChange(Member field) {
		return true;
	}
	
	@Override
	protected void updateLastModified() {
		// TODO Really take a look at this whole system again
		getQuest().updateLastModified();
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
	
	protected void loadDefaults() {
		loadMap(new HashMap<>());
	}

	protected void saveDialogue() {
		File file = getDialogueFile();
		if(file.exists())
			file.delete();
		
		updateLastModified();
		
		try {
			// The only downside to this is system-specific newlines
			Files.write(file.toPath(), dialogue, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadDialogue() {
		File file = getDialogueFile();
		if (file.exists()) {
			try {
				dialogue.addAll(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void loadMap(Map<String, Object> data) {
		setUnique((Integer)data.getOrDefault("unique", (int)getUnique()));
		
		quest    = new WeakReference<>((Quest)data.get("quest"));
		type     = QuestWorld.getMissionType((String)data.getOrDefault("type", "SUBMIT"));
		item     = (ItemStack)data.getOrDefault("item", new ItemStack(Material.STONE));
		amount   = (Integer)data.getOrDefault("amount", 1);
		entity   = EntityType.PLAYER;
		try { entity = EntityType.valueOf((String)data.get("entity")); }
		catch(Exception e) {}
		location = locationHelper((Map<String, Object>)data.get("location"));
		index    = (Integer)data.getOrDefault("index", -1);
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
	
	private File getDialogueFile() {
		return new File(QuestWorld.getPath("data.dialogue"), getQuest().getCategory().getID()
				+ "+" + getQuest().getID() + "+" + getIndex() + ".txt");
	}
}
