package com.questworld;

import com.questworld.util.Versions;
import com.questworld.util.version.VersionDependent;

public final class Constants {
	private Constants() {
	}

	public static final String MD_LAST_MENU = "questworld.last-object";
	public static final String MD_NO_CAT_BACK = "questworld.cat-back";
	public static final String MD_NO_QUEST_BACK = "questworld.quest-back";
	public static final String MD_PAGES = "questworld.pages";
	
	public static final String CH_BOOK =
			VersionDependent.pick(Versions.v1_12_2.getTaco(), "MC|BOpen", "minecraft:book_open");
}
