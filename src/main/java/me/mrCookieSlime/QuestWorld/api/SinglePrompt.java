package me.mrCookieSlime.QuestWorld.api;

import java.util.function.BiPredicate;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import me.mrCookieSlime.QuestWorld.util.Text;

/**
 * Basic prompt that accepts a single line of input from the player.
 * 
 * @author Erik Zeiger
 */
public class SinglePrompt extends StringPrompt {
	private final String question;
	private final String error;
	
	/**
	 * Returning <tt>true</tt> indicates the input was accepted.
	 * <p>
	 * Returning <tt>false</tt> indicates the input was rejected, and the player
	 * should be prompted again.
	 */
	private final BiPredicate<ConversationContext, String> callback;

	/**
	 * Creates a new prompt, with custom input rejection message.
	 * 
	 * @param request The text prompt given to the player requesting their input
	 * @param onFailedInput The message given when input is rejected
	 * @param inputHandler The callback when input is provided
	 * 
	 * @see SinglePrompt#callback callback
	 */
	public SinglePrompt(String request, String onFailedInput, BiPredicate<ConversationContext, String> inputHandler) {
		question = request;
		error = onFailedInput;
		callback = inputHandler;
	}
	
	/**
	 * Creates a new prompt.
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
		return Text.colorize(question);
	}

	@Override
	public Prompt acceptInput(ConversationContext context, String input) {
		if(callback.test(context, input))
			return END_OF_CONVERSATION;

		context.getForWhom().sendRawMessage(Text.colorize(error));
		return this;
	}

}
