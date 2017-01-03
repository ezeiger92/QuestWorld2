package me.mrCookieSlime.QuestWorld.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.bukkit.entity.EntityType;

public class EntityTools {
	private static EntityType[] alive;
	static {
		// Alive entities

		List<EntityType> entities = new ArrayList<>();
		for(EntityType ent : EntityType.values())
			if(ent.isAlive() && ent != EntityType.PLAYER && ent != EntityType.ARMOR_STAND)
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
}
