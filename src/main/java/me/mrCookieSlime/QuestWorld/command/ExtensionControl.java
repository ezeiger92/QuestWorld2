package me.mrCookieSlime.QuestWorld.command;


import java.util.Locale;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.mrCookieSlime.QuestWorld.QuestWorldPlugin;
import me.mrCookieSlime.QuestWorld.listener.ExtensionInstaller;
import me.mrCookieSlime.QuestWorld.util.Text;

public final class ExtensionControl {
	private ExtensionControl() {
	}
	
	private static void help(CommandSender sender, String label) {
		sender.sendMessage(Text.colorize("&3== &b/", label, " extension &3== "));
		sender.sendMessage(Text.colorize("  &blist &7- Lists all extensions"));
		sender.sendMessage(Text.colorize("  &benable <extension> &7- Enables an extension"));
		sender.sendMessage(Text.colorize("  &bdisable <extension> &4&l*&7 - Disables an extension"));
	}
	
	public static void func(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length == 0) {
			help(sender, label);
			return;
		}
		
		ExtensionInstaller extensions = QuestWorldPlugin.instance().getImpl().getExtensions();
		
		String arg1 = args[0].toLowerCase(Locale.US);
		
		if(arg1.equals("list")) {
			sender.sendMessage(Text.colorize("&3== &bExtensions &3== "));
			for(String ext : extensions.getActiveExtensions().stream()
					.map(e -> e.getName())
					.sorted((s1, s2) -> s1.compareToIgnoreCase(s2))
					.collect(Collectors.toList()))
				sender.sendMessage(Text.colorize("  &b", ext));
			for(String ext : extensions.getInactiveExtensions().stream()
					.map(e -> e.getName())
					.sorted((s1, s2) -> s1.compareToIgnoreCase(s2))
					.collect(Collectors.toList()))
				sender.sendMessage(Text.colorize("  &c", ext));
		}
		else {
			@SuppressWarnings("unused")
			boolean useEnable;
			if(arg1.equals("enable")) {
				useEnable = true;
			}
			else if(arg1.equals("disable")) {
				useEnable = false;
			}
			else {
				help(sender, label);
				return;
			}

			sender.sendMessage(Text.colorize("&cThis is a work in progress and doesn't function yet"));
			return;
			
			/*String type = useEnable ? "enable" : "disable"; 
			
			if(args.length > 1) {
				String arg2 = args[1].toLowerCase(Locale.US);
				
				List<Path> extensionFiles;
				try {
					extensionFiles = Files.list(QuestWorldPlugin.getPath("data.extensions").toPath())
							.filter(path -> !Files.isDirectory(path))
							.collect(Collectors.toList());
				} catch (Exception e) {
					sender.sendMessage(Text.colorize("&cError reading extensions, see console"));
					Log.warning("Could not read extensions");
					e.printStackTrace();
					return;
				}
				
				for(Path extensionFile : extensionFiles) {
					String fileName = extensionFile.toFile().getName();
					if(fileName.equalsIgnoreCase(arg2)) {
						if(useEnable) {
							if(fileName.endsWith(".jar")) {
								sender.sendMessage(Text.colorize("&cExtension ", arg2, " is already enabled"));
							}
							else {
								enable(sender, extensionFile);
							}
						}
						else {
							if(!fileName.endsWith(".jar")) {
								sender.sendMessage(Text.colorize("&cExtension ", arg2, " is not enabled"));
							}
							else {
								disable(sender, extensionFile);
							}
						}
						
						return;
					}
				}
				sender.sendMessage(Text.colorize("&cExtension ", arg2, " was not found"));
				
			}
			else
				sender.sendMessage(Text.colorize("&c/", label, " extension ", type, " <extension>"));*/
		}
	}
	
	/*private static void enable(CommandSender sender, Path file) {
		String fileName = file.toFile().getName();
		fileName = fileName.substring(0, fileName.lastIndexOf('.')) +".jar";
		try {
			file = Files.move(file, file.resolveSibling(fileName));
		} catch (Exception e) {
			sender.sendMessage(Text.colorize("&cError enabling extension, see console"));
			Log.warning("Error enabling extension " + fileName);
			e.printStackTrace();
			return;
		}
		
		QuestWorldPlugin.getImpl().getPlugin().getLoader().load(file.toFile());
	}
	
	private static void disable(CommandSender sender, Path file) {
		String fileName = file.toFile().getName();
		fileName = fileName.substring(0, fileName.lastIndexOf('.')) +".disabled";
		try {
			Files.move(file, file.resolveSibling(fileName));
		} catch (IOException e) {
			sender.sendMessage(Text.colorize("&cError enabling extension, see console"));
			Log.warning("Error enabling extension " + file.toFile().getName());
			e.printStackTrace();
			return;
		}
		
	}*/
}
