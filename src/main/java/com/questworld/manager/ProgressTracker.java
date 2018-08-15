package com.questworld.manager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.questworld.QuestWorldPlugin;
import com.questworld.api.QuestStatus;
import com.questworld.api.contract.ICategory;
import com.questworld.api.contract.IMission;
import com.questworld.api.contract.IMissionState;
import com.questworld.api.contract.IQuest;
import com.questworld.util.Reloadable;
import com.questworld.util.Text;

public class ProgressTracker implements Reloadable {
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
	private final File configFile;
	private final YamlConfiguration config;

	private static File fileFor(UUID uuid) {
		return new File(QuestWorldPlugin.getAPI().getDataFolders().playerdata, uuid.toString() + ".yml");
	}

	public static boolean exists(UUID uuid) {
		return fileFor(uuid).exists();
	}

	public ProgressTracker(UUID uuid) {
		configFile = fileFor(uuid);
		config = YamlConfiguration.loadConfiguration(configFile);
	}

	@Override
	public void onSave() {
		try {
			config.save(configFile);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onReload() {
	}

	@Override
	public void onDiscard() {
		try {
			config.load(configFile);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public YamlConfiguration config() {
		return config;
	}

	//// PARTY
	public UUID getPartyLeader() {
		return Text.toUniqueId(config.getString("party.associated", null));
	}

	public void setPartyLeader(UUID uuid) {
		if (uuid != null)
			config.set("party.associated", uuid.toString());
		else
			config.set("party.associated", null);
	}

	public Set<UUID> getPartyMembers() {
		return config.getStringList("party.members").stream().map(Text::toUniqueId).filter(uuid -> uuid != null)
				.collect(Collectors.toSet());
	}

	public void setPartyMembers(Set<UUID> members) {
		config.set("party.members", members.stream().map(UUID::toString).collect(Collectors.toList()));
	}

	public Set<UUID> getPartyPending() {
		return config.getStringList("party.pending-requests").stream().map(Text::toUniqueId)
				.filter(uuid -> uuid != null).collect(Collectors.toSet());
	}

	public void setPartyPending(Set<UUID> pending) {
		config.set("party.pending-requests", pending.stream().map(UUID::toString).collect(Collectors.toList()));
	}

	//// CATEGORY	
	@Deprecated
	private static String oldPath(ICategory category) {
		return String.valueOf(category.getID());
	}

	public void clearCategory(ICategory category) {
		for(IQuest q : category.getQuests())
			clearQuest(q);
	}

	//// QUEST
	private ConfigurationSection getQuestPath(IQuest quest, boolean create) {
		ConfigurationSection result = config.getConfigurationSection(path(quest));
		
		if(result == null) {
			ConfigurationSection old;
			
			if((old = config.getConfigurationSection(oldPath(quest))) != null) {
				result = config.createSection(path(quest), old.getValues(true));
				config.set(oldPath(quest), null);
			}
			else if(create) {
				result = config.createSection(path(quest));
			}
		}
		
		return result;
	}
	
	private static String path(IQuest quest) {
		return "quests." + quest.getUniqueId();
	}
	
	@Deprecated
	private static String oldPath(IQuest quest) {
		return oldPath(quest.getCategory()) + "." + quest.getID();
	}

	public long getQuestRefresh(IQuest quest) {
		long result = -1;
		ConfigurationSection section = getQuestPath(quest, false);
		
		if(section != null) {
			String end = section.getString("cooldown", null);
			if (end != null)
				try {
					result = dateFormat.parse(end).getTime();
				}
				catch (ParseException e) {
					e.printStackTrace();
				}
		}

		return result;
	}

	public void setQuestRefresh(IQuest quest, long until) {
		getQuestPath(quest, true).set("cooldown", dateFormat.format(until));
	}

	public QuestStatus getQuestStatus(IQuest quest) {
		QuestStatus result = QuestStatus.AVAILABLE;
		ConfigurationSection section = getQuestPath(quest, false);
		
		if(section != null) {
			String status = section.getString("status", null);
			if (status != null)
				try {
					result = QuestStatus.valueOf(status.toUpperCase(Locale.US));
				}
				catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
		}
		
		return result;
	}

	public void setQuestStatus(IQuest quest, QuestStatus state) {
		getQuestPath(quest, true).set("status", state.toString());
	}

	public boolean isQuestFinished(IQuest quest) {
		ConfigurationSection section = getQuestPath(quest, false);
		
		if(section != null) {
			return section.getBoolean("finished", false);
		}
		
		return false;
	}

	public void setQuestFinished(IQuest quest, boolean state) {
		getQuestPath(quest, true).set("finished", state);
	}

	public void clearQuest(IQuest quest) {
		config.set(path(quest), null);
		
		for(IMission m : quest.getMissions()) {
			clearMission(m);
		}
	}

	//// MISSION
	private static String path(IMission mission) {
		return "missions." + mission.getUniqueId();
	}

	@Deprecated
	private static String oldPath(IMission mission) {
		return oldPath(mission.getQuest()) + ".mission." + mission.getUniqueId();
	}

	@Deprecated
	private static String reallyOldPath(IMission mission) {
		return oldPath(mission.getQuest()) + ".mission." + mission.getIndex();
	}

	public static File dialogueFile(IMission mission) {
		return new File(QuestWorldPlugin.getAPI().getDataFolders().dialogue,
				mission.getUniqueId().toString() + ".dialogue");
	}

	@Deprecated
	public static File oldDialogueFile(IMission mission) {
		return new File(QuestWorldPlugin.getAPI().getDataFolders().dialogue,
				mission.getQuest().getCategory().getID() + "+" + mission.getQuest().getID() + "+" + mission.getIndex()
						+ ".txt");
	}

	public static void saveDialogue(IMission mission) {
		File file = dialogueFile(mission);

		if (mission.getDialogue().isEmpty()) {
			try {
				Files.deleteIfExists(file.toPath());
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		try {
			// The only downside to this is system-specific newlines
			Files.write(file.toPath(), mission.getDialogue().stream().map(Text::serializeNewline).map(Text::escapeColor)
					.collect(Collectors.toList()), StandardCharsets.UTF_8);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static List<String> readAllLines(File file) {
		try {
			return Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
		}
		catch (Exception e) {
			try {
				return Files.readAllLines(file.toPath(), StandardCharsets.US_ASCII);
			}
			catch (Exception e2) {
				e2.addSuppressed(e);
				throw new IllegalArgumentException("Only UTF8 and ANSI encodings are supported!", e2);
			}
		}
	}

	public static void loadDialogue(IMissionState mission) {
		File file = dialogueFile(mission);

		if (!file.exists()) {
			File oldFile = oldDialogueFile(mission);
			if (!oldFile.exists())
				return;

			try {
				List<String> lines = readAllLines(oldFile);
				if (lines.isEmpty())
					return;

				Files.write(file.toPath(), lines);
			}
			catch (Exception e) {
				e.printStackTrace();
				return;
			}

			try {
				Files.delete(oldFile.toPath());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			mission.setDialogue(readAllLines(file).stream().map(Text::deserializeNewline).map(Text::colorize)
					.collect(Collectors.toList()));
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	private ConfigurationSection getMissionPath(IMission mission, boolean create) {
		ConfigurationSection result = config.getConfigurationSection(path(mission));
		
		if(result == null) {
			ConfigurationSection old;
			
			if((old = config.getConfigurationSection(oldPath(mission))) != null) {
				result = config.createSection(path(mission), old.getValues(true));
				config.set(oldPath(mission), null);
			}
			else if((old = config.getConfigurationSection(reallyOldPath(mission))) != null) {
				result = config.createSection(path(mission), old.getValues(true));
				config.set(reallyOldPath(mission), null);
			}
			else if(create) {
				result = config.createSection(path(mission));
			}
		}
		
		return result;
	}

	public int getMissionProgress(IMission mission) {
		ConfigurationSection section = getMissionPath(mission, false);
		
		if(section != null) {
			return section.getInt("progress", 0);
		}
		
		return 0;
	}

	public void setMissionProgress(IMission mission, int progress) {
		getMissionPath(mission, true).set("progress", progress);
	}

	public long getMissionEnd(IMission mission) {
		ConfigurationSection section = getMissionPath(mission, false);
		
		if(section != null) {
			return section.getLong("complete-until", 0);
		}
		
		return 0;
	}

	public void setMissionEnd(IMission mission, Long time) {
		getMissionPath(mission, true).set("complete-until", time);
	}

	public void clearMission(IMission mission) {
		config.set(path(mission), null);
	}
}
