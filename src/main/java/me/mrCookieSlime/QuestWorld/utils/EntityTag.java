package me.mrCookieSlime.QuestWorld.utils;

import org.bukkit.entity.EntityType;

public class EntityTag extends ItemTag {
	
	EntityTag(EntityType base) {
		super("EntityTag");
		append("id:"+entityNameLookup(base));
	}
	
	EntityTag(EntityType base, String properties) {
		this(base);
		append(properties);
	}
	
	public static EntityTag from(EntityType base) {
		return new EntityTag(base);
	}
	
	public static EntityTag from(EntityOther base) {
		return base.getItemTag();
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
