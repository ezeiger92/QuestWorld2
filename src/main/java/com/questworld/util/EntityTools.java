package com.questworld.util;

import java.util.ArrayList;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import com.questworld.QuestWorldPlugin;
import com.questworld.api.QuestWorld;

/**
 * A shared home for tools related to entities and entity manipulation.
 * 
 * @author Erik Zeiger
 */
public class EntityTools {
	
	public static final EntityType ANY_ENTITY = null;
	public static final String ANY_ID = "ANY";
	
	private static final EntityType[] alive;
	static {
		// Alive entities
		ArrayList<EntityType> entities = new ArrayList<>();
		entities.add(ANY_ENTITY);

		for (EntityType ent : EntityType.values())
			if (ent.isAlive())
				entities.add(ent);

		alive = entities.toArray(new EntityType[entities.size()]);
	}

	/**
	 * Accessor for living entity types.
	 * 
	 * @return An array of all living entities
	 */
	public static EntityType[] aliveEntityTypes() {
		return alive.clone();
	}

	/**
	 * Creates an ItemBuidler that represents a desired entity type, primarily for
	 * use in menus.
	 * 
	 * @param type The desired entity type
	 * @return an {@link ItemBuilder} with display properties specific to
	 *         <tt>type</tt>
	 */
	public static ItemBuilder getEntityDisplay(EntityType type) {
		
		if(type == ANY_ENTITY) {
			return new ItemBuilder(QuestWorld.getIcons().items.mob_egg_overloads.get(EntityTools.ANY_ID));
		}
		
		ItemStack icon = QuestWorld.getIcons().items.mob_egg_overloads.get(type.name());
		
		if (ItemBuilder.isAir(icon)) {
			icon = new ItemBuilder((ItemStack) null).get();
			Reflect.getAdapter().makeSpawnEgg(icon, type);
		}
		
		if (ItemBuilder.isAir(icon)) {
			icon = QuestWorld.getIcons().editor.unknown_mob;
		}
		
		//case PLAYER: Material.PLAYER_HEAD;
		//case GIANT: Material.ZOMBIE_HEAD;
		//case ENDER_DRAGON: Material.DRAGON_HEAD;
		//case WITHER: Material.WITHER_SKELETON_SKULL;
		//case ILLUSIONER: Material.ENDER_EYE;
		//case ARMOR_STAND: Material.ARMOR_STAND;
		//case SNOWMAN: Material.SNOWBALL;
		//case IRON_GOLEM: Material.IRON_INGOT;
		//case PIG_ZOMBIE: Material.ZOMBIE_PIGMAN_SPAWN_EGG;
		//case MUSHROOM_COW: Material.MOOSHROOM_SPAWN_EGG;
		
		return new ItemBuilder(icon);
	}

	/**
	 * Returns a "nice" string representing the entities name. May have special
	 * names for entities that are not traditional mobs and are used internally for
	 * other purposes.
	 * <p>
	 * Specifically, <tt>EntityTools.ANY_ENTITY</tt> represents a wild card, and
	 * will return "Any Entity" as its name.
	 * 
	 * @param entity The desired entity type
	 * @return A clean name that describes the entity
	 */
	public static String nameOf(EntityType entity) {
		if (entity == ANY_ENTITY)
			return "Any Entity";

		return Text.niceName(entity.toString());
	}
	
	public static String serialNameOf(EntityType entity) {
		if(entity == ANY_ENTITY) {
			return "ANY";
		}
		
		return entity.toString();
	}
	
	public static EntityType deserializeType(String entityType) {
		
		// Poor decision on my part to represent "any entity" in 1.13 and earlier
		// Backwards compatibility for saves from those versions
		if(entityType == "COMPLEX_PART") {
			return ANY_ENTITY;
		}
		
		try {
			return EntityType.valueOf(entityType);
		}
		catch(IllegalArgumentException | NullPointerException e) {
			return ANY_ENTITY;
		}
	}

	/**
	 * Queries metadata to determine if QuestWorld thinks this mob came from a
	 * spawner.
	 * 
	 * @param entity The target entity
	 * @return Whether or not this entity was spawned by a mob spawner block
	 */
	public static boolean isFromSpawner(LivingEntity entity) {
		return QuestWorldPlugin.get().getSpawnListener().isFromSpawner(entity);
	}
}
