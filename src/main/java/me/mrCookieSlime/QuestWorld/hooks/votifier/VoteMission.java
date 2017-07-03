package me.mrCookieSlime.QuestWorld.hooks.votifier;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.vexsoftware.votifier.model.VotifierEvent;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.utils.PlayerTools;

public class VoteMission extends MissionType implements Listener {
	public VoteMission() {
		super("VOTIFIER_VOTE", true, false, new ItemStack(Material.PAPER));
	}
	
	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return new CustomItem(Material.COMMAND, "&7" + instance.getAmount(), 0);
	}
	
	@Override
	protected String userInstanceDescription(IMission instance) {
		return "&7Vote " + instance.getAmount() + " times";
	}
	
	@EventHandler
	public void onVote(VotifierEvent e) {
		Player p = PlayerTools.getPlayer(e.getVote().getUsername());
		if (p != null)
			QuestWorld.getInstance().getManager(p).forEachTaskOf(this, mission -> true);
	}
	
	@Override
	protected void layoutMenu(MissionChange changes) {
		super.layoutMenu(changes);
		putButton(17, MissionButton.amount(changes));
	}
}
