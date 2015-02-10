/*
 * Copyright (C) 2014 UICHUIMI
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package coat.vcf;

import coat.utils.OS;
import java.util.Map;

/**
 * This class represents a filter for a VCF file. The filter is characterized by a field (CHROM,
 * POS, INFO...), a connector (greater than, equals...) and a value. When a variant is passed to the
 * filter it is read: variant.field connector value (variant.chrom is equals to 7). As a particular
 * case of VCF, a variant can be filtered by its INFO field, so when the selected field is INFO, the
 * selectedInfo is activated.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class VCFFilter {

    private String value;
    private Connector connector;
    private Field field;
    private String selectedInfo;
    private boolean strict = true;
    private boolean enabled = true;

    /**
     * Creates a new VCFFIlter with default connector EQUALS and default field CHROMOSOME.
     */
    public VCFFilter() {
        connector = Connector.EQUALS;
        field = Field.CHROMOSOME;
    }

    /**
     * Creates a new VCFFilter with the given connector and field. If field is not INFO.
     *
     * @param connector the selected connector
     * @param field the selected field
     * @param selectedInfo the selected info in case INFO is selected as field
     */
    public VCFFilter(Connector connector, Field field, String selectedInfo) {
        this.connector = connector;
        this.field = field;
        this.selectedInfo = selectedInfo;
    }

    public VCFFilter(String value, Connector connector, Field field, String selectedInfo) {
        this.value = value;
        this.connector = connector;
        this.field = field;
        this.selectedInfo = selectedInfo;
    }

    /**
     * Gets the value of the filter.
     *
     * @return the value of the filter.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets a value for the filter.
     *
     * @param value the value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * The connector of the filter.
     *
     * @return the connector
     */
    public Connector getConnector() {
        return connector;
    }

    /**
     * Sets the connector
     *
     * @param connector the new connector
     */
    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    /**
     * Get the selected field
     *
     * @return the selected field
     */
    public Field getField() {
        return field;
    }

    /**
     * Set the selected field
     *
     * @param field the new selected field
     */
    public void setField(Field field) {
        this.field = field;
    }

    /**
     * gets the current selected info
     *
     * @return the selected info
     */
    public String getSelectedInfo() {
        return selectedInfo;
    }

    /**
     * Sets the new selected info
     *
     * @param selectedInfo the new selected info
     */
    public void setSelectedInfo(String selectedInfo) {
        this.selectedInfo = selectedInfo;
    }

    /**
     * If true, filter will not throw variants that do not contain the INFO field.
     *
     * @return true if accepting void values.
     */
    public boolean isStrict() {
        return strict;
    }

    /**
     * If true, filter will not throw variants that do not contain the INFO field.
     *
     * @param accept true to accept void values
     */
    public void setStrict(boolean accept) {
        this.strict = accept;
    }

    /**
     * If true it will filter variants, if false it will accept all variants.
     *
     * @return true if filter is enable
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * If true it will filter variants, if false it will accept all variants.
     *
     * @param enabled the new enable state
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns true in case this variant passes this filter or filter can NOT be applied due to
     * field/connector/value incompatibilities.
     *
     * @param variant the variant to filter.
     * @return true if passes the filter or the filter cannot be applied, false otherwise.
     */
    public boolean filter(Variant variant) {
        if (field == null) {
            return true;
        }
        if (!enabled) {
            return true;
        }
        // Get the value (one of the Field.values())
        String stringValue = null;
        double doubleValue = Double.MIN_VALUE;
        switch (field) {
            case CHROMOSOME:
                stringValue = variant.getChrom();
                break;
            case POSITION:
                doubleValue = variant.getPos();
                break;
            case QUALITY:
                doubleValue = variant.getQual();
                break;
            case FILTER:
                stringValue = variant.getFilter();
                break;
            case ID:
                stringValue = variant.getId();
                break;
            case REF:
                stringValue = variant.getRef();
                break;
            case ALT:
                stringValue = variant.getAlt();
                break;
            case INFO:
                Map<String, String> map = variant.getInfos();
                if (map.containsKey(selectedInfo)) {
                    stringValue = map.get(selectedInfo);
                    if (stringValue != null) {
                        try {
                            // Take only the first value, supposing they are comma separated
                            doubleValue = Double.valueOf(stringValue.split(",")[0]);
                        } catch (NumberFormatException e) {
                            // If not a number
                        }
                    }
                }
                break;
        }
        switch (connector) {
            case CONTAINS:
                if (stringValue != null) {
                    return stringValue.contains(value);
                }
                break;
            case DIFFERS:
                if (stringValue != null) {
                    return !value.equals(stringValue);
                }
                break;
            case EQUALS:
                if (doubleValue > Double.MIN_VALUE) {
                    try {
                        return Double.valueOf(value) == doubleValue;
                    } catch (NumberFormatException e) {
                        return true;
                    }
                } else if (stringValue != null) {
                    return stringValue.equals(value);
                }
                break;
            case GREATER:
                if (doubleValue > Double.MIN_VALUE) {
                    try {
                        return doubleValue > Double.valueOf(value);
                    } catch (NumberFormatException e) {
                        // If user did not input a number, filter is passed
                        return true;
                    }
                }
                break;
            case LESS:
                if (doubleValue > Double.MIN_VALUE) {
                    try {
                        return doubleValue < Double.valueOf(value);
                    } catch (NumberFormatException e) {
                        // If user did not input a number, filter is passed
                        return true;
                    }
                }
                break;
            case MATCHES:
                if (stringValue != null) {
                    return stringValue.matches(value);
                }
                break;
            case PRESENT:
                return variant.getInfos().containsKey(selectedInfo);
            case NOT_PRESENT:
                return !variant.getInfos().containsKey(selectedInfo);
        }
        return strict;
    }

    /**
     * The type of relation between the filter value and the field value.
     */
    public enum Connector {

        /**
         * Equals to (String or natural number).
         */
        EQUALS {

                    @Override
                    public String toString() {
                        return OS.getResources().getString("equals.to");
                    }

                },
        /**
         * Contains (String)
         */
        CONTAINS {

                    @Override
                    public String toString() {
                        return OS.getResources().getString("contains");
                    }
                },
        /**
         * Greater than (number).
         */
        GREATER {

                    @Override
                    public String toString() {
                        return OS.getResources().getString("greater.than");
                    }

                },
        /**
         * Less than (number).
         */
        LESS {

                    @Override
                    public String toString() {
                        return OS.getResources().getString("less.than");
                    }

                },
        /**
         * Regular expression (String).
         */
        MATCHES {

                    @Override
                    public String toString() {
                        return OS.getResources().getString("matches");
                    }

                },
        /**
         * Different (String, ¿number?).
         */
        DIFFERS {

                    @Override
                    public String toString() {
                        return OS.getResources().getString("differs.from");
                    }

                },
        /**
         * Exists.
         */
        PRESENT {

                    @Override
                    public String toString() {
                        return OS.getResources().getString("is.present");
                    }

                },
        /**
         * If is not present.
         */
        NOT_PRESENT {

                    @Override
                    public String toString() {
                        return OS.getResources().getString("is.not.present");
                    }

                }
    };

    /**
     * The field from the VCF.
     */
    public enum Field {

        /**
         * CHROM fields of the VCF.
         */
        CHROMOSOME,
        /**
         * POS field of the VCF.
         */
        POSITION,
        /**
         * QUAL field of the VCF.
         */
        QUALITY,
        /**
         * INFO field of the VCF.
         */
        INFO,
        /**
         * FILTER field of the VCF.
         */
        FILTER,
        /**
         * ID field of the VCF.
         */
        ID,
        /**
         * REF field of the VCF.
         */
        REF,
        /**
         * ALT field of the VCF
         */
        ALT
    }

}
