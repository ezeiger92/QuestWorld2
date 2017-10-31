package me.mrCookieSlime.QuestWorld.util;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.Metadatable;

public class EntityTools {
	private static final EntityType[] alive;
	static {
		// Alive entities
		ArrayList<EntityType> entities = new ArrayList<>();
		for(EntityType ent : EntityType.values())
			if(ent.isAlive())
				entities.add(ent);
		
		alive = entities.toArray(new EntityType[entities.size()]);
	}
	
	public static EntityType[] aliveEntityTypes() {
		return alive.clone();
	}
	
	public static ItemBuilder getEntityDisplay(EntityType type) {
		ItemBuilder ib = new ItemBuilder(Material.SKULL_ITEM);

		switch(type) {
			case PLAYER:       ib.skull(SkullType.PLAYER); break;
			case GIANT:        ib.skull(SkullType.ZOMBIE); break;
			case ENDER_DRAGON: ib.skull(SkullType.DRAGON); break;
			case WITHER:       ib.skull(SkullType.WITHER); break;
			case ARMOR_STAND:  ib.type(Material.ARMOR_STAND); break;
			case SNOWMAN:      ib.type(Material.SNOW_BALL); break;
			case IRON_GOLEM:   ib.type(Material.IRON_INGOT); break;
			case COMPLEX_PART: ib.type(Material.NETHER_STAR); break;
			
			default:
				try { 
					ib.type(Material.MONSTER_EGG).mob(type);
				}
				catch(IllegalArgumentException e) {
					ib.type(Material.BARRIER);
				}
		}
		
		return ib;
	}
	
	public static boolean isFromSpawner(Metadatable entity) {
		return entity.hasMetadata("spawned_by_spawner");
	}
}
