package com.questworld.extension.builtin;

import java.util.Arrays;
import java.util.EnumMap;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.questworld.api.MissionType;
import com.questworld.api.QuestWorld;
import com.questworld.api.contract.IMission;
import com.questworld.api.contract.IMissionState;
import com.questworld.api.contract.MissionEntry;
import com.questworld.api.menu.MissionButton;
import com.questworld.util.ItemBuilder;
import com.questworld.util.Text;

public class HarvestMission extends MissionType {

	private final EnumMap<Material, Material> crops = new EnumMap<>(Material.class);
	
	public HarvestMission() {
		super("HARVEST", true, new ItemStack(Material.WHEAT));
		addCrop(Material.WHEAT, Material.WHEAT_SEEDS);
		addCrop(Material.BEETROOT, Material.BEETROOT_SEEDS, Material.BEETROOTS);
		addCrop(Material.POTATO, Material.POTATOES);
		addCrop(Material.CARROT, Material.CARROTS);
		addCrop(Material.NETHER_WART);
		addCrop(Material.COCOA_BEANS, Material.COCOA);
		addCrop(Material.SWEET_BERRIES, Material.SWEET_BERRY_BUSH);
	}

	@Override
	protected String userInstanceDescription(IMission instance) {
		return "&7Harvest " + instance.getAmount() + "x " + Text.niceName(userDisplayItem(instance).getType().name());
	}
	
	public void addCrop(Material crop, Iterable<Material> aliases) {
		for(Material m : aliases) {
			crops.put(m, crop);
		}
		
		crops.put(crop, crop);
	}
	
	public void addCrop(Material crop, Material... aliases) {
		addCrop(crop, Arrays.asList(aliases));
	}
	
	public void clearCrops() {
		crops.clear();
	}
	
	private static boolean isCropGrown(BlockState state) {
		if(state instanceof Ageable) {
			Ageable crop = (Ageable) state;
			
			return crop.getAge() == crop.getMaximumAge();
		}
		
		return false;
	}

	@Override
	public ItemStack userDisplayItem(IMission instance) {
		Material crop = crops.get(instance.getItem().getType());
		if(crop == null)
			crop = Material.BARRIER;
			
		return new ItemStack(crop);
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onBlockBreak(BlockBreakEvent event) {
		
		if(isCropGrown(event.getBlock().getState())) {
			Material crop = crops.get(event.getBlock().getType());
			
			for(MissionEntry r : QuestWorld.getMissionEntries(this, event.getPlayer()))
				if(crop == r.getMission().getItem().getType())
					r.addProgress(1);
		}
	}
	
	@Override
	public void validate(IMissionState missionState) {
		if(!crops.containsKey(missionState.getItem().getType())) {
			missionState.setItem(new ItemStack(Material.WHEAT));
			missionState.apply();
		}
	}
	
	@Override
	protected void layoutMenu(IMissionState changes) {
		putButton(10, MissionButton.simpleButton(
				changes,
				new ItemBuilder(changes.getDisplayItem()).wrapLore(
						"",
						"&e> Click to change the Crop to the Item you are currently holding").get(),
				event -> {
					ItemStack hand = event.getWhoClicked().getInventory().getItemInMainHand();
					if(hand == null)
						return;
					
					Material crop = crops.get(hand.getType());
					if(crop != null)
						changes.setItem(new ItemStack(crop));
				}
		));
		putButton(17, MissionButton.amount(changes));
	}
}
