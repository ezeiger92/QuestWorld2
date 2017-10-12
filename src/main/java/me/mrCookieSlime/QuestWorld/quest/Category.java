package me.mrCookieSlime.QuestWorld.quest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.QuestStatus;
import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.ICategoryWrite;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;
import me.mrCookieSlime.QuestWorld.util.Text;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

class Category extends Renderable implements ICategoryWrite {
	
	Map<Integer, Quest> quests = new HashMap<>();;
	int id;
	String name;
	ItemStack item;
	
	Quest parent;
	String permission;
	boolean hidden;
	List<String> world_blacklist = new ArrayList<String>();
	FileConfiguration config;
	
	// Interal
	protected Category(Category cat) {
		copy(cat);
	}
	
	protected void copy(Category source) {
		id         = source.id;
		name       = source.name;
		item       = source.item.clone();
		parent     = source.parent;
		permission = source.permission;
		hidden     = source.hidden;
		
		world_blacklist = new ArrayList<>();
		world_blacklist.addAll(source.world_blacklist);
	}
	
	protected void copyTo(Category dest) {
		dest.copy(this);
	}
	
	// External
	public Category(String name, int id) {
		this.id = id;
		name = Text.colorize(name);
		item = new ItemBuilder(Material.BOOK_AND_QUILL).display(name).get();
		world_blacklist = new ArrayList<String>();
		permission = "";
		hidden = false;
		
		QuestWorld.getInstance().registerCategory(this);
	}
	
	// Package
	Category(int id, FileConfiguration config, List<FileConfiguration> quests) {
		this.id = id;
		if(quests != null)
			for (FileConfiguration f: quests) {
				new Quest(this, f);
			}
		this.config = config;
		name = Text.colorize(config.getString("name"));
		item = new ItemBuilder(config.getItemStack("item")).display(name).get();
		hidden = config.getBoolean("hidden");
		permission = config.getString("permission", "");
		world_blacklist = config.getStringList("world-blacklist");
		
		QuestWorld.getInstance().registerCategory(this);
	}
	
	public void refreshParent() {
		String parentId = config.getString("parent", null);
		if (parentId != null) {
			String[] parts = parentId.split("-C");
			Category category = (Category)QuestWorld.getInstance().getCategory(Integer.parseInt(parts[0]));
			if (category != null)
				parent = category.getQuest(Integer.parseInt(parts[1]));
		}
	}
	
	File getFile() {
		String path = QuestWorld.getInstance().getConfig().getString("save.questdata");
		return new File(path + id + ".category");
	}
	
	public void addQuest(IQuest quest) {
		if(quest instanceof QuestChange)
			quests.put(quest.getID(), ((QuestChange)quest).getSource());
		else
			quests.put(quest.getID(), (Quest)quest);
	}
	
	public void removeQuest(IQuest quest) {
		// TODO maybe was needed
		//quest.updateLastModified();
		quests.remove(quest.getID());
		new File("plugins/QuestWorld/quests/" + quest.getID() + "-C" + getID() + ".quest").delete();
	}
	
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
		
		//Config cfg = new Config(new File("plugins/QuestWorld/quests/" + id + ".category"));
		config.set("id", id);
		config.set("name", Text.escape(name));
		config.set("item", item);
		config.set("permission", permission);
		config.set("hidden", this.hidden);
		config.set("world-blacklist", world_blacklist);
		
		if (parent != null) config.set("parent", String.valueOf(parent.getCategory().getID() + "-C" + parent.getID()));
		else config.set("parent", null);
		
		try {
			config.save(getFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	public int countQuests(Player p, QuestStatus status) {
		int i = 0;
		for(Quest quest : getQuests())
			if(quest.getStatus(p) == status)
				++i;
		return i;
	}
	
	public int countFinishedQuests(Player p) {
		int i = 0;
		for(Quest quest : getQuests())
			if(QuestWorld.getInstance().getManager(p).hasFinished(quest))
				++i;
		return i;
	}

	public String getProgress(Player p) {
		return Text.progressBar(
				countFinishedQuests(p),
				getQuests().size(),
				null);
	}

	public Quest getQuest(int i) {
		return quests.get(i);
	}

	public void setItem(ItemStack item) {
		updateLastModified();
		if(name != null)
			this.item = new ItemBuilder(item).display(name).get();
		else
			this.item = item.clone();
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		updateLastModified();
		this.name = Text.colorize(name);
		ItemBuilder.edit(this.item).display(name);
	}
	
	public IQuest getParent() {
		return this.parent;
	}

	@Override
	public void setParent(IQuest quest) {
		updateLastModified();
		this.parent = (Quest)quest;
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
	
	@Override
	public CategoryChange getWriter() {
		return new CategoryChange(this);
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
	public ICategory getSource() {
		return this;
	}

	@Override
	public boolean hasChange(Member field) {
		return true;
	}
}
