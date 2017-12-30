package me.mrCookieSlime.QuestWorld.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class ResourceLoader {
	private final ClassLoader classLoader;
	private final Path dataPath;
	public ResourceLoader(Plugin plugin) {
		classLoader = plugin.getClass().getClassLoader();
		dataPath = plugin.getDataFolder().toPath();
	}
	
	public ResourceLoader(ClassLoader loader, Path folder) {
		classLoader = loader;
		dataPath = folder;
	}
	private FileSystem zipFS = null;
	
	private Path jarPath(String resource) {
		URI uri = URI.create(classLoader.getResource(resource).toString());
		
		try {
			zipFS = FileSystems.newFileSystem(uri, new HashMap<>());
			return zipFS.getPath(resource);
		} catch(FileSystemAlreadyExistsException e) {
			e.printStackTrace();
			try {
				return FileSystems.getFileSystem(uri).getPath(resource);
			}
			catch (Exception ee) {
				ee.printStackTrace();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private void closeZip() {
		if(zipFS != null) {
			try {
				zipFS.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			zipFS = null;
		}
	}
	
	public YamlConfiguration loadFileConfig(String path) {
		return YamlConfiguration.loadConfiguration(dataPath.resolve(path).toFile());
	}
	
	public YamlConfiguration loadJarConfig(String jarPath) {
		YamlConfiguration result = null;
		try {
			result = YamlConfiguration.loadConfiguration(Files.newBufferedReader(jarPath(jarPath)));
			closeZip();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public YamlConfiguration loadConfig(String path) throws FileNotFoundException, IOException, InvalidConfigurationException {
		YamlConfiguration result = new YamlConfiguration();
		Path file = dataPath.resolve(path);
		
		result.setDefaults(YamlConfiguration.loadConfiguration(Files.newBufferedReader(jarPath(path))));
		closeZip();
		if(!Files.exists(file)) {
			Files.copy(jarPath(path), file);
			closeZip();
		}
		result.load(file.toFile());
		
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
		config.save(dataPath.resolve(path).toFile());
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
