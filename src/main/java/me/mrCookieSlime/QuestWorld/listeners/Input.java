package me.mrCookieSlime.QuestWorld.listeners;

public class Input {
	
	InputType type;
	Object value;
	
	public Input(InputType type, Object value) {
		this.type = type;
		this.value = value;
	}
	
	public InputType getType() {
		return type;
	}
	
	public Object getValue() {
		return value;
	}

}
