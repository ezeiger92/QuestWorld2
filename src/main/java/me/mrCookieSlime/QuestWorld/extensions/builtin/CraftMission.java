package me.mrCookieSlime.QuestWorld.extensions.builtin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.utils.PlayerTools;

public class CraftMission extends MissionType implements Listener {
	public CraftMission() {
		super("CRAFT", true, true, new ItemStack(Material.WORKBENCH));
	}
	
	@Override
	public ItemStack userDisplayItem(IMission instance) {
		
		return instance.getMissionItem().clone();
	}
	
	@Override
	protected String userInstanceDescription(IMission instance) {
		return "&7Craft " + instance.getAmount() + "x " + StringUtils.formatItemName(instance.getDisplayItem(), false);
	}

	@EventHandler
	public void onCraft(CraftItemEvent e) {
		ItemStack test = e.getRecipe().getResult().clone();
		ClickType click = e.getClick();
		
		int recipeAmount = test.getAmount();
		
		switch(click) {
		case NUMBER_KEY:
			// If hotbar slot selected is full, crafting fails (vanilla behavior, even when items match)
			if(e.getWhoClicked().getInventory().getItem(e.getHotbarButton()) != null)
				recipeAmount = 0;
			break;
			
		case DROP:
		case CONTROL_DROP:
			// If we are holding items, craft-via-drop fails (vanilla behavior)
			ItemStack cursor = e.getCursor();
			// Apparently, rather than null, an empty cursor is AIR. I don't think that's intended.
			if(cursor != null && cursor.getType() != Material.AIR)
				recipeAmount = 0;
			break;
			
		case SHIFT_RIGHT:
		case SHIFT_LEFT:
			int maxCraftable = PlayerTools.getMaxCraftAmount(e.getInventory());
			int capacity = PlayerTools.fits(test, e.getView().getBottomInventory());
			
			// If we can't fit everything, increase "space" to include the items dropped by crafting
			// (Think: Uncrafting 8 iron blocks into 1 slot)
			if(capacity < maxCraftable)
				maxCraftable = ((capacity + recipeAmount - 1) / recipeAmount) * recipeAmount;
			
			recipeAmount = maxCraftable;
			break;
		default:
		}
		
		// No use continuing if we haven't actually crafted a thing
		if(recipeAmount == 0)
			return;
		
		test.setAmount(recipeAmount);
		
		Player player = (Player)e.getWhoClicked();

		QuestWorld.getInstance().getManager(player).forEachTaskOf(this, mission -> {
			return QuestWorld.getInstance().isItemSimiliar(test, mission.getMissionItem());
		}, test.getAmount(), false);
	}
	
	@Override
	protected void layoutMenu(MissionChange changes) {
		super.layoutMenu(changes);
		putButton(10, MissionButton.item(changes));
		putButton(17, MissionButton.amount(changes));
	}
}
