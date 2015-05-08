/*
 * Copyright (C) 2014 unidad03
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
package coat.view.graphic;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;

/**
 * This Cell is a TextField that can be read but not written.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class NaturalCell extends TableCell {

    private final TextField textField = new TextField();

    final ListChangeListener<String> listChangeListener = (ListChangeListener<String>) change -> {
        change.next();
        if (change.wasAdded()) textField.getStyleClass().addAll(change.getAddedSubList());
        else if (change.wasRemoved()) textField.getStyleClass().removeAll(change.getRemoved());
    };

    /**
     * Creates a new NaturalCell, which replaces the cell with a non-editable TextField.
     */
    public NaturalCell() {
        textField.styleProperty().bind(styleProperty());
        tableRowProperty().addListener(new ChangeListener<TableRow>() {
            @Override
            public void changed(ObservableValue<? extends TableRow> observable, TableRow oldTableRow, TableRow newTableRow) {
                if (oldTableRow != null) oldTableRow.getStyleClass().removeListener(listChangeListener);
                if (newTableRow != null) newTableRow.getStyleClass().addListener(listChangeListener);
            }
        });
        textField.setEditable(false);
        textField.setBackground(Background.EMPTY);
        textField.setPadding(new Insets(0));
        textField.setOnMouseClicked(event -> getTableView().getSelectionModel().select(getTableRow().getIndex()));
    }

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) writeItem(item);
        else setGraphic(null);
    }

    private void writeItem(Object item) {
        if (item != null) {
            textField.setText(item.toString());
            setTooltip(new Tooltip(item.toString()));
        }
        setGraphic(textField);
    }
}
