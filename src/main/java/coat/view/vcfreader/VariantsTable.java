/*
 * Copyright (c) UICHUIMI 2017
 *
 * This file is part of Coat.
 *
 * Coat is free software:
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Coat.
 *
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */

package coat.view.vcfreader;

import coat.utils.OS;
import coat.view.graphic.NaturalCell;
import coat.view.graphic.SizableImageView;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
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
import org.uichuimi.vcf.variant.Variant;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual <pasculorente@gmail.com>
 */
public class VariantsTable extends VBox {

    private final static Set<String> FREQUENCY_IDS = new LinkedHashSet<>(Arrays.asList("AA_F", "EUR_F", "AFR_F",
            "AMR_F", "EA_F", "ASN_F", "AA_MAF", "EUR_MAF", "AFR_MAF", "AMR_MAF", "EA_MAF", "ASN_MAF", "afr_maf",
            "eur_maf", "amr_maf", "ea_maf", "asn_maf", "GMAF", "1KG14", "MINOR_ALLELE_FREQ", "EXAC_ADJ_MAF",
            "EXAC_AFR_MAF", "EXAC_AMR_MAF", "EXAC_EAS_MAF", "EXAC_FIN_MAF", "EXAC_MAF", "EXAC_NFE_MAF",
            "EXAC_OTH_MAF", "EXAC_SAS_MAF"));

    private final TableView<Variant> table = new TableView<>();
    private final TextField searchBox = new TextField();

    private final ProgressBar progressBar = new ProgressBar();
    private final Button addFrequencyFilters = new Button(OS.getString("add.frequency.filters"));
    private final Button clearFilters = new Button(OS.getString("clear.all.filters"));
    private final Button addFilter = new Button(OS.getString("add.filter"));
    private final Label progressLabel = new Label();
    private final StackPane stackPane = new StackPane(progressBar, progressLabel);
    private final HBox hBox = new HBox(5, addFilter, clearFilters, addFrequencyFilters, stackPane);

    private final TableColumn<Variant, String> chrom
            = new TableColumn<>(OS.getResources().getString("chromosome"));
    private final TableColumn<Variant, String> position
            = new TableColumn<>(OS.getResources().getString("position"));
    private final TableColumn<Variant, Variant> variant
            = new TableColumn<>(OS.getResources().getString("vcf"));
    private final TableColumn<Variant, String> rsId
            = new TableColumn<>("ID");
    private final TableColumn<Variant, String> qual
            = new TableColumn<>(OS.getResources().getString("quality"));

    private final ComboBox<String> currentChromosome = new ComboBox<>();
    private final TextField currentPosition = new TextField();
    private final Label coordinate = new Label(OS.getResources().getString("coordinate"));

    private final EventHandler<ActionEvent> coordinateHandler = event -> selectVariant();
    private final List<Variant> variants;
    private ObservableList<SampleFilter> sampleFilters;

    private ObservableList<VcfFilter> filters = FXCollections.observableArrayList();

    public VariantsTable(List<Variant> variants) {
        this.variants = variants;
        changeFrequencyColumnsType();
        initStructure();
        filter();
        table.getSelectionModel().select(0);
    }

    public ObservableList<VcfFilter> getFilters() {
        return filters;
    }

    public void setFilters(ObservableList<VcfFilter> filters) {
        this.filters = filters;
    }

    private void changeFrequencyColumnsType() {
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
        progressLabel.getStyleClass().add("white-text");
        addFrequencyFilters.setOnAction(event -> addFrequencyFilters());
        clearFilters.setOnAction(event -> clearFilters());
        addFilter.setOnAction(event -> filters.add(new VcfFilter("CHROM", null, VcfFilter.Connector.EQUALS, "1")));

    }

