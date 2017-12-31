package me.mrCookieSlime.QuestWorld.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class ResourceLoader {
	private final ClassLoader classLoader;
	private final File dataPath;
	public ResourceLoader(Plugin plugin) {
		classLoader = plugin.getClass().getClassLoader();
		dataPath = plugin.getDataFolder();
	}
	
	public ResourceLoader(ClassLoader loader, File folder) {
		classLoader = loader;
		dataPath = folder;
	}
	
	private InputStream activeStream;
	private InputStreamReader activeReader;
	private InputStreamReader readerOf(String resource) {
		activeStream = classLoader.getResourceAsStream(resource);
		activeReader = activeStream != null ? new InputStreamReader(activeStream) : null;
		return activeReader;
	}
	
	private void close() {
		try { activeReader.close(); } catch (Exception e) {}
		try { activeStream.close(); } catch (Exception e) {}
		activeReader = null;
		activeStream = null;
	}
	
	public YamlConfiguration loadFileConfig(String resource) {
		return YamlConfiguration.loadConfiguration(new File(dataPath, resource));
	}
	
	public YamlConfiguration loadJarConfig(String resource) {
		try { return YamlConfiguration.loadConfiguration(readerOf(resource)); }
		finally { close(); }
	}
	
	public YamlConfiguration loadConfig(String resource) throws FileNotFoundException, IOException, InvalidConfigurationException {
		YamlConfiguration result = new YamlConfiguration();
		File file = new File(dataPath, resource);
		
		try { result.setDefaults(YamlConfiguration.loadConfiguration(readerOf(resource))); }
		finally { close(); }
		
		if(!file.exists()) {
			file.getParentFile().mkdirs();
			try {
				readerOf(resource);
				try(FileOutputStream fos = new FileOutputStream(file)) {
					byte[] buffer = new byte[2048];
					int len;
					while((len = activeStream.read(buffer)) != -1)
						fos.write(buffer, 0, len);
				}
			}
			finally { close(); }
		}
		
		result.load(file);
		
		return result;
	}
	
	public YamlConfiguration loadConfigNoexpect(String resource, boolean printException) {
		try {
			return loadConfig(resource);
		}
		catch(Exception e) {
			if(printException)
				e.printStackTrace();
		}
		
		return new YamlConfiguration();
	}
	
	public void saveConfig(FileConfiguration config, String resource) throws IOException {
		config.save(new File(dataPath, resource));
	}
	
	public boolean saveConfigNoexcept(FileConfiguration config, String resource, boolean printException) {
		try {
			saveConfig(config, resource);
		} catch (Exception e) {
			if(printException)
				e.printStackTrace();
			return false;
		}
		return true;
	}
}
