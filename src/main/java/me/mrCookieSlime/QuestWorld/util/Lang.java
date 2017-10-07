package me.mrCookieSlime.QuestWorld.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.mrCookieSlime.QuestWorld.api.Translator;

public final class Lang implements Reloadable {
	private String currentLang;
	private String fallbackLang;
	private Map<String, FileConfiguration> languages = new HashMap<>();
	private File dataFolder;
	private ClassLoader loader;
	
	public Lang(String langCode, File dataFolder, ClassLoader loader) {
		this.dataFolder = dataFolder;
		this.loader = loader;
		if(!setLang(langCode))
			throw new IllegalArgumentException("Language file " + langPath(langCode) + " could not be found");
		fallbackLang = langCode;
	}
	
	private static String langPath(String langCode) {
		return "lang/" + langCode + ".yml";
	}
	
	private FileConfiguration getYaml(String langCode) {
		InputStream localStream = loader.getResourceAsStream(langPath(langCode));
		if(localStream == null)
			return null;
		Charset set = Charset.defaultCharset();
		try { set = Charset.forName("UTF-8"); } catch(Exception e) {}
		
		InputStreamReader reader = new InputStreamReader(localStream, set);
		FileConfiguration result = YamlConfiguration.loadConfiguration(reader);
		try { reader.close(); } catch(IOException e) {}
		try { localStream.close(); } catch(IOException e) {}
		return result;
	}
	
	public boolean loadLangDefaults(String langCode) {
		FileConfiguration yaml = getYaml(langCode);
		if(yaml == null)
			return false;
		
		if(languages.putIfAbsent(langCode, yaml) == null)
			yaml = getYaml(langCode);

		if(yaml != null)
			languages.get(langCode).setDefaults(yaml);

		return true;
	}
	
	public boolean loadLang(String langCode) {
		boolean loadedDefaults = loadLangDefaults(langCode);
		File langFile = new File(dataFolder, langPath(langCode));
		if(!langFile.exists())
			return loadedDefaults;
		
		YamlConfiguration langData = YamlConfiguration.loadConfiguration(langFile);
		langData.setDefaults(languages.get(langCode).getDefaults());
		languages.put(langCode, langData);
		return true;
	}
	
	public boolean setLang(String langCode) {
		if(!languages.containsKey(langCode) && !loadLang(langCode))
			return false;
		
		currentLang = langCode;
		return true;
	}
	
	private void clearLang(String langCode) {
		if(languages.containsKey(langCode))
			languages.put(langCode, null);
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
			else Log.warning("Lang " + currentLang + " missing " + key.toString());
		}
		return replaceAll(translation, key.placeholders(), replacements);
	}

	@Override
	public void save() {
		File langDir = new File(dataFolder, "/lang");
		if(!langDir.exists() && !langDir.mkdir()) {
			Log.severe("Unable to create lang directory in QuestWorld folder, cannot save lang files!");
			return;
		}
		
		for(Map.Entry<String, FileConfiguration> entry : languages.entrySet()) {
			File file = new File(langDir, entry.getKey() + ".yml");
			try {
				entry.getValue().save(file);
			} catch (IOException e) {
				Log.severe("Unable to create lang file \"" + file.getPath() +"\" in QuestWorld folder!");
			}
		}
	}
	
	@Override
	public void reload() {
		List<String> keys = new ArrayList<>(languages.keySet());
		for(String key : keys) {
			clearLang(key);
			loadLang(key);
		}
	}
}
