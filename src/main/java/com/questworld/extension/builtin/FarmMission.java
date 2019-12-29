package com.questworld.extension.builtin;

import com.questworld.api.Decaying;
import com.questworld.api.MissionType;
import com.questworld.api.QuestWorld;
import com.questworld.api.contract.IMission;
import com.questworld.api.contract.IMissionState;
import com.questworld.api.contract.MissionEntry;
import com.questworld.api.menu.MissionButton;
import com.questworld.util.ItemBuilder;
import com.questworld.util.Text;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Optional;

public class FarmMission extends MissionType implements Listener, Decaying {

    public FarmMission() {
        super("FARM", true, new ItemStack(Material.IRON_HOE));
    }

    @Override
    public ItemStack userDisplayItem(IMission instance) {
        return instance.getItem();
    }

    @Override
    protected String userInstanceDescription(IMission instance) {
        return "&7Farm " + instance.getAmount() + "x " + Text.itemName(instance.getDisplayItem());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (e.getBlock().getBlockData() instanceof Ageable
            && ((Ageable) e.getBlock().getBlockData()).getAge() == ((Ageable) e.getBlock().getBlockData()).getMaximumAge()
        ) {
            Collection<ItemStack> drops = e.getBlock().getDrops(e.getPlayer().getInventory().getItemInMainHand());
            if (drops.isEmpty()) return;

            Optional<ItemStack> crop = drops.stream().filter(is -> !is.getType().name().endsWith("_SEEDS")).findFirst();
            if (!crop.isPresent()) return;

            for (MissionEntry r : QuestWorld.getMissionEntries(this, e.getPlayer())) {
                if (ItemBuilder.compareItems(crop.get(), r.getMission().getItem()))
                    r.addProgress(1);
            }
        }
    }

    @Override

    protected void layoutMenu(IMissionState changes) {
        putButton(10, MissionButton.item(changes));
        putButton(17, MissionButton.amount(changes));
    }
}
