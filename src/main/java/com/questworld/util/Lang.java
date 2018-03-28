package com.questworld.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.bukkit.configuration.file.YamlConfiguration;

import com.questworld.api.Translator;

public final class Lang implements Reloadable {
	private static String langPath(String langCode) {
		return "lang/" + langCode.toLowerCase(Locale.US) + ".yml";
	}
	private static final String fallbackLangCode = "en_us";
	
	private final String fallbackLang;
	private final ResourceLoader loader;
	private final HashMap<String, YamlConfiguration> languages = new HashMap<>();
	private String currentLang;
	
	public Lang(ResourceLoader loader, String langCode) throws IllegalArgumentException {
		this.loader = loader;
		currentLang = langPath(langCode);
		
		String fallback = langPath(fallbackLangCode);
		
		for(String langPath : loader.filesInResourceDir("lang/"))
			try {
				loadLang(langPath);
			}
			catch(IllegalArgumentException e) {
				if(fallback.equals(langPath)) {
					Log.warning("Could not find fallback language \""+fallback+"\"");
					fallback = currentLang;
				}
				else if(currentLang.equals(langPath)) {
					Log.severe("Could not find language \""+currentLang+"\"");
					throw e;
				}
			}
		
		fallbackLang = fallback;
	}
	
	public Lang(ResourceLoader loader) throws IllegalArgumentException {
		this(loader, fallbackLangCode);
	}
	
	private void loadLang(String langPath) throws IllegalArgumentException {
		if(langPath != null) {
			YamlConfiguration config;
			try {
				config = loader.loadConfig(langPath);
			}
			catch(Exception e) {
				throw new IllegalArgumentException("Failed read language \"" + langPath + "\"", e);
			}
			
			languages.put(langPath, config);
		}
		else
			throw new IllegalArgumentException("Language cannot be null");
	}
	
	public boolean setLang(String langCode) {
		String langPath = langPath(langCode);
		if(!languages.containsKey(langPath))
			try {
				loadLang(langPath);
			}
			catch(IllegalArgumentException e) {
				e.printStackTrace();
				return false;
			}
		
		currentLang = langPath;
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
				return "";
			}
			else
				Log.warning("Lang " + currentLang + " missing " + key.toString());
		}
		
		return replaceAll(translation, key.placeholders(), replacements);
	}

	@Override
	public void onSave() {
		for(HashMap.Entry<String, YamlConfiguration> entry : languages.entrySet()) {
			String path = entry.getKey();
			try {
				loader.saveConfig(entry.getValue(), path);
			} catch (IOException e) {
				Log.severe("Unable to write lang file \"" + path +"\"");
			}
		}
	}
	
	@Override
	public void onReload() {
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
	
	@Override
	public void onDiscard() {
	}
}
