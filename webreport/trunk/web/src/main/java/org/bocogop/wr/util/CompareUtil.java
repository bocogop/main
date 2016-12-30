package org.bocogop.wr.util;

import java.math.BigDecimal;

public class CompareUtil {

	/**
	 * Compares two big decimals using their compareTo method; accommodates
	 * null's.
	 * 
	 * @param lhs
	 * @param rhs
	 * @return
	 */
	public static int nullSafeCompareByValue(BigDecimal lhs, BigDecimal rhs) {
		if (lhs == rhs)
			return 0;

		if (lhs == null)
			return -1;

		if (rhs == null)
			return 1;

		return lhs.compareTo(rhs);
	}
	
}