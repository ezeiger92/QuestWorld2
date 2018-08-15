package com.questworld.util;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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

public class PlayerTools {

	public static ItemStack getMainHandItem(Player player) {
		int hand = player.getInventory().getHeldItemSlot();
		return player.getInventory().getItem(hand);
	}

	public static int getMaxCraftAmount(CraftingInventory inv) {
		if (inv.getResult() == null)
			return 0;

		int resultCount = inv.getResult().getAmount();
		int materialCount = Integer.MAX_VALUE;

		for (ItemStack is : inv.getMatrix())
			if (is != null && is.getAmount() < materialCount)
				materialCount = is.getAmount();

		return resultCount * materialCount;
	}

	public static int fits(ItemStack stack, Inventory inv) {
		ItemStack[] contents = inv.getContents();
		int result = 0;

		for (ItemStack is : contents)
			if (is == null)
				result += stack.getMaxStackSize();
			else if (is.isSimilar(stack))
				result += Math.max(stack.getMaxStackSize() - is.getAmount(), 0);

		return result;
	}

	private static Pattern keywordPattern = Pattern
			.compile("\\s*%(tellraw|title|subtitle|actionbar)%\\s*((?:(?!%(?:tellraw|title|subtitle|actionbar)%).)*)");

	public static void sendTranslation(CommandSender p, boolean prefixed, Translator key, String... replacements) {
		Player player = p instanceof Player ? (Player) p : null;

		String text = makeTranslation(prefixed, player, key, replacements);
		if (text.isEmpty())
			return;

		int tellrawPos = text.indexOf("%tellraw%");
		int titlePos = text.indexOf("%title%");
		int subtitlePos = text.indexOf("%subtitle%");
		int actionbarPos = text.indexOf("%actionbar%");

		if ((tellrawPos & titlePos & subtitlePos & actionbarPos) == -1) {
			p.sendMessage(text);
			return;
		}

		// sign bit zero'd
		int nb = -1 >>> 1;

		// Clear sign bits, forcing -1 -> INT_MAX, so that minimum value is positive
		// We found a match above, so this won't produce INT_MAX (unless the string is 2GB+ long)
		int matchStart = Math.min(Math.min(tellrawPos & nb, titlePos & nb),
				Math.min(subtitlePos & nb, actionbarPos & nb));
		
		if (matchStart > 0) {
			p.sendMessage(text.substring(0, matchStart));
		}

		if (player != null) {
			StringBuilder tellrawBuilder = new StringBuilder();
			StringBuilder titleBuilder = new StringBuilder();
			StringBuilder subtitleBuilder = new StringBuilder();
			StringBuilder actionbarBuilder = new StringBuilder();

			Matcher matcher = keywordPattern.matcher(text.substring(matchStart));

			while (matcher.find()) {
				String type = matcher.group(1);
				String message = matcher.group(2);
				switch (type) {
					case "tellraw":
						tellrawBuilder.append(message);
						break;
					case "title":
						titleBuilder.append(message);
						break;
					case "subtitle":
						subtitleBuilder.append(message);
						break;
					case "actionbar":
						actionbarBuilder.append(message);
						break;

					// Won't happen unless keywordPattern changes
					default:
						break;
				}
			}

			String tellrawMessage = tellrawBuilder.toString();
			String titleMessage = titleBuilder.toString();
			String subtitleMessage = subtitleBuilder.toString();
			String actionbarMessage = actionbarBuilder.toString();

			if (!tellrawMessage.isEmpty())
				tellraw(player, tellrawMessage);

			if (!titleMessage.isEmpty() || !subtitleMessage.isEmpty()) {
				Reflect.getAdapter().sendTitle(player, titleMessage, subtitleMessage, 10, 70, 20);
			}

			if (!actionbarMessage.isEmpty()) {
				Reflect.getAdapter().sendActionbar(player, actionbarMessage);
			}
		}
	}

	public static String makeTranslation(boolean prefixed, Translator key, String... replacements) {
		return makeTranslation(prefixed, null, key, replacements);
	}

	public static String makeTranslation(boolean prefixed, Player p, Translator key, String... replacements) {
		String text = QuestWorld.translate(p, key, replacements);
		if (!text.isEmpty() && prefixed)
			text = QuestWorld.translate(p, Translation.DEFAULT_PREFIX) + text;

		return Text.colorize(text);
	}
	
