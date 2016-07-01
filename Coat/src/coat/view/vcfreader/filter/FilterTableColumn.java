/*
 * Copyright (c) UICHUIMI 2016
 *
 * This file is part of Coat.
 *
 * Coat is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package coat.view.vcfreader.filter;

import coat.view.vcfreader.VariantsTable;
import javafx.scene.control.TableColumn;

/**
 * A column that can be filtered.
 */
public abstract class FilterTableColumn<S, T> extends TableColumn<S, T> {

    private final VariantsTable table;

    public FilterTableColumn(VariantsTable table, String title) {
        super(title);
        this.table = table;
    }

    /**
     * Forces table to be filtered.
     * <p>
     * <code>table.filter()</code>
     */
    protected void updateTable() {
        table.filter();
    }

    /**
     * Filters the item.
     *
     * @param item
     * @return true it the item passes the filter
     */
    public boolean filter(S item) {
        return filter(item, getCellObservableValue(item).getValue());
    }

    /**
     * Filter the item.
     *
     * @param item Row element
     * @return true if the item passes the filter
     */
    protected abstract boolean filter(S item, T value);

    public abstract void clear();
}
