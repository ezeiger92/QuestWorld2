package me.mrCookieSlime.QuestWorld;

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

import me.mrCookieSlime.QuestWorld.api.contract.QuestingAPI;

public class PresetLoader {
	private QuestingAPI api;
	private Directories dataFolders;
	
	public PresetLoader(QuestingAPI api, Directories dataFolders) {
		this.api = api;
		this.dataFolders = dataFolders;
	}
	
	public boolean save(String filename) {
		File file = new File(dataFolders.presets, filename);
		
		api.onSave();
		
		try {
			Files.deleteIfExists(file.toPath());
			Files.createFile(file.toPath());
			
			ArrayList<File> files = new ArrayList<>();
			File dialogueDir = dataFolders.dialogue;
			
			files.addAll(Arrays.asList(Directories.listFiles(dataFolders.questing)));
			files.addAll(Arrays.asList(Directories.listFiles(dataFolders.dialogue)));
			
			try(ZipOutputStream output = new ZipOutputStream(new FileOutputStream(file))) {
				byte[] buffer = new byte[1024];
				
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
		File file = new File(dataFolders.presets, filename);
		byte[] buffer = new byte[1024];
		if (!file.exists())
			return false;
		
		try(ZipInputStream input = new ZipInputStream(new FileInputStream(file))) {
			ZipEntry entry = input.getNextEntry();
			
			for (File f: Directories.listFiles(dataFolders.questing))
				Files.delete(f.toPath());
			
			for (File f: Directories.listFiles(dataFolders.dialogue))
				Files.delete(f.toPath());
			
			while (entry != null) {
				File target;
				if(entry.getName().startsWith("dialogue/"))
					target = new File(dataFolders.dialogue, entry.getName().substring(9));
				else
					target = new File(dataFolders.questing, entry.getName());
				
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
