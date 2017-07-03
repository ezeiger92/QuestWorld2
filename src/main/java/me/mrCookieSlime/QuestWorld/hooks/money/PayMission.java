package me.mrCookieSlime.QuestWorld.hooks.money;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Manual;
import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class PayMission extends MissionType implements Manual {

	public PayMission() {
		super("GIVE_MONEY", false, false, new ItemStack(Material.GOLD_INGOT));
	}

	@Override
	protected String userInstanceDescription(IMission instance) {
		String currency = "dollars,1:dollar";
		if(instance.getName() != null)
			currency = instance.getName();
		
		currency = MoneyHook.formatCurrency(currency, instance.getAmount());
		
		return "&7Give " + instance.getAmount() + " " + currency;
	}

	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return getSelectorItem().clone();
	}
	
	@Override
	protected void layoutMenu(MissionChange changes) {
		putButton(17, MissionButton.amount(changes, 50));
	}

	@Override
	public int onManual(Player p, IMission mission) {
		Economy e = QuestWorld.getInstance().getEconomy();
		if(e.hasAccount(p)) {
			double d = e.getBalance(p);
			if((int)d > 0) {
				EconomyResponse r = e.withdrawPlayer(p, "Quest Payment", (double)mission.getAmount());
				if(r.type == EconomyResponse.ResponseType.SUCCESS)
					return mission.getAmount() - (int)Math.round(r.amount);
			}
		}
		
		QuestWorld.getSounds().MissionReject().playTo(p);
		return FAIL;
	}

	@Override
	public String getLabel() {
		return "Payment";
	}

}
