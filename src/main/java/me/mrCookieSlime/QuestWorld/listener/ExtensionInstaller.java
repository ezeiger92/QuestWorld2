package me.mrCookieSlime.QuestWorld.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import me.mrCookieSlime.QuestWorld.QuestWorldPlugin;
import me.mrCookieSlime.QuestWorld.api.QuestExtension;
import me.mrCookieSlime.QuestWorld.util.Log;

public class ExtensionInstaller implements Listener {
	private List<QuestExtension> extensions = new ArrayList<>();
	private List<QuestExtension> active = new ArrayList<>();
	private Plugin parent;

	public ExtensionInstaller(Plugin parent) {
		this.parent = parent;
		PluginManager manager = parent.getServer().getPluginManager();
		manager.registerEvents(this, parent);
	}
	
	public void save() {
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
	
	public void add(QuestExtension extension) {
		PluginManager manager = parent.getServer().getPluginManager();
		
		String name = extensionName(extension);
		
		Log.fine("Installer - Adding extension: " + name);
		
		String[] reqs = extension.getDepends();
		if(reqs != null)
			for(int i = 0; i < reqs.length; ++i) {
				Plugin p = manager.getPlugin(reqs[i]);
				if(p != null && p.isEnabled()) {
					extension.directEnablePlugin(p, i);
					active.add(extension);
				}
			}
		
		if(extension.isReady()) {
			Log.fine("Installer - Dependencies found: " + name);
			initialize(extension, name);
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
			extension.init(parent);
		}
		catch(Throwable e) {
			Log.warning("Error initializing extension: " + name);
			e.printStackTrace();
			return;
		}

		QuestWorldPlugin.getImpl().getPlugin().enable(extension);
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
