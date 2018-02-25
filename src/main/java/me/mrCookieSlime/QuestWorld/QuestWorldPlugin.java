package me.mrCookieSlime.QuestWorld;

import me.mrCookieSlime.QuestWorld.api.contract.QuestingAPI;
import me.mrCookieSlime.QuestWorld.command.ClickCommand;
import me.mrCookieSlime.QuestWorld.command.EditorCommand;
import me.mrCookieSlime.QuestWorld.command.QuestsCommand;
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

	private QuestingImpl api;
	
	private int questCheckHandle = -1;
	private int autosaveHandle = -1;
	
	@Override
	public void onLoad() {
		saveDefaultConfig();
		
		Log.setLogger(getLogger());
		setInstance(this);
	}
	
	@Override
	public void onEnable() {
		api = new QuestingImpl(this);
		
		onReload();
		
		getCommand("quests").setExecutor(new QuestsCommand());
		getCommand("questeditor").setExecutor(new EditorCommand(this));
		
		getServer().getServicesManager().register(QuestingAPI.class, api, this, ServicePriority.Normal);

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(), this);
		pm.registerEvents(api.getViewer(), this);
		pm.registerEvents(new MenuListener(), this);
		pm.registerEvents(new SpawnerListener(), this);
		pm.registerEvents(new ClickCommand(), this);
		pm.registerEvents(api.getExtensions(), this);

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
		
		int autosave = getConfig().getInt("options.autosave-interval") * 20 * 60; // minutes to ticks
		
		if(autosaveHandle != -1) {
			getServer().getScheduler().cancelTask(autosaveHandle);
			autosaveHandle = -1;
		}
		
		if(autosave > 0)
			autosaveHandle = getServer().getScheduler().scheduleSyncRepeatingTask(this,
					() -> api.onSave(),
					autosave,
					autosave
			);
	}
	
	public void onReload() {
		loadConfigs();
		api.onReload();
		GuideBook.reset();
	}
	
	@Override
	public void onDisable() {
		api.onSave();
		api.onDiscard();
		
		Log.setLogger(null);
		setInstance(null);
		
		getServer().getServicesManager().unregisterAll(this);
		getServer().getScheduler().cancelTasks(this);
	}
	
	public static QuestWorldPlugin instance() {
		return instance;
	}
	
	public String iGetString(String key) {
		String result = getConfig().getString(key,null);
		if(result == null) {
			String fallback = api.getResources().loadJarConfig("config.yml").getString(key, null);
			if(fallback == null) {
				Log.severe("No setting for \""+key+"\" found in config.yml, defaulting to \"\"");
				result = "";
			}
			else {
				Log.warning("Missing config.yml setting \""+key+"\", did you just update? Saving default \""+fallback+"\" from jar");
				result = fallback;
				getConfig().set(key, fallback);
				saveConfig();
			}
		}
		return result;
	}
	
	public static String getString(String key) {
		return instance.iGetString(key);
	}
	
	public QuestingImpl getImpl() {
		return api;
	}
}
