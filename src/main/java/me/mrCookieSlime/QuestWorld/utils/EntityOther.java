package me.mrCookieSlime.QuestWorld.utils;

import org.bukkit.entity.EntityType;

public enum EntityOther {
	//STRAY(EntityType.SKELETON, "SkeletonType:2"),
	//WITHER_SKELETON(EntityType.SKELETON, "SkeletonType:1"),
	ELDER_GUARDIAN(EntityType.GUARDIAN, "Elder:1"),
	CHARGED_CREEPER(EntityType.CREEPER, "powered:1"),
	//HUSK(                      EntityType.ZOMBIE, "ZombieType:6"),
	ZOMBIE_FARMER_VILLAGER(    EntityType.ZOMBIE, "ZombieType:1"),
	ZOMBIE_LIBRARIAN_VILLAGER( EntityType.ZOMBIE, "ZombieType:2"),
	ZOMBIE_PRIEST_VILLAGER(    EntityType.ZOMBIE, "ZombieType:3"),
	ZOMBIE_BLACKSMITH_VILLAGER(EntityType.ZOMBIE, "ZombieType:4"),
	ZOMBIE_BUTCHER_VILLAGER(   EntityType.ZOMBIE, "ZombieType:5"),
	//GENERIC_ZOMBIE_VILLAGER(   EntityType.ZOMBIE, "IsVillager:1"),
	FARMER_VILLAGER(       EntityType.VILLAGER, "Profession:0,Career:1"),
	FISHERMAN_VILLAGER(    EntityType.VILLAGER, "Profession:0,Career:2"),
	SHEPHERD_VILLAGER(     EntityType.VILLAGER, "Profession:0,Career:3"),
	FLETCHER_VILLAGER(     EntityType.VILLAGER, "Profession:0,Career:4"),
	LIBRARIAN_VILLAGER(    EntityType.VILLAGER, "Profession:1,Career:1"),
	CARTOGRAPHER_VILLAGER( EntityType.VILLAGER, "Profession:1,Career:2"),
	CLERIC_VILLAGER(       EntityType.VILLAGER, "Profession:2,Career:1"),
	ARMORER_VILLAGER(      EntityType.VILLAGER, "Profession:3,Career:1"),
	WEAPON_SMITH_VILLAGER( EntityType.VILLAGER, "Profession:3,Career:2"),
	TOOL_SMITH_VILLAGER(   EntityType.VILLAGER, "Profession:3,Career:3"),
	BUTCHER_VILLAGER(      EntityType.VILLAGER, "Profession:4,Career:1"),
	LEATHERWORKER_VILLAGER(EntityType.VILLAGER, "Profession:4,Career:2"),
	GENERIC_FARMER_VILLAGER(    EntityType.VILLAGER, "Profession:0"),
	GENERIC_LIBRARIAN_VILLAGER( EntityType.VILLAGER, "Profession:1"),
	GENERIC_PRIEST_VILLAGER(    EntityType.VILLAGER, "Profession:2"),
	GENERIC_BLACKSMITH_VILLAGER(EntityType.VILLAGER, "Profession:3"),
	GENERIC_BUTCHER_VILLAGER(   EntityType.VILLAGER, "Profession:4"),
	;
	private EntityType base;
	private String properties;

	EntityOther(EntityType base, String properties) {
		this.base = base;
		this.properties = properties;
	}
	
	public EntityTag getItemTag() {
		return new EntityTag(base, properties);
	}
	
	public EntityType getEntity() {
		return base;
	}
	
	public String getProperties() {
		return properties;
	}
}
