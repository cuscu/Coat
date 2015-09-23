package coat.view.poirot;

import coat.view.graphic.AutoFillComboBox;
import coat.view.graphic.SizableImage;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
class PhenotypeSelector extends VBox {

    private final ListView<String> listView = new ListView<>();
    private final Button delete = new Button(null, new SizableImage("coat/img/delete.png", SizableImage.SMALL_SIZE));

    private final AutoFillComboBox comboBox = new AutoFillComboBox();
    private final Button addButton = new Button(null, new SizableImage("coat/img/add.png", SizableImage.SMALL_SIZE));

    public PhenotypeSelector() {

        final StackPane stackPane = new StackPane(listView, delete);
        final HBox hBox = new HBox(5, comboBox, addButton);

        getChildren().addAll(stackPane, hBox);

        listView.getSelectionModel().selectedItemProperty().addListener((obs, previous, current) -> delete.setVisible(current != null));

        configureDeleteButton();
        configureAddButton();

        configureComboBox();
    }

    private void configureComboBox() {
        comboBox.setPromptText("Add phenotype");
    }

    private void configureAddButton() {
        addButton.getStyleClass().add("graphic-button");
        addButton.setOnAction(event -> addPhenotype(comboBox.getValue()));
    }

    private void configureDeleteButton() {
        StackPane.setAlignment(delete, Pos.BOTTOM_RIGHT);
        delete.getStyleClass().add("graphic-button");
        delete.setVisible(false);
        delete.setOnAction(event -> listView.getItems().remove(listView.getSelectionModel().getSelectedItem()));
    }

    private void addPhenotype(String value) {
        if (value != null && !listView.getItems().contains(value)) {
            listView.getItems().add(value);
            comboBox.setValue(null);
        }
    }

    public List<String> getSelectedPhenotypes() {
        return listView.getItems();
    }

    public void setPhenotypes(List<String> list) {
        comboBox.getItems().setAll(list);
    }
}
