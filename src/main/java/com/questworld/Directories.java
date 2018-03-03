package com.questworld;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

import org.bukkit.configuration.ConfigurationSection;

import com.questworld.util.ResourceLoader;

public class Directories {
	public final File dialogue;
	public final File extensions;
	public final File playerdata;
	public final File presets;
	public final File questing;
	
	private static File initDirectory(File base, String path) {
		if(!path.endsWith("/"))
			path = path + "/";
		
		File result = new File(base, path);
		
		if(!result.exists() && !result.mkdirs())
			throw new IllegalStateException("Directory missing but cannot be created: " + result.getPath());
		
		return result;
	}
	
	public Directories(ResourceLoader resources) {
		File base = resources.getBaseDir();
		ConfigurationSection config;
		try {
			config = resources.loadConfig("config.yml");
		}
		catch(Exception e) {
			throw new IllegalStateException("Failed to load config.yml", e);
		}
		
		dialogue   = initDirectory(base, config.getString("data.dialogue"));
		extensions = initDirectory(base, config.getString("data.extensions"));
		playerdata = initDirectory(base, config.getString("data.player"));
		presets    = initDirectory(base, config.getString("data.presets"));
		questing   = initDirectory(base, config.getString("data.questing"));
	}
	
	public static File[] listFiles(File in) {
		File[] files = in.listFiles();
		
		if(files != null)
			return files;
		
		return new File[0];
	}
	
	public static File[] listFiles(File in, FileFilter filter) {
		File[] files = in.listFiles(filter);
		
		if(files != null)
			return files;
		
		return new File[0];
	}
	
	public static File[] listFiles(File in, FilenameFilter filter) {
		File[] files = in.listFiles(filter);
		
		if(files != null)
			return files;
		
		return new File[0];
	}
}
