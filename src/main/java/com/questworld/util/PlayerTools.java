package com.questworld.util;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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

import com.questworld.api.QuestWorld;
import com.questworld.api.Translation;
import com.questworld.api.Translator;
import com.questworld.api.event.GenericPlayerLeaveEvent;
import com.questworld.util.json.JsonBlob;

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
	
	private static Pattern keywordPattern = Pattern.compile("\\s*%(tellraw|title|subtitle|actionbar)%\\s*((?:(?!%(?:tellraw|title|subtitle|actionbar)%).)*)");
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
	
			StringBuilder tellrawBuilder = new StringBuilder();
			StringBuilder titleBuilder = new StringBuilder();
			StringBuilder subtitleBuilder = new StringBuilder();
			StringBuilder actionbarBuilder = new StringBuilder();
			
			Matcher matcher = keywordPattern.matcher(text.substring(matchStart));
			
			while(matcher.find()) {
				String type = matcher.group(1);
				String message = matcher.group(2);
				switch(type) {
				case "tellraw":   tellrawBuilder.append(message); break;
				case "title":     titleBuilder.append(message); break;
				case "subtitle":  subtitleBuilder.append(message); break;
				case "actionbar": actionbarBuilder.append(message); break;
				
				// Won't happen unless keywordPattern changes
				default: break;
				}
			}
			
			String tellrawMessage = tellrawBuilder.toString();
			String titleMessage = titleBuilder.toString();
			String subtitleMessage = subtitleBuilder.toString();
			String actionbarMessage = actionbarBuilder.toString();
			
			if(!tellrawMessage.isEmpty())
				tellraw(player, tellrawMessage);
			
			if(!titleMessage.isEmpty() || !subtitleMessage.isEmpty())
				player.sendTitle(titleMessage, subtitleMessage, 10, 70, 20);
			
			if(!actionbarMessage.isEmpty())
				actionbar(player, actionbarMessage);
		}
	}
	
	public static String makeTranslation(boolean prefixed, Translator key, String... replacements) {
		String text = QuestWorld.translate(key, replacements);
		if(!text.isEmpty() && prefixed)
			text = QuestWorld.translate(Translation.DEFAULT_PREFIX) + text;

		return Text.colorize(text);
	}
	
	private static volatile ConversationFactory factory;
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
	
	private static class ConversationListener implements Listener, ConversationAbandonedListener {
		private Conversation con;
		private Prompt prompt;
		
		private ConversationListener(Conversation con, Prompt prompt) {
			this.con = con;
			this.prompt = prompt;
		}
		
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
		
		@Override
		public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
			HandlerList.unregisterAll(this);
		}
	}
	
	private static Listener commandListener(Conversation con, Prompt prompt) {
		ConversationListener listener = new ConversationListener(con, prompt);
		con.addConversationAbandonedListener(listener);
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
	
	private static final BiConsumer<Player, String> actionbarMethod;
	static {
		@SuppressWarnings("unused")
		Class<?> clazz;
		
		BiConsumer<Player, String> abMethod;
		try {
			Class.forName("org.bukkit.entity.Player$Spigot");
			abMethod = (p, m) -> p.spigot().sendMessage(
						net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
						net.md_5.bungee.api.chat.TextComponent.fromLegacyText(m));
		} catch (ClassNotFoundException e) {
			abMethod = (p, m) -> Bukkit.dispatchCommand(
					Bukkit.getConsoleSender(), 
					"minecraft:title "+p.getName()+" actionbar "+JsonBlob.fromLegacy(m).toString());
		}
		actionbarMethod = abMethod;
	}
	
	public static void actionbar(Player player, String message) {
		actionbarMethod.accept(player, message);
	}
	
	public static Player getPlayer(String name) {
		return Bukkit.getPlayerExact(name);
	}
	
	@SuppressWarnings("deprecation")
	public static Optional<UUID> findUUID(String nameOrUUID) {
		Player p = getPlayer(nameOrUUID);
		if(p != null)
			return Optional.of(p.getUniqueId());
		
		try {
			return Optional.of(UUID.fromString(nameOrUUID));
		}
		catch(IllegalArgumentException e) {
			return Optional.of(nameOrUUID)
					.filter(s -> {
						try {
							Integer.parseInt(nameOrUUID);
							return false;
						}
						catch(NumberFormatException e2) {
							return !s.equals("reset") && !s.equals("page");
						}
					})
					.map(Bukkit::getOfflinePlayer)
					.filter(OfflinePlayer::hasPlayedBefore)
					.map(OfflinePlayer::getUniqueId);
		}
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
