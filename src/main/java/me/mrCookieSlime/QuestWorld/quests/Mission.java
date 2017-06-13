package me.mrCookieSlime.QuestWorld.quests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.MenuHelper;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.MenuHelper.ChatHandler;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMissionWrite;
import me.mrCookieSlime.QuestWorld.utils.PlayerTools;
import me.mrCookieSlime.QuestWorld.utils.Text;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;
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
	String name;
	String displayName;
	long timeframe;
	boolean deathReset;
	String lore;
	int custom_int;
	boolean spawnersAllowed;
	
	List<String> dialogue = new ArrayList<String>();
	
	public Mission(Quest quest, String id, MissionType type, EntityType entity, String name, ItemStack item, Location location, int amount, String displayName, long timeframe, boolean deathReset, int custom_int, boolean spawnersAllowed, String lore) {
		this.quest = quest;
		this.id = id;
		this.type = type;
		this.item = item;
		this.amount = amount;
		this.entity = entity;
		this.name = name;
		this.location = location;
		this.displayName = displayName;
		this.timeframe = timeframe;
		this.custom_int = custom_int;
		this.deathReset = deathReset;
		this.lore = lore == null ? "": lore;
		this.spawnersAllowed = spawnersAllowed;

		File file = new File("plugins/QuestWorld/dialogues/" + quest.getCategory().getID() + "+" + quest.getID() + "+" + getID() + ".txt");
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
		
		if (this.name == null) this.name = "";
		
		// Repair any quests that would have been broken by updates, namely location quests
		type.attemptUpgrade(this);
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
		if (getCustomName() != null)
			return getCustomName();

		return type.defaultDisplayName(this);
	}
	
	public ItemStack getMissionItem() {
		return item;
	}

	public ItemStack getDisplayItem() {
		return type.displayItem(this);
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
	
	public String getEntityName() {
		return name;
	}
	
	public void setEntityName(String name) {
		quest.updateLastModified();
		this.name = name;
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
		
		// Moved location radius to "customInt" variable, rather than "amount"
		/* // Location is a one-time thing, we don't want to display "(1/6)" or something silly
		if(getType().getSubmissionType() == SubmissionType.LOCATION) {
			total = 1;
		}*/
		
		// In the event that amount somehow exceeded total, clamp it.
		// TODO: Although this fix works, this situation shouldn't happen. Find the real cause.
		amount = Math.min(amount, total);
		
		//float percentage = Math.round((amount * 100.0f) / total);
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

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setParent(Quest quest) {}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Player p) {
		quest.updateLastModified();
		this.location = p.getLocation().getBlock().getLocation();
	}

	@Override
	public String getPermission() {
		return null;
	}

	@Override
	public void setPermission(String permission) {}
	
	public void setupDialogue(Player p) {
		this.dialogue = new ArrayList<String>();
		String path = "plugins/QuestWorld/dialogues/" + quest.getCategory().getID() + "+" + quest.getID() + "+" + getID() + ".txt";
		File file = new File(path);
		if (file.exists()) file.delete();
		
		addDialogueLine(p, path);
	}
	
	public void addDialogueLine(Player p, final String path) {
		PlayerTools.sendTranslation(p, true, Translation.dialog_add);
		String dprefix = QuestWorld.getInstance().getCfg().getString("dialogue.prefix");
		final Mission mission = this;
		MenuHelper.awaitChatInput(p, true, new ChatHandler() {
			
			@Override
			public boolean onChat(Player p, String message) {
				if (message.equalsIgnoreCase("exit()")) {
					quest.updateLastModified();
					PlayerTools.sendTranslation(p, true, Translation.dialog_set, path);
					QuestBook.openQuestMissionEditor(p, mission);
					
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
					dialogue.add(dprefix + message);
					addDialogueLine(p, path);
					QuestWorld.getSounds().DialogAdd().playTo(p);
				}
				return true;
			}
		});
	}

	public List<String> getDialogue() {
		return this.dialogue;
	}

	public void setCustomName(String name) {
		quest.updateLastModified();
		this.displayName = name;
	}
	
	public String getCustomName() {
		return this.displayName;
	}
	
	public long getTimeframe() {
		return this.timeframe;
	}
	
	public boolean hasTimeframe() {
		return this.timeframe > 0;
	}
	
	public void setTimeframe(long timeframe) {
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

	public String getLore() {
		return this.lore;
	}
	
	public void setLore(String lore) {
		quest.updateLastModified();
		this.lore = lore;
	}

	public void setCustomInt(int val) {
		quest.updateLastModified();
		this.custom_int = val;
	}

	@Deprecated
	public NPC getCitizen() {
		return CitizensAPI.getNPCRegistry().getById(custom_int);
	}

	public int getCustomInt() {
		return custom_int;
	}

	public boolean acceptsSpawners() {
		return spawnersAllowed;
	}

	public void setSpawnerSupport(boolean acceptsSpawners) {
		quest.updateLastModified();
		this.spawnersAllowed = acceptsSpawners;
	}

}
