package me.mrCookieSlime.QuestWorld.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.configuration.file.YamlConfiguration;
import me.mrCookieSlime.QuestWorld.api.Translator;

public final class Lang implements Reloadable {
	private final String fallbackLang = "en_us";
	private final ResourceLoader loader;
	private final HashMap<String, YamlConfiguration> languages = new HashMap<>();
	private String currentLang;
	
	// TODO Load all languages from jar://lang/
	public Lang(ResourceLoader loader) throws IllegalArgumentException {
		this.loader = loader;
		loadLang(fallbackLang);
		currentLang = fallbackLang;
	}
	
	private void loadLang(String langCode) throws IllegalArgumentException {
		String path = "lang/" + langCode + ".yml";
		YamlConfiguration config;
		try {
			config = loader.loadConfig(path);
		}
		catch(Exception e) {
			throw new IllegalArgumentException("Failed read language \"" + path +"\"", e);
		}
		
		languages.put(langCode.toLowerCase(), config);
	}
	
	public boolean setLang(String langCode) {
		if(!languages.containsKey(langCode))
			try {
				loadLang(langCode);
			}
			catch(IllegalArgumentException e) {
				e.printStackTrace();
				return false;
			}
		
		currentLang = langCode;
		return true;
	}
	
	private String replaceAll(String translation, String[] search, String[] replacements) {
		int len = Math.min(replacements.length, search.length);
		for(int i = 0; i < len; ++i)
			translation = translation.replace(search[i], replacements[i]);
		
		return translation;
	}
	
	public String translate(Translator key, String... replacements) {
		String translation = languages.get(currentLang).getString(key.path());
		if(translation == null) {
			translation = languages.get(fallbackLang).getString(key.path());
			if(translation == null) {
				Log.severe("Lang " + currentLang + (currentLang.equals(fallbackLang) ? "" : " and fallback " + fallbackLang) + " missing " + key.toString());
				return "ERROR: missing " + key.toString();
			}
			else
				Log.warning("Lang " + currentLang + " missing " + key.toString());
		}
		
		return replaceAll(translation, key.placeholders(), replacements);
	}

	@Override
	public void save() {
		for(HashMap.Entry<String, YamlConfiguration> entry : languages.entrySet()) {
			String path = "lang/" + entry.getKey() + ".yml";
			try {
				loader.saveConfig(entry.getValue(), path);
			} catch (IOException e) {
				Log.severe("Unable to write lang file \"" + path +"\"");
			}
		}
	}
	
	@Override
	public void reload() {
		for(String key : new ArrayList<>(languages.keySet())) {
			languages.remove(key);
			try {
				loadLang(key);
			}
			catch(IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}
}
