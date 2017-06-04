package me.mrCookieSlime.QuestWorld.utils;

import org.bukkit.entity.EntityType;

public class EntityTag {
	private String tag;
	public EntityTag(EntityType base) {
		tag = "{EntityTag:{id:\"minecraft:" + entityNameLookup(base) + "\"}}";
	}
	
	@Override
	public String toString() {
		return tag;
	}
	
	/**
	 * Yes, this is deprecated, but from the source: "These strings MUST match the strings in nms.EntityTypes"
	 * Since we're using NBT that requires the exact name from nms, this is what we want.
	 * 
	 * @param ent Enum entity
	 * @return NMS entity name
	 */
	@SuppressWarnings("deprecation")
	public static String entityNameLookup(EntityType ent) {
		return ent.getName();
	}
}
