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

package coat.view.lightreader;

import coat.utils.OS;
import coat.view.graphic.NaturalCell;
import coat.view.vcfreader.Info;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFHeader;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class LightInfoTable extends VBox {

    private final Property<VariantContext> variantProperty = new SimpleObjectProperty<>();

    private final CheckBox showAllCheckBox = new CheckBox(OS.getString("show.all.properties"));

    private final TableView<Info> table = new TableView<>();
    private final TableColumn<Info, String> property
            = new TableColumn<>(OS.getResources().getString("property"));
    private final TableColumn<Info, String> value
            = new TableColumn<>(OS.getResources().getString("value"));

    private final TextFlow description = new TextFlow();
    private VCFHeader header;

    public LightInfoTable(VCFHeader header) {
        this.header = header;
        initTable();
        initDescription();
        initShowAll();
        getChildren().addAll(showAllCheckBox, table, description);
        variantProperty.addListener((obs, previous, current) -> updateTable());
    }

    private void initDescription() {
        table.getSelectionModel().selectedItemProperty().addListener((obs, previous, current)
                -> description.getChildren().setAll(new Text(current != null ? current.getDescription() : "")));
    }

    private void initTable() {
        VBox.setVgrow(table, Priority.ALWAYS);
        setCellValueFactories();
        table.getColumns().addAll(property, value);
        table.getColumns().forEach(column -> column.setSortable(false));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void initShowAll() {
        showAllCheckBox.setSelected(false);
        showAllCheckBox.setOnAction(event -> updateTable());
        showAllCheckBox.setPadding(new Insets(5));
    }

    private void setCellValueFactories() {
        property.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        value.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue()));
        value.setCellFactory(param -> new NaturalCell<>());
    }

    public Property<VariantContext> getVariantProperty() {
        return variantProperty;
    }

    private void updateTable() {
        table.getItems().clear();
        if (variantProperty.getValue() != null) addInfos();
    }

    private void addInfos() {
        final VariantContext variant = variantProperty.getValue();
        header.getInfoHeaderLines().forEach(headerLine -> {
            final String id = headerLine.getID();
            final String description = headerLine.getDescription();
            if (variant.hasAttribute(id)) {
                final String value = String.valueOf(variant.getAttribute(id));
                table.getItems().add(new Info(id, value, description));
            } else if (showAllCheckBox.isSelected())
                table.getItems().add(new Info(id, null, description));
        });
    }

}
