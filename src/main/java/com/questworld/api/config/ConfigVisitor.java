package com.questworld.api.config;

public interface ConfigVisitor {
	void branch(String propName, Object propVal, Object configVal, int depth);
	void leaf(String propName, Object propVal, Object configVal, int depth);
	ConfigAction getAction();
}

