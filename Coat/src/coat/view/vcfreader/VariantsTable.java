/*
 * Copyright (c) UICHUIMI 2016
 *
 * This file is part of Coat.
 *
 * Coat is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package coat.view.vcfreader;

import coat.core.vcf.VcfFilter;
import coat.utils.OS;
import coat.view.graphic.NaturalCell;
import coat.view.graphic.SizableImageView;
import coat.view.graphic.ThresholdDialog;
import coat.view.vcfreader.filter.FilterTableColumn;
import coat.view.vcfreader.filter.BooleanFilterColumn;
import coat.view.vcfreader.filter.StringFilterTableColumn;
import coat.view.vcfreader.filter.NumberFilterTableColumn;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import vcf.Variant;
import vcf.VariantSet;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual <pasculorente@gmail.com>
 */
public class VariantsTable extends VBox {

    private final static Set<String> FREQUENCY_IDS = new LinkedHashSet<>(Arrays.asList("AA_F", "EUR_F", "AFR_F",
            "AMR_F", "EA_F", "ASN_F", "AA_MAF", "EUR_MAF", "AFR_MAF", "AMR_MAF", "EA_MAF", "ASN_MAF", "afr_maf",
            "eur_maf", "amr_maf", "ea_maf", "asn_maf", "GMAF", "1KG14", "MINOR_ALLELE_FREQ"));

    private final TableView<Variant> table = new TableView<>();
    private final TextField searchBox = new TextField();

    private final ProgressBar progressBar = new ProgressBar();
    private final Button addFrequencyFilterButton = new Button("Add frequency filters");
    private final Button clearFilters = new Button("Clear all filters");
    private final Label progressLabel = new Label();
    private final StackPane stackPane = new StackPane(progressBar, progressLabel);
    private final HBox hBox = new HBox(5, addFrequencyFilterButton, clearFilters, stackPane);

    private final TableColumn<Variant, String> chrom
            = new StringFilterTableColumn<>(this, OS.getResources().getString("chromosome"));
    private final TableColumn<Variant, String> position
            = new NumberFilterTableColumn<>(this, OS.getResources().getString("position"));
    private final TableColumn<Variant, Variant> variant
            = new TableColumn<>(OS.getResources().getString("vcf"));
    private final TableColumn<Variant, String> rsId = new StringFilterTableColumn<>(this, "ID");
    private final TableColumn<Variant, String> qual
            = new NumberFilterTableColumn<>(this, OS.getResources().getString("quality"));

    private final ComboBox<String> currentChromosome = new ComboBox<>();
    private final TextField currentPosition = new TextField();
    private final Label coordinate = new Label(OS.getResources().getString("coordinate"));

    private final EventHandler<ActionEvent> coordinateHandler = event -> selectVariant();
    private final VariantSet variantSet;

    public VariantsTable(VariantSet variantSet) {
        this.variantSet = variantSet;
        changeFrequencyColumnsType();
        initStructure();
        filter();
    }

    private void changeFrequencyColumnsType() {
        variantSet.getHeader().getComplexHeaders().get("INFO").stream()
                .filter(map -> FREQUENCY_IDS.contains(map.get("ID")))
                .forEach(map -> map.put("Type", "Float"));
    }

    private void initStructure() {
        initTable();
        initSearchBox();
        final HBox coordinateBox = initCoordinatesBox();
        getChildren().addAll(coordinateBox, table);
        getChildren().add(hBox);
        hBox.setPadding(new Insets(5));
        hBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(stackPane, Priority.ALWAYS);
        progressBar.setMaxWidth(9999);
        addFrequencyFilterButton.setOnAction(event -> addFrequencyFilters());
        clearFilters.setOnAction(event -> clearFilters());

    }

    private void clearFilters() {
        table.getColumns().filtered(column -> FilterTableColumn.class.isAssignableFrom(column.getClass()))
                .stream().map(column -> (FilterTableColumn) column)
                .forEach(FilterTableColumn::clear);
        filter();
    }

    private void initTable() {
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setEditable(true);
        setCopyContextMenu();
        createColumns();
        table.getColumns().forEach(column -> column.setSortable(false));
        chrom.getStyleClass().add("first-column");
        setTableCellFactories();
        setTableCellValueFactories();
        setTableColumnWidths();
        table.setTableMenuButtonVisible(true);
        table.getSelectionModel().selectedItemProperty().addListener((obs, previous, current) -> setCoordinate(current));
    }

    private void setCopyContextMenu() {
        final SizableImageView COPY = new SizableImageView("coat/img/black/copy.png", SizableImageView.SMALL_SIZE);
        final MenuItem menuItem = new MenuItem("Copy variant", COPY);
        final ContextMenu contextMenu = new ContextMenu(menuItem);
        table.setContextMenu(contextMenu);
        menuItem.setOnAction(event -> {
            if (table.getSelectionModel().isEmpty()) return;
            final ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(table.getSelectionModel().getSelectedItem().toString());
            Clipboard.getSystemClipboard().setContent(clipboardContent);
        });
    }

