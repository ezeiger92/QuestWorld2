package com.questworld.util;

public enum Versions {
	v1_8_R3,
	v1_12_2,
	v1_13_2,
	;
	private final Version version;
	private final Version versionSpigot;
	private final Version versionPaper;
	private final Version versionTaco;
	
	Versions() {
		version = Version.ofString(name());
		versionSpigot = Version.ofString(name() + "_SPIGOT");
		versionPaper = Version.ofString(name() + "_PAPER");
		versionTaco = Version.ofString(name() + "_TACO");
	}
	
	public Version get() {
		return version;
	}
	
	public Version getSpigot() {
		return versionSpigot;
	}
	
	public Version getPaper() {
		return versionPaper;
	}
	
	public Version getTaco() {
		return versionTaco;
	}
}
