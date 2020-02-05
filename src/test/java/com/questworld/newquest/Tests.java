package com.questworld.newquest;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class Tests {
	public static class Thing {
		
	}
	
	public static class Thing1 extends Thing {
		
	}
	
	public static class Thing2 extends Thing {
		
	}
	
	Map<Class<Thing>, Object> map = new HashMap<>();
	
	public void insert(Class<? extends Thing> key, Object value) {
		map.put((Class)key, value);
	}
	
	public Object get(Class<? extends Thing> key) {
		return map.get(key);
	}

	@Test
	public void test() {
		Object one = new Object();
		Object two = new Object();

		insert(Thing1.class, one);
		insert(Thing2.class, two);

		Object res0 = get(Thing.class);
		Object res1 = get(Thing1.class);
		Object res2 = get(Thing2.class);
		
		assertNull(res0);
		assertEquals(one, res1);
		assertEquals(two, res2);
	}
}
