package com.questworld;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.questworld.api.MissionType;
import com.questworld.api.MissionViewer;
import com.questworld.api.QuestWorld;
import com.questworld.api.Translator;
import com.questworld.api.contract.ICategory;
import com.questworld.api.contract.IMission;
import com.questworld.api.contract.IParty;
import com.questworld.api.contract.MissionEntry;
import com.questworld.api.contract.QuestingAPI;
import com.questworld.api.menu.Menu;
import com.questworld.extension.builtin.Builtin;
import com.questworld.listener.ExtensionInstaller;
import com.questworld.manager.MissionSet;
import com.questworld.manager.Party;
import com.questworld.manager.PlayerStatus;
import com.questworld.manager.ProgressTracker;
import com.questworld.quest.Facade;
import com.questworld.util.BukkitService;
import com.questworld.util.Lang;
import com.questworld.util.Log;
import com.questworld.util.ResourceLoader;
import com.questworld.util.Sounds;

import net.milkbowl.vault.economy.Economy;

public final class QuestingImpl implements QuestingAPI {
	private final Facade facade = new Facade();
	private final Map<UUID, Party> parties = new HashMap<>();
	private final Map<UUID, PlayerStatus> statuses = new HashMap<>();
	private final Map<String, MissionType> types = new HashMap<>();
	
	private final ExtensionInstaller extensions;
	private final Lang language;
	private final QuestWorldPlugin plugin;
	private final PresetLoader presets;
	private final ResourceLoader resources;
	private final MissionViewer viewer;

	private Directories dataFolders;
	private Optional<Economy> econ = Optional.empty();
	private Sounds eventSounds;
	
	public QuestingImpl(QuestWorldPlugin questWorld) {
		extensions = new ExtensionInstaller(questWorld, this);
		plugin = questWorld;
		presets = new PresetLoader(this);
		resources = new ResourceLoader(questWorld);
		viewer = new MissionViewer(questWorld);
		
		dataFolders = new Directories(resources);
		language = new Lang(resources);
		
		String lang = plugin.getConfig().getString("options.language");
		if(lang != null)
			language.setLang(lang);
		
		if(Bukkit.getPluginManager().getPlugin("Vault") != null)
			econ = BukkitService.find(Economy.class);
		
		eventSounds = new Sounds(resources.loadConfigNoexpect("sounds.yml", true));
		
		if(!econ.isPresent())
			Log.info("No economy (vault) found, money rewards disabled");
		
		QuestWorld.setAPI(this);
	}
	
	public void load() {
		ExtensionLoader extLoader = new ExtensionLoader(resources.getClassLoader(), dataFolders.extensions);
		extensions.add(new Builtin());
		extensions.addAll(extLoader.loadLocal());

		Log.fine("Retrieving Quest Configuration...");
		facade.load();
		int categories = facade.getCategories().size(), quests = 0;
		for (ICategory category: facade.getCategories())
			quests += category.getQuests().size();

		Log.fine("Successfully loaded " + categories + " Categories");
		Log.fine("Successfully loaded " + quests + " Quests");
	}
	
	public Directories getDataFolders() {
		return dataFolders;
	}
	
	public ResourceLoader getResources() {
		return resources;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends MissionType> T getMissionType(String typeName) {
		T result = (T)types.get(typeName);
		if(result != null) 
			return result;
		
		return (T)UnknownMission.get(typeName);
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
	
	public ExtensionInstaller getExtensions() {
		return extensions;
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
		return new MissionSet(type, getPlayerStatus(player));
	}
	
	@Override
	public MissionEntry getMissionEntry(IMission mission, OfflinePlayer player) {
		return new MissionSet.Result(mission, getPlayerStatus(player));
	}
	
	@Override
	public QuestWorldPlugin getPlugin() {
		return plugin;
	}
	
	@Override
	public PlayerStatus getPlayerStatus(OfflinePlayer player) {
		return getPlayerStatus(player.getUniqueId());
	}
	
	@Override
	public PlayerStatus getPlayerStatus(UUID uuid) {
		PlayerStatus result = statuses.get(uuid);
		
		if(result != null)
			return result;
		
		result = new PlayerStatus(uuid);
		statuses.put(uuid, result);
		return result;
	}
	
	public Party getParty(OfflinePlayer player) {
		return getParty(player.getUniqueId());
	}
	
	public Party getParty(UUID uuid) {
		ProgressTracker tracker = getPlayerStatus(uuid).getTracker();
		UUID leader = tracker.getPartyLeader();
		
		if(leader != null) {
			Party p = parties.get(leader);
			if(p == null)
				p = createParty(leader);

			if(!p.getPending().contains(uuid))
				return p;
			
			// Party did not contain the player, something unusual must have happened
			tracker.setPartyLeader(null);
		}
		
		return null;
	}
	
	@Override
	public void disbandParty(IParty party) {
		Party rawParty = (Party)party;
		parties.remove(rawParty.getLeaderUUID());
		rawParty.disband();
	}
	
	@Override
	public Party createParty(OfflinePlayer player) {
		return createParty(player.getUniqueId());
	}

	@Override
	public Party createParty(UUID uuid) {
		Party p = new Party(uuid);
		parties.put(uuid, p);
		return p;
	}
	
	@Override
	public void onSave() {
		facade.save(false);
		
		plugin.getServer().getOnlinePlayers().stream()
			.map(p -> statuses.get(p.getUniqueId()))
			.forEach(status -> status.getTracker().onSave());
		
		extensions.onSave();
	}
	
	@Override
	public void onReload() {
		plugin.loadConfigs();
		dataFolders = new Directories(resources);
		eventSounds = new Sounds(resources.loadConfigNoexpect("sounds.yml", true));
		facade.onReload();
		language.onReload();
		
		extensions.onReload();
		
		GuideBook.reset();
	}
	
	@Override
	public void onDiscard() {
		facade.onDiscard();
		viewer.clear();
		
		for(PlayerStatus status : statuses.values())
			status.unload();
		statuses.clear();
		
		// TODO: Better place, message
		for(Player p : Bukkit.getOnlinePlayers())
			if(p.getOpenInventory().getTopInventory().getHolder() instanceof Menu)
				p.closeInventory();
		
		extensions.onDiscard();
	}
	
	public void unloadPlayerStatus(OfflinePlayer player) {
		PlayerStatus status = statuses.remove(player.getUniqueId());
		if(status != null)
			status.unload();
	}
	
	public PresetLoader presets() {
		return presets;
	}
}
