package com.questworld.extension.builtin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import com.questworld.api.Decaying;
import com.questworld.api.MissionType;
import com.questworld.api.QuestWorld;
import com.questworld.api.contract.IMission;
import com.questworld.api.contract.IMissionState;
import com.questworld.api.contract.MissionEntry;
import com.questworld.api.menu.MissionButton;
import com.questworld.util.ItemBuilder;
import com.questworld.util.PlayerTools;
import com.questworld.util.Text;

public class CraftMission extends MissionType implements Listener, Decaying {
	public CraftMission() {
		super("CRAFT", true, new ItemStack(Material.CRAFTING_TABLE));
	}

	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return instance.getItem();
	}

	@Override
	protected String userInstanceDescription(IMission instance) {
		return "&7Craft " + instance.getAmount() + "x " + Text.itemName(instance.getDisplayItem());
	}

	@EventHandler
	public void onCraft(CraftItemEvent e) {
		ItemStack test = e.getRecipe().getResult().clone();
		ClickType click = e.getClick();

		int recipeAmount = test.getAmount();

		switch (click) {
			case NUMBER_KEY:
				// If hotbar slot selected is full, crafting fails (vanilla behavior, even when
				// items match)
				if (e.getWhoClicked().getInventory().getItem(e.getHotbarButton()) != null)
					recipeAmount = 0;
				break;

			case DROP:
			case CONTROL_DROP:
				// If we are holding items, craft-via-drop fails (vanilla behavior)
				ItemStack cursor = e.getCursor();
				// Apparently, rather than null, an empty cursor is AIR. I don't think that's
				// intended.
				if (cursor != null && cursor.getType() != Material.AIR)
					recipeAmount = 0;
				break;

			case SHIFT_RIGHT:
			case SHIFT_LEFT:
				// Fixes ezeiger92/QuestWorld2#40
				if (recipeAmount == 0)
					break;

				int maxCraftable = PlayerTools.getMaxCraftAmount(e.getInventory());
				int capacity = PlayerTools.fits(test, e.getView().getBottomInventory());

				// If we can't fit everything, increase "space" to include the items dropped by
				// crafting
				// (Think: Uncrafting 8 iron blocks into 1 slot)
				if (capacity < maxCraftable)
					maxCraftable = ((capacity + recipeAmount - 1) / recipeAmount) * recipeAmount;

				recipeAmount = maxCraftable;
				break;
			default:
		}

		// No use continuing if we haven't actually crafted a thing
		if (recipeAmount == 0)
			return;

		test.setAmount(recipeAmount);

		Player player = (Player) e.getWhoClicked();

		for (MissionEntry r : QuestWorld.getMissionEntries(this, player))
			if (ItemBuilder.compareItems(test, r.getMission().getItem()))
				r.addProgress(test.getAmount());
	}

	@Override
	protected void layoutMenu(IMissionState changes) {
		putButton(10, MissionButton.item(changes));
		putButton(17, MissionButton.amount(changes));
	}
}
