package com.questworld.newquest.extension;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.questworld.newquest.NodeConfig;
import com.questworld.newquest.Profile;
import com.questworld.newquest.Reward;

public class ItemReward extends Reward {
	protected ItemReward() {
		super(RewardKey("item"));
	}

	@Override
	public boolean apply(NodeConfig<Reward.Properties> config, Profile profile) {
		Properties props = config.deserialize(Properties.class);
		
		Player player = Bukkit.getPlayer(profile.getUniqueId());
		
		if(player == null) {
			return false;
		}
		
		player.getInventory().addItem(props.item.clone());
		
		return true;
	}

	
	private static class Properties extends Reward.Properties {
		public ItemStack item = new ItemStack(Material.GOLD_NUGGET);
	}
}
