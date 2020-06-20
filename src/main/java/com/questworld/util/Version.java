package com.questworld.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.bukkit.Bukkit;

public final class Version implements Comparable<Version> {
	private static final String CURRENT_VER;
	private final String serialVersion;
	private final int hash;
	
	private static final Map<String, Version> cache = new HashMap<>();
	
	public static Version ofString(String version) {
		String serialVersion = processVersion(version);
		
		Version result = cache.get(serialVersion);
		
		if(result == null) {
			cache.put(serialVersion, result = new Version(serialVersion));
		}
		
		return result;
	}
	
	public static Version current() {
		return ofString(CURRENT_VER);
	}
	
	static {
		String pack = Bukkit.getServer().getClass().getPackage().getName();
		CURRENT_VER = pack.substring(pack.lastIndexOf('.') + 1);
	}
	
	private Version(String serialVersion) {
		this.serialVersion = serialVersion;
		this.hash = makeHash(serialVersion);
	}

	@Override
	public final int compareTo(Version other) {
		return Integer.compareUnsigned(hashCode(), other.hashCode());
	}
	
	public boolean lessThan(Version other) {
		return compareTo(other) < 0;
	}
	
	public boolean greaterThan(Version other) {
		return compareTo(other) > 0;
	}

	@Override
	public String toString() {
		return serialVersion;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Version)
			return other.hashCode() == hashCode();

		return false;
	}

	@Override
	public int hashCode() {
		return hash;
	}
	
	private static final int[] bitPosition;
	
	static {
		// release_version_patch_Rapi_type
		int[] bitPartition = {
				// 16 unused
				2, // MC release, 4
				5, // MC version, 32
				4, // MC patch, 16
				3, // API revision, 8
				2, // API type, 4
		};
		bitPosition = new int[bitPartition.length];

		int accum = 0;
		int end = bitPartition.length - 1;
		for (int i = 1; i <= end; ++i) {
			accum = bitPosition[end - i] = bitPartition[end - i + 1] + accum;
		}
	}
	
	private static final int makeHash(String serialVersion) {
		int hash = 0;
		int length = serialVersion.length();
		
		for (int j = 0, start = 1; j < bitPosition.length && start < length; ++j) {
			int end = serialVersion.indexOf('_', start);
			
			if(end == -1) {
				end = length;
			}
			
			if(serialVersion.charAt(start) == 'R') {
				++start;
				j = 3;
			}
			else if(!Character.isDigit(serialVersion.charAt(start))) {
				char c = serialVersion.charAt(start);
				int val = c == 'S' ? 1 : (c == 'P' ? 2 : 3);
				hash |= (val << bitPosition[4]);
				break;
			}
			
			int value = 0;
			for (int i = start; i < end; ++i) {
				value = value * 10 + (serialVersion.charAt(i) - '0');
			}
			hash |= (value << bitPosition[j]);
			
			start = end + 1;
		}
		
		return hash;
	}
	
	private static final String processVersion(String in) {
		in = in.toUpperCase(Locale.ENGLISH).replace('.', '_');

		if (in.startsWith("V"))
			in = in.substring(1);

		return "v" + in;
	}
}
