package me.mrCookieSlime.QuestWorld.api;

import java.util.Arrays;
import java.util.function.Consumer;

public enum Translation implements Translator {
	default_category("defaults.category.name"),
	default_quest   ("defaults.quest.name"),
	default_mission ("defaults.mission.name"),
	default_prefix  ("defaults.prefix"),
	
	book_display("book.display"),
	book_lore   ("book.lore"),
	
	nav_display("navigation.display", "page",     "pages"),
	nav_lore(   "navigation.lore",    "pre-next", "pre-last"),
	nav_next(   "navigation.prefix-next"),
	nav_prev(   "navigation.prefix-last"),
	nav_nextbad("navigation.prefix-next-inactive"),
	nav_prevbad("navigation.prefix-last-inactive"),
	
	category_created   ("editor.category.created",     "name"),
	category_deleted   ("editor.category.deleted",     "name"),
	category_namechange("editor.category.name-change", "name"),
	category_nameset   ("editor.category.name-set",    "name", "name_old"),
	category_permchange("editor.category.perm-change", "name", "perm"),
	category_permset   ("editor.category.perm-set",    "name", "perm", "perm_old"),
	
	quest_created   ("editor.quest.created",     "name"),
	quest_deleted   ("editor.quest.deleted",     "name"),
	quest_namechange("editor.quest.name-change", "name"),
	quest_nameset   ("editor.quest.name-set",    "name", "name_old"),
	quest_permchange("editor.quest.perm-change", "name", "perm"),
	quest_permset   ("editor.quest.perm-set",    "name", "perm", "perm_old"),
	
	// TODO better names, PH
	mission_await("editor.await-mission-name"),
	mission_name("editor.edit-mission-name"),
	
	dialog_add("editor.add-dialogue"),
	dialog_set("editor.set-dialogue", "path"),
	
	mission_desc("editor.misssion-description"),
	
	killmission_rename("editor.rename-kill-mission"),
	killtype_rename("editor.renamed-kill-type"),
	citizen_rename("editor.renamed-citizen"),
	location_rename("editor.renamed-location"),
	// End PH
	
	notify_timefail    ("notifications.task-failed-timeframe",  "quest"),
	notify_timestart   ("notifications.task-timeframe-started", "task", "time"),
	notify_completetask("notifications.task-completed",         "quest"),

	party_errorfull  ("party.full",       "max"),
	party_errorabsent("party.not-online", "name"),
	party_errormember("party.already",    "name"),
	party_playerpick ("party.invite"),
	party_playeradd  ("party.invited",    "name"),
	party_playerjoin ("party.join",       "name"),
	party_playerkick ("party.kicked",     "name"),
	party_groupinvite("party.invitation", "name"),
	party_groupjoin  ("party.joined",     "name"),
	;
	private String path;
	private String[] placeholders;
	Translation(String path, String... placeholders) {
		this.path = path;
		this.placeholders = placeholders;
		for(int i = 0; i < this.placeholders.length; ++i)
			this.placeholders[i] = "%" + this.placeholders[i] + "%"; 
	}
	
	@Override
	public String path() {
		return path;
	}
	
	@Override
	public String[] placeholders() {
		return placeholders;
	}
	
	@Override
	public String toString() {
		return name() + " {path: " + path() + ", placeholders: " + Arrays.asList(placeholders()).toString() + "}";
	}
	
	public static void forEach(Consumer<Translator> func) {
		for(Translation t : values())
			func.accept(t);
	}
}
