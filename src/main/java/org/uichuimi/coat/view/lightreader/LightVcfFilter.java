/*
 * Copyright (c) UICHUIMI 2017
 *
 * This file is part of Coat.
 *
 * Coat is free software:
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Coat.
 *
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.uichuimi.coat.view.lightreader;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFHeader;
import org.uichuimi.coat.utils.OS;

/**
 * Created by uichuimi on 22/03/17.
 */
public class LightVcfFilter {

	private static VCFHeader header;
	private String column;
	private String key;
	private Connector connector;
	private Object value;

	public LightVcfFilter(String column, String key, Connector connector, Object value) {
		this.column = column;
		this.key = key;
		this.connector = connector;
		this.value = value;
	}

	public static void setVcfHeader(VCFHeader header) {
		LightVcfFilter.header = header;
	}

	public boolean filter(VariantContext variant) {
		if (column.equals("INFO")) {
			final Object attribute = variant.getAttribute(key);
			if (attribute == null) return true;
			if (attribute instanceof Iterable) {
				for (Object element : ((Iterable) attribute)) if (filter(element)) return true;
				return false;
			}
			return filter(attribute);
		} else {
			switch (column) {
				case "CHROM":
					return filterString(variant.getContig());
				case "POS":
					return filterInteger(variant.getStart());
				case "ID":
					return filterString(variant.getID());
				case "REF":
					return filterString(variant.getReference().getBaseString());
				case "ALT":
					return filterString(variant.getAltAlleleWithHighestAlleleCount().getBaseString());
				case "QUAL":
					return filterFloat(variant.getPhredScaledQual());
				case "FILTER":
					return filterString(variant.getFilters().toString());
			}
		}
		return true;
	}

	private boolean filter(Object element) {
		switch (header.getInfoHeaderLine(key).getType()) {
			case Integer:
				return filterInteger(element);
			case Float:
				return filterFloat(element);
			case Flag:
				return filterFlag(element);
			case String:
			case Character:
			default:
				return filterString(element);
		}
	}

	private boolean filterFlag(Object variantValue) {
		if (variantValue == null) return connector == Connector.FALSE;
		if (variantValue instanceof Boolean) return connector == Connector.TRUE;
		final Boolean value = Boolean.valueOf((String) variantValue);
		return value && connector == Connector.TRUE;
	}

	private boolean filterInteger(Object variantValue) {
		if (value.getClass().isAssignableFrom(String.class))
			try {
				value = Integer.valueOf((String) value);
			} catch (NumberFormatException ex) {
				System.err.println("Not an integer number");
				value = 0;
			}
		if (variantValue == null) return connector != Connector.TRUE;
		int val = variantValue instanceof Integer
				? (int) variantValue
				: Integer.parseInt((String) variantValue);
		switch (connector) {
			case EQUALS:
				return val == (int) value;
			case MORE_THAN:
				return val > (int) value;
			case LESS_THAN:
				return val < (int) value;
			case TRUE:
				return val != 0;
			case FALSE:
				return val == 0;
			case IS_NOT:
				return val != (int) value;
		}
		return false;
	}

	private boolean filterFloat(Object variantValue) {
		if (value.getClass().isAssignableFrom(String.class))
			try {
				value = Double.valueOf((String) value);
			} catch (NumberFormatException ex) {
				System.err.println("Not a float number");
				value = 0;
			}
		if (variantValue == null) return connector != Connector.TRUE;
		double val = variantValue instanceof Double
				? (double) variantValue
				: Double.parseDouble((String) variantValue);
		switch (connector) {
			case EQUALS:
				return val == (double) value;
			case MORE_THAN:
				return val > (double) value;
			case LESS_THAN:
				return val < (double) value;
			case TRUE:
				return val != 0;
			case FALSE:
				return val == 0;
			case IS_NOT:
				return val != (double) value;
		}
		return false;
	}

	private boolean filterString(Object variantValue) {
		if (!value.getClass().isAssignableFrom(String.class))
			value = String.valueOf(value);
		if (variantValue == null) return connector == Connector.FALSE;
		final String val = (String) variantValue;
		switch (connector) {
			case EQUALS:
				return variantValue.equals(value);
			case MORE_THAN:
				return val.compareTo((String) value) > 0;
			case LESS_THAN:
				return val.compareTo((String) value) < 0;
			case TRUE:
				return !val.equals(".");
			case FALSE:
				return val.equals(".");
			case IS_NOT:
				return !val.equals(value);
			case CONTAINS:
				return val.contains((String) value);
		}
		return false;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Connector getConnector() {
		return connector;
	}

	public void setConnector(Connector connector) {
		this.connector = connector;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public enum Connector {
		MORE_THAN {
			@Override
			public String toString() {
				return OS.getString("is.more.than");
			}
		}, EQUALS {
			@Override
			public String toString() {
				return OS.getString("is.equals.to");
			}
		}, CONTAINS {
			@Override
			public String toString() {
				return "contains";
			}
		}, TRUE {
			@Override
			public String toString() {
				return "is true";
			}
		}, FALSE {
			@Override
			public String toString() {
				return "is false";
			}
		}, IS_NOT {
			@Override
			public String toString() {
				return "is not";
			}
		}, LESS_THAN {
			@Override
			public String toString() {
				return "is less than";
			}
		}
	}
}
