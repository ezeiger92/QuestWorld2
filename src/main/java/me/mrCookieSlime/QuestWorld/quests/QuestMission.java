package me.mrCookieSlime.QuestWorld.quests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Variable;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.MenuHelper;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.MenuHelper.ChatHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.quests.MissionType.SubmissionType;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;
import me.mrCookieSlime.QuestWorld.utils.Text;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class QuestMission extends QWObject {
	
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
	int citizen;
	boolean spawners;
	
	List<String> dialogue = new ArrayList<String>();
	
	public QuestMission(Quest quest, String id, MissionType type, EntityType entity, String name, ItemStack item, Location location, int amount, String displayName, long timeframe, boolean deathReset, int citizen, boolean spawners, String lore) {
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
		this.citizen = citizen;
		this.deathReset = deathReset;
		this.lore = lore == null ? "": lore;
		this.spawners = spawners;

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
		if (getCustomName() != null) return getCustomName();
		return ChatColor.GRAY + type.getFormat(entity, item, location, amount, ChatColor.translateAlternateColorCodes('&', name), citizen, spawners) + (hasTimeframe() ? (" &7within " + (getTimeframe() / 60) + "h " + (getTimeframe() % 60) + "m"): "") + (resetsonDeath() ? " &7without dying": "");
	}

	@SuppressWarnings("deprecation")
	public ItemStack getItem() {
		switch (type.getSubmissionType()) {
		case ENTITY:
			return new ItemBuilder(Material.MONSTER_EGG).mob(entity).display("&7Entity Type: &r" + Text.niceName(entity.name())).get();
		case ITEM:
		case BLOCK:
		case CITIZENS_ITEM:
			return item.clone();
		case INTEGER:
			return new CustomItem(Material.COMMAND, "&7" + amount, 0);
		case TIME:
			return new ItemStack(Material.WATCH);
		case LOCATION:
			return new CustomItem(Material.LEATHER_BOOTS, "&7X: " + location.getBlockX() + " Y: " + location.getBlockY() + " Z: " + location.getBlockZ(), 0);
		case CITIZENS_INTERACT:
			return type.getItem().toItemStack(1);
		case CITIZENS_KILL:
			return new MaterialData(Material.SKULL_ITEM, (byte) 3).toItemStack(1);
		default:
			return new ItemStack(Material.COMMAND);
		}
	}
	
	public void setItem(ItemStack item) {
		this.item = new CustomItem(item, 1);
	}
	
	public EntityType getEntity() {
		return entity;
	}
	
	public void setEntity(EntityType entity) {
		this.entity = entity;
	}
	
	public String getEntityName() {
		return name;
	}
	
	public void setEntityName(String name) {
		this.name = name;
	}

	public MissionType getType() {
		return type;
	}

	public void setType(MissionType type) {
		this.type = type;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	public String getProgress(Player p) {
		StringBuilder progress = new StringBuilder();
		int amount = QuestWorld.getInstance().getManager(p).getProgress(this);
		float percentage = Math.round((((amount * 100.0f) / getAmount()) * 100.0f) / 100.0f);
		
		if (percentage < 16.0F) progress.append("&4");
		else if (percentage < 32.0F) progress.append("&c");
		else if (percentage < 48.0F) progress.append("&6");
		else if (percentage < 64.0F) progress.append("&e");
		else if (percentage < 80.0F) progress.append("&2");
		else progress = progress.append("&a");
		
		int rest = 20;
		for (int i = (int) percentage; i >= 5; i = i - 5) {
			progress.append(":");
			rest--;
		}
		
		progress.append("&7");
		
		for (int i = 0; i < rest; i++) {
			progress.append(":");
		}
		if (getType().getSubmissionType().equals(SubmissionType.TIME)) {
			int remaining = getAmount() - amount;
			progress.append(" - " + percentage + "% (" + (remaining / 60) + "h " + (remaining % 60) + "m remaining)");
		}
		else progress.append(" - " + percentage + "% (" + amount + "/" + getAmount() + ")");
		
		return ChatColor.translateAlternateColorCodes('&', progress.toString());
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
		QuestWorld.getInstance().getLocalization().sendTranslation(p, "editor.add-dialogue", true);
		String dprefix = QuestWorld.getInstance().getCfg().getString("dialogue.prefix");
		final QuestMission mission = this;
		MenuHelper.awaitChatInput(p, true, new ChatHandler() {
			
			@Override
			public boolean onChat(Player p, String message) {
				if (message.equalsIgnoreCase("exit()")) {
					QuestWorld.getInstance().getLocalization().sendTranslation(p, "editor.set-dialogue", true, new Variable("<path>", path));
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
		this.timeframe = timeframe;
	}

	public boolean resetsonDeath() {
		return this.deathReset;
	}
	
	public void setDeathReset(boolean deathReset) {
		this.deathReset = deathReset;
	}

	public String getLore() {
		return this.lore;
	}
	
	public void setLore(String lore) {
		this.lore = lore;
	}

	public void setCitizen(int id) {
		this.citizen = id;;
	}

	public NPC getCitizen() {
		return CitizensAPI.getNPCRegistry().getById(citizen);
	}

	public int getCitizenID() {
		return citizen;
	}

	public boolean acceptsSpawners() {
		return !spawners;
	}

	public void setSpawnerSupport(boolean acceptsSpawners) {
		this.spawners = acceptsSpawners;
	}

}
