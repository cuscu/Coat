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

import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;

import java.util.stream.Collectors;

/**
 * An autoFill ComboBox for Strings. This textField will show possible ending values for the input text. As opposite to
 * conventional ComboBox, value is not updated when an item is selected in the dropdown list. Instead, only when user
 * presses ENTER key or double-click on an item, value is updated.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class AutoFillComboBox extends TextField {

    private final Popup popup = new Popup();
    private final ListView<String> list = new ListView<>();

    private final ObservableList<String> items = FXCollections.observableArrayList();

    public AutoFillComboBox() {
        initializePopup();
        addListenersToThis();
        items.addListener((ListChangeListener<String>) c -> {
            list.getItems().setAll(items);
            this.setWidth(list.getWidth());
        });
        list.setOnKeyReleased(this::listKeyReleased);
        list.setOnMouseReleased(this::listMouseClicked);
    }

    private void addListenersToThis() {
        setOnKeyReleased(this::editorKeyReleased);
        setOnMouseClicked(this::editorMouseClicked);
    }

    private void initializePopup() {
        popup.getContent().add(list);
        popup.setAutoHide(true);
    }

    private void editorKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.DOWN) {
            if (popup.isShowing()) list.requestFocus();
            else show();
        } else if (event.getCode().isDigitKey() || event.getCode() == KeyCode.SPACE || event.getCode().isLetterKey())
            filterAndShow();
    }

    private void editorMouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) show();
    }

    private void filterAndShow() {
        if (getText() != null) {
            filter();
            show();
        }
    }

    private void listMouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) setValue(list.getSelectionModel().getSelectedItem());
    }

    private void filter() {
        final String text = getText() == null ? "" : getText().toLowerCase();
        list.getItems().setAll(items.stream()
                .filter(t -> t.toLowerCase().contains(text))
                .collect(Collectors.toList()));
    }

    private void listKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) popup.hide();
        else if (event.getCode() == KeyCode.ENTER) {
            if (!list.getSelectionModel().isEmpty()) setValue(list.getSelectionModel().getSelectedItem());
            else popup.hide();
        }
    }

    private void show() {
        if (!list.getItems().isEmpty()) {
            final Point2D p = localToScene(0.0, 0.0);
            popup.show(this,
                    p.getX() + getScene().getX() + getScene().getWindow().getX(),
                    p.getY() + getScene().getY() + getScene().getWindow().getY() + getHeight());
        }
    }

    public ObservableList<String> getItems() {
        return items;
    }

    public void setValue(String value) {
        setText(value);
        end();
        popup.hide();
    }

    public StringProperty valueProperty() {
        return textProperty();
    }
}