    private void clearFilters() {
        filters.clear();
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
        final SizableImageView COPY = new SizableImageView("img/black/copy.png", SizableImageView.SMALL_SIZE);
        final MenuItem menuItem = new MenuItem(OS.getString("copy.variant"), COPY);
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
                if (column.getCellObservableValue(variant) == null)
                    return false;
                String value = String.valueOf(column.getCellObservableValue(variant).getValue());
                if (value.toLowerCase().contains(searchValue.toLowerCase()))
                    return true;
            }
        }
        return false;
    }

    private void setTableCellFactories() {
        table.getColumns().forEach(column -> column.setCellFactory(param -> new NaturalCell<>()));
        variant.setCellFactory(param -> new VariantCard());
    }

    private void setTableCellValueFactories() {
        chrom.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCoordinate().getChrom()));
        variant.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        rsId.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getIdentifiers().get(0)));
        qual.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getQuality() + ""));
        position.setCellValueFactory(param
                -> new SimpleStringProperty(String.format("%,d", param.getValue().getCoordinate().getPosition())));
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
            currentChromosome.setValue(current.getCoordinate().getChrom());
            currentPosition.setText(String.format(Locale.US, "%,d", current.getCoordinate().getPosition()));
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
            int cPos = Integer.parseInt(currentPosition.getText().replace(",", ""));
            goTo(cChromosome, cPos);
        } catch (NumberFormatException ignored) {
        }
    }

    private void goTo(String cChromosome, int cPos) {
        for (Variant v : table.getItems())
            if (v.getCoordinate().getChrom().equals(cChromosome) && v.getCoordinate().getPosition() >= cPos) {
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
                .map(v -> v.getCoordinate().getChrom())
                .distinct()
                .collect(Collectors.toList());
        currentChromosome.getItems().setAll(list);
    }

    public ObservableList<Variant> getFilteredVariants() {
        return table.getItems();
    }

    private void createColumns() {
        table.getColumns().setAll(chrom, position, variant, rsId, qual);
        variants.get(0).getHeader().getIdList("INFO").stream().map(this::createInfoColumn).forEach(table.getColumns()::add);
    }

    @NotNull
    private TableColumn<Variant, String> createInfoColumn(String info) {
        TableColumn<Variant, String> column = new TableColumn<>(info);
        column.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getInfo(info)));
        column.setVisible(info.matches("GNAME|SYMBOL"));
        return column;
    }

    private void addFrequencyFilters() {
//        final String threshold = ThresholdDialog.askThresholdToUser();
        final TextInputDialog dialog = new TextInputDialog("0.01");
        dialog.setHeaderText("Set max value frequency");
        dialog.setTitle("Max frequency");
        dialog.showAndWait().ifPresent(s -> {
            try {
                final double th = Double.valueOf(s);
                final List<String> idList = variants.get(0).getHeader().getIdList("INFO");
                for (String id : idList) {
                    if (FREQUENCY_IDS.contains(id)) {
                        final VcfFilter filter = new VcfFilter("INFO", id, VcfFilter.Connector.LESS_THAN, th);
                        filters.add(filter);
                    }
                }
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }

        });
        filter();
    }

    public void filter() {
        table.getItems().setAll(variants.stream().parallel()
                .filter(this::filterBySample)
                .filter(this::filterByColumns)
                .collect(Collectors.toList()));
        final double progress = (double) table.getItems().size() / variants.size();
        progressBar.setProgress(progress);
        progressLabel.setText(String.format("%s/%s (%.2f%%)", table.getItems().size(), variants.size(),
                100 * progress));
        updateChromosomeComboBox();
    }

    private boolean filterBySample(Variant variant) {
        if (sampleFilters != null)
            return sampleFilters.stream().allMatch(sampleFilter -> sampleFilter.filter(variant));
        return true;
    }

    private boolean filterByColumns(Variant variant) {
        return filters.stream().allMatch(vcfFilter -> vcfFilter.filter(variant));
//        return table.getColumns().stream()
//                .filter(column -> FilterTableColumn.class.isAssignableFrom(column.getClass()))
//                .map(column -> (FilterTableColumn) column)
//                .allMatch(column -> column.filter(variant));
    }

    public void setSampleFilters(ObservableList<SampleFilter> sampleFilters) {
        this.sampleFilters = sampleFilters;
    }
}
