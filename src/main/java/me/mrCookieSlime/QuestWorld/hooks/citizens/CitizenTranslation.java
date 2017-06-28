package me.mrCookieSlime.QuestWorld.hooks.citizens;

import me.mrCookieSlime.QuestWorld.api.Translator;

public enum CitizenTranslation implements Translator {
	// TODO Better names, PH
	citizen_l("editor.link-citizen"),
	citizen_link("editor.link-citizen-finished"),
	;
	private String path;
	private String[] placeholders;
	CitizenTranslation(String path, String... placeholders) {
		this.path = path;
		this.placeholders = placeholders;
		for(int i = 0; i < this.placeholders.length; ++i)
			this.placeholders[i] = "%" + this.placeholders[i] + "%"; 
	}
	
	@Override
	public String path() {
		return path;
	}
	
	@Override
	public String[] placeholders() {
		return placeholders;
	}
}
