package me.mrCookieSlime.QuestWorld.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.Translator;
import me.mrCookieSlime.QuestWorld.api.event.GenericPlayerLeaveEvent;

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
		if(inv.getResult() == null)
			return 0;
		
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
	
	private static Pattern keywordPattern = Pattern.compile("%(tellraw|title|subtitle|actionbar)%((?:(?!%(?:tellraw|title|subtitle|actionbar)%).)*)", Pattern.CASE_INSENSITIVE);
	public static void sendTranslation(CommandSender p, boolean prefixed, Translator key, String... replacements) {
		String text = makeTranslation(prefixed, key, replacements);
		if(text.isEmpty())
			return;
		
		int tellrawPos = text.indexOf("%tellraw%");
		int titlePos = text.indexOf("%title%");
		int subtitlePos = text.indexOf("%subtitle%");
		int actionbarPos = text.indexOf("%actionbar%");
		
		if((tellrawPos & titlePos & subtitlePos & actionbarPos) == -1) {
			p.sendMessage(text);
			return;
		}
		
		// negative bit
		int nb = ~(1 << 31);
		
		// index of first match, need to exclude non-matches (-1)
		int matchStart = Math.min(Math.min(tellrawPos & nb, titlePos & nb), Math.min(subtitlePos & nb, actionbarPos & nb));
		if(matchStart > 0)
			p.sendMessage(text.substring(0, matchStart));
		
		if(p instanceof Player) {
			Player player = (Player) p;
	
			String tellrawMessage = "";
			String titleMessage = "";
			String subtitleMessage = "";
			String actionbarMessage = "";
			
			Matcher matcher = keywordPattern.matcher(text.substring(matchStart));
			
			while(matcher.find()) {
				String type = matcher.group(1).toLowerCase();
				String message = matcher.group(2);
				switch(type) {
				case "tellraw":   tellrawMessage   += message; break;
				case "title":     titleMessage     += message; break;
				case "subtitle":  subtitleMessage  += message; break;
				case "actionbar": actionbarMessage += message; break;
				}
			}
			
			if(!tellrawMessage.isEmpty())
				tellraw(player, tellrawMessage);
			
			if(!titleMessage.isEmpty() || !subtitleMessage.isEmpty())
				title(player, titleMessage, subtitleMessage);
			
			if(!actionbarMessage.isEmpty())
				actionbar(player, actionbarMessage);
		}
	}
	
	public static String makeTranslation(boolean prefixed, Translator key, String... replacements) {
		String text = QuestWorld.translate(key, replacements);
		if(!text.isEmpty() && prefixed)
			text = QuestWorld.translate(Translation.DEFAULT_PREFIX) + text;

		return Text.deserializeColor(text);
	}
	
	private static ConversationFactory factory;
	public static ConversationFactory getConversationFactory() {
		if(factory == null)
			factory = new ConversationFactory(QuestWorld.getPlugin());
		return factory;
	}
	public static void promptInput(Player p, Prompt prompt) {
		prepareConversation(p, prompt).begin();
	}
	
	public static void promptCommand(Player p, Prompt prompt) {
		Conversation con = prepareConversation(p, prompt);
		p.sendMessage(prompt.getPromptText(con.getContext()));
		
		Bukkit.getPluginManager().registerEvents(commandListener(con, prompt), QuestWorld.getPlugin());
	}
	
	public static void promptInputOrCommand(Player p, Prompt prompt) {
		Conversation con = prepareConversation(p, prompt);
		
		Bukkit.getPluginManager().registerEvents(commandListener(con, prompt), QuestWorld.getPlugin());
		con.begin();
	}
	
	private static Listener commandListener(Conversation con, Prompt prompt) {
		Listener listener = new Listener() {
			@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
			public void onCommand(PlayerCommandPreprocessEvent event) {
				if(event.getPlayer() == con.getForWhom()) {
					event.setCancelled(true);
					if(prompt.acceptInput(con.getContext(), event.getMessage()) != Prompt.END_OF_CONVERSATION)
						con.getForWhom().sendRawMessage(prompt.getPromptText(con.getContext()));
					else
						con.abandon();
				}
			}
			
			@EventHandler
			public void onLeave(GenericPlayerLeaveEvent event) {
				con.abandon();
			}
		};
		
		con.addConversationAbandonedListener(new ConversationAbandonedListener() {
			@Override
			public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
				HandlerList.unregisterAll(listener);
			}
		});
		
		return listener;
	}
	
	private static Conversation prepareConversation(Player p, Prompt prompt) {
		return getConversationFactory()
				.withLocalEcho(false).withModality(false)
				.withFirstPrompt(prompt).buildConversation(p);
	}
	
	public static boolean checkPermission(Player p, String permission) {
		return permission == null || permission.length() == 0 || p.hasPermission(permission.split(" ", 2)[0]);
	}
	
	public static void tellraw(Player p, String json) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:tellraw "+p.getName()+" "+json);
	}
	
	public static void title(Player p, String titleJson, String subtitleJson) {
		if(!subtitleJson.isEmpty())
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:title "+p.getName()+" subtitle "+subtitleJson);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:title "+p.getName()+" title "+titleJson);
	}
	
	public static void actionbar(Player p, String json) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:title "+p.getName()+" actionbar "+json);
	}
	
	@SuppressWarnings("deprecation")
	public static Player getPlayer(String name) {
		return Bukkit.getPlayerExact(name);
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
