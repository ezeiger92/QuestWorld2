package com.questworld.api.config;

public class UpdatingVisitor implements ConfigVisitor {
	boolean updatedConfig = false;
	boolean updatedProperties = false;
	
	@Override
	public void branch(String propName, Object propVal, Object configVal, int depth) {
		checkUpdate(propVal, configVal);
	}

	@Override
	public void leaf(String propName, Object propVal, Object configVal, int depth) {
		checkUpdate(propVal, configVal);
	}
	
	private void checkUpdate(Object propVal, Object configVal) {
		updatedConfig |= (configVal == null) && (propVal != null);
		updatedProperties |= (configVal != null) && ((propVal == null) || (propVal != configVal));
	}

	public boolean UpdatesConfig() {
		return updatedConfig;
	}
	
	public boolean UpdatesProperties() {
		return updatedProperties;
	}

	@Override
	public ConfigAction getAction() {
		return ConfigAction.MERGE;
	}
}
