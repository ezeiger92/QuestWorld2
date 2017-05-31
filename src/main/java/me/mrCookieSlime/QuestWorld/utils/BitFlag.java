package me.mrCookieSlime.QuestWorld.utils;

import java.util.ArrayList;
import java.util.List;

public class BitFlag {
	
	/**
	 * Makes a bit string from an enum
	 * 
	 * @param  flags Enum values to convert
	 * @return A magic number (bit string)
	 */
	public static long getBits(BitString... flags) {
		long result = 0;
		for(BitString f : flags)
			result |= (1 << f.ordinal());
		
		return result;
	}
	
	/**
	 * Converts a bit string into a list of enum values
	 * 
	 * @param  <T> Enum implementing BitString
	 * @param  values EnumType.values(), because I'd rather not use reflection magic to get this
	 * @param  bitString The bit string generated with BitFlags.getBits() 
	 * @return A list containing every value flagged in the bit string
	 */
	public static <T extends BitString> List<T> getFlags(T[] values, long bitString) {
		List<T> results = new ArrayList<T>();
		
		for(int i = 0; i < values.length; ++i)
			if((bitString & (1 << i)) != 0)
				results.add(values[i]);
		
		return results;
	}

	public interface BitString {
		int ordinal();
	}
}
