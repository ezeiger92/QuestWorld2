package me.mrCookieSlime.QuestWorld.quest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
	
	private void loadDialogue() {
		String fileName = quest.getCategory().getID() + "+" + quest.getID() + "+" + getID() + ".txt";
		File file = new File("plugins/QuestWorld/dialogues/" + fileName);
		if (file.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line;
				while ((line = br.readLine()) != null) {
				   dialogue.add(line);
				}
				br.close();
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
		String fileName = quest.getCategory().getID() + "+" + quest.getID() + "+" + getID() + ".txt";
		String path = "plugins/QuestWorld/dialogues/" + fileName;
		File file = new File(path);
		if (file.exists()) file.delete();
		
		addDialogueLine(p, path);
	}
	
	public void addDialogueLine(Player p, final String path) {
		String dprefix = QuestWorld.getInstance().getCfg().getString("dialogue.prefix");
		final Mission mission = this;
		PlayerTools.promptInput(p, new SinglePrompt(
				PlayerTools.makeTranslation(true, Translation.MISSION_DIALOG_ADD),
				(c,s) -> {
					Player p2 = (Player) c.getForWhom();
					if (s.equalsIgnoreCase("exit()")) {
						updateLastModified();
						PlayerTools.sendTranslation(p2, true, Translation.MISSION_DIALOG_SET, path);
						QuestBook.openQuestMissionEditor(p2, mission);
						
						File file = new File(path);
						try {
							file.createNewFile();
							
							BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
							for (int i = 0; i < dialogue.size(); i++) {
								if (i == 0) writer.append(dialogue.get(i));
								else writer.append("\n" + dialogue.get(i));
							}
							writer.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					else {
						dialogue.add(dprefix + s);
						addDialogueLine(p2, path);
						QuestWorld.getSounds().DialogAdd().playTo(p2);
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
	public MissionChange getWriter() {
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
