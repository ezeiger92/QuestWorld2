package me.mrCookieSlime.QuestWorld.hooks.builtin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.quests.QuestChecker;
import me.mrCookieSlime.QuestWorld.quests.QuestListener;
import me.mrCookieSlime.QuestWorld.quests.QuestManager;
import me.mrCookieSlime.QuestWorld.quests.Mission;

public class CraftMission extends MissionType implements Listener {
	public CraftMission() {
		super("CRAFT", true, true, false, SubmissionType.ITEM, new MaterialData(Material.WORKBENCH));
	}
	
	@Override
	public ItemStack displayItem(IMission instance) {
		return instance.getMissionItem().clone();
	}
	
	@Override
	protected String displayString(IMission instance) {
		return "&7Craft " + instance.getAmount() + "x " + StringUtils.formatItemName(instance.getDisplayItem(), false);
	}

	@EventHandler
	public void onCraft(CraftItemEvent e) {
		QuestChecker.check((Player) e.getWhoClicked(), e, "CRAFT", new QuestListener() {
			
			@Override
			public void onProgressCheck(Player p, QuestManager manager, Mission task, Object event) {
				if (QuestWorld.getInstance().isItemSimiliar(e.getRecipe().getResult(), task.getMissionItem())) manager.addProgress(task, e.getCurrentItem().getAmount());
			}
		});
	}
}