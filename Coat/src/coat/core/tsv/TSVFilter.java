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
package coat.core.tsv;

/**
 * This class represents a filter for a TSV file.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class TSVFilter {

    private String value;
    private Connector selectedConnector;
    private int selectedIndex = -1;

    /**
     * Active is true when the filter is working. If active is set to false, the filter will always
     * return true.
     */
    private boolean active = true;

    private boolean aceptingVoids = true;

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
     * @param value the new value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * The selectedConnector of the filter.
     *
     * @return the current Connector
     */
    public Connector getSelectedConnector() {
        return selectedConnector;
    }

    /**
     * Changes the connector.
     *
     * @param selectedConnector the new connector
     */
    public void setSelectedConnector(Connector selectedConnector) {
        this.selectedConnector = selectedConnector;
    }

    /**
     * Gets the selected index of the INFO, if no info is selected, return -1.
     *
     * @return the selected index or -1
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * Changes the selected index.
     *
     * @param selectedIndex the new selected index
     */
    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    /**
     * Active is true when the filter is working. If active is set to false, the filter will always
     * return true.
     *
     * @return true if filter is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Active is true when the filter is working. If active is set to false, the filter will always
     * return true.
     *
     * @param active true if you want to activate the filter.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * A void is a icarus incompatible with a filter. For instance, you specify that a column must be
     * greater than, but the filter cannot convert the value of a cell to a Double.
     *
     * @return true if when the icarus is incompatible with the filter, the icarus pass the filter,
     * false otherwise
     */
    public boolean isAceptingVoids() {
        return aceptingVoids;
    }

    /**
     * A void is a icarus incompatible with a filter. For instance, you specify that a column must be
     * greater than, but the filter cannot convert the value of a cell to a Double.
     *
     * @param aceptingVoids true if you want to keep incompatible icarus, false if you want to drop it
     */
    public void setAceptingVoids(boolean aceptingVoids) {
        this.aceptingVoids = aceptingVoids;
    }

    /**
     * Returns true in case this line passes this filter or this filter can NOT be applied due to
     * field/selectedConnector/value incompatibilities.
     *
     * @param line the line to filter
     * @return true if passes the filter or the filter cannot be applied, false otherwise.
     */
    public boolean filter(String[] line) {
        if (selectedIndex < 0) {
            return true;
        }
        if (!active) {
            return true;
        }
        String stringValue = line[selectedIndex];
        double doubleValue = Double.MIN_VALUE;
        try {
            doubleValue = Double.valueOf(stringValue);
        } catch (Exception e) {
        }
        switch (selectedConnector) {
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
                        return true;
                    }
                }
                break;
            case LESS:
                if (doubleValue > Double.MIN_VALUE) {
                    try {
                        return doubleValue < Double.valueOf(value);
                    } catch (NumberFormatException e) {
                        return true;
                    }
                }
                break;
            case MATCHES:
                if (stringValue != null) {
                    return stringValue.matches(value);
                }
        }
        return aceptingVoids;
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
                        return "is equals to";
                    }

                },
        /**
         * Contains (String).
         */
        CONTAINS {

                    @Override
                    public String toString() {
                        return "contains";
                    }
                },
        /**
         * Greater than (number).
         */
        GREATER {

                    @Override
                    public String toString() {
                        return "is greater than";
                    }

                },
        /**
         * Less than (number).
         */
        LESS {

                    @Override
                    public String toString() {
                        return "is less than";
                    }

                },
        /**
         * Regular expression (String).
         */
        MATCHES {

                    @Override
                    public String toString() {
                        return "matches";
                    }

                },
        /**
         * Different (String, ¿number?).
         */
        DIFFERS {

                    @Override
                    public String toString() {
                        return "differs from";
                    }

                }
    }

}
