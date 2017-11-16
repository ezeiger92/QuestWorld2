package me.mrCookieSlime.QuestWorld.api.contract;

import java.util.Map;

import org.bukkit.plugin.Plugin;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.MissionViewer;
import me.mrCookieSlime.QuestWorld.api.Translator;
import me.mrCookieSlime.QuestWorld.util.Sounds;
import net.milkbowl.vault.economy.Economy;

public interface QuestingAPI {
	MissionType missionTypeOf(String typeName);
	Map<String, MissionType> missionTypes();
	MissionViewer missionViewer();
	Economy economy();
	IFacade facade();
	Sounds sounds();
	String translation(Translator key, String... replacements);
	Plugin plugin();
}
