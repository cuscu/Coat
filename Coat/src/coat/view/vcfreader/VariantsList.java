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
package coat.view.vcfreader;

import coat.core.poirot.dataset.Dataset;
import coat.core.poirot.dataset.Instance;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual <pasculorente@gmail.com>
 */
public class VariantsList extends VBox implements ListChangeListener<Instance> {

    private final TableView<Instance> tableView = new TableView<>();
    private final ComboBox<String> chromosomeSelector = new ComboBox<>();
    private final TextField positionSelector = new TextField();
    private final TextField searchTextField = new TextField();
    private final Separator separator = new Separator(Orientation.HORIZONTAL);
    private final EventHandler<ActionEvent> handler = event -> coordinateSelected();


    public VariantsList() {
        final HBox navigationBox = getNaviagtionBar();
        getChildren().addAll(tableView, navigationBox);
        configureTable();
        configureNavigationBar();
    }

    private HBox getNaviagtionBar() {
        final Label coordinatesLabel = new Label("Coordinates");
        final HBox navigationBox = new HBox(5, coordinatesLabel, chromosomeSelector, positionSelector, separator, searchTextField);
        navigationBox.setAlignment(Pos.CENTER_LEFT);
        navigationBox.setPadding(new Insets(5));
        return navigationBox;
    }

    private void configureTable() {
        VBox.setVgrow(tableView, Priority.ALWAYS);
        tableView.getItems().addListener(this);
        tableView.setTableMenuButtonVisible(true);
        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> selected(newValue));
        tableView.setRowFactory(param -> new VcfRow());
    }

    private void selected(Instance instance) {
        if (instance != null) {
            setCoordinateListener(null);
            setCoordinateValues(instance);
            setCoordinateListener(handler);
        }
    }

    private void setCoordinateListener(EventHandler<ActionEvent> handler) {
        chromosomeSelector.setOnAction(handler);
        positionSelector.setOnAction(handler);
    }

    private void setCoordinateValues(Instance instance) {
        chromosomeSelector.setValue((String) instance.getField(0));
        positionSelector.setText((String) instance.getField(1));
    }

    private void configureNavigationBar() {
        HBox.setHgrow(separator, Priority.ALWAYS);
        separator.setVisible(false);
        setCoordinateListener(handler);
        searchTextField.setPromptText("Search...");
        searchTextField.setOnAction(event -> search());

    }

    private void search() {
        int from = tableView.getSelectionModel().getSelectedIndex();
        final String text = searchTextField.getText().toLowerCase();
        if (from < -1) from = -1;
        for (int i = 0; i < tableView.getItems().size(); i++) {
            int index = (i + from + 1) % tableView.getItems().size();
            final Instance instance = tableView.getItems().get(index);
            if (containsValue(text, instance)) tableView.getSelectionModel().select(index);
        }
    }

    private boolean containsValue(String text, Instance instance) {
        for (int j = 0; j < instance.getDataset().getColumnNames().size(); j++) {
            if (instance.getField(j) != null && instance.getField(j).toString().toLowerCase().contains(text))
                return true;
        }
        return false;
    }

    private void coordinateSelected() {
        final String chromosome = chromosomeSelector.getValue();
        final int position = getSelectedPosition();
        for (Instance instance : getVariants())
            if (isEqualsOrNext(instance, chromosome, position)) {
                tableView.getSelectionModel().select(instance);
                return;
            }

    }

    private boolean isEqualsOrNext(Instance instance, String chromosome, int position) {
        final int instancePosition = Integer.valueOf((String) instance.getField(1));
        return instance.getField(0).equals(chromosome) && instancePosition >= position;
    }

    private int getSelectedPosition() {
        try {
            return Integer.valueOf(positionSelector.getText());
        } finally {
            return 0;
        }
    }


    public ObservableList<Instance> getVariants() {
        return tableView.getItems();
    }

    private void generateColumns() {
        if (tableView.getColumns().isEmpty()) {
            if (!tableView.getItems().isEmpty()) {
                final Dataset dataset = tableView.getItems().get(0).getDataset();
                dataset.getColumnNames().forEach(s -> {
                    final TableColumn<Instance, Object> column = new TableColumn<>(s);
                    column.setCellValueFactory(param -> new SimpleObjectProperty<>((param.getValue().getField(s))));
                    column.setSortable(false);
                    tableView.getColumns().add(column);
                });
                for (int i = 5; i < tableView.getColumns().size(); i++) tableView.getColumns().get(i).setVisible(false);
                tableView.getColumns().get(0).getStyleClass().add("variant-column");
                tableView.getSelectionModel();
            }
        }
    }

    private void updateChromosomeSelector() {
        chromosomeSelector.getItems().setAll(tableView.getItems().stream()
                .map(instance -> (String) instance.getField(0))
                .distinct().collect(Collectors.toList()));
    }


    public void setInputVariants(ObservableList<Instance> variants) {
        tableView.getItems().removeListener(this);
        tableView.setItems(variants);
        tableView.getItems().addListener(this);
        generateColumns();
        updateChromosomeSelector();
    }

    @Override
    public void onChanged(Change<? extends Instance> change) {
        generateColumns();
        updateChromosomeSelector();
    }

    public ReadOnlyObjectProperty<Instance> selectedVariantProperty() {
        return tableView.getSelectionModel().selectedItemProperty();
    }
}
