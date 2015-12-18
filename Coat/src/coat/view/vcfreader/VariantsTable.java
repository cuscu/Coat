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

import coat.core.variant.Variant;
import coat.utils.OS;
import coat.view.graphic.IndexCell;
import coat.view.graphic.NaturalCell;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
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

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual <pasculorente@gmail.com>
 */
public class VariantsTable extends VBox {

    private final TableView<Variant> table = new TableView<>();
    private final TextField searchBox = new TextField();

    private final TableColumn<Variant, Integer> lineNumber = new TableColumn<>();
    private final TableColumn<Variant, String> chrom
            = new TableColumn<>(OS.getResources().getString("chromosome"));
    private final TableColumn<Variant, String> position
            = new TableColumn<>(OS.getResources().getString("position"));
    private final TableColumn<Variant, Variant> variant
            = new TableColumn<>(OS.getResources().getString("variant"));
    private final TableColumn<Variant, String> rsId = new TableColumn<>("ID");
    private final TableColumn<Variant, String> qual
            = new TableColumn<>(OS.getResources().getString("quality"));
    private final TableColumn<Variant, String> geneColumn = new TableColumn<>(OS.getResources().getString("gene"));

    private final ComboBox<String> currentChromosome = new ComboBox<>();
    private final TextField currentPosition = new TextField();
    private final Label coordinate = new Label(OS.getResources().getString("coordinate"));

    private final EventHandler<ActionEvent> coordinateHandler = event -> selectVariant();
    private final ListChangeListener<Variant> progressInfoUpdater = (ListChangeListener<Variant>) c -> tableHasChanged();

    public VariantsTable() {
        initStructure();
        table.getSelectionModel().selectedItemProperty().addListener((obs, previous, current) -> setCoordinate(current));
    }

    private void tableHasChanged() {
        updateChromosomeComboBox();
    }

    private void initStructure() {
        initTable();
        final HBox coordinateBox = initCoordinatesBox();
        getChildren().addAll(coordinateBox, table);
    }

    private void initTable() {
        VBox.setVgrow(table, Priority.ALWAYS);
//        table.getColumns().addAll(lineNumber, chrom, position, variant, rsId, geneColumn, qual);
        table.getColumns().addAll(chrom, position, geneColumn, variant, rsId, qual);
        table.getColumns().forEach(column -> column.setSortable(false));
        chrom.getStyleClass().add("first-column");
        setTableCellFactories();
        setTableCellValueFactories();
        setTableColumnWidths();
        table.setTableMenuButtonVisible(true);
        initSearchBox();
    }

    private void initSearchBox() {
        searchBox.setPromptText(OS.getResources().getString("search"));
//        searchBox.setMaxWidth(200);
        searchBox.getStyleClass().add("fancy-text-field");
        searchBox.setOnAction(event -> search(table.getSelectionModel().getSelectedIndex()));
    }

    private void search(int from) {
        if (from < -1) from = -1;
        for (int i = 0; i < table.getItems().size(); i++) {
            int index = (i + from + 1) % table.getItems().size();
            final Variant variant = table.getItems().get(index);
            if (mathces(variant, searchBox.getText().toLowerCase())) {
                select(table.getItems().get(index));
                break;
            }
        }
    }

    private boolean mathces(Variant variant, String searchValue) {
        if (variant.getChrom().contains(searchValue)) return true;
        if (variant.getRef().contains(searchValue)) return true;
        if (variant.getAlt().contains(searchValue)) return true;
        if (variant.getFilter().contains(searchValue)) return true;
        return variant.getInfo("SYMBOL") != null && ((String) variant.getInfo("SYMBOL")).toLowerCase().contains(searchValue);
        //        return Arrays.stream(variant.getInfoValues()).filter(s -> s != null).anyMatch(s1 -> s1.toLowerCase().contains(searchValue));
    }

    private void setTableCellFactories() {
        table.getColumns().forEach(column -> column.setCellFactory(param -> new NaturalCell<>()));
        lineNumber.setCellFactory(param -> new IndexCell());
        variant.setCellFactory(param -> new VariantCard());
    }

    private void setTableCellValueFactories() {
        chrom.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getChrom()));
        variant.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        rsId.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getId()));
        qual.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getQual() + ""));
        position.setCellValueFactory(param
                -> new SimpleStringProperty(String.format("%,d", param.getValue().getPos())));
        geneColumn.setCellValueFactory(param -> new SimpleStringProperty((String) param.getValue().getInfo("SYMBOL")));
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
        return getCoordinatesBox();
    }

    private HBox getCoordinatesBox() {
        final Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setVisible(false);
        HBox.setHgrow(separator, Priority.ALWAYS);
        final HBox box = new HBox(5, coordinate, currentChromosome, currentPosition, separator, searchBox);
        currentPosition.getStyleClass().add("fancy-text-field");
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(5));
        return box;
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
        return table.getSelectionModel().selectedItemProperty();
    }

    public void setVariants(ObservableList<Variant> variants) {
        table.getItems().removeListener(progressInfoUpdater);
        table.setItems(variants);
        table.getItems().addListener(progressInfoUpdater);
        tableHasChanged();
        table.getSelectionModel().select(0);
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
        for (Variant v : table.getItems())
            if (v.getChrom().equals(cChromosome) && v.getPos() >= cPos) {
                select(v);
                break;
            }
    }

    private void select(Variant v) {
        table.getSelectionModel().select(v);
        table.scrollTo(v);
    }

    private void updateChromosomeComboBox() {
        final List<String> list = table.getItems().stream()
                .map(Variant::getChrom)
                .distinct()
                .collect(Collectors.toList());
        currentChromosome.getItems().setAll(list);
    }

    public ObservableList<Variant> getVariants() {
        return table.getItems();
    }
}
