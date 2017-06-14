package me.mrCookieSlime.QuestWorld.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class EntityTools {
	private static EntityType[] alive;
	static {
		// Alive entities

		List<EntityType> entities = new ArrayList<>();
		for(EntityType ent : EntityType.values())
			if(ent.isAlive())
				entities.add(ent);
		
		alive = entities.toArray(new EntityType[entities.size()]);
	}
	
	public static EntityType[] getAliveEntityTypes() {
		return alive;
	}
	
	public static List<EntityType> listAliveEntityTypes() {
		return new ArrayList<EntityType>(Arrays.asList(alive));
	}
	
	public static class NameComp implements Comparator<EntityType> {
		private boolean forward = true;
		
		private NameComp() {
		}
		
		private static NameComp aToZ = new NameComp();
		private static NameComp zToA = new NameComp().rev();

		private NameComp rev() {
			forward = false;
			return this;
		}
		
		public static NameComp forward() {
			return aToZ;
		}
		
		public static NameComp backward() {
			return zToA;
		}
		
		@Override
		public int compare(EntityType e1, EntityType e2) {
			if(forward)
				return e1.name().compareTo(e2.name());
			else
				return e2.name().compareTo(e1.name());
		}
	}
	
	public static ItemStack getEntityDisplay(EntityType type) {
		ItemBuilder ib = new ItemBuilder(Material.SKULL_ITEM);
		
		switch(type) {
			case PLAYER:       ib.skull(SkullType.PLAYER); break;
			case GIANT:        ib.skull(SkullType.ZOMBIE); break;
			case ENDER_DRAGON: ib.skull(SkullType.DRAGON); break;
			case WITHER:       ib.skull(SkullType.WITHER); break;
			case ARMOR_STAND:  ib.type(Material.ARMOR_STAND); break;
			case SNOWMAN:      ib.type(Material.SNOW_BALL); break;
			case IRON_GOLEM:   ib.type(Material.IRON_INGOT); break;
			
			default:
				try { 
					ib.type(Material.MONSTER_EGG).mob(type);
				}
				catch(IllegalArgumentException e) {
					ib.type(Material.BARRIER);
				}
		}
		
		return ib.get();
	}
}
