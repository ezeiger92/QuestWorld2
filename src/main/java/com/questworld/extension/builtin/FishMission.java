package com.questworld.extension.builtin;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import com.questworld.api.Decaying;
import com.questworld.api.MissionType;
import com.questworld.api.QuestWorld;
import com.questworld.api.contract.IMission;
import com.questworld.api.contract.IMissionState;
import com.questworld.api.contract.MissionEntry;
import com.questworld.api.menu.MissionButton;
import com.questworld.util.ItemBuilder;
import com.questworld.util.Text;

public class FishMission extends MissionType implements Listener, Decaying {
	public FishMission() {
		super("FISH", true);
	}

	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return instance.getItem();
	}

	@Override
	protected String userInstanceDescription(IMission instance) {
		return "&7Reel in " + instance.getAmount() + "x " + Text.itemName(instance.getDisplayItem());
	}

	@EventHandler
	public void onFish(PlayerFishEvent e) {
		if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH)
			return;

		ItemStack caught = ((Item) e.getCaught()).getItemStack();

		for (MissionEntry r : QuestWorld.getMissionEntries(this, e.getPlayer()))
			if (ItemBuilder.compareItems(caught, r.getMission().getItem()))
				r.addProgress(caught.getAmount());
	}

	@Override
	protected void layoutMenu(IMissionState changes) {
		putButton(10, MissionButton.item(changes));
		putButton(17, MissionButton.amount(changes));
	}
}
