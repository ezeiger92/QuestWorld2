package me.mrCookieSlime.QuestWorld.api;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMissionWrite;
import me.mrCookieSlime.QuestWorld.utils.Log;

import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public abstract class MissionType {
	@Deprecated
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
		Log.fine("MissionType - Creating: " + name);
		this.id = name;
		this.selectorItem = item;
		this.type = type;
		this.supportsTimeframes = supportsTimeframes;
		this.supportsDeathReset = supportsDeathReset;
		this.ticking = ticking;
		
	}
	
	public final String defaultDisplayName(IMission instance) {
		return displayString(instance) + formatTimeframe(instance) + formatDeathReset(instance);
	}
	
	protected abstract String displayString(IMission instance);
	
	public abstract ItemStack displayItem(IMission instance);
	
	private String formatTimeframe(IMission quest) {
		if(!quest.hasTimeframe() || !supportsTimeframes)
			return "";
		long duration = quest.getTimeframe();
		
		return " &7within " + (duration / 60) + "h " + (duration % 60) + "m";
	}
	
	private String formatDeathReset(IMission quest) {
		if(!quest.resetsonDeath() || !supportsDeathReset)
			return "";
		
		return " &7without dying";
	}

	public MaterialData getSelectorItem() {
		return selectorItem;
	}

	public static MissionType valueOf(String id) {
		MissionType result =  QuestWorld.getInstance().getMissionTypes().get(id);
		
		if(result == null) {
			throw new NullPointerException("Tried to fetch mission type:" + id + " that doesn't exist!");
		}
		
		return result;
	}

	@Deprecated
	public String getID() {
		return id;
	}
	
	@Deprecated
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
	
	@Deprecated
	protected void setId(String id) {
		this.id = id;
	}
	
	public boolean attemptUpgrade(IMissionWrite instance) {
		return false;
	}
	
	protected void setSelectorMaterial(MaterialData material) {
		selectorItem = material;
	}
	
	public String progressString(float percent, int current, int total) {
		return Math.round(percent * 100) + "% (" + current + "/" + total + ")";
	}
}
