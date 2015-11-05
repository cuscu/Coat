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

package coat.view.graphic;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;

/**
 * This Cell is a TextField that can be read but not written.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class NaturalCell<S, T> extends TableCell<S, T> {

    private final TextField textField = new TextField();
    private final HBox box = new HBox(textField);

    /**
     * Creates a new NaturalCell, which replaces the cell with a non-editable TextField.
     */
    public NaturalCell() {
        textField.styleProperty().bind(styleProperty());
        box.setAlignment(Pos.CENTER);
        textField.setEditable(false);
        textField.setBackground(Background.EMPTY);
        textField.setPadding(new Insets(0));
        textField.setOnMouseClicked(event -> getTableView().getSelectionModel().select(getTableRow().getIndex()));
        textField.setOnKeyReleased(this::keyReleased);
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) writeItem(item);
        else setGraphic(null);
    }


    private void writeItem(T item) {
        if (item != null) {
            textField.setText(item.toString());
            setTooltip(new Tooltip(item.toString()));
        } else textField.setText(null);
        setGraphic(box);
    }

    private void keyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.DOWN) select(getIndex() + 1);
        else if (event.getCode() == KeyCode.UP) select(getIndex() - 1);
    }

    private void select(int row) {
        getTableView().getSelectionModel().select(row);
        getTableView().requestFocus();
    }
}
