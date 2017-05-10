/*
 * Copyright (c) UICHUIMI 2017
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

package coat.view.lightreader;

import coat.utils.OS;
import coat.view.graphic.NaturalCell;
import coat.view.graphic.SizableImageView;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.*;
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
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual <pasculorente@gmail.com>
 */
public class LightVariantsTable extends VBox {

    private final TableView<VariantContext> table = new TableView<>();
    private final TextField searchBox = new TextField();

    private final TableColumn<VariantContext, String> chrom
            = new TableColumn<>(OS.getResources().getString("chromosome"));
    private final TableColumn<VariantContext, String> position
            = new TableColumn<>(OS.getResources().getString("position"));
    private final TableColumn<VariantContext, VariantContext> variant
            = new TableColumn<>(OS.getResources().getString("vcf"));
    private final TableColumn<VariantContext, String> rsId
            = new TableColumn<>("ID");
    private final TableColumn<VariantContext, String> qual
            = new TableColumn<>(OS.getResources().getString("quality"));

    private final ComboBox<String> currentChromosome = new ComboBox<>();
    private final TextField currentPosition = new TextField();
    private final Label coordinate = new Label(OS.getResources().getString("coordinate"));

    private final EventHandler<ActionEvent> coordinateHandler = event -> selectVariant();
    private final VCFHeader header;
    private Label warningLabel = new Label("This table will show maximum " +
            "10000 results");


    public LightVariantsTable(VCFHeader header, ObservableList<VariantContext> variants) {
        this.table.setItems(variants);
        this.header = header;
        initStructure();
        table.getSelectionModel().select(0);
    }

    private void initStructure() {
        initTable();
        initSearchBox();
        final HBox coordinateBox = initCoordinatesBox();
        final HBox warningBox = new HBox(warningLabel);
        getChildren().addAll(coordinateBox, table, warningBox);
        warningLabel.getStyleClass().add("warning-label");
        warningBox.getStyleClass().add("warning-box");
        warningBox.setPadding(new Insets(5));
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
            final VariantContext variant = table.getItems().get(index);
            if (matches(variant, searchBox.getText().toLowerCase())) {
                select(table.getItems().get(index));
                break;
            }
        }
    }

    private boolean matches(VariantContext variant, String searchValue) {
        for (TableColumn<VariantContext, ?> column : table.getColumns()) {
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
        variant.setCellFactory(param -> new LightVariantCard());
    }

    private void setTableCellValueFactories() {
        chrom.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getContig()));
        variant.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        rsId.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getID()));
        qual.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPhredScaledQual() + ""));
        position.setCellValueFactory(param
                -> new SimpleStringProperty(String.format("%,d", param.getValue().getStart())));
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

    private void setCoordinate(VariantContext current) {
        if (current != null) {
            disableCoordinateHandler();
            currentChromosome.setValue(current.getContig());
            currentPosition.setText(String.format(Locale.US, "%,d", current.getStart()));
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

    public ReadOnlyObjectProperty<VariantContext> getVariantProperty() {
        return table.getSelectionModel().selectedItemProperty();
    }

    private void selectVariant() {
        try {
            String cChromosome = currentChromosome.getValue();
            int cPos = Integer.valueOf(currentPosition.getText().replace(",", ""));
            goTo(cChromosome, cPos);
        } catch (NumberFormatException ignored) {
        }
    }

    private void goTo(String cChromosome, int cPos) {
        for (VariantContext v : table.getItems())
            if (v.getContig().equals(cChromosome) && v.getStart() >= cPos) {
                select(v);
                break;
            }
    }

    private void select(VariantContext v) {
        table.getSelectionModel().select(v);
        table.scrollTo(v);
    }

    public void updateChromosomeComboBox() {
        synchronized (table.getItems()){
            if (table.getItems().isEmpty()) return;
            final List<String> list = table.getItems().stream()
                    .map(VariantContext::getContig)
                    .distinct()
                    .collect(Collectors.toList());
            currentChromosome.getItems().setAll(list);
        }
    }

    private void createColumns() {
        table.getColumns().setAll(chrom, position, variant, rsId, qual);
        header.getInfoHeaderLines().stream().map(this::createInfoColumn).forEach(table.getColumns()::add);
    }

    @NotNull
    private TableColumn<VariantContext, String> createInfoColumn(VCFInfoHeaderLine info) {
        final TableColumn<VariantContext, String> column = new TableColumn<>(info.getID());
        column.setCellValueFactory(param -> {
            final Object value = param.getValue().getAttribute(info.getID());
            return new SimpleObjectProperty<>(value == null ? VCFConstants.EMPTY_ID_FIELD : value.toString());
        });
        column.setVisible(info.getID().equals("SYMBOL"));
        return column;
    }

}
