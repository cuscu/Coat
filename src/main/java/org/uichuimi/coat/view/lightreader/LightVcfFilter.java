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

import org.uichuimi.coat.utils.OS;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFConstants;
import htsjdk.variant.vcf.VCFHeader;

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
            switch (header.getInfoHeaderLine(key).getType()) {
                case Integer:
                    return filterInteger(variant.getAttributeAsInt(key, 0));
                case Float:
                    return filterFloat(variant.getAttributeAsDouble(key, 0.0));
                case Flag:
                    return filterFlag(variant.getAttributeAsBoolean(key, false));
                case String:
                case Character:
                default:
                    return filterString(variant.getAttributeAsString(key,
                            VCFConstants.EMPTY_ID_FIELD));
            }
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

    private boolean filterFlag(Boolean variantValue) {
        if (variantValue == null) return connector == Connector.FALSE;
        else if (variantValue) return connector == Connector.TRUE;
        else return connector == Connector.FALSE;
    }

    private boolean filterInteger(Integer variantValue) {
        if (value.getClass().isAssignableFrom(String.class))
            try {
                value = Integer.valueOf((String) value);
            } catch (NumberFormatException ex) {
                System.err.println("Not an integer number");
                value = 0;
            }
        if (variantValue == null) return connector != Connector.TRUE;
        switch (connector) {
            case EQUALS:
                return variantValue == (int) value;
            case MORE_THAN:
                return variantValue > (int) value;
            case LESS_THAN:
                return variantValue < (int) value;
            case TRUE:
                return variantValue != 0;
            case FALSE:
                return variantValue == 0;
            case IS_NOT:
                return variantValue != (int) value;
        }
        return false;
    }

    private boolean filterFloat(Double variantValue) {
        if (value.getClass().isAssignableFrom(String.class))
            try {
                value = Double.valueOf((String) value);
            } catch (NumberFormatException ex) {
                System.err.println("Not a float number");
                value = 0;
            }
        if (variantValue == null) return connector != Connector.TRUE;
        switch (connector) {
            case EQUALS:
                return variantValue == (double) value;
            case MORE_THAN:
                return variantValue > (double) value;
            case LESS_THAN:
                return variantValue < (double) value;
            case TRUE:
                return variantValue != 0;
            case FALSE:
                return variantValue == 0;
            case IS_NOT:
                return variantValue != (double) value;
        }
        return false;
    }

    private boolean filterString(String variantValue) {
        if (!value.getClass().isAssignableFrom(String.class))
            value = String.valueOf(value);
        if (variantValue == null) return connector == Connector.FALSE;
        switch (connector) {
            case EQUALS:
                return variantValue.equals(value);
            case MORE_THAN:
                return variantValue.compareTo((String) value) > 0;
            case LESS_THAN:
                return variantValue.compareTo((String) value) < 0;
            case TRUE:
                return !variantValue.equals(".");
            case FALSE:
                return variantValue.equals(".");
            case IS_NOT:
                return !variantValue.equals(value);
            case CONTAINS:
                return variantValue.contains((String) value);
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
