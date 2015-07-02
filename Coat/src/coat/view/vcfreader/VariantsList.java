/*
 * Copyright (C) 2015 UICHUIMI
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
package coat.view.vcfreader;

import coat.model.vcfreader.Variant;
import coat.utils.OS;
import coat.view.graphic.IndexCell;
import coat.view.graphic.SizableImage;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual <pasculorente@gmail.com>
 */
public class VariantsList extends VBox {

    private final ListView<Variant> listView = new ListView<>();

//    private final TableView<Variant> table = new TableView<>();
    private final TextField searchBox = new TextField();
    private final Button searchButton = new Button(null, new SizableImage("coat/img/search.png", SizableImage.SMALL_SIZE));
    private final StackPane stackPane = new StackPane(listView, searchBox, searchButton);

    private final TableColumn<Variant, Integer> lineNumber = new TableColumn<>();
    private final TableColumn<Variant, String> chrom
            = new TableColumn<>(OS.getResources().getString("chromosome"));
    private final TableColumn<Variant, String> position
            = new TableColumn<>(OS.getResources().getString("position"));
    private final TableColumn<Variant, String> variant
            = new TableColumn<>(OS.getResources().getString("variant"));
    private final TableColumn<Variant, String> rsId = new TableColumn<>("ID");
    private final TableColumn<Variant, String> qual
            = new TableColumn<>(OS.getResources().getString("quality"));
    private final TableColumn<Variant, String> geneColumn = new TableColumn<>(OS.getResources().getString("gene"));

    private final ComboBox<String> currentChromosome = new ComboBox<>();
    private final TextField currentPosition = new TextField();
    private final Label coordinate = new Label(OS.getResources().getString("coordinate"));

    private final Label infoLabel = new Label();
    private final ProgressBar progressBar = new ProgressBar(0);
    private final Label progressLabel = new Label("0/0");

    private final EventHandler<ActionEvent> coordinateHandler = event -> selectVariant();
    private final ListChangeListener<Variant> progressInfoUpdater = (ListChangeListener<Variant>) c -> tableHasChanged();
    private int totalVariants = 0;

    public VariantsList() {
        initStructure();
        listView.getSelectionModel().selectedItemProperty().addListener((obs, previous, current) -> setCoordinate(current));
    }

    private void tableHasChanged() {
        updateChromosomeComboBox();
        updateInfo();
    }

    private void initStructure() {
        initTable();
        HBox coordinateBox = initCoordinatesBox();
        getChildren().addAll(stackPane, coordinateBox);
    }

    private void initTable() {
        VBox.setVgrow(stackPane, Priority.ALWAYS);
//        table.getColumns().addAll(lineNumber, chrom, position, variant, rsId, geneColumn, qual);
//        table.getColumns().forEach(column -> column.setSortable(false));
        setTableCellFactories();
        setTableCellValueFactories();
        setTableColumnWidths();
//        table.setTableMenuButtonVisible(true);
        initSearchBox();
    }

