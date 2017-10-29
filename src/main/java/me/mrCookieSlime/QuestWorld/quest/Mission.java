package me.mrCookieSlime.QuestWorld.quest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.SinglePrompt;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionWrite;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.menu.QuestBook;
import me.mrCookieSlime.QuestWorld.util.PlayerTools;

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
	String id;
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
	
	Mission(String id, Quest quest) {
		this.id = id;
		this.quest = quest;
		loadDefaults();
	}
	
	// TODO id should not be a String
	public Mission(Quest quest, String id, MissionType type, EntityType entity, String customString,
			ItemStack item, Location location, int amount, String displayName, int timeframe,
			boolean deathReset, int customInt, boolean spawnersAllowed, String description) {
		this.quest = quest;
		this.id = id;
		this.type = type;
		this.item = item;
		this.amount = amount;
		this.entity = entity;
		this.customString = customString;
		this.location = location;
		this.displayName = displayName;
		this.timeframe = timeframe;
		this.customInt = customInt;
		this.deathReset = deathReset;
		this.description = description == null ? "": description;
		this.spawnersAllowed = spawnersAllowed;
		loadDialogue();
		if (this.customString == null) this.customString = "";
		
		// Repair any quests that would have been broken by updates, namely location quests
		MissionChange changes = new MissionChange(this);
		type.attemptUpgrade(changes);
		changes.apply();
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
	
	protected void loadDefaults() {
		type = QuestWorld.getMissionType("SUBMIT");
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

	public String getID() {
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
	}

	public void setAmount(int amount) {
		updateLastModified();
		this.amount = amount;
	}
	
	public String getCustomString() {
		return customString;
	}

	@Override
	public String getName() {
		return getText();
	}

	@Override
	public void setParent(IQuest quest) {}

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

	@Override
	public void setPermission(String permission) {}
	
	public void setupDialogue(Player p) {
		this.dialogue = new ArrayList<String>();
		addDialogueLine(p);
	}
	
	private void addDialogueLine(Player p) {
		String dprefix = QuestWorld.getInstance().getConfig().getString("dialogue.prefix");
		final Mission mission = this;
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
						QuestBook.openQuestMissionEditor(p2, mission);

						try {
							// The only downside to this is system-specific newlines
							Files.write(file.toPath(), dialogue, Charset.forName("UTF-8"));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					else {
						dialogue.add(dprefix + s);
						addDialogueLine(p2);
						QuestWorld.getSounds().DIALOG_ADD.playTo(p2);
					}
					return true;
				}
		));
	}

	public List<String> getDialogue() {
		return this.dialogue;
	}

	public void setDisplayName(String name) {
		updateLastModified();
		this.displayName = name;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}
	
	public int getTimeframe() {
		return timeframe;
	}
	
	public boolean hasTimeframe() {
		return this.timeframe > 0;
	}
	
	public void setTimeframe(int timeframe) {
		updateLastModified();
		this.timeframe = timeframe;
	}

	public boolean resetsonDeath() {
		return this.deathReset;
	}
	
	public void setDeathReset(boolean deathReset) {
		updateLastModified();
		this.deathReset = deathReset;
	}

	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String description) {
		updateLastModified();
		this.description = description;
	}

	public void setCustomInt(int val) {
		updateLastModified();
		this.customInt = val;
	}

	public int getCustomInt() {
		return customInt;
	}

	public boolean acceptsSpawners() {
		return spawnersAllowed;
	}

	public void setSpawnerSupport(boolean acceptsSpawners) {
		updateLastModified();
		this.spawnersAllowed = acceptsSpawners;
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
}
