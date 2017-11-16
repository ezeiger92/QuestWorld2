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
import me.mrCookieSlime.QuestWorld.command.EditorCommand;
import me.mrCookieSlime.QuestWorld.command.QuestsCommand;
import me.mrCookieSlime.QuestWorld.extension.builtin.Builtin;
import me.mrCookieSlime.QuestWorld.listener.ExtensionInstaller;
import me.mrCookieSlime.QuestWorld.listener.MenuListener;
import me.mrCookieSlime.QuestWorld.listener.PlayerListener;
import me.mrCookieSlime.QuestWorld.manager.PlayerManager;
import me.mrCookieSlime.QuestWorld.util.Log;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class QuestWorld extends JavaPlugin implements Listener, QuestLoader {
	private static QuestWorld instance = null;
	private long lastSave = 0;

	private QuestingImpl api = new QuestingImpl(this);

	private ExtensionLoader extLoader;
	private ExtensionInstaller hookInstaller = null;
	
	private int questCheckHandle = -1;
	private int autosaveHandle = -1;
	
	public QuestWorld() {
		instance = this;
		Log.setLogger(getLogger());

		saveDefaultConfig();
		getPath("data.extensions");
		getPath("data.presets");
		
		extLoader = new ExtensionLoader(getClassLoader(), getPath("data.extensions"));
		getServer().getServicesManager().register(QuestLoader.class, this, this, ServicePriority.Normal);
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
		hookInstaller = new ExtensionInstaller(this);
		new Builtin();
		hookInstaller.addAll(preEnableHooks);
		preEnableHooks.clear();
		preEnableHooks = null;
		
		api.initEconomy();
		if(api.economy() == null)
			Log.info("No economy (vault) found, money rewards disabled");
			
		getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
			Log.fine("Retrieving Quest Configuration...");
			load();
			int categories = api.facade().getCategories().size(), quests = 0;
			for (ICategory category:api.facade().getCategories())
				quests += category.getQuests().size();

			Log.fine("Successfully loaded " + categories + " Categories");
			Log.fine("Successfully loaded " + quests + " Quests");
		}, 0L);
		
		loadConfigs();
		
		getCommand("quests").setExecutor(new QuestsCommand());
		getCommand("questeditor").setExecutor(new EditorCommand(this));

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(), this);
		pm.registerEvents(api.missionViewer(), this);
		pm.registerEvents(new MenuListener(), this);

		getServer().addRecipe(GuideBook.recipe());
	}
	
	public void loadConfigs() {
		reloadConfig();
		
		api.onConfig();
		
		if(questCheckHandle != -1)
			getServer().getScheduler().cancelTask(questCheckHandle);
		
		questCheckHandle = getServer().getScheduler().scheduleSyncRepeatingTask(this,
				() -> {
					for (Player p: getServer().getOnlinePlayers())
						PlayerManager.of(p).update(true);
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
		api.facade().load();
		lastSave = System.currentTimeMillis();
	}
	
	public void reloadQWConfig() {
		loadConfigs();
		api.reload();
	}
	
	public void reloadQuests() {
		api.facade().unload();
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
		api.facade().save(force);

		for(Player p : getServer().getOnlinePlayers())
			PlayerManager.of(p).getTracker().save();
		
		lastSave = System.currentTimeMillis();
	}

	public void unload() {
		api.facade().save(true);
		api.facade().unload();
		
		for(Player p : getServer().getOnlinePlayers())
			PlayerManager.of(p).getTracker().save();
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
		return instance.getConfig().getString(key,"");
	}
}
