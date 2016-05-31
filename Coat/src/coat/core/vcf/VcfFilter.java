/******************************************************************************
 * Copyright (C) 2015 UICHUIMI                                                *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify it    *
 * under the terms of the GNU General Public License as published by the      *
 * Free Software Foundation, either version 3 of the License, or (at your     *
 * option) any later version.                                                 *
 *                                                                            *
 * This program is distributed in the hope that it will be useful, but        *
 * WITHOUT ANY WARRANTY; without even the implied warranty of                 *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 ******************************************************************************/

package coat.core.vcf;

import coat.utils.OS;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import vcf.Variant;

/**
 * This class represents a filter for a VCF file. The filter is characterized by a field (CHROM, POS,
 * INFO...), a connector (greater than, equals...) and a value. When a vcf is passed to the filter
 * it is read: vcf.field connector value (vcf.chrom is equals to 7). As a particular case of
 * VCF, a vcf can be filtered by its INFO field, so when the selected field is INFO, the
 * selectedInfo is activated.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class VcfFilter {

    private final Property<Connector> connectorProperty = new SimpleObjectProperty<>();
    private final Property<String> valueProperty = new SimpleStringProperty();
    private final Property<String> infoProperty = new SimpleStringProperty();
    private final Property<Field> fieldProperty = new SimpleObjectProperty<>();
    private final Property<Boolean> enabledProperty = new SimpleBooleanProperty(true);
    private final Property<Boolean> strictProperty = new SimpleBooleanProperty(true);
    private final Property<String> nameProperty = new SimpleObjectProperty<>();

    /**
     * Creates a new VcfFIlter with default connector EQUALS and default field CHROMOSOME.
     */
    public VcfFilter() {
    }

    public VcfFilter(String name) {
        this.nameProperty.setValue(name);
    }


    public VcfFilter(Field field, String selectedInfo, Connector connector, String value) {
        this.valueProperty.setValue(value);
        this.connectorProperty.setValue(connector);
        this.fieldProperty.setValue(field);
        this.infoProperty.setValue(selectedInfo);
    }

    public String getValue() {
        return valueProperty.getValue();
    }

    public void setValue(String value) {
        this.valueProperty.setValue(value);
    }

    public Connector getConnector() {
        return connectorProperty.getValue();
    }

    public void setConnector(Connector connector) {
        this.connectorProperty.setValue(connector);
    }

    public Property<Connector> getConnectorProperty() {
        return connectorProperty;
    }

    public Field getField() {
        return fieldProperty.getValue();
    }

    public void setField(Field field) {
        this.fieldProperty.setValue(field);
    }

    public Property<Field> getFieldProperty() {
        return fieldProperty;
    }

    public String getSelectedInfo() {
        return infoProperty.getValue();
    }

    public void setSelectedInfo(String selectedInfo) {
        this.infoProperty.setValue(selectedInfo);
    }

    public Property<String> getValueProperty() {
        return valueProperty;
    }

    public Property<String> getInfoProperty() {
        return infoProperty;
    }

    /**
     * Returns true in case this vcf passes this pass or pass can NOT be applied due to
     * field/connector/value incompatibilities.
     *
     * @param variant the vcf to pass.
     * @return true if passes the pass or the pass cannot be applied, false otherwise.
     */
    public boolean pass(Variant variant) {
        return !enabledProperty.getValue() || checkField(variant);
    }

    private boolean checkField(Variant variant) {
        final Field field = fieldProperty.getValue();
        if (field == null) return true;
        final String value = valueProperty.getValue();
        final String info = infoProperty.getValue();
        // Get the value (one of the Field.values())
        String stringValue = null;
        double doubleValue = Double.MIN_VALUE;
        switch (field) {
            case CHROMOSOME:
                stringValue = variant.getChrom();
                break;
            case POSITION:
                doubleValue = variant.getPosition();
                break;
            case QUALITY:
                doubleValue = variant.getQual();
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
                if (info == null) return true;
                stringValue = (String) variant.getInfo().get(info);


////                Map<String, Object> map = vcf.getInfoValues();
//                if (map.containsKey(info)) {
//                    final Object val = map.get(info);
//                    if (val != null) {
//                        if (val.getClass() == String.class) {
//                            stringValue = (String) val;
//                            try {
//                                doubleValue = Double.valueOf(stringValue);
//                            } catch (NumberFormatException e) {
//                                // If not a number
//                            }
//                        } else {
//                            try {
//                                doubleValue = (Double) map.get(info);
//                            } catch (NumberFormatException e) {
//                                // If not a number
//                            }
//                        }
//                    }
//                }
//                break;
        }
        if (stringValue != null)
            try {
                doubleValue = Double.valueOf(stringValue);
            } catch (NumberFormatException ignored) {

            }
        final Connector connector = connectorProperty.getValue();
        if (connector == null) return true;
        switch (connector) {
            case CONTAINS:
                if (stringValue != null) return stringValue.contains(value);
                break;
            case DIFFERS:
                if (stringValue != null) return !value.equals(stringValue);
                break;
            case EQUALS:
                if (doubleValue > Double.MIN_VALUE)
                    try {
                        return Double.valueOf(value) == doubleValue;
                    } catch (NumberFormatException e) {
                        return true;
                    }
                else if (stringValue != null) return stringValue.equals(value);
                break;
            case GREATER:
                if (doubleValue > Double.MIN_VALUE)
                    try {
                        return doubleValue > Double.valueOf(value);
                    } catch (NumberFormatException e) {
                        // If user did not input a number, filter is passed
                        return true;
                    }
                break;
            case LESS:
                if (doubleValue > Double.MIN_VALUE)
                    try {
                        return doubleValue < Double.valueOf(value);
                    } catch (NumberFormatException e) {
                        // If user did not input a number, pass is passed
                        return true;
                    }
                break;
            case MATCHES:
                if (stringValue != null) return stringValue.matches(value);
                break;
            case PRESENT:
                return variant.getInfo().get(info) != null;
            case NOT_PRESENT:
                return variant.getInfo().get(info) == null;
        }
        return strictProperty.getValue();
    }


    public Property<Boolean> getEnabledProperty() {
        return enabledProperty;
    }

    public Property<Boolean> getStrictProperty() {
        return strictProperty;
    }

    public Property<String> getNameProperty() {
        return nameProperty;
    }

    /**
     * The type of relation between the pass value and the field value.
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
    }

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
