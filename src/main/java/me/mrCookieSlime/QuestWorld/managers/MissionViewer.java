package me.mrCookieSlime.QuestWorld.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestChange;
import me.mrCookieSlime.QuestWorld.api.Ticking;
import me.mrCookieSlime.QuestWorld.events.CategoryDeleteEvent;
import me.mrCookieSlime.QuestWorld.events.MissionChangeEvent;
import me.mrCookieSlime.QuestWorld.events.MissionDeleteEvent;
import me.mrCookieSlime.QuestWorld.events.QuestChangeEvent;
import me.mrCookieSlime.QuestWorld.events.QuestDeleteEvent;
import me.mrCookieSlime.QuestWorld.quests.Category;
import me.mrCookieSlime.QuestWorld.quests.Mission;
import me.mrCookieSlime.QuestWorld.quests.Quest;

public class MissionViewer implements Listener {
	private Map<MissionType, Set<Mission>> missions = new HashMap<>();
	private Set<Mission> ticking_missions = new HashSet<>();
	
	public Set<Mission> getMissionsOf(MissionType type) {
		missions.putIfAbsent(type, new HashSet<>());
		return missions.get(type);
	}
	
	public Set<Mission> getTickingMissions() {
		return ticking_missions;
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCreateMission(QuestChangeEvent event) {
		if(event.getNextState().hasChange(QuestChange.Member.TASKS)) {
			List<Mission> questMissions = event.getNextState().getMissions();
			Mission m = questMissions.get(questMissions.size() - 1);
			add(m, m.getType());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onUpdateMission(MissionChangeEvent event) {
		if(event.getNextState().hasChange(MissionChange.Member.TYPE))
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
	
	private void add(Mission mission, MissionType type) {
		getMissionsOf(type).add(mission);
		
		if(type instanceof Ticking)
			ticking_missions.add(mission);
	}
	
	private void removeAll(Category category) {
		for(Quest q : category.getQuests())
			removeAll(q);
	}
	
	private void removeAll(Quest quest) {
		for(Mission m : quest.getMissions())
			remove(m);
	}
	
	private void remove(Mission mission) {
		getMissionsOf(mission.getType()).remove(mission);
		
		if(mission.getType() instanceof Ticking)
			ticking_missions.remove(mission);
	}
}
