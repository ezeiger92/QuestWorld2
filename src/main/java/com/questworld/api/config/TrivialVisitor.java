package com.questworld.api.config;

public class TrivialVisitor implements ConfigVisitor {
	private ConfigAction response;
	
	public TrivialVisitor(ConfigAction response) {
		this.response = response;
	}
	
	@Override
	public void branch(String propName, Object propVal, Object configVal, int depth) {
	}

	@Override
	public void leaf(String propName, Object propVal, Object configVal, int depth) {
	}
	
	@Override
	public ConfigAction getAction() {
		return response;
	}
}
