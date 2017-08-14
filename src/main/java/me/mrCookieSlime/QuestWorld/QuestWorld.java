package me.mrCookieSlime.QuestWorld;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import me.mrCookieSlime.CSCoreLibPlugin.PluginUtils;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.CSCoreLibSetup.CSCoreLibLoader;
import me.mrCookieSlime.QuestWorld.api.QuestLoader;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestExtension;
import me.mrCookieSlime.QuestWorld.api.Translator;
import me.mrCookieSlime.QuestWorld.commands.EditorCommand;
import me.mrCookieSlime.QuestWorld.commands.QuestsCommand;
import me.mrCookieSlime.QuestWorld.extensions.builtin.Builtin;
import me.mrCookieSlime.QuestWorld.listeners.HookInstaller;
import me.mrCookieSlime.QuestWorld.listeners.PlayerListener;
import me.mrCookieSlime.QuestWorld.listeners.SelfListener;
import me.mrCookieSlime.QuestWorld.managers.MissionViewer;
import me.mrCookieSlime.QuestWorld.managers.PlayerManager;
import me.mrCookieSlime.QuestWorld.quests.Category;
import me.mrCookieSlime.QuestWorld.quests.Mission;
import me.mrCookieSlime.QuestWorld.quests.Quest;
import me.mrCookieSlime.QuestWorld.utils.EconWrapper;
import me.mrCookieSlime.QuestWorld.utils.Lang;
import me.mrCookieSlime.QuestWorld.utils.Log;
import me.mrCookieSlime.QuestWorld.utils.Sounds;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class QuestWorld extends JavaPlugin implements Listener, QuestLoader {
	private static QuestWorld instance = null;
	private long lastSave;

	private Config cfg, sounds;
	private MissionViewer missionViewer = new MissionViewer();
	private Map<String, MissionType> types      = new HashMap<>();
	private Map<Integer, Category>   categories = new HashMap<>();
	private Map<UUID, PlayerManager>  profiles   = new HashMap<>();
	
	private EconWrapper economy = null;
	private Sounds eventSounds;
	
	private Lang language;
	private ExtensionLoader extLoader = null;
	private HookInstaller hookInstaller = null;
	
	public static String translate(Translator key, String... replacements) {
		return getInstance().language.translate(key, replacements);
	}
	
	public QuestWorld() {
		instance = this;
		Log.setLogger(getLogger());
		
		extLoader = new ExtensionLoader(this.getClassLoader(), new File(this.getDataFolder(), "extensions"));
		language = new Lang("en_us", getDataFolder(), getClassLoader());
		language.save();
		getServer().getServicesManager().register(QuestLoader.class, this, this, ServicePriority.Normal);
	}
	
	private List<QuestExtension> preEnableHooks = new ArrayList<>();
	public void attach(QuestExtension hook) {
		if(hookInstaller == null)
			preEnableHooks.add(hook);
		else
			hookInstaller.add(hook);
	}
	
	public Set<Mission> getMissionsOf(MissionType type) {
		return missionViewer.getMissionsOf(type);
	}
	
	public Set<Mission> getTickingMissions() {
		return missionViewer.getTickingMissions();
	}
	
	@Override
	public void onLoad() {
		extLoader.loadLocal();
	}
	
	@Override
	public void onEnable() {
		// Initialize all we can before we need CSCoreLib
		hookInstaller = new HookInstaller(this);
		new Builtin();
		hookInstaller.addAll(preEnableHooks);
		
		setupEconomy();
		
		// Attempt to load Core to continue
		CSCoreLibLoader loader = new CSCoreLibLoader(this);
		if(!loader.load())
			return;
		
		if (!new File("data-storage/Quest World").exists()) new File("data-storage/Quest World").mkdirs();
		if (!new File("plugins/QuestWorld/quests").exists()) new File("plugins/QuestWorld/quests").mkdirs();
		if (!new File("plugins/QuestWorld/dialogues").exists()) new File("plugins/QuestWorld/dialogues").mkdirs();
		if (!new File("plugins/QuestWorld/presets").exists()) new File("plugins/QuestWorld/presets").mkdirs();
		
		if(!getDataFolder().exists())
			getDataFolder().mkdir();
		
		File extensionDir = new File(getDataFolder(), "extensions");
		if(!extensionDir.exists())
			extensionDir.mkdir();
			
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			
			@Override
			public void run() {
				Log.fine("Retrieving Quest Configuration...");
				load();
				int categories = getCategories().size(), quests = 0;
				for (Category category: getCategories())
					quests += category.getQuests().size();

				Log.fine("Successfully loaded " + categories + " Categories");
				Log.fine("Successfully loaded " + quests + " Quests");
			}
		}, 0L);
		
		loadConfigs();
		
		getCommand("quests").setExecutor(new QuestsCommand());
		getCommand("questeditor").setExecutor(new EditorCommand());
		//editorCommand.setAliases(Arrays.asList("qe"));

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(), this);
		pm.registerEvents(missionViewer, this);
		pm.registerEvents(new SelfListener(), this);
		
		ShapelessRecipe recipe = new ShapelessRecipe(GuideBook.get());
		recipe.addIngredient(Material.WORKBENCH);
		getServer().addRecipe(recipe);
		
		getServer().getScheduler().runTaskTimer(this, new Runnable() {
			
			@Override
			public void run() {
				for (Player p: getServer().getOnlinePlayers()) {
					getManager(p).update(true);
				}
			}
		}, 0L, cfg.getInt("options.quest-check-delay"));
		
		getServer().getScheduler().runTaskTimer(this, new Runnable() {
			
			@Override
			public void run() {
				for (Player p: getServer().getOnlinePlayers()) {
					getManager(p).save();
				}
			}
		}, 0L, 5 * 60L * 20L);
		
		int autosave = cfg.getInt("options.autosave-interval");
		if(autosave > 0) {
			autosave = autosave * 20 * 60; // minutes to ticks
			this.getServer().getScheduler().runTaskTimer(this, new Runnable() {
	
				@Override
				public void run() {
					save();
				}
				
			}, autosave, autosave);
		}
	}
	
	private boolean setupEconomy() {
		if(getServer().getPluginManager().getPlugin("Vault") != null) {
			economy = new EconWrapper();
		}
		
		if(economy == null)
			Log.info("No economy (vault) found, money rewards disabled");
		
	    return true;
	}
	
	public void loadConfigs() {
		PluginUtils utils = new PluginUtils(this);
		utils.setupConfig();
		cfg = utils.getConfig();
		utils.setupMetrics();
		utils.setupUpdater(77071, getFile());
		
		//TODO make logger info (and other) levels actually work
		//Log.setLevel(cfg.getString("options.log-level"));
		
		File soundFile = new File(getDataFolder(), "sounds.yml");
		if(!soundFile.exists())
			try {
				YamlConfiguration.loadConfiguration(new InputStreamReader(getResource("sounds.yml"))).save(soundFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		sounds = new Config("plugins/QuestWorld/sounds.yml");

		// Needs sound config loaded
		eventSounds = new Sounds();
	}
	
	public void load() {
		Set<File> categories = new HashSet<File>();
		Map<Integer, List<File>> quests = new HashMap<Integer, List<File>>();
		for (File file: new File("plugins/QuestWorld/quests").listFiles()) {
			if (file.getName().endsWith(".quest")) {
				Config cfg = new Config(file);
				int category = cfg.getInt("category");
				List<File> files = new ArrayList<File>();
				if (quests.containsKey(category)) files = quests.get(category);
				files.add(file);
				quests.put(category, files);
			}
			else if (file.getName().endsWith(".category")) categories.add(file);
		}
		
		for (File file: categories) {
			int id = Integer.parseInt(file.getName().replace(".category", ""));
			List<File> files = new ArrayList<File>();
			if (quests.containsKey(id)) files = quests.get(id);
			new Category(file, files);
		}
		
		for (Category category: this.categories.values()) {
			category.updateParent(new Config("plugins/QuestWorld/quests/" + category.getID() + ".category"));
			for (Quest quest: category.getQuests()) {
				quest.updateParent(new Config("plugins/QuestWorld/quests/" + quest.getID() + "-C" + category.getID() + ".quest"));
			}
		}
		
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
		for(Category c : categories.values())
			c.save(force);
		
		for(PlayerManager p : profiles.values())
			p.save();
		
		lastSave = System.currentTimeMillis();
	}
	
	public void save() {
		save(false);
	}
	
	public void unload() {
		for(Category c : categories.values())
			c.save(true);
		categories.clear();
		
		for(PlayerManager p : profiles.values())
			p.save();
		profiles.clear();
	}
	
	public boolean importPreset(String fileName) {
		File file = new File("plugins/QuestWorld/presets/" + fileName);
		byte[] buffer = new byte[1024];
		if (!file.exists())
			return false;
		
		QuestWorld.getInstance().unload();
		try {
			ZipInputStream input = new ZipInputStream(new FileInputStream(file));
			ZipEntry entry = input.getNextEntry();
			
			for (File f: new File("plugins/QuestWorld/quests").listFiles()) {
				f.delete();
			}
			
			while (entry != null) {
				FileOutputStream output = new FileOutputStream(new File("plugins/QuestWorld/quests/" + entry.getName()));
				
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
		
		QuestWorld.getInstance().load();
		return true;
	}
	
	public boolean exportPreset(String fileName) {
		File file = new File("plugins/QuestWorld/presets/" + fileName);
		byte[] buffer = new byte[1024];
		
		if (file.exists()) file.delete();
		
		save(); // Why unload and load in a try/catch block when you can just use a save function?
		
		try {
			file.createNewFile();
			
			ZipOutputStream output = new ZipOutputStream(new FileOutputStream(file));
			for (File f: new File("plugins/QuestWorld/quests").listFiles()) {
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
	
	public Collection<Category> getCategories() {
		return categories.values();
	}
	
	public Category getCategory(int id) {
		return categories.get(id);
	}
	
	public void registerCategory(Category category) {
		categories.put(category.getID(), category);
	}
	
	public void unregisterCategory(Category category) {
		for (Quest quest: category.getQuests()) {
			PlayerManager.clearAllQuestData(quest);
			new File("plugins/QuestWorld/quests/" + quest.getID() + "-C" + category.getID() + ".quest").delete();
		}
		categories.remove(category.getID());
		new File("plugins/QuestWorld/quests/" + category.getID() + ".category").delete();
	}
	
	public void registerManager(PlayerManager manager) {
		this.profiles.put(manager.getUUID(), manager);
	}
	
	public void unregisterManager(PlayerManager manager) {
		this.profiles.remove(manager.getUUID());
	}
	
	public PlayerManager getManager(OfflinePlayer p) {
		return profiles.containsKey(p.getUniqueId()) ? profiles.get(p.getUniqueId()): new PlayerManager(p);
	}
	
	public PlayerManager getManager(String uuid) {
		return profiles.containsKey(UUID.fromString(uuid)) ? profiles.get(UUID.fromString(uuid)): new PlayerManager(UUID.fromString(uuid));
	}
	
	public boolean isManagerLoaded(String uuid) {
		return profiles.containsKey(UUID.fromString(uuid));
	}
	
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
		
		return item.isSimilar(SFitem);
	}

	public Config getCfg() {
		return cfg;
	}
	
	public Config getSoundCfg() {
		return sounds;
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
}