	private static final String excape(String json) {
		if(json != null) {
			return json.replace("\\", "\\\\").replace("\"", "\\\"");
		}
		
		return "";
	}
	
	private static final String BOOK_CHANNEL = "MC|BOpen";

	@SuppressWarnings("deprecation")
	public static void sendBookView(Player player, String jsonPage, String... extra) {
		StringBuilder pages = new StringBuilder("{pages:[\"")
				.append(excape(jsonPage)).append('"');
		
		int length = extra.length;

		for (int i = 0; i < length; ++i) {
			pages.append(",\"").append(excape(extra[i])).append('"');
		}

		pages.append("]}");

		boolean listening = player.getListeningPluginChannels().contains(BOOK_CHANNEL);
		
		if(!listening) {
			try {
				Reflect.playerAddChannel(player, BOOK_CHANNEL);
			}
			catch (Exception e) {
				Log.warning("Could not open book channel for player: " + player.getName());
				e.printStackTrace();
				return;
			}
		}
		
		int slot = player.getInventory().getHeldItemSlot();
		ItemStack old = player.getInventory().getItem(slot);
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		
		Bukkit.getUnsafe().modifyItemStack(book, pages.toString());

		player.getInventory().setItem(slot, book);

		try {
			player.sendPluginMessage(QuestWorld.getPlugin(), BOOK_CHANNEL, new byte[] {0});
		}
		catch(Exception e) {
			Log.warning("Failed sending book to player: " + player.getName());
			e.printStackTrace();
		}
		finally {
			player.getInventory().setItem(slot, old);
	
			if(!listening) {
				try {
					Reflect.playerRemoveChannel(player, BOOK_CHANNEL);
				}
				catch (Exception e) {
					Log.warning("Could not close book channel for player " + player.getName());
					e.printStackTrace();
				}
			}
		}

	}

	private static volatile ConversationFactory factory;

	public static ConversationFactory getConversationFactory() {
		if (factory == null)
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

		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		public void onCommand(PlayerCommandPreprocessEvent event) {
			if (event.getPlayer() == con.getForWhom()) {
				event.setCancelled(true);
				if (prompt.acceptInput(con.getContext(), event.getMessage()) != Prompt.END_OF_CONVERSATION)
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
		return getConversationFactory().withLocalEcho(false).withModality(false).withFirstPrompt(prompt)
				.buildConversation(p);
	}

	public static boolean checkPermission(Player p, String permission) {
		return permission == null || permission.length() == 0 || p.hasPermission(permission.split(" ", 2)[0]);
	}

	public static void tellraw(Player p, String json) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:tellraw " + p.getName() + " " + json);
	}

	public static Player getPlayer(String name) {
		return Bukkit.getPlayerExact(name);
	}

	@SuppressWarnings("deprecation")
	public static Optional<UUID> findUUID(String nameOrUUID) {
		Player p = getPlayer(nameOrUUID);
		if (p != null)
			return Optional.of(p.getUniqueId());

		try {
			return Optional.of(UUID.fromString(nameOrUUID));
		}
		catch (IllegalArgumentException e) {
			return Optional.of(nameOrUUID).filter(s -> {
				try {
					Integer.parseInt(nameOrUUID);
					return false;
				}
				catch (NumberFormatException e2) {
					return !s.equals("reset") && !s.equals("page");
				}
			}).map(Bukkit::getOfflinePlayer).filter(OfflinePlayer::hasPlayedBefore).map(OfflinePlayer::getUniqueId);
		}
	}

	private static boolean noexceptPick = true;

	public static ItemStack getStackOf(Block block) {
		if (noexceptPick)
			try {
				return Reflect.nmsPickBlock(block);
			}
			catch (Exception e) {
				noexceptPick = false;
				Log.warning(
						"Failed to reflect \"pickBlock\" method, QuestWorld was not fully prepared for your minecraft version");
				Log.warning(
						"Falling back to MaterialData comparison for all future checks. Mining quests may not detect blocks as accurately");
			}
		return block.getState().getData().toItemStack(1);
	}
}
