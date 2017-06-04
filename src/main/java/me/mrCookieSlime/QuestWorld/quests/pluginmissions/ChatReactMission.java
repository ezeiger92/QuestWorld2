package me.mrCookieSlime.QuestWorld.quests.pluginmissions;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.clip.chatreaction.events.ReactionWinEvent;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.quests.QuestChecker;
import me.mrCookieSlime.QuestWorld.quests.QuestListener;
import me.mrCookieSlime.QuestWorld.quests.QuestManager;
import me.mrCookieSlime.QuestWorld.quests.Mission;

public class ChatReactMission extends MissionType implements Listener {
	public ChatReactMission() {
		super("CHATREACTION_WIN", true, false, false, SubmissionType.INTEGER, new MaterialData(Material.DIAMOND));
	}
	
	@Override
	public ItemStack displayItem(IMission instance) {
		return new CustomItem(Material.COMMAND, "&7" + instance.getAmount(), 0);
	}
	
	@Override
	protected String displayString(IMission instance) {
		String games = " Games";
		if(instance.getAmount() == 1)
			games = " Game";
		
		return "&7Win " + instance.getAmount() + games + " of ChatReaction";
	}
	
	@EventHandler
	public void onWin(ReactionWinEvent e) {
		Player p = e.getWinner();
		
		QuestChecker.check(p, e, "CHATREACTION_WIN", new QuestListener() {
			
			@Override
			public void onProgressCheck(Player p, QuestManager manager, Mission task, Object event) {
				manager.addProgress(task, 1);
			}
		});
	}
}
