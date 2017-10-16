package me.mrCookieSlime.QuestWorld.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class ResourceLoader {
	private final Plugin plugin;
	public ResourceLoader(Plugin plugin) {
		this.plugin = plugin;
	}
	
	private File relativeFile(String path) {
		return new File(plugin.getDataFolder(), path);
	}
	
	public YamlConfiguration loadFileConfig(String path) {
		return YamlConfiguration.loadConfiguration(relativeFile(path));
	}
	
	public YamlConfiguration loadJarConfig(String jarPath) {
		return YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(jarPath), Charset.forName("UTF-8")));
	}
	
	public YamlConfiguration loadConfig(String path) throws FileNotFoundException, IOException, InvalidConfigurationException {
		YamlConfiguration result = new YamlConfiguration();
		InputStream resource = plugin.getResource(path);
		
		if(resource != null) {
			result.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(resource, Charset.forName("UTF-8"))));
			plugin.saveResource(path, false);
		}
		
		result.load(relativeFile(path));
		
		return result;
	}
	
	public YamlConfiguration loadConfigNoexpect(String path, boolean printException) {
		try {
			return loadConfig(path);
		}
		catch(Exception e) {
			if(printException)
				e.printStackTrace();
		}
		
		return new YamlConfiguration();
	}
	
	public void saveConfig(FileConfiguration config, String path) throws IOException {
		config.save(relativeFile(path));
	}
	
	public boolean saveConfigNoexcept(FileConfiguration config, String path, boolean printException) {
		try {
			saveConfig(config, path);
		} catch (Exception e) {
			if(printException)
				e.printStackTrace();
			return false;
		}
		return true;
	}
}
