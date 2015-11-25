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

import coat.utils.FileManager;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class FileParameter extends StackPane {

    private final String title;
    private final Property<File> file = new SimpleObjectProperty<>();
    private List<FileChooser.ExtensionFilter> filters = new ArrayList<>();

    public FileParameter(String title) {
        this.title = title;

        final Button button = getButton();
        final TextField textField = getTextField(title);

        file.addListener((observable, oldValue, newValue) -> textField.setText(newValue.getAbsolutePath()));

        StackPane.setAlignment(button, Pos.CENTER_RIGHT);
        getChildren().addAll(textField, button);

    }

    private Button getButton() {
        final Button button = new Button(null, new SizableImage("coat/img/black/folder.png", SizableImage.SMALL_SIZE));
        button.setOnAction(event -> openFile());
        button.getStyleClass().add("graphic-button");
        return button;
    }

    private TextField getTextField(String title) {
        final TextField textField = new TextField();
        textField.setEditable(false);
        textField.setPromptText(title);
        textField.setTooltip(new Tooltip(title));
        textField.setOnMouseClicked(this::mouseClicked);
        textField.getStyleClass().add("fancy-text-field");
        return textField;
    }

    private void mouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) openFile();
    }

    private void openFile() {
        final File f = FileManager.openFile("Select " + title, filters);
        if (f != null) file.setValue(f);
    }

    public List<FileChooser.ExtensionFilter> getFilters() {
        return filters;
    }

    public Property<File> fileProperty() {
        return file;
    }
}
