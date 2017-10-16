package me.mrCookieSlime.QuestWorld;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestExtension;
import me.mrCookieSlime.QuestWorld.api.Translator;
import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.contract.QuestLoader;
import me.mrCookieSlime.QuestWorld.command.EditorCommand;
import me.mrCookieSlime.QuestWorld.command.QuestsCommand;
import me.mrCookieSlime.QuestWorld.extension.builtin.Builtin;
import me.mrCookieSlime.QuestWorld.listener.ExtensionInstaller;
import me.mrCookieSlime.QuestWorld.listener.MenuListener;
import me.mrCookieSlime.QuestWorld.listener.PlayerListener;
import me.mrCookieSlime.QuestWorld.listener.SelfListener;
import me.mrCookieSlime.QuestWorld.manager.MissionViewer;
import me.mrCookieSlime.QuestWorld.manager.PlayerManager;
import me.mrCookieSlime.QuestWorld.quest.RenderableFacade;
import me.mrCookieSlime.QuestWorld.util.EconWrapper;
import me.mrCookieSlime.QuestWorld.util.Lang;
import me.mrCookieSlime.QuestWorld.util.Log;
import me.mrCookieSlime.QuestWorld.util.ResourceLoader;
import me.mrCookieSlime.QuestWorld.util.Sounds;

