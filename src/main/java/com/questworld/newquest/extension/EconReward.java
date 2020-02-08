package com.questworld.newquest.extension;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.questworld.newquest.NodeConfig;
import com.questworld.newquest.Profile;
import com.questworld.newquest.Reward;
import com.questworld.util.BukkitService;

import net.milkbowl.vault.economy.Economy;

public class EconReward extends Reward {

	protected EconReward() {
		super(RewardKey("money"));
	}

	@Override
	public boolean apply(NodeConfig<BaseProperties> config, Profile profile) {
		Properties props = config.deserialize(Properties.class);
		
		Economy econ = BukkitService.find(Economy.class).orElse(null);
		OfflinePlayer player = Bukkit.getOfflinePlayer(profile.getUniqueId());
		
		if(econ == null || !player.hasPlayedBefore()) {
			return false;
		}
		
		if(!econ.hasAccount(player) && !econ.createPlayerAccount(player)) {
			return false;
		}
		
		return econ.depositPlayer(player, props.amount).transactionSuccess();
	}
	
	private static class Properties extends BaseProperties {
		public double amount = 0;
	}

}
