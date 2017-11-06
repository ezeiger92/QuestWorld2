package me.mrCookieSlime.QuestWorld.extension.builtin;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Decaying;
import me.mrCookieSlime.QuestWorld.api.Manual;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionState;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.manager.PlayerManager;
import me.mrCookieSlime.QuestWorld.util.Text;

public class FishMission extends MissionType implements Listener, Decaying {
	public FishMission() {
		super("FISH", true, new ItemStack(Material.FISHING_ROD));
	}
	
	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return instance.getMissionItem();
	}
	
	@Override
	protected String userInstanceDescription(IMission instance) {
		return "&7Fish up " + instance.getAmount() + "x " + Text.itemName(instance.getDisplayItem());
	}
	
	@EventHandler
	public void onFish(PlayerFishEvent e) {
		if (!(e.getCaught() instanceof Item)) return;
		ItemStack caught = ((Item)e.getCaught()).getItemStack();

		PlayerManager.of(e.getPlayer()).forEachTaskOf(this, (mission, needed) -> {
			if(QuestWorld.get().isItemSimiliar(caught, mission.getMissionItem()))
				return caught.getAmount();
			
			return Manual.FAIL;
		}, false);
	}
	
	@Override
	protected void layoutMenu(IMissionState changes) {
		super.layoutMenu(changes);
		putButton(10, MissionButton.item(changes));
		putButton(17, MissionButton.amount(changes));
	}
}
