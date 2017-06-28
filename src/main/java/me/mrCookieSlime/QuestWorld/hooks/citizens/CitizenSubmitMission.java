package me.mrCookieSlime.QuestWorld.hooks.citizens;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.api.menu.MenuData;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;
import me.mrCookieSlime.QuestWorld.utils.PlayerTools;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class CitizenSubmitMission extends MissionType implements Listener {
	public CitizenSubmitMission() {
		super("CITIZENS_SUBMIT", false, false, new MaterialData(Material.EMERALD));
	}
	
	@Override
	public ItemStack displayItem(IMission instance) {
		return instance.getMissionItem().clone();
	}
	
	@Override
	protected String displayString(IMission instance) {
		String name = "N/A";
		NPC npc = CitizensHook.npcFrom(instance);
		if(npc != null)
			name = npc.getName();
		
		return "&7Give " + instance.getAmount() + "x " + StringUtils.formatItemName(instance.getDisplayItem(), false) + " to " + name;
	}
	
	@EventHandler
	public void onInteract(NPCRightClickEvent e){
		QuestWorld.getInstance().getManager(e.getClicker()).forEachTaskOf(this, mission -> {
			Player p = e.getClicker();
			ItemStack hand = PlayerTools.getMainHandItem(p);
			if(mission.getCustomInt() != e.getNPC().getId()
					|| !hand.isSimilar(mission.getMissionItem()))
				return false;
			
			int rest = QuestWorld.getInstance().getManager(p).addProgress(mission, hand.getAmount());
			if (rest > 0) hand.setAmount(rest);// PlayerTools.setActiveHandItem(p, new ItemStack(PlayerTools.getMainHandItem(p), rest));
			else p.getInventory().setItemInMainHand(null); //PlayerTools.setActiveHandItem(p, null);
			
			// TODO Manually handled, should not be
			return false;
		});
	}
	
	@Override
	protected void layoutMenu(MissionChange changes) {
		super.layoutMenu(changes);
		NPC npc = CitizensHook.npcFrom(changes);
		putButton(10, new MenuData(
				new ItemBuilder(Material.NAME_TAG).display("&dCitizen &f#" + changes.getCustomInt()).lore(
						"&7Name: &r" + (npc != null ? npc.getName(): "&4N/A"),
						"",
						"&e> Click to change the selected NPC").get(),
				MissionButton.simpleHandler(changes, event -> {
					Player p = (Player)event.getWhoClicked();
					PlayerTools.sendTranslation(p, true, CitizenTranslation.citizen_l);
					CitizensHook.link.put(p.getUniqueId(), changes.getSource());
					p.closeInventory();
				})
		));
		putButton(11, MissionButton.item(changes));
		putButton(17, MissionButton.amount(changes));
	}
}
