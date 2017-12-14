package me.mrCookieSlime.QuestWorld.api;

import java.util.function.BiPredicate;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import me.mrCookieSlime.QuestWorld.util.Text;

/**
 * Basic prompt that accepts a single line of input from the player. By using
 * {@link SinglePrompt#SinglePrompt(String, String, BiPredicate)} and an
 * appropriate callback, you can accept multiple lines.
 * 
 * @author Erik Zeiger
 */
public class SinglePrompt extends StringPrompt {
	private String effectiveQuestion;
	private final String question;
	private final String additional;
	
	/**
	 * Returning <tt>true</tt> indicates the input was accepted.
	 * <p>
	 * Returning <tt>false</tt> indicates the input was rejected, and the player
	 * should be prompted again.
	 */
	private final BiPredicate<ConversationContext, String> callback;

	/**
	 * Creates a new prompt, with custom input request message.
	 * 
	 * @param request The text prompt given to the player requesting their input
	 * @param requestMoreInput The message given when more input is needed
	 * @param inputHandler The callback when input is provided
	 * 
	 * @see SinglePrompt#callback callback
	 */
	public SinglePrompt(String request, String requestMoreInput, BiPredicate<ConversationContext, String> inputHandler) {
		question = request;
		additional = requestMoreInput;
		callback = inputHandler;
		effectiveQuestion = question;
	}
	
	/**
	 * Creates a new prompt. Assumes that <tt>false</tt> is an error and sends
	 * <tt>"Input not valid"</tt>. Use a custom message for clarity when
	 * accepting multiple lines of input.
	 * 
	 * @param request The text prompt given to the player requesting their input
	 * @param inputHandler The callback when input is provided
	 * 
	 * @see SinglePrompt#callback callback
	 */
	public SinglePrompt(String request, BiPredicate<ConversationContext, String> inputHandler) {
		this(request, "&cInput not valid", inputHandler);
	}
	
	@Override
	public String getPromptText(ConversationContext context) {
		return Text.colorize(effectiveQuestion);
	}

	@Override
	public Prompt acceptInput(ConversationContext context, String input) {
		if(callback.test(context, input))
			return END_OF_CONVERSATION;

		effectiveQuestion = additional;
		String display = (String)context.getSessionData("display");
		if(display != null) {
			effectiveQuestion = display;
			context.setSessionData("display", null);
		}
		
		return this;
	}

	public static void setNextDisplay(ConversationContext context, String display) {
		context.setSessionData("display", display);
	}
}
