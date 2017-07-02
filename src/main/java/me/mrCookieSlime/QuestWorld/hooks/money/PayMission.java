package me.mrCookieSlime.QuestWorld.hooks.money;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Manual;
import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.managers.PlayerManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class PayMission extends MissionType implements Manual {

	public PayMission() {
		super("GIVE_MONEY", false, false, new MaterialData(Material.GOLD_INGOT));
	}

	@Override
	protected String displayString(IMission instance) {
		String currency = "dollars,1:dollar";
		if(instance.getName() != null)
			currency = instance.getName();
		
		currency = MoneyHook.formatCurrency(currency, instance.getAmount());
		
		return "&7Give " + instance.getAmount() + " " + currency;
	}

	@Override
	public ItemStack displayItem(IMission instance) {
		return getSelectorItem().toItemStack(1);
	}
	
	@Override
	protected void layoutMenu(MissionChange changes) {
		putButton(17, MissionButton.amount(changes, 50));
	}

	@Override
	public int onManual(PlayerManager manager, IMission mission) {
		Player p = Bukkit.getPlayer(manager.getUUID());
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
