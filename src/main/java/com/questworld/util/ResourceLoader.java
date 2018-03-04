package com.questworld.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class ResourceLoader {
	private static final int BUFFER_SIZE = 16 * 1024;
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
	
	public File getBaseDir() {
		return dataPath;
	}
	
	public ClassLoader getClassLoader() {
		return classLoader;
	}
	
	public List<String> filesInResourceDir(String path) {
		ArrayList<String> result = new ArrayList<>();
		URL dirUrl = classLoader.getResource(path);
		if(dirUrl != null) {
			String jarPath = dirUrl.getPath();
			int end = jarPath.indexOf('!');
			if(end >= 5 && jarPath.length() > 5) {
				jarPath = dirUrl.getPath().substring(5, end);
				
				try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))) {
					Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
					while(entries.hasMoreElements()) {
						String name = entries.nextElement().getName();
						// Don't include (empty) path in list
						if(name.length() == path.length())
							continue;
						
						if (name.startsWith(path))
							result.add(path);
					}
				}
				catch (RuntimeException e) {
					throw e;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return result;
	}
	
	private InputStreamReader readerOf(String resource) {
		InputStream stream = classLoader.getResourceAsStream(resource);
		if(stream == null)
			throw new IllegalArgumentException("Resource \"" + resource + "\" could not be found");
		
		return new InputStreamReader(stream, StandardCharsets.UTF_8);
	}
	
	public YamlConfiguration loadFileConfig(String resource) {
		return YamlConfiguration.loadConfiguration(new File(dataPath, resource));
	}
	
	public YamlConfiguration loadJarConfig(String resource) {
		return YamlConfiguration.loadConfiguration(readerOf(resource));
	}
	
	public YamlConfiguration loadConfig(String resource) throws FileNotFoundException, IOException, InvalidConfigurationException {
		YamlConfiguration result = new YamlConfiguration();
		File file = new File(dataPath, resource);
		
		result.setDefaults(YamlConfiguration.loadConfiguration(readerOf(resource)));
		
		if(!file.exists()) {
			if(!file.getParentFile().exists() && !file.getParentFile().mkdirs())
				throw new IOException("Could not create directories for: "+file.getName());
			
			// stream will not be null unless some other thread destroys the jar file
			// readerOf above would have thrown an exception had it been null
			try(InputStream stream = classLoader.getResourceAsStream(resource)) {
				try(FileOutputStream fos = new FileOutputStream(file)) {
					byte[] buffer = new byte[BUFFER_SIZE];
					int len;
					while((len = stream.read(buffer)) != -1)
						fos.write(buffer, 0, len);
				}
			}
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
