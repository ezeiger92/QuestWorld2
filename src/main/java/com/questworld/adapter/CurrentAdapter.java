package com.questworld.adapter;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.questworld.util.ItemBuilder;
import com.questworld.util.Text;
import com.questworld.util.Version;
import com.questworld.util.adapter.Action;
import com.questworld.util.adapter.VersionAdapter;

public class CurrentAdapter extends VersionAdapter {
	public CurrentAdapter() {
		super(Version.current());
	}

	@Implementing(Action.MAKE_PLAYER_HEAD)
	public void makePlayerHead(ItemStack result, OfflinePlayer player) {
		result.setType(Material.PLAYER_HEAD);
		
		if (result.getItemMeta() instanceof SkullMeta) {
			SkullMeta meta = (SkullMeta) result.getItemMeta();
			meta.setOwningPlayer(player);
			result.setItemMeta(meta);
		}
	}

	@Implementing(Action.SEND_ACTIONBAR)
	public void sendActionbar(Player player, String message) {
		player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
				net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message));
	}
	
	@Implementing(Action.SEND_TITLE)
	public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		player.sendTitle(Text.colorize(title), Text.colorize(subtitle), fadeIn, stay, fadeOut);
	}
	
	@Implementing(Action.MAKE_SPAWN_EGG)
	public void makeSpawnEgg(ItemStack stack, EntityType type) {
		Material egg = Material.matchMaterial(type.name() + "_SPAWN_EGG");
		
		if(egg != null) {
			stack.setType(egg);
		}
		else {
			ItemBuilder.edit(stack).type(null);
		}
	}
}
