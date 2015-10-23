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

package coat.view.poirot;

import coat.view.graphic.AutoFillComboBox;
import coat.view.graphic.SizableImage;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.List;

/**
 * Pane to select input phenotypes. There are two controls: a list with the selected phenotypes and an autoFillComboBox
 * to add new phenotypes. Phenotypes must be loaded using <code>setPhenotypes</code>
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
class PhenotypeSelector extends VBox {

    private final ListView<String> listView = new ListView<>();
    private final Button delete = new Button(null, new SizableImage("coat/img/rubbish.png", SizableImage.SMALL_SIZE));

    private final AutoFillComboBox comboBox = new AutoFillComboBox();
    private final Button addButton = new Button(null, new SizableImage("coat/img/add.png", SizableImage.SMALL_SIZE));

    public PhenotypeSelector() {

        final StackPane listPane = new StackPane(listView, delete);

        final StackPane textFieldPane = new StackPane(comboBox, addButton);
//        final HBox hBox = new HBox(comboBox, addButton);

        listView.getSelectionModel().selectedItemProperty().addListener((obs, previous, current) -> delete.setVisible(current != null));
        listView.getStyleClass().add("custom-list");
        listView.setPlaceholder(new Text("Add phenotypes or expressions"));

        configureDeleteButton();
        configureAddButton();
        configureTextPane();

        setSpacing(5);
        getChildren().addAll(listPane, textFieldPane);
    }

    private void configureTextPane() {
//        HBox.setHgrow(comboBox, Priority.ALWAYS);
        StackPane.setAlignment(addButton, Pos.CENTER_RIGHT);
        comboBox.getStyleClass().add("fancy-text-field");
        comboBox.setOnAction(event -> {
            if (comboBox.getItems().contains(comboBox.getText())) addPhenotype(comboBox.getText());
        });
        comboBox.setPromptText("Add phenotype");
        comboBox.textProperty().addListener((observable, oldValue, newValue) -> {
            addButton.setDisable(!comboBox.getItems().contains(newValue));
        });
    }

    private void configureDeleteButton() {
        StackPane.setAlignment(delete, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(delete, new Insets(0, 10, 15, 0));
        delete.getStyleClass().add("graphic-button");
        delete.setVisible(false);
        delete.setOnAction(event -> {
            final String value = listView.getSelectionModel().getSelectedItem();
            if (listView.getItems().remove(value)) comboBox.getItems().add(value);

        });
    }

    private void configureAddButton() {
        addButton.getStyleClass().add("graphic-button");
        addButton.setOnAction(event -> addPhenotype(comboBox.getText()));
        addButton.setDisable(true);
    }

    private void addPhenotype(String value) {
        if (value != null && !listView.getItems().contains(value)) {
            listView.getItems().add(value);
            comboBox.setValue(null);
            comboBox.getItems().remove(value);
        }
    }

    public ObservableList<String> getSelectedPhenotypes() {
        return listView.getItems();
    }

    public void setPhenotypes(List<String> list) {
        comboBox.getItems().setAll(list);
    }
}
