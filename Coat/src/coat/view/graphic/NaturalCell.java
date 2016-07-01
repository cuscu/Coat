/******************************************************************************
 * Copyright (C) 2015 UICHUIMI                                                *
 * *
 * This program is free software: you can redistribute it and/or modify it    *
 * under the terms of the GNU General Public License as published by the      *
 * Free Software Foundation, either version 3 of the License, or (at your     *
 * option) any later version.                                                 *
 * *
 * This program is distributed in the hope that it will be useful, but        *
 * WITHOUT ANY WARRANTY; without even the implied warranty of                 *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 * *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 ******************************************************************************/

package coat.view.graphic;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

/**
 * This Cell is a TextField that can be read but not written.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class NaturalCell<S, T> extends TableCell<S, T> {

    /**
     * Creates a new NaturalCell, which replaces the cell with a non-editable TextField.
     */
    public NaturalCell() {
        setEditable(true);
        setPadding(new Insets(5));
        setAlignment(Pos.CENTER_LEFT);
        setOnMouseClicked(event -> getTableView().getSelectionModel().select(getTableRow().getIndex()));
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
        } else {
            setText(String.valueOf(item));
            setTooltip(new Tooltip(String.valueOf(item)));
        }
    }

    @Override
    public void startEdit() {
        super.startEdit();
        final TextField textField = new TextField(getText());
        textField.setPrefWidth(getWidth());
        textField.setEditable(false);
        textField.selectAll();
        setGraphic(textField);
        setText(null);
        textField.setBackground(null);
        textField.requestFocus();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setGraphic(null);
        setText(getItem().toString());
    }
}
