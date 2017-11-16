package me.mrCookieSlime.QuestWorld.quest;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.mrCookieSlime.QuestWorld.QuestWorldPlugin;
import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.ICategoryState;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;
import me.mrCookieSlime.QuestWorld.util.Text;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

class Category extends Renderable implements ICategoryState {
	private int id;
	private boolean hidden;
	private String name;
	private String permission;
	private ItemStack item;
	private WeakReference<Quest> parent;
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
		
		QuestWorld.getFacade().registerCategory(this);
	}
	
	public Category(Map<String, Object> data) {
		loadMap(data);
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
		
		QuestWorld.getFacade().registerCategory(this);
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
		if(parent == null)
			return null;
		return parent.get();
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
	public CategoryState getState() {
		return new CategoryState(this);
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
		this.parent = new WeakReference<>((Quest)quest);
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
			
			Category c = (Category)QuestWorld.getFacade().getCategory(parts[1]);
			if (c != null)
				parent = new WeakReference<>(c.getQuest(parts[0]));
		}
	}
	
	File getFile() {
		return new File(QuestWorldPlugin.getPath("data.questing"), id + ".category");
	}
	
	public void addQuest(IQuest quest) {
		if(quest instanceof QuestState)
			quests.put(quest.getID(), ((QuestState)quest).getSource());
		else
			quests.put(quest.getID(), (Quest)quest);
	}
	
	public void removeQuest(IQuest quest) {
		quests.remove(quest.getID());
		((Quest)quest).getFile().delete();
	}
	
	public void save(boolean force) {
		long lastSave = QuestWorld.getFacade().getLastSave();
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
		
		Quest parent = getParent();
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
	
	public Map<String, Object> serialize() {
		HashMap<String, Object> result = new HashMap<>();
		
		result.put("unique", (int)getUnique());
		result.put("index", id);
		result.put("hidden", hidden);
		result.put("name", Text.escape(name));
		result.put("permission", permission);
		result.put("item", item);
		result.put("parent", getParent() == null ? null : (int)getParent().getUnique());
		result.put("world-blacklist", world_blacklist);
		
		return result;
	}
	
	protected void copy(Category source) {
		id         = source.id;
		config     = YamlConfiguration.loadConfiguration(getFile());
		name       = source.name;
		item       = source.item.clone();
		parent     = new WeakReference<>(source.getParent());
		permission = source.permission;
		hidden     = source.hidden;
		
		world_blacklist = new ArrayList<>();
		world_blacklist.addAll(source.world_blacklist);
	}
	
	protected void copyTo(Category dest) {
		dest.copy(this);
	}
	
	WeakReference<Quest> fancyParentResolveFunction(Integer id) {
		if(id == null)
			return null;
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private void loadMap(Map<String, Object> data) {
		setUnique((Integer)data.getOrDefault("unique", (int)getUnique()));
		
		id = (Integer)data.getOrDefault("index", -1);
		hidden = (Boolean)data.getOrDefault("hidden", false);
		name = Text.colorize((String)data.getOrDefault("name", ""));
		permission = (String)data.getOrDefault("permission", "");
		item = (ItemStack)data.getOrDefault("item", new ItemStack(Material.STONE));
		parent = fancyParentResolveFunction((Integer)data.getOrDefault("parent", null));
		world_blacklist = (List<String>)data.getOrDefault("world-blacklist", new ArrayList<>());
	}
}
