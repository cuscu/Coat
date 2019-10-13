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

package org.uichuimi.coat.view.graphic;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;

/**
 * A convenient Cell that plots the index of the cell. Every cell has styleClass 'index-cell' to
 * style with CSS.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class IndexCell<S, T> extends TableCell<S, T> {

    /**
     * Creates a new index cell that will have "index-cell" css class and will be aligned on the
     * center right.
     */
    public IndexCell() {
        getStyleClass().add("index-cell");
        setAlignment(Pos.CENTER_RIGHT);
        setGraphic(null);
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        setText((empty) ? null : String.valueOf(1 + getIndex()));
    }

}
