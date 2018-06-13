package com.questworld.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for easy bit flagging via enum types.
 * 
 * @author Erik Zeiger
 */
public final class BitFlag {

	/**
	 * Makes a bit string from an enum
	 * 
	 * @see BitString
	 * 
	 * @param flags Enum values to convert
	 * @return A magic number (bit string)
	 */
	public static long getBits(BitString... flags) {
		long result = 0;
		for (BitString f : flags)
			result |= (1 << f.ordinal());

		return result;
	}

	/**
	 * Converts a bit string into a list of enum values.
	 * 
	 * @see BitString
	 * 
	 * @param <T> Enum implementing BitString
	 * @param values EnumType.values(), because I'd rather not use reflection magic
	 *            to get this
	 * @param bitString The bit string generated with BitFlags.getBits()
	 * @return A list containing every value flagged in the bit string
	 */
	public static <T extends BitString> List<T> getFlags(T[] values, long bitString) {
		List<T> results = new ArrayList<T>();

		for (int i = 0; i < values.length; ++i)
			if ((bitString & (1 << i)) != 0)
				results.add(values[i]);

		return results;
	}

	/**
	 * Sub-interface whose requirements are met by any enum type. Implementing this
	 * interface allows {@link BitFlag#getBits getBits} and {@link BitFlag#getFlags
	 * getFlags} to function.
	 */
	public interface BitString {
		int ordinal();

		static long ALL = -1L;
	}
}
