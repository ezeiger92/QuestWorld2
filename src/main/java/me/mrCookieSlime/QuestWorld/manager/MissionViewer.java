package me.mrCookieSlime.QuestWorld.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.Ticking;
import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionWrite;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.contract.IQuestWrite;
import me.mrCookieSlime.QuestWorld.event.CategoryDeleteEvent;
import me.mrCookieSlime.QuestWorld.event.MissionChangeEvent;
import me.mrCookieSlime.QuestWorld.event.MissionDeleteEvent;
import me.mrCookieSlime.QuestWorld.event.QuestChangeEvent;
import me.mrCookieSlime.QuestWorld.event.QuestDeleteEvent;

public class MissionViewer implements Listener {
	private Map<MissionType, Set<IMission>> missions = new HashMap<>();
	private Set<IMission> ticking_missions = new HashSet<>();
	
	public Set<IMission> getMissionsOf(MissionType type) {
		missions.putIfAbsent(type, new HashSet<>());
		return missions.get(type);
	}
	
	public Set<IMission> getTickingMissions() {
		return ticking_missions;
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCreateMission(QuestChangeEvent event) {
		if(event.hasChange(IQuestWrite.Member.TASKS)) {
			List<? extends IMission> questMissions = event.getNextState().getMissions();
			IMission m = questMissions.get(questMissions.size() - 1);
			add(m, m.getType());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onUpdateMission(MissionChangeEvent event) {
		if(event.hasChange(IMissionWrite.Member.TYPE))
			remove(event.getMission());
			add(event.getMission(), event.getNextState().getType());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDeleteCategory(CategoryDeleteEvent event) {
		removeAll(event.getCategory());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDeleteQuest(QuestDeleteEvent event) {
		removeAll(event.getQuest());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDeleteMission(MissionDeleteEvent event) {
		remove(event.getMission());
	}
	
	private void add(IMission mission, MissionType type) {
		getMissionsOf(type).add(mission);
		
		if(type instanceof Ticking)
			ticking_missions.add(mission);
	}
	
	private void removeAll(ICategory category) {
		for(IQuest q : category.getQuests())
			removeAll(q);
	}
	
	private void removeAll(IQuest quest) {
		for(IMission m : quest.getMissions())
			remove(m);
	}
	
	private void remove(IMission mission) {
		getMissionsOf(mission.getType()).remove(mission);
		
		if(mission.getType() instanceof Ticking)
			ticking_missions.remove(mission);
	}
}
