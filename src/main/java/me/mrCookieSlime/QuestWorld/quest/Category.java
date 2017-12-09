package me.mrCookieSlime.QuestWorld.quest;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.mrCookieSlime.QuestWorld.api.contract.ICategoryState;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

class Category extends UniqueObject implements ICategoryState {
	private int id;
	private Facade facade;
	private boolean hidden;
	private String name;
	private String permission;
	private ItemStack item;
	private WeakReference<Quest> parent = new WeakReference<>(null);
	private Map<Integer, Quest> quests = new HashMap<>();
	private List<String> world_blacklist = new ArrayList<String>();
	
	private YamlConfiguration config;

	// External
	public Category(String name, int id, Facade facade) {
		this.id = id;
		this.name = name;
		this.facade = facade;
		config = YamlConfiguration.loadConfiguration(Facade.fileFor(this));
		item = new ItemStack(Material.BOOK_AND_QUILL);
		world_blacklist = new ArrayList<String>();
		permission = "";
		hidden = false;
	}
	
	protected Category(Category copy) {
		copy(copy);
	}
	
	@Deprecated
	public Category(Map<String, Object> data) {
		loadMap(data);
	}
	
	// Package
	Category(int id, YamlConfiguration config, Facade facade) {
		this.id = id;
		this.config = config;
		this.facade = facade;
		name = config.getString("name");
		item = config.getItemStack("item", item);
		hidden = config.getBoolean("hidden");
		permission = config.getString("permission", "");
		world_blacklist = config.getStringList("world-blacklist");
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
	public Quest getParent() {
		return parent.get();
	}
	
	@Override
	public Collection<Quest> getQuests() {
		return Collections.unmodifiableCollection(quests.values());
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
	public void clearAllUserData() {
		facade.clearAllUserData(getSource());
	}
	
	@Override
	public CategoryState getState() {
		return new CategoryState(this);
	}

	//// ICategoryState Impl
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setParent(IQuest quest) {
		parent = new WeakReference<>(quest != null ? ((Quest)quest).getSource() : null);
	}

	@Override
	public void setPermission(String permission) {
		this.permission = permission;
	}
	
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	//// DONE
	
	public void refreshParent() {
		parent = new WeakReference<>(
				Facade.questOfString(config.getString("parent", null)));
	}
	
	@Override
	public void addQuest(String name, int id) {
		// Quests should never reference a CategoryState, getSource always returns the actual Category
		quests.put(id, facade.createQuest(name, id, getSource()));
	}
	
	public Facade getFacade() {
		return facade;
	}
	
	public void directAddQuest(Quest quest) {
		quests.put(quest.getID(), quest);
	}
	
	public void removeQuest(IQuest quest) {
		quests.remove(quest.getID());
	}
	
	public void save(boolean force) {
		long lastSave = facade.getLastSave();
		for (Quest quest: quests.values()) {
			// Forcing save or quest appears changed
			if(force || lastSave < quest.getLastModified())
				quest.save();
		}
		
		// Not forcing a save and category appears unchanged
		if(!force && lastSave >= getLastModified())
			return;
		
		config.set("id", id);
		config.set("name", name);
		config.set("item", item);
		config.set("permission", permission);
		config.set("hidden", this.hidden);
		config.set("world-blacklist", world_blacklist);
		
		config.set("parent", Facade.stringOfQuest(getParent()));
		
		try {
			config.save(Facade.fileFor(this));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setItem(ItemStack item) {
		this.item = item.clone();
	}

	public void toggleWorld(String world) {
		if (world_blacklist.contains(world)) world_blacklist.remove(world);
		else world_blacklist.add(world);
	}

	@Override
	public Category getSource() {
		return this;
	}

	@Override
	public boolean hasChange(Member field) {
		return true;
	}
	
	@Deprecated
	public Map<String, Object> serialize() {
		HashMap<String, Object> result = new HashMap<>();
		
		result.put("unique", (int)getUnique());
		result.put("index", id);
		result.put("hidden", hidden);
		result.put("name", name);
		result.put("permission", permission);
		result.put("item", item);
		result.put("parent", getParent() == null ? null : (int)getParent().getUnique());
		result.put("world-blacklist", world_blacklist);
		
		return result;
	}
	
	protected void copy(Category source) {
		id         = source.id;
		facade     = source.facade;
		hidden     = source.hidden;
		name       = source.name;
		permission = source.permission;
		item       = source.item.clone();
		parent     = new WeakReference<>(source.getParent());
		parent     = new WeakReference<>(source.getParent());
		
		quests.clear();
		quests.putAll(source.quests);
		
		world_blacklist.clear();
		world_blacklist.addAll(source.world_blacklist);
		
		config     = YamlConfiguration.loadConfiguration(Facade.fileFor(this));
	}
	
	protected void copyTo(Category dest) {
		dest.copy(this);
	}
	
	@Deprecated
	WeakReference<Quest> fancyParentResolveFunction(Integer id) {
		if(id == null)
			return null;
		
		Quest q = facade.getQuest(id.longValue());
		if(q == null)
			return null;
		
		return new WeakReference<>(q);
	}
	
	@Deprecated
	@SuppressWarnings("unchecked")
	private void loadMap(Map<String, Object> data) {
		setUnique((Integer)data.getOrDefault("unique", (int)getUnique()));
		
		id = (Integer)data.getOrDefault("index", -1);
		hidden = (Boolean)data.getOrDefault("hidden", false);
		name = (String)data.getOrDefault("name", "");
		permission = (String)data.getOrDefault("permission", "");
		item = (ItemStack)data.getOrDefault("item", new ItemStack(Material.STONE));
		parent = fancyParentResolveFunction((Integer)data.getOrDefault("parent", null));
		world_blacklist = (List<String>)data.getOrDefault("world-blacklist", new ArrayList<>());
	}
}
