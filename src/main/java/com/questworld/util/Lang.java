package com.questworld.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.questworld.api.Translator;
import com.questworld.api.lang.PlaceholderSupply;

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

		for (String langPath : loader.filesInResourceDir("lang/"))
			try {
				loadLang(langPath, loader);
			}
			catch (IllegalArgumentException e) {
				if (fallback.equals(langPath)) {
					Log.warning("Could not find fallback language \"" + fallback + "\"");
					fallback = currentLang;
				}
				else if (currentLang.equals(langPath)) {
					Log.severe("Could not find language \"" + currentLang + "\"");
					throw e;
				}
			}

		fallbackLang = fallback;
	}

	public Lang(ResourceLoader loader) throws IllegalArgumentException {
		this(loader, fallbackLangCode);
	}

	private void loadLang(String langPath, ResourceLoader loader) throws IllegalArgumentException {
		if (langPath != null) {
			YamlConfiguration config;
			try {
				config = loader.loadConfig(langPath);
			}
			catch (Exception e) {
				throw new IllegalArgumentException("Failed read language \"" + langPath + "\"", e);
			}

			YamlConfiguration old = languages.put(langPath, config);
			
			if(old != null) {
				for(HashMap.Entry<String, Object> o : old.getValues(true).entrySet()) {
					
					if(!(o.getValue() instanceof ConfigurationSection)) {
						config.set(o.getKey(), o.getValue());
					}
				}
			}
		}
		else
			throw new IllegalArgumentException("Language cannot be null");
	}
	
	public void importLanguages(ResourceLoader loader) {
		for (String langPath : loader.filesInResourceDir("lang/"))
			try {
				loadLang(langPath, loader);
			}
			catch (IllegalArgumentException e) {
				Log.warning("Could not import language \"" + langPath + "\"");
			}
	}
	
	public boolean setLang(String langCode) {
		return setLang(langCode, loader);
	}

	public boolean setLang(String langCode, ResourceLoader loader) {
		String langPath = langPath(langCode);
		if (!languages.containsKey(langPath))
			try {
				loadLang(langPath, loader);
			}
			catch (IllegalArgumentException e) {
				e.printStackTrace();
				return false;
			}

		currentLang = langPath;
		return true;
	}
	
	private String getTemplate(Translator key) {
		String translation = languages.get(currentLang).getString(key.path());
		if (translation == null) {
			translation = languages.get(fallbackLang).getString(key.path());
			if (translation == null) {
				Log.severe("Lang " + currentLang
						+ (currentLang.equals(fallbackLang) ? "" : " and fallback " + fallbackLang) + " missing "
						+ key.toString());
				return "";
			}
			else
				Log.warning("Lang " + currentLang + " missing " + key.toString());
		}
		
		return translation;
	}

	private static final Pattern PH_KEY = Pattern.compile("%([\\w\\._-]+)%");
	
	public String translateRaw(Translator key, PlaceholderSupply<?>... replacementSource) {
		String template = getTemplate(key);
		Matcher m = PH_KEY.matcher(template);

		Log.info("Translating " + key.path() + " with " + replacementSource.length + " sources");
		while (m.find()) {
			
			Log.info("    Attempting replacment for " + m.group(1));
			for (PlaceholderSupply<?> e : replacementSource) {
				String replacement = e.getReplacement(m.group(1));
				
				if (!replacement.isEmpty()) {
					template = template.replace(m.group(), replacement);
					break;
				}
			}
		}
		
		return template;
	}

	@Override
	public void onSave() {
		for (HashMap.Entry<String, YamlConfiguration> entry : languages.entrySet()) {
			String path = entry.getKey();
			try {
				loader.saveConfig(entry.getValue(), path);
			}
			catch (IOException e) {
				Log.severe("Unable to write lang file \"" + path + "\"");
			}
		}
	}

	@Override
	public void onReload() {
		for (String key : new ArrayList<>(languages.keySet())) {
			languages.remove(key);
			try {
				loadLang(key, loader);
			}
			catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDiscard() {
	}
}
