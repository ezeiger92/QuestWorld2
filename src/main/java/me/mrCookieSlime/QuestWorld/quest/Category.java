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
import me.mrCookieSlime.QuestWorld.api.contract.ICategoryState;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;
import me.mrCookieSlime.QuestWorld.util.Text;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

class Category extends Renderable implements ICategoryState {
	private int id;
	private boolean hidden;
	private String name;
	private String permission;
	private ItemStack item;
	private Quest parent;
	private Map<Integer, Quest> quests = new HashMap<>();
	private List<String> world_blacklist = new ArrayList<String>();
	
	private YamlConfiguration config;

	// External
	public Category(String name, int id) {
		this.id = id;
		this.name = Text.colorize(name);
		config = YamlConfiguration.loadConfiguration(getFile());
		item = new ItemBuilder(Material.BOOK_AND_QUILL).display(name).get();
		world_blacklist = new ArrayList<String>();
		permission = "";
		hidden = false;
		
		QuestWorld.getInstance().registerCategory(this);
	}
	
	// Package
	Category(int id, YamlConfiguration config) {
		this.id = id;
		this.config = config;
		name = Text.colorize(config.getString("name"));
		item = new ItemBuilder(config.getItemStack("item")).display(name).get();
		hidden = config.getBoolean("hidden");
		permission = config.getString("permission", "");
		world_blacklist = config.getStringList("world-blacklist");
		
		QuestWorld.getInstance().registerCategory(this);
	}
	
	// Interal
	protected Category(Category cat) {
		copy(cat);
	}
	
	protected void copy(Category source) {
		id         = source.id;
		config     = YamlConfiguration.loadConfiguration(getFile());
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
	
	//// ICategory Impl
	@Override
	public int getID() {
		return id;
	}
	
	@Override
	public boolean isHidden() {
		return this.hidden;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getPermission() {
		return permission;
	}

	@Override
	public ItemStack getItem() {
		return item.clone();
	}
	
	@Override
	public IQuest getParent() {
		return this.parent;
	}
	
	@Override
	public Collection<Quest> getQuests() {
		return quests.values();
	}

	@Override
	public Quest getQuest(int i) {
		return quests.get(i);
	}
	
	@Override
	public boolean isWorldEnabled(String world) {
		return !world_blacklist.contains(world);
	}

	@Override
	public int countQuests(Player p, QuestStatus status) {
		int i = 0;
		for(Quest quest : getQuests())
			if(quest.getStatus(p) == status)
				++i;
		return i;
	}
	
	@Override
	public int countFinishedQuests(Player p) {
		int i = 0;
		for(Quest quest : getQuests())
			if(QuestWorld.getInstance().getManager(p).hasFinished(quest))
				++i;
		return i;
	}
	
	@Override
	public CategoryChange getState() {
		return new CategoryChange(this);
	}

	//// ICategoryState Impl
	public void setName(String name) {
		updateLastModified();
		this.name = Text.colorize(name);
		ItemBuilder.edit(this.item).display(name);
	}

	@Override
	public void setParent(IQuest quest) {
		updateLastModified();
		this.parent = (Quest)quest;
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
	
	//// DONE
	
	public void refreshParent() {
		String parentId = config.getString("parent", null);
		if (parentId != null) {
			int[] parts = RenderableFacade.splitQuestString(parentId);
			
			Category c = (Category)QuestWorld.getInstance().getCategory(parts[1]);
			if (c != null)
				parent = c.getQuest(parts[0]);
		}
	}
	
	File getFile() {
		return new File(QuestWorld.getPath("data.questing"), id + ".category");
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
		((Quest)quest).getFile().delete();
	}
	
	@Deprecated
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
	
	

	public void setItem(ItemStack item) {
		updateLastModified();
		if(name != null)
			this.item = new ItemBuilder(item).display(name).get();
		else
			this.item = item.clone();
	}



	public void toggleWorld(String world) {
		updateLastModified();
		if (world_blacklist.contains(world)) world_blacklist.remove(world);
		else world_blacklist.add(world);
	}

	@Override
	public ICategory getSource() {
		return this;
	}

	@Override
	public boolean hasChange(Member field) {
		return true;
	}
	
	Category(Map<String, Object> data) {
		loadMap(data);
	}
	
	public Map<String, Object> serialize() {
		return null;
	}
	
	private void loadMap(Map<String, Object> data) {
		
	}
}
