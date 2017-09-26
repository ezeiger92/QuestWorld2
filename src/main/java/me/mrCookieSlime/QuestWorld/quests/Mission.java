package me.mrCookieSlime.QuestWorld.quests;

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
import me.mrCookieSlime.QuestWorld.api.interfaces.IMissionWrite;
import me.mrCookieSlime.QuestWorld.utils.PlayerTools;
import me.mrCookieSlime.QuestWorld.utils.Text;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Mission extends QuestingObject implements IMissionWrite {
	
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
	
	List<String> dialogue = new ArrayList<String>();
	
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
		type.attemptUpgrade(this);
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
	
	protected Mission(Mission copy) {
		copy.copyTo(this);
	}
	
	protected void copyTo(Mission dest) {
		dest.quest = quest;
		dest.type = type;
		dest.item = item.clone();
		dest.amount = amount;
		dest.id = id;
		dest.entity = entity;
		dest.location = location.clone();
		dest.customString = customString;
		dest.displayName = displayName;
		dest.timeframe = timeframe;
		dest.deathReset = deathReset;
		dest.description = description;
		dest.customInt = customInt;
		dest.spawnersAllowed = spawnersAllowed;
		
		dest.dialogue = new ArrayList<String>();
		dest.dialogue.addAll(dialogue);
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
		quest.updateLastModified();
		this.item = new ItemStack(item);
		this.item.setAmount(1);
	}
	
	public EntityType getEntity() {
		return entity;
	}
	
	public void setEntity(EntityType entity) {
		quest.updateLastModified();
		this.entity = entity;
	}
	
	public void setCustomString(String customString) {
		quest.updateLastModified();
		this.customString = customString;
	}

	public MissionType getType() {
		return type;
	}

	public void setType(MissionType type) {
		quest.updateLastModified();
		this.type = type;
	}

	public void setAmount(int amount) {
		quest.updateLastModified();
		this.amount = amount;
	}
	
	public String getProgress(Player p) {
		StringBuilder progress = new StringBuilder();
		int amount = QuestWorld.getInstance().getManager(p).getProgress(this);
		int total = this.amount;

		// In the event that amount somehow exceeded total, clamp it.
		// TODO: Although this fix works, this situation shouldn't happen. Find the real cause.
		amount = Math.min(amount, total);
		
		float percentage = amount / (float)total;
		
		if (percentage < .16f) progress.append("&4");
		else if (percentage < .32f) progress.append("&c");
		else if (percentage < .48f) progress.append("&6");
		else if (percentage < .64f) progress.append("&e");
		else if (percentage < .80f) progress.append("&2");
		else progress = progress.append("&a");
		
		String bar = "::::::::::::::::::::";
		int prog = (int)(percentage * 20.f);
		int rest = 20 - prog;
		
		progress.append(bar.substring(0, prog));
		
		progress.append("&7");
		progress.append(bar.substring(0, rest));
		progress.append(" - ");
		
		progress.append(getType().progressString(percentage, amount, total));
		
		return Text.colorize(progress.toString());
	}
	
	public String getCustomString() {
		return customString;
	}

	@Override
	public String getName() {
		return getText();
	}

	@Override
	public void setParent(Quest quest) {}

	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location loc) {
		quest.updateLastModified();
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
				PlayerTools.makeTranslation(true, Translation.dialog_add),
				(c,s) -> {
					Player p2 = (Player) c.getForWhom();
					if (s.equalsIgnoreCase("exit()")) {
						quest.updateLastModified();
						PlayerTools.sendTranslation(p2, true, Translation.dialog_set, path);
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
		quest.updateLastModified();
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
		quest.updateLastModified();
		this.timeframe = timeframe;
	}

	public boolean resetsonDeath() {
		return this.deathReset;
	}
	
	public void setDeathReset(boolean deathReset) {
		quest.updateLastModified();
		this.deathReset = deathReset;
	}

	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String description) {
		quest.updateLastModified();
		this.description = description;
	}

	public void setCustomInt(int val) {
		quest.updateLastModified();
		this.customInt = val;
	}

	public int getCustomInt() {
		return customInt;
	}

	public boolean acceptsSpawners() {
		return spawnersAllowed;
	}

	public void setSpawnerSupport(boolean acceptsSpawners) {
		quest.updateLastModified();
		this.spawnersAllowed = acceptsSpawners;
	}
}
