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

package coat.view.vcfreader;

import coat.core.poirot.dataset.Instance;
import coat.core.vcfreader.VcfFilter;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class InstanceFilter {

    private String field;
    private VcfFilter.Connector connector;
    private String value;
    private boolean strict;

    public String getField() {
        return field;
    }

    public VcfFilter.Connector getConnector() {
        return connector;
    }

    public String getValue() {
        return value;
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setConnector(VcfFilter.Connector connector) {
        this.connector = connector;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean filter(Instance instance) {
        final String instanceValue = (String) instance.getField(field);
        switch (connector) {
            case EQUALS:
                return instanceValue.equalsIgnoreCase(value);
            case CONTAINS:
                return instanceValue.toLowerCase().contains(value.toLowerCase());
            case GREATER:
                try {
                    return Integer.valueOf(instanceValue) > Integer.valueOf(value);
                } catch (NumberFormatException ex) {
                }
                break;
            case LESS:
                try {
                    return Integer.valueOf(instanceValue) < Integer.valueOf(value);
                } catch (NumberFormatException ex) {
                }
                break;
            case MATCHES:
                return instanceValue.matches(value);
            case DIFFERS:
                return !instanceValue.toLowerCase().equals(value.toLowerCase());
            case PRESENT:
                return instanceValue != null;
            case NOT_PRESENT:
                return instanceValue == null;
        }
        return strict;
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }
}
