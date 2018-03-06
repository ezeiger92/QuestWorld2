package com.questworld.api.contract;

import java.util.UUID;

import com.questworld.api.annotation.NoImpl;

@NoImpl
public interface DataObject {
	UUID getUniqueId();
}
