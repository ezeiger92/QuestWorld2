package me.mrCookieSlime.QuestWorld.quests;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.utils.Text;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class Category extends QWObject {
	
	Map<Integer, Quest> quests;
	int id;
	String name;
	ItemStack item;
	
	Quest parent;
	String permission;
	boolean hidden;
	List<String> world_blacklist = new ArrayList<String>();
	
	public Category(String name, int id) {
		this.id = id;
		this.quests = new HashMap<Integer, Quest>();
		this.name = Text.colorize(name);
		this.item = new CustomItem(new MaterialData(Material.BOOK_AND_QUILL).toItemStack(1), name);
		this.world_blacklist = new ArrayList<String>();
		this.permission = "";
		this.hidden = false;
		
		QuestWorld.getInstance().registerCategory(this);
	}
	
	public Category(File file, List<File> quests) {
		this.id = Integer.parseInt(file.getName().replace(".category", ""));
		this.quests = new HashMap<Integer, Quest>();
		for (File f: quests) {
			new Quest(this, f);
		}
		Config cfg = new Config(file);
		this.name = Text.colorize(cfg.getString("name"));
		this.item = cfg.getItem("item");
		this.item = new CustomItem(item, name);
		this.hidden = cfg.getBoolean("hidden");
		if (cfg.contains("permission")) this.permission = cfg.getString("permission");
		if (cfg.contains("world-blacklist")) world_blacklist = cfg.getStringList("world-blacklist");
		else this.permission = "";
		
		QuestWorld.getInstance().registerCategory(this);
	}
	
	public void updateParent(Config cfg) {
		if (cfg.contains("parent")) {
			Category category = QuestWorld.getInstance().getCategory(Integer.parseInt(cfg.getString("parent").split("-C")[0]));
			if (category != null) parent = category.getQuest(Integer.parseInt(cfg.getString("parent").split("-C")[1]));
		}
	}
	
	public void addQuest(Quest quest) {
		quests.put(quest.getID(), quest);
	}
	
	public void removeQuest(Quest quest) {
		quest.updateLastModified();
		quests.remove(quest.getID());
		new File("plugins/QuestWorld/quests/" + quest.getID() + "-C" + getID() + ".quest").delete();
	}
	
	public void save() { save(false); }
	public void save(boolean force) {
		long lastSave = QuestWorld.getInstance().getLastSaved();
		for (Quest quest: quests.values()) {
			// Forcing save or quest appears changed
			if(force || lastSave < quest.getLastModified())
				quest.save();
		}
		
		// Not forcing a save and category appears unchanged
		if(!force && lastSave >= getLastModified())
			return;
		
		Config cfg = new Config(new File("plugins/QuestWorld/quests/" + id + ".category"));
		cfg.setValue("id", id);
		cfg.setValue("name", Text.escape(name));
		cfg.setValue("item", new ItemStack(item));
		cfg.setValue("permission", permission);
		cfg.setValue("hidden", this.hidden);
		cfg.setValue("world-blacklist", world_blacklist);
		
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
	
	public Collection<Quest> getQuests() {
		return quests.values();
	}
	
	public Set<Quest> getQuests(Player p, QuestStatus status) {
		Set<Quest> quests = new HashSet<Quest>();
		for (Quest quest: getQuests()) {
			if (quest.getStatus(p) == status) quests.add(quest);
		}
		return quests;
	}
	
	public Set<Quest> getFinishedQuests(Player p) {
		Set<Quest> quests = new HashSet<Quest>();
		for (Quest quest: getQuests()) {
			if (QuestWorld.getInstance().getManager(p).hasFinished(quest)) quests.add(quest);
		}
		return quests;
	}

	public String getProgress(Player p) {
		StringBuilder progress = new StringBuilder();
		float percentage = Math.round((((getFinishedQuests(p).size() * 100.0f) / getQuests().size()) * 100.0f) / 100.0f);
		
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
		
		progress.append(" - " + percentage + "%");
		
		return Text.colorize(progress.toString());
	}

	public Quest getQuest(int i) {
		return quests.get(i);
	}

	public void setItem(ItemStack item) {
		updateLastModified();
		this.item = new CustomItem(item, name);
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		updateLastModified();
		this.name = Text.colorize(name);
		this.item = new CustomItem(item, name);
	}
	
	public Quest getParent() {
		return this.parent;
	}

	public void setParent(Quest quest) {
		updateLastModified();
		this.parent = quest;
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
	
	public void setHidden(boolean hidden) {
		updateLastModified();
		this.hidden = hidden;
	}
	
	public boolean isHidden() {
		return this.hidden;
	}

	public boolean isWorldEnabled(String world) {
		return !world_blacklist.contains(world);
	}

	public void toggleWorld(String world) {
		updateLastModified();
		if (world_blacklist.contains(world)) world_blacklist.remove(world);
		else world_blacklist.add(world);
	}
	
	@Override
	public boolean isValid() {
		return QuestWorld.getInstance().getCategory(id) != null;
	}
}
