package com.questworld;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import com.questworld.api.QuestWorld;
import com.questworld.api.contract.IMission;
import com.questworld.api.contract.QuestingAPI;
import com.questworld.command.ClickCommand;
import com.questworld.command.EditorCommand;
import com.questworld.command.QuestsCommand;
import com.questworld.listener.MenuListener;
import com.questworld.listener.PlayerListener;
import com.questworld.listener.SpawnerListener;
import com.questworld.util.Log;
import com.questworld.util.Reflect;

public class QuestWorldPlugin extends JavaPlugin implements Listener {
	private QuestingImpl api;
	private int autosaveHandle = -1;
	private int questCheckHandle = -1;
	
	private SpawnerListener spawnListener;

	public static QuestWorldPlugin get() {
		return (QuestWorldPlugin) QuestWorld.getPlugin();
	}
	
	public static QuestingImpl getAPI() {
		return (QuestingImpl) QuestWorld.getAPI();
	}
	
	public SpawnerListener getSpawnListener() {
		return spawnListener;
	}

	@Override
	public void onLoad() {
		saveDefaultConfig();
		Log.setLogger(getLogger());
	}

	@Override
	public void onEnable() {
		api = new QuestingImpl(this);
		api.load();

		loadConfigs();

		getCommand("quests").setExecutor(new QuestsCommand());
		getCommand("questeditor").setExecutor(new EditorCommand(api));

		getServer().getServicesManager().register(QuestingAPI.class, api, this, ServicePriority.Normal);

		new PlayerListener(api);
		new MenuListener(this);
		spawnListener = new SpawnerListener(this);
		new ClickCommand(this);

		GuideBook.instance();
		
		try {
			Reflect.serverAddChannel(this, Constants.CH_BOOK);
		}
		catch(Exception e) {
			Log.warning("could not register book channel");
			e.printStackTrace();
		}
	}

	public void loadConfigs() {
		reloadConfig();

		if (questCheckHandle != -1) {
			getServer().getScheduler().cancelTask(questCheckHandle);
		}

		questCheckHandle = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
			for (Player p : getServer().getOnlinePlayers()) {
				api.getPlayerStatus(p).update();
			}
			
			for (IMission mission : api.getViewer().getTickingMissions()) {
				if(MenuListener.isDisabled(mission) || MenuListener.isDisabled(mission.getQuest())) {
					continue;
				}
				
				for (Player p : getServer().getOnlinePlayers()) {
					api.getPlayerStatus(p).tick(mission);
				}
			}
		}, 0L, getConfig().getInt("options.quest-check-delay"));

		int autosave = getConfig().getInt("options.autosave-interval") * 20 * 60; // minutes to ticks

		if (autosaveHandle != -1) {
			getServer().getScheduler().cancelTask(autosaveHandle);
			autosaveHandle = -1;
		}

		if (autosave > 0) {
			autosaveHandle = getServer().getScheduler()
					.scheduleSyncRepeatingTask(this, api::onSave, autosave, autosave);
		}
	}

	@Override
	public void onDisable() {
		api.onSave();
		api.onDiscard();
		
		for(Player p : getServer().getOnlinePlayers()) {
			p.removeMetadata(Constants.MD_LAST_MENU, this);
			p.removeMetadata(Constants.MD_PAGES, this);
		}

		autosaveHandle = -1;
		questCheckHandle = -1;

		Log.setLogger(null);

		getServer().getServicesManager().unregisterAll(this);
		getServer().getScheduler().cancelTasks(this);
		
		try {
			Reflect.serverRemoveChannel(this, Constants.CH_BOOK);
		}
		catch(Exception e) {
			Log.warning("could not unregister book channel");
			e.printStackTrace();
		}
	}
}
