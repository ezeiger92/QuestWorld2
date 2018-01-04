package me.mrCookieSlime.QuestWorld.util.json;

import me.mrCookieSlime.QuestWorld.command.ClickCommand;

public class ClickRunnableProp extends ClickProp {
	public ClickRunnableProp(Runnable callback) {
		super("\"run_command\"", "/qw-invoke " + ClickCommand.add(callback));
	}
}
