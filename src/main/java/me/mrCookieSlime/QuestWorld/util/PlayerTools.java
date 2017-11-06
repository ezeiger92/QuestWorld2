package me.mrCookieSlime.QuestWorld.util;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.Translator;

public class PlayerTools {
	public static ItemStack getActiveHandItem(Player p) {
		org.bukkit.inventory.PlayerInventory pi = p.getInventory();
		
		ItemStack result = pi.getItemInMainHand();
		if(result == null)
			result = pi.getItemInOffHand();
		
		return result;
	}
	
	public static ItemStack getMainHandItem(Player p) {
		return p.getInventory().getItemInMainHand();
	}
	
	public static int getMaxCraftAmount(CraftingInventory inv) {
		int resultCount = inv.getResult().getAmount();
		int materialCount = Integer.MAX_VALUE;
		
		for(ItemStack is : inv.getMatrix())
			if(is != null && is.getAmount() < materialCount)
				materialCount = is.getAmount();

		return resultCount * materialCount;
	}
	
	public static int fits(ItemStack stack, Inventory inv) {
		ItemStack[] contents = inv.getStorageContents();
		int result = 0;
		
		for(ItemStack is : contents)
			if(is == null)
				result += stack.getMaxStackSize();
			else if(is.isSimilar(stack))
				result += Math.max(stack.getMaxStackSize() - is.getAmount(), 0);
		
		return result;
	}
	
	public static void setActiveHandItem(Player p, ItemStack is) {
		org.bukkit.inventory.PlayerInventory pi = p.getInventory();
		
		if(pi.getItemInMainHand() == null && pi.getItemInOffHand() != null)
			pi.setItemInOffHand(is);
		else
			pi.setItemInMainHand(is);
	}
	
	public static void sendTranslation(CommandSender p, boolean prefixed, Translator key, String... replacements) {
		String text = makeTranslation(prefixed, key, replacements);
		if(text.isEmpty())
			return;
		
		p.sendMessage(text);
	}
	
	public static String makeTranslation(boolean prefixed, Translator key, String... replacements) {
		String text = QuestWorld.translate(key, replacements);
		if(!text.isEmpty() && prefixed)
			text = QuestWorld.translate(Translation.DEFAULT_PREFIX) + text;

		return Text.colorize(text);
	}
	
	private static ConversationFactory factory;
	public static ConversationFactory getConversationFactory() {
		if(factory == null)
			factory = new ConversationFactory(QuestWorld.get());
		return factory;
	}
	public static void promptInput(Player p, Prompt prompt) {
		getConversationFactory()
		.withLocalEcho(false).withModality(false)
		.withFirstPrompt(prompt).buildConversation(p).begin();
	}
	
	public static void promptCommand(Player p, Prompt prompt) {
		Conversation con = getConversationFactory()
				.withLocalEcho(false).withModality(false)
				.withFirstPrompt(prompt).buildConversation(p);
		p.sendMessage(prompt.getPromptText(con.getContext()));
		
		Bukkit.getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onCommand(PlayerCommandPreprocessEvent event) {
				if(!event.getPlayer().getUniqueId().equals(p.getUniqueId()))
					return;

				event.setCancelled(true);
				if(prompt.acceptInput(con.getContext(), event.getMessage()) != Prompt.END_OF_CONVERSATION) {
					p.sendMessage(prompt.getPromptText(con.getContext()));
				}
				else
					HandlerList.unregisterAll(this);
			}
			
			@EventHandler
			public void onLeave(PlayerQuitEvent event) {
				HandlerList.unregisterAll(this);
			}
		}, QuestWorld.get());
	}
	
	public static boolean checkPermission(Player p, String permission) {
		return permission == null || permission.length() == 0 || p.hasPermission(permission);
	}
	
	public static void tellraw(Player p, String json, String... extra) {
		if(extra.length > 0)
			json = "[" + json + "," + String.join(",", extra) + "]";
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:tellraw "+p.getName()+" "+json);
	}
	
	@SuppressWarnings("deprecation")
	public static Player getPlayer(String name) {
		return Bukkit.getPlayerExact(name);
	}
	
	public static void closeInventoryWithEvent(Player p) {
		Bukkit.getPluginManager().callEvent(new InventoryCloseEvent(p.getOpenInventory()));
		p.closeInventory();
	}

	private static boolean noexceptPick = true;
	public static ItemStack getStackOf(Block block) {
		if(noexceptPick)
			try {
				return Reflect.nmsPickBlock(block);
			}
			catch(Exception e) {
				noexceptPick = false;
				Log.warning("Failed to reflect \"pickBlock\" method, QuestWorld was not fully prepared for your minecraft version");
				Log.warning("Falling back to MaterialData comparison for all future checks. Mining quests may not detect blocks as accurately");
			}
		return block.getState().getData().toItemStack(1);
	}
}
