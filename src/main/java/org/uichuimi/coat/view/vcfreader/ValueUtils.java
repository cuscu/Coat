package org.uichuimi.coat.view.vcfreader;

import org.uichuimi.vcf.variant.VcfConstants;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.StringJoiner;

public class ValueUtils {
	private static final NumberFormat DECIMAL = new DecimalFormat("#.###");

	private ValueUtils(){}
	public static Object getValue(String text, String type) {
		return null;
	}

	public static String toString(Object value) {
		if (value == null) return VcfConstants.EMPTY_VALUE;
		if (value instanceof Iterable) {
			final StringJoiner joiner = new StringJoiner(VcfConstants.ARRAY_DELIMITER);
			for (Object element : ((Iterable) value)) joiner.add(toString(element));
			return joiner.toString();
		}
		if (value instanceof Double || value instanceof Float) return DECIMAL.format(value);
		return String.valueOf(value);
	}
}
