package com.questworld.util.version;

import org.bukkit.CropState;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

import com.questworld.util.Reflect;
import com.questworld.util.Version;
import com.questworld.util.Versions;

public class ObjectMap {
	private static final Version pre13 = Versions.v1_12_2.getTaco();
	private static final Version pre19 = Versions.v1_8_R3.getTaco();
	
	public static class VDItemStack {
		private static final ItemStack rev(String newName, String oldName, int data) {
			ItemStack result;
			if(Reflect.getVersion().compareTo(pre13) >= 0) {
				result = new ItemStack(Material.matchMaterial(oldName));
				Reflect.getAdapter().setItemDamage(result, data);
			}
			else {
				result = new ItemStack(Material.matchMaterial(newName));
			}
			
			return result;
		}
		
		public static final ItemStack getSkeletonSkull() {
			return new ItemStack(Material.SKELETON_SKULL);
		}
		
		public static final ItemStack getWitherSkull() {
			return rev("WITHER_SKELETON_SKULL", "SKULL_ITEM", 1);
		}
		
		public static final ItemStack getZombieHead() {
			return rev("ZOMBIE_HEAD", "SKULL_ITEM", 2);
		}
		
		public static final ItemStack getPlayerHead() {
			return rev("PLAYER_HEAD", "SKULL_ITEM", 3);
		}
		
		public static final ItemStack getCreeperHead() {
			return rev("CREEPER_HEAD", "SKULL_ITEM", 4);
		}
		
		public static final ItemStack getDragonHead() {
			if(Reflect.getVersion().compareTo(pre19) <= 0) {
				return new ItemStack(Material.DRAGON_EGG);
			}
			else {
				return rev("DRAGON_HEAD", "SKULL_ITEM", 5);
			}
		}
		
		public static final ItemStack getRedWool() {
			return rev("RED_WOOL", "WOOL", 14);
		}
		
		public static final ItemStack getLimeWool() {
			return rev("LIME_WOOL", "WOOL", 5);
		}
		
		public static final ItemStack getGreenWool() {
			return rev("GREEN_WOOL", "WOOL", 2);
		}
		
		public static final ItemStack getRedGlassPane() {
			return rev("RED_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", 14);
		}
		
		public static final ItemStack getPurpleGlassPane() {
			return rev("PURPLE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", 10);
		}
		
		public static final ItemStack getYellowGlassPane() {
			return rev("YELLOW_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", 4);
		}
		
		public static final ItemStack getGrayGlassPane() {
			return rev("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", 7);
		}
	}
	
	public static class VDStatistic {

		public static final Statistic PLAY_ONE_MINUTE = Statistic.valueOf(VersionDependent.pick(pre13, "PLAY_ONE_TICK", "PLAY_ONE_MINUTE"));
	}
	
	public static class VDBlock {
		public static final boolean isCropGrown(Block cropBlock) {
			if(Reflect.getVersion().compareTo(pre13) >= 0) {
				return cropGrown112(cropBlock);
			}
			else {
				return cropGrown113(cropBlock);
			}
		}
		
		private static final boolean cropGrown113(Block cropBlock) {
			BlockData data = cropBlock.getBlockData();
			
			return data instanceof Ageable && ((Ageable)data).getAge() == ((Ageable)data).getMaximumAge();
		}
		
		@Deprecated // Uses pre-1.13 api for pre-1.13 support
		private static final boolean cropGrown112(Block cropBlock) {
			org.bukkit.material.MaterialData data = cropBlock.getState().getData();
			
			return data instanceof org.bukkit.material.Crops && ((org.bukkit.material.Crops)data).getState() == CropState.RIPE;
		}
	}
}
