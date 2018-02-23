package me.mrCookieSlime.QuestWorld;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.MissionViewer;
import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Translator;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IParty;
import me.mrCookieSlime.QuestWorld.api.contract.MissionEntry;
import me.mrCookieSlime.QuestWorld.api.contract.QuestingAPI;
import me.mrCookieSlime.QuestWorld.api.menu.Menu;
import me.mrCookieSlime.QuestWorld.manager.MissionSet;
import me.mrCookieSlime.QuestWorld.manager.Party;
import me.mrCookieSlime.QuestWorld.manager.PlayerStatus;
import me.mrCookieSlime.QuestWorld.manager.ProgressTracker;
import me.mrCookieSlime.QuestWorld.quest.Facade;
import me.mrCookieSlime.QuestWorld.util.BukkitService;
import me.mrCookieSlime.QuestWorld.util.Lang;
import me.mrCookieSlime.QuestWorld.util.Log;
import me.mrCookieSlime.QuestWorld.util.Reloadable;
import me.mrCookieSlime.QuestWorld.util.ResourceLoader;
import me.mrCookieSlime.QuestWorld.util.Sounds;
import net.milkbowl.vault.economy.Economy;

public final class QuestingImpl implements QuestingAPI, Reloadable {
	private Map<String, MissionType> types = new HashMap<>();
	
	private MissionViewer viewer = new MissionViewer();
	private Optional<Economy> econ = Optional.ofNullable(null);
	private Facade facade = new Facade();
	private Sounds eventSounds;
	private ResourceLoader resources;
	private Lang language;
	private QuestWorldPlugin questWorld;
	
	private HashMap<UUID, PlayerStatus> statuses = new HashMap<>();
	private HashMap<UUID, Party> parties = new HashMap<>();
	
	public QuestingImpl(QuestWorldPlugin questWorld) {
		Log.setLogger(questWorld.getLogger());
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
	
	public void onEnable() {
		
		String lang = getPlugin().getConfig().getString("options.language");
		if(lang != null)
			language.setLang(lang);
		
		if(Bukkit.getPluginManager().getPlugin("Vault") != null)
			econ = BukkitService.find(Economy.class);
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
		return questWorld;
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

			if(p.getPending().contains(uuid))
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
		
	}
	
	@Override
	public void onReload() {
		facade.onReload();
		eventSounds = new Sounds(resources.loadConfigNoexpect("sounds.yml", true));
		language.onReload();
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
	}
	
	public void unloadPlayerStatus(OfflinePlayer player) {
		PlayerStatus status = statuses.remove(player.getUniqueId());
		if(status != null)
			status.unload();
	}
}
