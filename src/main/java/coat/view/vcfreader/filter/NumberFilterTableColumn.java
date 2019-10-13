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

package coat.view.vcfreader.filter;

import coat.view.vcfreader.VariantsTable;

import java.util.Arrays;
import java.util.List;

/**
 * Use this column to filter numbers that are stored as Strings.That's because Variant stores info as Strings. All
 * numbers are converted to Double to compare.
 * <p>
 * Created by uichuimi on 30/06/16.
 */
public class NumberFilterTableColumn<S, T> extends ConnectorTextFilterTableColumn<S, T> {

    public NumberFilterTableColumn(VariantsTable table, String title) {
        super(table, title);
    }

    @Override
    protected List<Connector> getConnectors() {
        return Arrays.asList(ConnectorTextFilterTableColumn.Connector.EQUALS,
                ConnectorTextFilterTableColumn.Connector.DIFFERS,
                ConnectorTextFilterTableColumn.Connector.GREATER,
                ConnectorTextFilterTableColumn.Connector.LESS);
    }

    @Override
    protected boolean filter(S item, T value) {
        final String filterText = getFilterText();
        if (filterText == null || filterText.isEmpty()) return true;
        try {
            if (value == null || ((String) value).isEmpty()) return !isStrict();
            final double val = Double.valueOf((String) value);
            final double filterValue = Double.valueOf(filterText);
            final int compare = Double.compare(val, filterValue);
            switch (getConnector()) {
                case DIFFERS:
                    return compare != 0;
                case LESS:
                    return compare < 0;
                case GREATER:
                    return compare > 0;
                case EQUALS:
                default:
                    return compare == 0;
            }
        } catch (NumberFormatException e) {
            return true;
        }
    }
}
