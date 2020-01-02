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
