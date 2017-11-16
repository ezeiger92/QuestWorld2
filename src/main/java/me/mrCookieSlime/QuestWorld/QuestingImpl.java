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
	public MissionType missionTypeOf(String typeName) {
		return types.get(typeName);
	}

	@Override
	public Map<String, MissionType> missionTypes() {
		return immutableTypes;
	}
	
	public void registerType(MissionType type) {
		types.put(type.getName(), type);
	}
	
	@Override
	public MissionViewer missionViewer() {
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
	public Economy economy() {
		return econ;
	}
	
	@Override
	public RenderableFacade facade() {
		return facade;
	}
	
	@Override
	public Sounds sounds() {
		return eventSounds;
	}
	
	@Override
	public String translation(Translator key, String... replacements) {
		return language.translate(key, replacements);
	}
	
	@Override
	public QuestWorldPlugin plugin() {
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
