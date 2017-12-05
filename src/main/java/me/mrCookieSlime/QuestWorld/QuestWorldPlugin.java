package me.mrCookieSlime.QuestWorld;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestExtension;
import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.QuestLoader;
import me.mrCookieSlime.QuestWorld.api.contract.QuestingAPI;
import me.mrCookieSlime.QuestWorld.command.EditorCommand;
import me.mrCookieSlime.QuestWorld.command.QuestsCommand;
import me.mrCookieSlime.QuestWorld.extension.builtin.Builtin;
import me.mrCookieSlime.QuestWorld.listener.ExtensionInstaller;
import me.mrCookieSlime.QuestWorld.listener.MenuListener;
import me.mrCookieSlime.QuestWorld.listener.PlayerListener;
import me.mrCookieSlime.QuestWorld.util.Log;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class QuestWorldPlugin extends JavaPlugin implements Listener, QuestLoader {
	private static QuestWorldPlugin instance = null;
	private long lastSave = 0;

	private QuestingImpl api = new QuestingImpl(this);

	private ExtensionLoader extLoader;
	private ExtensionInstaller hookInstaller = null;
	
	private int questCheckHandle = -1;
	private int autosaveHandle = -1;
	
	public QuestWorldPlugin() {
		instance = this;
		Log.setLogger(getLogger());

		saveDefaultConfig();
		getPath("data.extensions");
		getPath("data.presets");
		
		extLoader = new ExtensionLoader(getClassLoader(), getPath("data.extensions"));
		getServer().getServicesManager().register(QuestLoader.class, this, this, ServicePriority.Normal);
		getServer().getServicesManager().register(QuestingAPI.class, api, this, ServicePriority.Normal);
	}
	
	private ArrayList<QuestExtension> preEnableHooks = new ArrayList<>();
	public void attach(QuestExtension hook) {
		if(hookInstaller == null)
			preEnableHooks.add(hook);
		else
			hookInstaller.add(hook);
	}
	
	@Override
	public void onLoad() {
		extLoader.loadLocal();
	}
	
	@Override
	public void onEnable() {
		api.onEnable();
		hookInstaller = new ExtensionInstaller(this);
		new Builtin();
		hookInstaller.addAll(preEnableHooks);
		preEnableHooks.clear();
		preEnableHooks = null;
		
		if(api.getEconomy() == null)
			Log.info("No economy (vault) found, money rewards disabled");
			
		getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
			Log.fine("Retrieving Quest Configuration...");
			load();
			int categories = api.getFacade().getCategories().size(), quests = 0;
			for (ICategory category:api.getFacade().getCategories())
				quests += category.getQuests().size();

			Log.fine("Successfully loaded " + categories + " Categories");
			Log.fine("Successfully loaded " + quests + " Quests");
		}, 0L);
		
		reloadQWConfig();
		
		getCommand("quests").setExecutor(new QuestsCommand());
		getCommand("questeditor").setExecutor(new EditorCommand(this));

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(), this);
		pm.registerEvents(api.getViewer(), this);
		pm.registerEvents(new MenuListener(), this);

		getServer().addRecipe(GuideBook.recipe());
	}
	
	public void loadConfigs() {
		reloadConfig();
		
		if(questCheckHandle != -1)
			getServer().getScheduler().cancelTask(questCheckHandle);
		
		questCheckHandle = getServer().getScheduler().scheduleSyncRepeatingTask(this,
				() -> {
					for (Player p: getServer().getOnlinePlayers())
						api.getPlayerStatus(p).update(true);
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
		api.getFacade().load();
		lastSave = System.currentTimeMillis();
	}
	
	public void reloadQWConfig() {
		loadConfigs();
		api.reload();
		GuideBook.reset();
	}
	
	public void reloadQuests() {
		api.getFacade().unload();
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
		api.getFacade().save(force);

		for(Player p : getServer().getOnlinePlayers())
			api.getPlayerStatus(p).getTracker().save();
		
		lastSave = System.currentTimeMillis();
	}

	public void unload() {
		api.getFacade().save(true);
		api.getFacade().unload();
		
		for(Player p : getServer().getOnlinePlayers())
			api.getPlayerStatus(p).getTracker().save();
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
	
	@Override
	public void enable(QuestExtension hook) {
		for(MissionType type : hook.getMissions()) {
			Log.fine("Registrar - Storing mission: " + type.getName());
			api.registerType(type);
			
			if(type instanceof Listener) {
				Log.fine("Registrar - Registering events: " + type.getName());
				getServer().getPluginManager().registerEvents((Listener)type, this);
			}
		}
	}
	
	public static File getPath(String key) {
		File result = new File(instance.getDataFolder(), resolvePath(key));
		if(!result.exists())
			result.mkdirs();
		
		return result;
	}
	
	public static String resolvePath(String key) {
		String result = instance.getConfig().getString(key,null);
		if(result == null) {
			String fallback = instance.api.getResources().loadJarConfig("config.yml").getString(key, null);
			if(fallback == null) {
				Log.severe("No setting for \""+key+"\" found in config.yml, defaulting to \"\"");
				result = "";
			}
			else {
				Log.warning("Missing config.yml setting \""+key+"\", did you just update? Saving default \""+fallback+"\" from jar");
				result = fallback;
				instance.getConfig().set(key, fallback);
				instance.saveConfig();
			}
		}
		return result;
	}
	
	public static QuestingImpl getImpl() {
		return instance.api;
	}
}
