package com.questworld.util.adapter;

import java.lang.reflect.Method;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public enum Action {
	MAKE_PLAYER_HEAD(void.class, ItemStack.class, OfflinePlayer.class),
	MAKE_SPAWN_EGG(void.class, ItemStack.class, EntityType.class),
	SEND_ACTIONBAR(void.class, Player.class, String.class),
	SEND_TITLE(void.class, Player.class, String.class, String.class, int.class, int.class, int.class),
	
	@Deprecated
	MAKE_SHAPELESS(void.class, String.class, ItemStack.class),
	
	@Deprecated
	SET_ITEM_DAMAGE(void.class, ItemStack.class, int.class),
	;
	
	private final Class<?>[] expectedParams;
	private final Class<?> expectedReturn;
	
	Action(Class<?> expectedReturn, Class<?>... args) {
		this.expectedParams = args;
		this.expectedReturn = expectedReturn;
	}
	
	public void validate(Method method) {
		Class<?> providedReturn = method.getReturnType();
		
		if (!providedReturn.isAssignableFrom(expectedReturn)) {
			throw new IllegalStateException("Provided definition for " + this +
					" has return type " + providedReturn +
					", which does not conform to " + expectedReturn);
		}
		
		for (int i = 0; i < method.getParameterCount(); ++i) {
			Class<?> expectedParam = expectedParams[i];
			Class<?> providedParam = method.getParameterTypes()[i];
			
			if(!providedParam.isAssignableFrom(expectedParam)) {
				throw new IllegalStateException("Provided definition for " + this +
						" has parameter " + (i + 1) + " with type " + providedParam +
						", which is not based on " + expectedParam);
			}
		}
	}
}
