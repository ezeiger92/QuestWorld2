package me.mrCookieSlime.QuestWorld;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import me.mrCookieSlime.QuestWorld.api.QuestExtension;
import me.mrCookieSlime.QuestWorld.utils.Log;

public class ExtensionLoader {
	private ClassLoader loader;
	private File folder;
	
	public ExtensionLoader(ClassLoader loader, File folder) {
		this.loader = loader;
		this.folder = folder;
		
		if(loader == null || folder == null) {
			String args = (loader == null ? (folder == null ? "both were" : "'loader' was") : "'folder' was");
			throw new NullPointerException("'loader' and 'folder' must not be null, but " + args + " null!");
		}
		else if(!folder.isDirectory()) {
			//throw new IllegalArgumentException("'folder' must be a directory!");
		}
	}
	
	public void loadLocal() {
		File[] extensions = folder.listFiles((file, name) -> {
			// TODO check for jar files the best way, because it's probably not
			return name.endsWith(".jar");
		});
		
		// Not a directory or unable to list files for some reason
		if(extensions == null)
			return;
		
		// Could be parallel, but not worth it yet
		for(File f : extensions)
			load(f);
	}
	
	public void load(File extensionFile) {
		Log.fine("Loader - Reading file: " + extensionFile.getName());
		URL jarURL;
		try { jarURL = extensionFile.toURI().toURL(); }
		catch (MalformedURLException e) { e.printStackTrace(); return; }
		
		ClassLoader newLoader = URLClassLoader.newInstance(new URL[] {
			jarURL,
		}, loader);
		
		JarFile jar;
		try { jar = new JarFile(extensionFile); }
		catch (IOException e) { e.printStackTrace(); return; }
		
		Enumeration<JarEntry> entries = jar.entries();
		List<Class<?>> extensionClasses = new ArrayList<>();
		
		while(entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			if(entry.isDirectory() || !entry.getName().endsWith(".class"))
				continue;
			
			String className = entry.getName().substring(0, entry.getName().length() - 6).replace('/', '.');
			Log.finer("Loader - Loading class: " + className);
			Class<?> clazz;
			try { clazz = newLoader.loadClass(className); }
			catch (ClassNotFoundException e) { e.printStackTrace(); continue; }

			if(QuestExtension.class.isAssignableFrom(clazz)) {
				Log.finer("Loader - Found extension class: " + className);
				extensionClasses.add(clazz);
			}
		}
		
		for(Class<?> extensionClass : extensionClasses) {
			Log.fine("Loader - Constructing: " + extensionClass.getName());
			try { extensionClass.getConstructor().newInstance(); }
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// TODO better messages for exceptions during construction
				e.printStackTrace();
			}
		}
		
		try { jar.close(); }
		catch (IOException e) { e.printStackTrace(); }
	}
}
