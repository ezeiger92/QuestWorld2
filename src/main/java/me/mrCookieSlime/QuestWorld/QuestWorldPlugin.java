package me.mrCookieSlime.QuestWorld;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestExtension;
import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.QuestingAPI;
import me.mrCookieSlime.QuestWorld.command.ClickCommand;
import me.mrCookieSlime.QuestWorld.command.EditorCommand;
import me.mrCookieSlime.QuestWorld.command.QuestsCommand;
import me.mrCookieSlime.QuestWorld.extension.builtin.Builtin;
import me.mrCookieSlime.QuestWorld.listener.ExtensionInstaller;
import me.mrCookieSlime.QuestWorld.listener.MenuListener;
import me.mrCookieSlime.QuestWorld.listener.PlayerListener;
import me.mrCookieSlime.QuestWorld.listener.SpawnerListener;
import me.mrCookieSlime.QuestWorld.util.Log;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class QuestWorldPlugin extends JavaPlugin implements Listener {
	private static volatile QuestWorldPlugin instance = null;
	
	private static void setInstance(QuestWorldPlugin plugin) {
		if(instance != null && plugin != null)
			throw new IllegalStateException("Cannot redefine singleton");
		
		instance = plugin;
	}

	private QuestingImpl api = new QuestingImpl(this);

	private ExtensionLoader extLoader;
	private ExtensionInstaller hookInstaller = null;
	
	private int questCheckHandle = -1;
	private int autosaveHandle = -1;
	
	public QuestWorldPlugin() {
		setInstance(this);

		saveDefaultConfig();
		getPath("data.extensions");
		getPath("data.presets");
		
		extLoader = new ExtensionLoader(getClassLoader(), getPath("data.extensions"));
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
		hookInstaller.add(new Builtin());
		hookInstaller.addAll(preEnableHooks);
		preEnableHooks.clear();
		preEnableHooks = null;
		
		if(!api.getEconomy().isPresent())
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
		
		onReload();
		
		getCommand("quests").setExecutor(new QuestsCommand());
		getCommand("questeditor").setExecutor(new EditorCommand(this));

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(), this);
		pm.registerEvents(api.getViewer(), this);
		pm.registerEvents(new MenuListener(), this);
		pm.registerEvents(new SpawnerListener(), this);
		pm.registerEvents(new ClickCommand(), this);

		getServer().addRecipe(GuideBook.instance().recipe());
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
					() -> onSave(false),
					autosave,
					autosave
			);
		}
	}
	
	public ExtensionInstaller getInstaller() {
		return hookInstaller;
	}
	
	public ExtensionLoader getLoader() {
		return extLoader;
	}
	
	public void load() {
		api.getFacade().load();
	}
	
	public void onReload() {
		loadConfigs();
		api.onReload();
		hookInstaller.onReload();
		GuideBook.reset();
	}
	
	public void onDiscard() {
		api.onDiscard();
		load();
	}
	
	@Override
	public void onDisable() {
		unload();
		
		Log.setLogger(null);
		setInstance(null);
		
		getServer().getServicesManager().unregisterAll(this);
		getServer().getScheduler().cancelTasks(this);
	}

	public void onSave(boolean force) {
		api.getFacade().save(force);

		for(Player p : getServer().getOnlinePlayers())
			api.getPlayerStatus(p).getTracker().onSave();
		
		hookInstaller.save();
	}

	public void unload() {
		api.getFacade().save(true);
		
		for(Player p : getServer().getOnlinePlayers())
			api.getPlayerStatus(p).getTracker().onSave();
		
		api.onDiscard();
	}
	
	public boolean importPreset(String fileName) {
		File file = new File(getPath("data.presets"), fileName);
		byte[] buffer = new byte[1024];
		if (!file.exists())
			return false;
		
		unload();
		try(ZipInputStream input = new ZipInputStream(new FileInputStream(file))) {
			ZipEntry entry = input.getNextEntry();
			
			for (File f: getFiles("data.questing"))
				Files.delete(f.toPath());
			
			for (File f: getFiles("data.dialogue"))
				Files.delete(f.toPath());
			
			while (entry != null) {
				File target;
				if(entry.getName().startsWith("dialogue/"))
					target = new File(getPath("data.dialogue"), entry.getName().substring(9));
				else
					target = new File(getPath("data.questing"), entry.getName());
				
				try (FileOutputStream output = new FileOutputStream(target)) {
					int length;
					while ((length = input.read(buffer)) > 0) {
						output.write(buffer, 0, length);
					}
				}
				entry = input.getNextEntry();
			}
			
			input.closeEntry();
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		load();
		return true;
	}
	
	public boolean exportPreset(String fileName) {
		File file = new File(getPath("data.presets"), fileName);
		
		onSave(true); // Why unload and load in a try/catch block when you can just use a save function?
		
		try {
			Files.deleteIfExists(file.toPath());
			
			Files.createFile(file.toPath());
			//if(!file.createNewFile())
			//	throw new IOException("Failed to create file: "+file.getName());
			
			ArrayList<File> files = new ArrayList<>();
			File dialogueDir = getPath("data.dialogue");
			
			files.addAll(Arrays.asList(getFiles("data.questing")));
			files.addAll(Arrays.asList(getFiles("data.dialogue")));
			
			try(ZipOutputStream output = new ZipOutputStream(new FileOutputStream(file))) {
				byte[] buffer = new byte[1024];
				
				for (File f: files) {
					String entryName = f.getName();
					
					if(f.getParentFile().equals(dialogueDir))
						entryName = "dialogue/"+entryName;
					
					ZipEntry entry = new ZipEntry(entryName);
					output.putNextEntry(entry);
					try(FileInputStream input = new FileInputStream(f)) {
						int length;
						while ((length = input.read(buffer)) > 0) {
							output.write(buffer, 0, length);
						}
					}
					output.closeEntry();
				}
				
			}
		}
		catch(IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	public void enable(QuestExtension hook) {
		for(MissionType type : hook.getMissionTypes()) {
			Log.fine("Registrar - Storing mission: " + type.getName());
			api.registerType(type);
			
			if(type instanceof Listener) {
				Log.fine("Registrar - Registering events: " + type.getName());
				getServer().getPluginManager().registerEvents((Listener)type, this);
			}
		}
	}
	
	public static File getPath(String key) {
		File result = new File(instance.getDataFolder(), getString(key));
		if(!result.exists() && !result.mkdirs())
			throw new IllegalArgumentException("Failed to create path for: "+key+" (file:"+result.getName()+")");
		
		return result;
	}
	
	public static File[] getFiles(String key) {
		File[] result = getPath(key).listFiles();
		
		if(result != null)
			return result;
		
		return new File[0];
	}
	
	public static File[] getFiles(String key, FilenameFilter filter) {
		File[] result = getPath(key).listFiles(filter);
		
		if(result != null)
			return result;
		
		return new File[0];
	}
	
	public static String getString(String key) {
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