    private void initSearchBox() {
        searchBox.setPromptText(OS.getResources().getString("search"));
        searchBox.getStyleClass().add("fancy-text-field");
        searchBox.setOnAction(event -> search(table.getSelectionModel().getSelectedIndex()));
    }

    private void search(int from) {
        if (searchBox.getText().isEmpty()) return;
        if (from < -1) from = -1;
        for (int i = 0; i < table.getItems().size(); i++) {
            int index = (i + from + 1) % table.getItems().size();
            final Variant variant = table.getItems().get(index);
            if (matches(variant, searchBox.getText().toLowerCase())) {
                select(table.getItems().get(index));
                break;
            }
        }
    }

    private boolean matches(Variant variant, String searchValue) {
        for (TableColumn<Variant, ?> column : table.getColumns()) {
            if (column.isVisible()) {
                if (column.getCellObservableValue(variant) == null) return false;
                String value = String.valueOf(column.getCellObservableValue(variant).getValue());
                if (value.toLowerCase().contains(searchValue.toLowerCase())) return true;
            }
        }
        return false;
    }

    private void setTableCellFactories() {
        table.getColumns().forEach(column -> column.setCellFactory(param -> new NaturalCell<>()));
        variant.setCellFactory(param -> new VariantCard());
    }

    private void setTableCellValueFactories() {
        chrom.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getChrom()));
        variant.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        rsId.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getId()));
        qual.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getQual() + ""));
        position.setCellValueFactory(param
                -> new SimpleStringProperty(String.format("%,d", param.getValue().getPosition())));
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
            currentPosition.setText(String.format(Locale.US, "%,d", current.getPosition()));
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
            if (v.getChrom().equals(cChromosome) && v.getPosition() >= cPos) {
                select(v);
                break;
            }
    }

    private void select(Variant v) {
        table.getSelectionModel().select(v);
        table.scrollTo(v);
    }

    private void updateChromosomeComboBox() {
        if (table.getItems().isEmpty()) return;
        final List<String> list = table.getItems().stream()
                .map(Variant::getChrom)
                .distinct()
                .collect(Collectors.toList());
        currentChromosome.getItems().setAll(list);
    }

    public ObservableList<Variant> getFilteredVariants() {
        return table.getItems();
    }

    private void createColumns() {
        table.getColumns().setAll(chrom, position, variant, rsId, qual);
        variantSet.getHeader().getIdList("INFO").stream().map(this::createInfoColumn).forEach(table.getColumns()::add);
    }

    @NotNull
    private TableColumn<Variant, String> createInfoColumn(String info) {
        TableColumn<Variant, String> column = new TableColumn<>(info);
        final Map<String, String> map = variantSet.getHeader().getComplexHeader("INFO", info);
        final String type = map.get("Type");
        // Integer, Float, Flag, Character, and String
        if (type.matches("String|Character")) column = new StringFilterTableColumn<>(this, info);
        if (type.matches("Integer|Float")) column = new NumberFilterTableColumn<>(this, info);
        if (type.matches("Flag")) column = new BooleanFilterColumn<>(this, info);
        column.setCellValueFactory(param -> new SimpleStringProperty(
                param.getValue().getInfo().hasInfo(info) ? param.getValue().getInfo().get(info).toString() : ""));
        column.setVisible(info.matches("GNAME|SYMBOL"));
        return column;
    }

    private void addFrequencyFilters() {
        String threshold = ThresholdDialog.askThresholdToUser();
        if (threshold != null) {
            table.getColumns().stream()
                    .filter(column -> FREQUENCY_IDS.contains(column.getText()))
                    .map(column -> (NumberFilterTableColumn<Variant, ?>) column)
                    .forEach(column -> {
                        column.setFilterValue("0.01");
                        column.setConnector(VcfFilter.Connector.LESS);
                    });
            filter();
        }
    }

    public void filter() {
        System.out.println("Filtering");
        table.getItems().setAll(variantSet.getVariants().stream().parallel().filter(variant ->
                table.getColumns().stream()
                        .filter(column -> FilterTableColumn.class.isAssignableFrom(column.getClass()))
                        .map(column -> (FilterTableColumn<Variant, ?>) column)
                        .allMatch((column) -> column.filter(variant))).collect(Collectors.toList()));
        final double progress = (double) table.getItems().size() / variantSet.getVariants().size();
        progressBar.setProgress(progress);
        progressLabel.setText(String.format("%s/%s (%.2f%%)", table.getItems().size(), variantSet.getVariants().size(),
                100 * progress));
        updateChromosomeComboBox();
    }

}
