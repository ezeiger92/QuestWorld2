package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
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

public class FishMission extends MissionType implements Listener {
	public FishMission() {
		super("FISH", true, true, false, SubmissionType.ITEM, new MaterialData(Material.FISHING_ROD));
	}
	
	@Override
	public ItemStack displayItem(IMission instance) {
		return instance.getMissionItem().clone();
	}
	
	@Override
	protected String displayString(IMission instance) {
		return "&7Fish up " + instance.getAmount() + "x " + StringUtils.formatItemName(instance.getDisplayItem(), false);
	}
	
	@EventHandler
	public void onFish(final PlayerFishEvent e) {
		if (!(e.getCaught() instanceof Item)) return;
		
		QuestChecker.check(e.getPlayer(), e, "FISH", new QuestListener() {
			
			@Override
			public void onProgressCheck(Player p, QuestManager manager, Mission task, Object event) {
				if (QuestWorld.getInstance().isItemSimiliar(((Item) e.getCaught()).getItemStack(), task.getMissionItem())) manager.addProgress(task, ((Item) e.getCaught()).getItemStack().getAmount());
			}
		});
	}
}
