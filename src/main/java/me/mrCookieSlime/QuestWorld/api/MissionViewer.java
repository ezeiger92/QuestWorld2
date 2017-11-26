package me.mrCookieSlime.QuestWorld.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionState;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.contract.IQuestState;
import me.mrCookieSlime.QuestWorld.event.CategoryDeleteEvent;
import me.mrCookieSlime.QuestWorld.event.MissionChangeEvent;
import me.mrCookieSlime.QuestWorld.event.MissionDeleteEvent;
import me.mrCookieSlime.QuestWorld.event.QuestChangeEvent;
import me.mrCookieSlime.QuestWorld.event.QuestDeleteEvent;

public class MissionViewer implements Listener {
	private Map<MissionType, Set<IMission>> missions = new HashMap<>();
	private Set<IMission> ticking_missions = new HashSet<>();
	private Set<IMission> decaying_missions = new HashSet<>();
	
	public Set<IMission> getMissionsOf(MissionType type) {
		Set<IMission> result = missions.get(type);
		if(result == null) {
			result = new HashSet<>();
			missions.put(type, result);
		}
		
		return result;
	}
	
	public Set<IMission> getTickingMissions() {
		return ticking_missions;
	}
	
	public Set<IMission> getDecayingMissions() {
		return decaying_missions;
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCreateMission(QuestChangeEvent event) {
		if(event.hasChange(IQuestState.Member.TASKS)) {
			for(IMission m : event.getNextState().getMissions())
				add(m, m.getType());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onUpdateMission(MissionChangeEvent event) {
		if(event.hasChange(IMissionState.Member.TYPE))
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
		
		if(type instanceof Decaying)
			decaying_missions.add(mission);
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
		
		if(mission.getType() instanceof Decaying)
			decaying_missions.remove(mission);
	}
}
