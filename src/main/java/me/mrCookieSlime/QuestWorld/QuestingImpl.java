package me.mrCookieSlime.QuestWorld;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.MissionViewer;
import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Translator;
import me.mrCookieSlime.QuestWorld.api.contract.QuestingAPI;
import me.mrCookieSlime.QuestWorld.quest.RenderableFacade;
import me.mrCookieSlime.QuestWorld.util.BukkitService;
import me.mrCookieSlime.QuestWorld.util.Lang;
import me.mrCookieSlime.QuestWorld.util.Reloadable;
import me.mrCookieSlime.QuestWorld.util.ResourceLoader;
import me.mrCookieSlime.QuestWorld.util.Sounds;
import net.milkbowl.vault.economy.Economy;

public class QuestingImpl implements QuestingAPI, Reloadable {
	private Map<String, MissionType> types = new HashMap<>();
	private Map<String, MissionType> immutableTypes = Collections.unmodifiableMap(types);
	private MissionViewer viewer = new MissionViewer();
	private Economy econ = null;
	private RenderableFacade facade = new RenderableFacade();
	private Sounds eventSounds;
	private ResourceLoader resources;
	private Lang language;
	private QuestWorldPlugin questWorld;
	
	public QuestingImpl(QuestWorldPlugin questWorld) {
		this.questWorld = questWorld;
		resources = new ResourceLoader(questWorld);
		language = new Lang(resources);
		QuestWorld.setAPI(this);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends MissionType> T getMissionType(String typeName) {
		return (T)types.get(typeName);
	}

	@Override
	public Map<String, MissionType> getMissionTypes() {
		return immutableTypes;
	}
	
	public void registerType(MissionType type) {
		types.put(type.getName(), type);
	}
	
	@Override
	public MissionViewer getViewer() {
		return viewer;
	}
	
	public void initEconomy() {
		if(Bukkit.getPluginManager().getPlugin("Vault") != null)
			econ = BukkitService.get(Economy.class);
	}

	public void onConfig() {
		eventSounds = new Sounds(resources.loadConfigNoexpect("sounds.yml", true));
	}
	
	@Override
	public Economy getEconomy() {
		return econ;
	}
	
	@Override
	public RenderableFacade getFacade() {
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
	public QuestWorldPlugin getPlugin() {
		return questWorld;
	}

	@Override
	public void save() {
		
	}

	@Override
	public void reload() {
		language.reload();
	}
}
