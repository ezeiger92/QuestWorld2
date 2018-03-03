package com.questworld.api.contract;

import com.questworld.api.annotation.NoImpl;

@NoImpl
public interface IStateful {
	default boolean apply() {
		return true;
	}
	
	default boolean discard() {
		return false;
	}
	
	default IStateful getState() {
		return this;
	}
}
