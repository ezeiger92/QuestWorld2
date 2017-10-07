package me.mrCookieSlime.QuestWorld.extension.builtin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.menu.MenuData;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;
import me.mrCookieSlime.QuestWorld.util.PlayerTools;

public class MineMission extends MissionType implements Listener {
	public MineMission() {
		super("MINE_BLOCK", true, true, new ItemStack(Material.IRON_PICKAXE));
	}
	
	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return instance.getMissionItem().clone();
	}
	
	@Override
	protected String userInstanceDescription(IMission instance) {
		return "&7Mine " + instance.getAmount() + "x " + StringUtils.formatItemName(instance.getDisplayItem(), false);
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onMine(BlockBreakEvent e) {
		QuestWorld.getInstance().getManager(e.getPlayer()).forEachTaskOf(this, mission -> {
			ItemStack is = PlayerTools.getStackOf(e.getBlock());
			return is.isSimilar(mission.getMissionItem());
		});
	}
	
	@Override
	protected void layoutMenu(MissionChange changes) {
		super.layoutMenu(changes);
		putButton(10, new MenuData(
				new ItemBuilder(changes.getDisplayItem()).lore(
						"",
						"&e> Click to change the Block to",
						"&ethe Item you are currently holding").get(),
				event -> {
					Player p = (Player)event.getWhoClicked();
					ItemStack mainItem = p.getInventory().getItemInMainHand();
					if(mainItem != null && mainItem.getType().isBlock()) {
						mainItem = mainItem.clone();
						mainItem.setAmount(1);
						changes.setItem(mainItem);
					}
					MissionButton.apply(event, changes);
				}
		));
		putButton(17, MissionButton.amount(changes));
	}
}
