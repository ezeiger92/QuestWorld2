package me.mrCookieSlime.QuestWorld.hooks.chatreaction;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.clip.chatreaction.events.ReactionWinEvent;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;

public class ChatReactMission extends MissionType implements Listener {
	public ChatReactMission() {
		super("CHATREACTION_WIN", true, false, new MaterialData(Material.DIAMOND));
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
		QuestWorld.getInstance().getManager(p).forEachTaskOf(this, mission -> true);
	}
	
	@Override
	protected void layoutMenu(MissionChange changes) {
		super.layoutMenu(changes);
		putButton(17, MissionButton.amount(changes));
	}
}
