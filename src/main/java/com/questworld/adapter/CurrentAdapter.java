package com.questworld.adapter;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.questworld.api.QuestWorld;
import com.questworld.util.Version;
import com.questworld.util.VersionAdapter;

public class CurrentAdapter extends VersionAdapter {
	public CurrentAdapter() {
		super(Version.current());
	}

	@Override
	public void makeSpawnEgg(ItemStack result, EntityType mob) {
		String name;
		
		switch(mob) {
		case PIG_ZOMBIE:
			name = "ZOMBIE_PIGMAN";
			break;
			
		case MUSHROOM_COW:
			name = "MOOSHROOM";
			break;
			
		default:
			name = mob.name();
			break;
		}
		
		Material m = Material.matchMaterial(name + "_SPAWN_EGG");
		
		if(m != null) {
			result.setType(m);
		}
		else {
			result.setType(Material.AIR);
		}
	}

	@Override
	public void makePlayerHead(ItemStack result, OfflinePlayer player) {
		result.setType(Material.PLAYER_HEAD);
		
		if (result.getItemMeta() instanceof SkullMeta) {
			SkullMeta meta = (SkullMeta) result.getItemMeta();
			meta.setOwningPlayer(player);
			result.setItemMeta(meta);
		}
	}

	@Override
	public ShapelessRecipe shapelessRecipe(String recipeName, ItemStack output) {
		return new ShapelessRecipe(new NamespacedKey(QuestWorld.getPlugin(), recipeName), output);
	}

	@Override
	public void sendActionbar(Player player, String message) {
		player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
				net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message));
	}
	
	@Override
	public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
	}
	
	@Override
	public void setItemDamage(ItemStack item, int damage) {
		ItemMeta meta;
		
		if(item.hasItemMeta()) {
			meta = item.getItemMeta();
		}
		else {
			meta = Bukkit.getItemFactory().getItemMeta(item.getType());
		}
		
		if(meta instanceof Damageable) {
			Damageable d = (Damageable) meta;
			d.setDamage(damage);
			
			item.setItemMeta(meta);
		}
	}
}
