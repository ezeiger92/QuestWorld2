package com.questworld;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class PresetLoader {
	private static final int BUFFER_SIZE = 1024;
	private final QuestingImpl api;
	
	public PresetLoader(QuestingImpl api) {
		this.api = api;
	}
	
	public boolean save(String filename) {
		File file = new File(api.getDataFolders().presets, filename);
		
		api.onSave();
		
		try {
			Files.deleteIfExists(file.toPath());
			Files.createFile(file.toPath());
			
			ArrayList<File> files = new ArrayList<>();
			File dialogueDir = api.getDataFolders().dialogue;
			
			files.addAll(Arrays.asList(Directories.listFiles(api.getDataFolders().questing)));
			files.addAll(Arrays.asList(Directories.listFiles(api.getDataFolders().dialogue)));
			
			try(ZipOutputStream output = new ZipOutputStream(new FileOutputStream(file))) {
				byte[] buffer = new byte[BUFFER_SIZE];
				
				for (File f: files) {
					String entryName = f.getName();
					
					if(f.getParentFile().equals(dialogueDir))
						entryName = "dialogue/"+entryName;
					
					ZipEntry entry = new ZipEntry(entryName);
					output.putNextEntry(entry);
					try(FileInputStream input = new FileInputStream(f)) {
						int length;
						while ((length = input.read(buffer)) > 0) {
							output.write(buffer, 0, length);
						}
					}
					output.closeEntry();
				}
				
			}
		}
		catch(IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	public boolean load(String filename) {
		File questingDirectory = api.getDataFolders().questing;
		File dialogueDirectory = api.getDataFolders().dialogue; 
		
		File file = new File(api.getDataFolders().presets, filename);
		byte[] buffer = new byte[BUFFER_SIZE];
		if (!file.exists())
			return false;
		
		try(ZipInputStream input = new ZipInputStream(new FileInputStream(file))) {
			ZipEntry entry = input.getNextEntry();
			
			for (File f: Directories.listFiles(questingDirectory))
				Files.delete(f.toPath());
			
			for (File f: Directories.listFiles(dialogueDirectory))
				Files.delete(f.toPath());
			
			while (entry != null) {
				File target;
				if(entry.getName().startsWith("dialogue/"))
					target = new File(dialogueDirectory, entry.getName().substring(9));
				else
					target = new File(questingDirectory, entry.getName());
				
				try (FileOutputStream output = new FileOutputStream(target)) {
					int length;
					while ((length = input.read(buffer)) > 0) {
						output.write(buffer, 0, length);
					}
				}
				entry = input.getNextEntry();
			}
			
			input.closeEntry();
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		api.onDiscard();
		return true;
	}
}
