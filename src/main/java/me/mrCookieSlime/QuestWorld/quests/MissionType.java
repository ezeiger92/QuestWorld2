package me.mrCookieSlime.QuestWorld.quests;

import me.mrCookieSlime.QuestWorld.QuestWorld;

import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public abstract class MissionType implements Listener {
	
	public enum SubmissionType {
		ITEM,
		ENTITY, 
		UNKNOWN, 
		CITIZENS_ITEM,
		LOCATION,
		INTEGER,
		TIME,
		CITIZENS_INTERACT,
		CITIZENS_KILL, 
		BLOCK;
		
	}
	
	String id;
	MaterialData selectorItem;
	SubmissionType type;
	boolean supportsTimeframes, supportsDeathReset, ticking;

	public MissionType(String name, boolean supportsTimeframes, boolean supportsDeathReset, boolean ticking, SubmissionType type, MaterialData item) {
		this.id = name;
		this.selectorItem = item;
		this.type = type;
		this.supportsTimeframes = supportsTimeframes;
		this.supportsDeathReset = supportsDeathReset;
		this.ticking = ticking;
	}
	
	public final String formatQuestDisplay(QuestMission instance) {
		return formatMissionDisplay(instance) + formatTimeframe(instance) + formatDeathReset(instance);
	}
	
	protected abstract String formatMissionDisplay(QuestMission instance);
	
	private String formatTimeframe(QuestMission quest) {
		if(!quest.hasTimeframe() || !supportsTimeframes)
			return "";
		long duration = quest.getTimeframe();
		
		return " &7within " + (duration / 60) + "h " + (duration % 60) + "m";
	}
	
	private String formatDeathReset(QuestMission quest) {
		if(!quest.resetsonDeath() || !supportsDeathReset)
			return "";
		
		return " &7without dying";
	}

	public MaterialData getSelectorItem() {
		return selectorItem;
	}
	
	public ItemStack getDisplayItem(QuestMission qm) {
		return new ItemStack(Material.COMMAND);
	}

	public static MissionType valueOf(String id) {
		return QuestWorld.getInstance().getMissionTypes().get(id);
	}

	public String getID() {
		return id;
	}
	
	public SubmissionType getSubmissionType() {
		return type;
	}
	
	public boolean supportsTimeframes() {
		return this.supportsTimeframes;
	}
	
	@Override
	public String toString() {
		return id;
	}

	public boolean supportsDeathReset() {
		return this.supportsDeathReset;
	}

	public boolean isTicker() {
		return this.ticking;
	}
}
