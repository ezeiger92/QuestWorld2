package me.mrCookieSlime.QuestWorld.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionState;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.contract.IQuestState;
import me.mrCookieSlime.QuestWorld.api.event.CategoryDeleteEvent;
import me.mrCookieSlime.QuestWorld.api.event.MissionChangeEvent;
import me.mrCookieSlime.QuestWorld.api.event.MissionDeleteEvent;
import me.mrCookieSlime.QuestWorld.api.event.QuestChangeEvent;
import me.mrCookieSlime.QuestWorld.api.event.QuestDeleteEvent;
import me.mrCookieSlime.QuestWorld.util.AutoListener;

/**
 * Provides access to missions based on type, {@link Decaying} status, and
 * {@link Ticking} status.
 * 
 * @author Erik Zeiger
 */
public class MissionViewer extends AutoListener {
	private Map<MissionType, Set<IMission>> missions = new HashMap<>();
	private Set<IMission> ticking_missions = new HashSet<>();
	private Set<IMission> decaying_missions = new HashSet<>();
	
	public MissionViewer(Plugin plugin) {
		register(plugin);
	}
	
	/**
	 * Provides all missions of a desired type. The returned Set is immutable,
	 * do not attempt to modify it.
	 * 
	 * @param type The target mission type
	 * @return A set containing all missions of type <tt>type</tt>
	 */
	public Set<IMission> getMissionsOf(MissionType type) {
		return Collections.unmodifiableSet(rawGetMissionsOf(type));
	}
	
	/**
	 * Provides all Ticking missions. The returned Set is immutable, do not
	 * attempt to modify it.
	 * 
	 * @return A set containing all Ticking missions
	 */
	public Set<IMission> getTickingMissions() {
		return ticking_missions;
	}
	
	/**
	 * Provides all Decaying missions. The returned Set is immutable, do not
	 * attempt to modify it.
	 * 
	 * @return A set containing all Decaying missions
	 */
	public Set<IMission> getDecayingMissions() {
		return decaying_missions;
	}
	
	public void clear() {
		missions.clear();
		ticking_missions.clear();
		decaying_missions.clear();
	}
	
	/**
	 * Updates mission sets based on event data. This is an event method and
	 * should not be called directly.
	 * 
	 * @param event The event
	 */
	@Deprecated
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCreateMission(QuestChangeEvent event) {
		if(event.hasChange(IQuestState.Member.TASKS)) {
			for(IMission m : event.getNextState().getMissions())
				add(m, m.getType());
		}
	}
	
	/**
	 * Updates mission sets based on event data. This is an event method and
	 * should not be called directly.
	 * 
	 * @param event The event
	 */
	@Deprecated
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onUpdateMission(MissionChangeEvent event) {
		if(event.hasChange(IMissionState.Member.TYPE))
			remove(event.getMission());
			add(event.getMission(), event.getNextState().getType());
	}

	/**
	 * Updates mission sets based on event data. This is an event method and
	 * should not be called directly.
	 * 
	 * @param event The event
	 */
	@Deprecated
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDeleteCategory(CategoryDeleteEvent event) {
		removeAll(event.getCategory());
	}

	/**
	 * Updates mission sets based on event data. This is an event method and
	 * should not be called directly.
	 * 
	 * @param event The event
	 */
	@Deprecated
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDeleteQuest(QuestDeleteEvent event) {
		removeAll(event.getQuest());
	}

	/**
	 * Updates mission sets based on event data. This is an event method and
	 * should not be called directly.
	 * 
	 * @param event The event
	 */
	@Deprecated
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDeleteMission(MissionDeleteEvent event) {
		remove(event.getMission());
	}
	
	/**
	 * Adds a mission to relevant Sets based on its type.
	 * 
	 * @param mission The mission
	 * @param type The type
	 */
	private void add(IMission mission, MissionType type) {
		rawGetMissionsOf(type).add(mission);
		
		if(type instanceof Ticking)
			ticking_missions.add(mission);
		
		if(type instanceof Decaying)
			decaying_missions.add(mission);
	}
	
	/**
	 * Removes all missions in a category from their Sets.
	 * 
	 * @param category The category
	 */
	private void removeAll(ICategory category) {
		for(IQuest q : category.getQuests())
			removeAll(q);
	}
	
	/**
	 * Removes all missions in a quest from their Sets.
	 * 
	 * @param quest The quest
	 */
	private void removeAll(IQuest quest) {
		for(IMission m : quest.getMissions())
			remove(m);
	}
	
	/**
	 * Removes a mission from all Sets.
	 * 
	 * @param mission The mission
	 */
	private void remove(IMission mission) {
		rawGetMissionsOf(mission.getType()).remove(mission);
		
		if(mission.getType() instanceof Ticking)
			ticking_missions.remove(mission);
		
		if(mission.getType() instanceof Decaying)
			decaying_missions.remove(mission);
	}
	
	private Set<IMission> rawGetMissionsOf(MissionType type) {
		Set<IMission> result = missions.get(type);
		if(result == null) {
			result = new HashSet<>();
			missions.put(type, result);
		}
		
		return result;
	}
}
