package com.questworld.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.questworld.QuestingImpl;
import com.questworld.api.MissionType;
import com.questworld.api.QuestExtension;
import com.questworld.util.AutoListener;
import com.questworld.util.Log;
import com.questworld.util.Reloadable;

public final class ExtensionInstaller extends AutoListener implements Reloadable {
	private final List<QuestExtension> extensions = new ArrayList<>();
	private final List<QuestExtension> active = new ArrayList<>();
	private final Plugin plugin;
	private final QuestingImpl api;

	public ExtensionInstaller(Plugin plugin, QuestingImpl api) {
		this.plugin = plugin;
		this.api = api;
		register(plugin);
	}
	
	@Override
	public void onSave() {
		for(QuestExtension extension : active) {
			String name = extensionName(extension);
			try {
				extension.onSave();
			}
			catch(Throwable e) {
				Log.warning("Error saving extension: " + name);
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onReload() {
		for(QuestExtension extension : active) {
			String name = extensionName(extension);
			try {
				extension.onReload();
			}
			catch(Throwable e) {
				Log.warning("Error reloading extension: " + name);
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onDiscard() {
		for(QuestExtension extension : active) {
			String name = extensionName(extension);
			try {
				extension.onDiscard();
			}
			catch(Throwable e) {
				Log.warning("Error discarding extension: " + name);
				e.printStackTrace();
			}
		}
	}
	
	public List<QuestExtension> getActiveExtensions() {
		return Collections.unmodifiableList(active);
	}
	
	public List<QuestExtension> getInactiveExtensions() {
		return Collections.unmodifiableList(extensions);
	}
	
	public void add(QuestExtension extension) {
		PluginManager manager = plugin.getServer().getPluginManager();
		
		String name = extensionName(extension);
		
		Log.fine("Installer - Adding extension: " + name);
		
		String[] reqs = extension.getDepends();

		for(int i = 0; i < reqs.length; ++i) {
			Plugin p = manager.getPlugin(reqs[i]);
			if(p != null && p.isEnabled()) {
				extension.directEnablePlugin(p, i);
			}
		}
		
		if(extension.isReady()) {
			Log.fine("Installer - Dependencies found: " + name);
			initialize(extension, name);
			active.add(extension);
		}
		else {
			Log.fine("Installer - Listening for dependencies: " + name);
			extensions.add(extension);
		}
	}
	
	public void addAll(Collection<QuestExtension> extensions) {
		for(QuestExtension extension : extensions)
			add(extension);
	}
	
	private void initialize(QuestExtension extension, String name) {
		if(extension.isInitialized()) {
			Log.warning("Error initializing extension: " + name + ": Double initializationS!");
			return;
		}
		
		Log.fine("Installer - Initializing extension: " + name);
		
		try {
			extension.init(plugin);
		}
		catch(Throwable e) {
			Log.warning("Error initializing extension: " + name);
			e.printStackTrace();
			return;
		}
		
		PluginManager pm = plugin.getServer().getPluginManager();

		for(MissionType type : extension.getMissionTypes()) {
			Log.fine("Installer - Storing mission: " + type.getName());
			api.registerType(type);
			
			if(type instanceof Listener) {
				Log.fine("Installer - Registering events: " + type.getName());
				pm.registerEvents((Listener)type, plugin);
			}
		}
	}
	
	private String extensionName(QuestExtension extension) {
		String name;
		try {
			name = extension.getName();
		}
		catch(Throwable e) {
			name = extension.getClass().getSimpleName();
			Log.warning("Error getting extension name for class " + name);
			e.printStackTrace();
		}
		
		return name;
	}
	
	@EventHandler
	public void onPluginEnable(PluginEnableEvent event) {
		Iterator<QuestExtension> iterator = extensions.iterator();
		
		while(iterator.hasNext()) {
			QuestExtension extension = iterator.next();
			extension.enablePlugin(event.getPlugin());
			
			if(extension.isReady()) {
				String name = extensionName(extension);
				Log.fine("Installer - Dependencies loaded: " + name);
				initialize(extension, name);
				iterator.remove();
				active.add(extension);
			}
		}
	}
}
