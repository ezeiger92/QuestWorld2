package com.questworld.api.config;

public enum ConfigAction {
	NONE(false, false),
	LOAD(true, false),
	SAVE(false, true),
	MERGE(true, true),
	;
	private boolean modifiesProps;
	private boolean modifiesConfig;
	
	ConfigAction(boolean modifiesProps, boolean modifiesConfig) {
		this.modifiesProps = modifiesProps;
		this.modifiesConfig = modifiesConfig;
	}
	
	public boolean modifiesInstance() {
		return modifiesProps;
	}
	
	public boolean modifiesFile() {
		return modifiesConfig;
	}
}
