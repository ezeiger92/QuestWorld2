package me.mrCookieSlime.QuestWorld.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Monster;
import org.bukkit.entity.WaterMob;
import org.bukkit.inventory.ItemStack;

public class EntityTools {
	private static EntityType[] alive;
	static {
		// Alive entities

		List<EntityType> entities = new ArrayList<>();
		for(EntityType ent : EntityType.values()) {
			Class<? extends Entity> clazz = ent.getEntityClass();
			if(Monster.class.isAssignableFrom(clazz)
			|| Animals.class.isAssignableFrom(clazz)
			|| WaterMob.class.isAssignableFrom(clazz)
			|| Flying.class.isAssignableFrom(clazz)
			|| ComplexLivingEntity.class.isAssignableFrom(clazz)
			)
				entities.add(ent);
		}
		
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
	
	public static ItemStack getEgg(EntityType type) {
		ItemBuilder ib = new ItemBuilder(Material.MONSTER_EGG);
		
		try { 
			ib.mob(type);
		}
		catch(IllegalArgumentException e) {
			switch(type) {
			case PLAYER:
				ib.type(Material.SKULL_ITEM).skull(SkullType.PLAYER);
			default:
				ib.type(Material.BARRIER);
			}
		}
		
		return ib.get();
	}
}
