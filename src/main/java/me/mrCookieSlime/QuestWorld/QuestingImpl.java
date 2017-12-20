package me.mrCookieSlime.QuestWorld;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.MissionViewer;
import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Translator;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.MissionEntry;
import me.mrCookieSlime.QuestWorld.api.contract.QuestingAPI;
import me.mrCookieSlime.QuestWorld.api.menu.Menu;
import me.mrCookieSlime.QuestWorld.manager.MissionSet;
import me.mrCookieSlime.QuestWorld.manager.PlayerStatus;
import me.mrCookieSlime.QuestWorld.manager.StatusManager;
import me.mrCookieSlime.QuestWorld.quest.Facade;
import me.mrCookieSlime.QuestWorld.util.BukkitService;
import me.mrCookieSlime.QuestWorld.util.Lang;
import me.mrCookieSlime.QuestWorld.util.Reloadable;
import me.mrCookieSlime.QuestWorld.util.ResourceLoader;
import me.mrCookieSlime.QuestWorld.util.Sounds;
import net.milkbowl.vault.economy.Economy;

public final class QuestingImpl implements QuestingAPI, Reloadable {
	private Map<String, MissionType> types = new HashMap<>();
	//private Map<String, MissionType> immutableTypes = 
	private MissionViewer viewer = new MissionViewer();
	private Optional<Economy> econ = Optional.ofNullable(null);
	private Facade facade = new Facade();
	private Sounds eventSounds;
	private ResourceLoader resources;
	private Lang language;
	private QuestWorldPlugin questWorld;
	private StatusManager statusManager = new StatusManager();
	
	public QuestingImpl(QuestWorldPlugin questWorld) {
		this.questWorld = questWorld;
		resources = new ResourceLoader(questWorld);
		language = new Lang(resources);
		QuestWorld.setAPI(this);
	}
	
	public ResourceLoader getResources() {
		return resources;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends MissionType> T getMissionType(String typeName) {
		T result = (T)types.get(typeName);
		if(result == null) {
			result = (T)UnknownMission.get(typeName);
			registerType(result);
		}
		
		return result;
	}

	@Override
	public Map<String, MissionType> getMissionTypes() {
		return Collections.unmodifiableMap(types);
	}
	
	public void registerType(MissionType type) {
		types.put(type.getName(), type);
	}
	
	@Override
	public MissionViewer getViewer() {
		return viewer;
	}
	
	public void onEnable() {
		if(Bukkit.getPluginManager().getPlugin("Vault") != null)
			econ = Optional.ofNullable(BukkitService.get(Economy.class));
	}
	
	@Override
	public Optional<Economy> getEconomy() {
		return econ;
	}
	
	@Override
	public Facade getFacade() {
		return facade;
	}
	
	@Override
	public Sounds getSounds() {
		return eventSounds;
	}
	
	@Override
	public String translate(Translator key, String... replacements) {
		return language.translate(key, replacements);
	}
	
	@Override
	public Iterable<MissionEntry> getMissionEntries(MissionType type, OfflinePlayer player) {
		return new MissionSet(statusManager.get(player.getUniqueId()), type);
	}
	
	@Override
	public MissionEntry getMissionEntry(IMission mission, OfflinePlayer player) {
		return new MissionSet.Result(mission, statusManager.get(player.getUniqueId()));
	}
	
	@Override
	public QuestWorldPlugin getPlugin() {
		return questWorld;
	}
	
	@Override
	public PlayerStatus getPlayerStatus(OfflinePlayer player) {
		return statusManager.get(player.getUniqueId());
	}

	@Override
	public void save() {
		
	}
	
	public void unload() {
		facade.unload();
		viewer.clear();
		statusManager.unloadAll();
		
		// TODO: Better place, message
		for(Player p : Bukkit.getOnlinePlayers())
			if(p.getOpenInventory().getTopInventory().getHolder() instanceof Menu)
				p.closeInventory();
	}

	@Override
	public void reload() {
		eventSounds = new Sounds(resources.loadConfigNoexpect("sounds.yml", true));
		language.reload();
	}
	
	public void unloadPlayerStatus(OfflinePlayer player) {
		statusManager.unload(player.getUniqueId());
	}
}
