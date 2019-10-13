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

package org.uichuimi.coat.view.vcfreader;

import org.uichuimi.coat.utils.OS;
import org.uichuimi.vcf.header.VcfHeader;
import org.uichuimi.vcf.variant.Variant;

import java.util.Arrays;
import java.util.List;

/**
 * Created by uichuimi on 22/03/17.
 */
public class VcfFilter {

    private static VcfHeader header;
    private String column;
    private String key;
    private Connector connector;
    private Object value;

    public VcfFilter(String column, String key, Connector connector, Object value) {
        this.column = column;
        this.key = key;
        this.connector = connector;
        this.value = value;
    }

    public static void setVcfHeader(VcfHeader header) {
        VcfFilter.header = header;
    }

    public boolean filter(Variant variant) {
        final Object variantValue = getVariantValue(variant);
        switch (getType()) {
            case "String":
            case "Character":
                return filterString((String) variantValue);
            case "Float":
                return filterFloat((Double) variantValue);
            case "Integer":
                return filterInteger((Integer) variantValue);
            case "Flag":
                return filterFlag((Boolean) variantValue);
        }
        return true;
    }

    private boolean filterFlag(Boolean variantValue) {
        if (variantValue == null) return connector == Connector.FALSE;
        else if (variantValue) return connector == Connector.TRUE;
        else return connector == Connector.FALSE;
    }

    private boolean filterInteger(Integer variantValue) {
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
        if (variantValue == null) return connector != Connector.TRUE;
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
        }
        return false;
    }

    private Object getVariantValue(Variant variant) {
        if (column.equals("INFO"))
            if (variant.getInfo().contains(key)) return variant.getInfo().get(key);
            else return null;
        if (column.equalsIgnoreCase("chrom") || column.equalsIgnoreCase("chromosome"))
            return variant.getCoordinate().getChrom();
        if (column.equalsIgnoreCase("pos") || column.equalsIgnoreCase("position"))
            return variant.getCoordinate().getPosition();
        if (column.equalsIgnoreCase("id"))
            return variant.getIdentifiers();
        if (column.equalsIgnoreCase("qual") || column.equalsIgnoreCase("quality"))
            return variant.getQuality();
        if (column.equalsIgnoreCase("ref") || column.equalsIgnoreCase("reference"))
            return variant.getReferences();
        if (column.equalsIgnoreCase("alt") || column.equalsIgnoreCase("alternative"))
            return variant.getAlternatives();
        if (column.equalsIgnoreCase("filter"))
            return variant.getFilters();
        return null;
    }

    public String getType() {
        if (column.equals("INFO"))
            return header.getComplexHeader("INFO", key).getValue("Type");
        final List<String> stringColumns = Arrays.asList("chrom", "chromosome", "position", " ref",
                "reference", "alt", "alternative", "filter", "id");
        for (String sc : stringColumns) if (sc.equalsIgnoreCase(column)) return "String";
        if (column.equalsIgnoreCase("pos") || column.equalsIgnoreCase("position"))
            return "Integer";
        if (column.equalsIgnoreCase("qual") || column.equalsIgnoreCase("quality"))
            return "Float";
        return null;
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