import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class QuestWorld extends JavaPlugin implements Listener, QuestLoader {
	private static QuestWorld instance = null;
	private long lastSave = 0;

	private MissionViewer missionViewer = new MissionViewer();
	private Map<String, MissionType> types      = new HashMap<>();
	private Map<Integer, ICategory>   categories = new HashMap<>();
	private Map<UUID, PlayerManager>  profiles   = new HashMap<>();
	
	private EconWrapper economy = null;
	private Sounds eventSounds;
	
	// TODO Make an interface
	private RenderableFacade facade = new RenderableFacade();

	
	private ResourceLoader resources = new ResourceLoader(this);
	private Lang language;
	private ExtensionLoader extLoader = null;
	private ExtensionInstaller hookInstaller = null;
	
	private int questCheckHandle = -1;
	private int autosaveHandle = -1;
	
	public static String translate(Translator key, String... replacements) {
		return instance.language.translate(key, replacements);
	}
	
	public QuestWorld() {
		instance = this;
		Log.setLogger(getLogger());

		saveDefaultConfig();
		getPath("data.extensions");
		getPath("data.presets");
		
		extLoader = new ExtensionLoader(getClassLoader(), getPath("data.extensions"));
		language = new Lang(resources);
		getServer().getServicesManager().register(QuestLoader.class, this, this, ServicePriority.Normal);
	}
	
	private ArrayList<QuestExtension> preEnableHooks = new ArrayList<>();
	public void attach(QuestExtension hook) {
		if(hookInstaller == null)
			preEnableHooks.add(hook);
		else
			hookInstaller.add(hook);
	}
	
	public Set<IMission> getMissionsOf(MissionType type) {
		return missionViewer.getMissionsOf(type);
	}
	
	public Set<IMission> getTickingMissions() {
		return missionViewer.getTickingMissions();
	}
	
	@Override
	public void onLoad() {
		extLoader.loadLocal();
	}
	
	@Override
	public void onEnable() {
		hookInstaller = new ExtensionInstaller(this);
		new Builtin();
		hookInstaller.addAll(preEnableHooks);
		
		economy = EconWrapper.wrap();
		if(economy == null)
			Log.info("No economy (vault) found, money rewards disabled");
			
		getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
			Log.fine("Retrieving Quest Configuration...");
			load();
			int categories = getCategories().size(), quests = 0;
			for (ICategory category: getCategories())
				quests += category.getQuests().size();

			Log.fine("Successfully loaded " + categories + " Categories");
			Log.fine("Successfully loaded " + quests + " Quests");
		}, 0L);
		
		loadConfigs();
		
		getCommand("quests").setExecutor(new QuestsCommand());
		getCommand("questeditor").setExecutor(new EditorCommand());

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(), this);
		pm.registerEvents(missionViewer, this);
		pm.registerEvents(new SelfListener(), this);
		pm.registerEvents(new MenuListener(), this);

		getServer().addRecipe(GuideBook.recipe());
		
		
	}
	
	public void loadConfigs() {
		reloadConfig();
		
		//TODO make logger info (and other) levels actually work
		//Log.setLevel(cfg.getString("options.log-level"));

		eventSounds = new Sounds(resources.loadConfigNoexpect("sounds.yml", true));
		
		if(questCheckHandle != -1)
			getServer().getScheduler().cancelTask(questCheckHandle);
		
		questCheckHandle = getServer().getScheduler().scheduleSyncRepeatingTask(this,
				() -> {
					for (Player p: getServer().getOnlinePlayers())
						getManager(p).update(true);
				},
				0L,
				getConfig().getInt("options.quest-check-delay")
		);
		
		int autosave = getConfig().getInt("options.autosave-interval");
		if(autosave > 0) {
			autosave = autosave * 20 * 60; // minutes to ticks
			if(autosaveHandle != -1)
				getServer().getScheduler().cancelTask(autosaveHandle);
			
			autosaveHandle = getServer().getScheduler().scheduleSyncRepeatingTask(this,
					() -> save(true),
					autosave,
					autosave
			);
		}
	}
	
	public void load() {
		facade.load();
		lastSave = System.currentTimeMillis();
	}
	
	public void reloadQWConfig() {
		loadConfigs();
		language.reload();
	}
	
	public void reloadQuests() {
		categories.clear();
		profiles.clear();
		
		load();
	}
	
	@Override
	public void onDisable() {
		unload();
		
		Log.setLogger(null);
		instance = null;
		
		getServer().getServicesManager().unregisterAll(this);
		getServer().getScheduler().cancelTasks(this);
	}
	
	public long getLastSaved() {
		return lastSave;
	}

	public void save(boolean force) {
		for(ICategory c : categories.values())
			c.save(force);
		
		for(PlayerManager p : profiles.values())
			p.save();
		
		lastSave = System.currentTimeMillis();
	}

	public void unload() {
		for(ICategory c : categories.values())
			c.save(true);
		categories.clear();
		
		for(PlayerManager p : profiles.values())
			p.save();
		profiles.clear();
	}
	
	public boolean importPreset(String fileName) {
		File file = new File(getPath("data.presets"), fileName);
		byte[] buffer = new byte[1024];
		if (!file.exists())
			return false;
		
		unload();
		try {
			ZipInputStream input = new ZipInputStream(new FileInputStream(file));
			ZipEntry entry = input.getNextEntry();
			
			for (File f: getPath("data.questing").listFiles()) {
				f.delete();
			}
			
			while (entry != null) {
				FileOutputStream output = new FileOutputStream(new File(getPath("data.questing"), entry.getName()));
				
				int length;
				while ((length = input.read(buffer)) > 0) {
					output.write(buffer, 0, length);
				}
				
				output.close();
				entry = input.getNextEntry();
			}
			
			input.closeEntry();
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		load();
		return true;
	}
	
	public boolean exportPreset(String fileName) {
		File file = new File(getPath("data.presets"), fileName);
		byte[] buffer = new byte[1024];
		
		if (file.exists()) file.delete();
		
		save(true); // Why unload and load in a try/catch block when you can just use a save function?
		
		try {
			file.createNewFile();
			
			ZipOutputStream output = new ZipOutputStream(new FileOutputStream(file));
			for (File f: getPath("data.questing").listFiles()) {
				ZipEntry entry = new ZipEntry(f.getName());
				output.putNextEntry(entry);
				FileInputStream input = new FileInputStream(f);
				
				int length;
				while ((length = input.read(buffer)) > 0) {
					output.write(buffer, 0, length);
				}
				
				input.close();
				output.closeEntry();
			}
			output.close();
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static QuestWorld getInstance() {
		return instance;
	}
	
	public Collection<ICategory> getCategories() {
		return categories.values();
	}
	
	public ICategory getCategory(int id) {
		return categories.get(id);
	}
	
	public void registerCategory(ICategory category) {
		categories.put(category.getID(), category);
	}
	
	public void unregisterCategory(ICategory category) {
		for (IQuest quest: category.getQuests()) {
			PlayerManager.clearAllQuestData(quest);
			facade.deleteQuestFile(quest);
		}
		categories.remove(category.getID());
		facade.deleteCategoryFile(category);
	}
	
	public void registerManager(PlayerManager manager) {
		this.profiles.put(manager.getUUID(), manager);
	}
	
	public void unregisterManager(PlayerManager manager) {
		this.profiles.remove(manager.getUUID());
	}
	
	public PlayerManager getManager(UUID uuid) {
		return profiles.containsKey(uuid) ? profiles.get(uuid): new PlayerManager(uuid);
	}
	
	// This was changed to AnimalTamer because it's the most basic interface with getUniqueId
	public PlayerManager getManager(AnimalTamer player) {
		return getManager(player.getUniqueId());
	}
	
	@Override
	public void enable(QuestExtension hook) {
		for(MissionType type : hook.getMissions())
			registerMissionType(type);
	}
	
	private void registerMissionType(MissionType type) {
		Log.fine("Registrar - Storing mission: " + type.getName());
		types.put(type.getName(), type);
		
		if(type instanceof Listener) {
			Log.fine("Registrar - Registering events: " + type.getName());
			getServer().getPluginManager().registerEvents((Listener)type, this);
		}
	}
	
	public boolean isItemSimiliar(ItemStack item, ItemStack SFitem) {
		if(item == null || SFitem == null)
			return item == SFitem;
		
		boolean wildcardLeft = false;
		boolean wildcardRight = false;
		
		if(item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			wildcardLeft = meta.hasLore() && meta.getLore().get(0).equals("*");
		}
		
		if(SFitem.hasItemMeta()) {
			ItemMeta meta = SFitem.getItemMeta();
			wildcardRight = meta.hasLore() && meta.getLore().get(0).equals("*");
		}
		
		if(wildcardLeft || wildcardRight) {
			item = item.clone();
			ItemMeta meta = item.getItemMeta();
			meta.setLore(null);
			item.setItemMeta(meta);
			
			SFitem = SFitem.clone();
			meta = SFitem.getItemMeta();
			meta.setLore(null);
			SFitem.setItemMeta(meta);
		}
		
		return item.isSimilar(SFitem);
	}
	
	public EconWrapper getEconomy() {
		return economy;
	}

	public Map<String, MissionType> getMissionTypes() {
		return types;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends MissionType> T getMissionType(String typeName) {
		return (T)instance.types.get(typeName);
	}
	
	public static Sounds getSounds() {
		return instance.eventSounds;
	}
	
	public static RenderableFacade renderFactory() {
		return instance.facade;
	}
	
	public static File getPath(String key) {
		File result = new File(instance.getDataFolder(), resolvePath(key));
		if(!result.exists())
			result.mkdirs();
		
		return result;
	}
	
	public static String resolvePath(String key) {
		return instance.getConfig().getString(key,"");
	}
}
