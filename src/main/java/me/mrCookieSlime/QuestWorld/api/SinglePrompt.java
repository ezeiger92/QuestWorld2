package me.mrCookieSlime.QuestWorld.api;

import java.util.function.BiPredicate;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import me.mrCookieSlime.QuestWorld.utils.Text;

public class SinglePrompt extends StringPrompt {
	private String question;
	private String error;
	private BiPredicate<ConversationContext, String> callback;

	public SinglePrompt(String request, String onFailedInput, BiPredicate<ConversationContext, String> inputHandler) {
		question = request;
		error = onFailedInput;
		callback = inputHandler;
	}
	
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