    private void initSearchBox() {
        StackPane.setAlignment(searchBox, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(searchBox, new Insets(20));
        searchBox.setPromptText(OS.getResources().getString("search"));
        searchBox.setMaxWidth(200);
        searchBox.getStyleClass().add("search-box");
        searchBox.setOnAction(event -> search(listView.getSelectionModel().getSelectedIndex()));
        searchBox.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                searchBox.setVisible(false);
                searchButton.setVisible(true);
            }
        });
        StackPane.setAlignment(searchButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(searchButton, new Insets(20));
        searchButton.setOnAction(event -> {
            searchButton.setVisible(false);
            searchBox.setVisible(true);
            searchBox.requestFocus();
        });
        searchButton.getStyleClass().add("graphic-button");
        searchBox.setVisible(false);
    }

    private void search(int from) {
        if (from < -1) from = -1;

        for (int i = 0; i < listView.getItems().size(); i++) {
            int index = (i + from + 1) % listView.getItems().size();
            if (listView.getItems().get(index).getInfo().toLowerCase().contains(searchBox.getText().toLowerCase())) {
                select(listView.getItems().get(index));
                break;
            }
        }
    }

    private void setTableCellFactories() {
//        listView.setCellFactory(param1 -> new VariantCard());
//        table.getColumns().forEach(column -> column.setCellFactory(param -> new VariantCell()));
        lineNumber.setCellFactory(param -> new IndexCell());
    }

    private void setTableCellValueFactories() {
        chrom.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getChrom()));
        variant.setCellValueFactory(param
                -> new SimpleStringProperty(param.getValue().getRef() + "->" + param.getValue().getAlt()));
        rsId.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getId()));
        qual.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getQual() + ""));
        position.setCellValueFactory(param
                -> new SimpleStringProperty(String.format(Locale.US, "%,d", param.getValue().getPos())));
        geneColumn.setCellValueFactory(param -> new SimpleStringProperty((String) param.getValue().getInfos().getOrDefault("GNAME", ".")));
    }

    private void setTableColumnWidths() {
        chrom.setPrefWidth(100);
        position.setPrefWidth(150);
        variant.setPrefWidth(150);
        rsId.setPrefWidth(150);
        qual.setPrefWidth(150);
    }

    private HBox initCoordinatesBox() {
        enableCoordinateHandler();
        return gethBox();
    }

    private HBox gethBox() {
        StackPane stackPane = getStackPane();
        HBox box = new HBox(5, coordinate, currentChromosome, currentPosition, infoLabel, stackPane);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(5));
        return box;
    }

    private StackPane getStackPane() {
        progressBar.setMaxWidth(Double.MAX_VALUE);
        StackPane stackPane = new StackPane(progressBar, progressLabel);
        HBox.setHgrow(stackPane, Priority.ALWAYS);
        return stackPane;
    }

    private void setCoordinate(Variant current) {
        if (current != null) {
            disableCoordinateHandler();
            currentChromosome.setValue(current.getChrom());
            currentPosition.setText(String.format(Locale.US, "%,d", current.getPos()));
            enableCoordinateHandler();
        }
    }

    private void enableCoordinateHandler() {
        currentChromosome.setOnAction(coordinateHandler);
        currentPosition.setOnAction(coordinateHandler);
    }

    private void disableCoordinateHandler() {
        currentChromosome.setOnAction(null);
        currentPosition.setOnAction(null);
    }

    public ReadOnlyObjectProperty<Variant> getVariantProperty() {
        return listView.getSelectionModel().selectedItemProperty();
    }

    public void setVariants(ObservableList<Variant> variants) {
        listView.getItems().removeListener(progressInfoUpdater);
        listView.setItems(variants);
        listView.getItems().addListener(progressInfoUpdater);
        tableHasChanged();
        listView.getSelectionModel().select(0);
    }

    private void selectVariant() {
        try {
            String cChromosome = currentChromosome.getValue();
            int cPos = Integer.valueOf(currentPosition.getText().replace(",", ""));
            goTo(cChromosome, cPos);
        } catch (NumberFormatException ex) {
        }
    }

    private void goTo(String cChromosome, int cPos) {
        for (Variant v : listView.getItems())
            if (v.getChrom().equals(cChromosome) && v.getPos() >= cPos) {
                select(v);
                break;
            }
    }

    private void select(Variant v) {
        listView.getSelectionModel().select(v);
        listView.scrollTo(v);
    }

    private void updateChromosomeComboBox() {
        // stream: for each
        // map: select chromosomes
        // distinct: unique values
        // collect: to list again
        List<String> list;
        list = listView.getItems().stream().map(Variant::getChrom).distinct().collect(Collectors.toList());
        currentChromosome.getItems().setAll(list);
    }

    /**
     * Updates the number of variants in the table (in infoLabel) and the chromosome selector.
     */
    private void updateInfo() {
        final int filtered = listView.getItems().size();
        final double percentage = filtered * 100.0 / totalVariants;
        infoLabel.setText(String.format("%,d / %,d", filtered, totalVariants));
        progressLabel.setText(String.format("%.2f%%", percentage));
        progressBar.setProgress(percentage / 100);
//        updateChromosomeComboBox();

    }

    public void setTotalVariants(int totalVariants) {
        this.totalVariants = totalVariants;
        updateInfo();
    }

}
