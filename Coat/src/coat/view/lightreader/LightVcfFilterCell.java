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

import htsjdk.variant.vcf.VCFCompoundHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.util.stream.Collectors;

/**
 * Created by uichuimi on 23/03/17.
 */
public class LightVcfFilterCell extends ListCell<LightVcfFilter> {

    private final LightVcfReader reader;
    private ComboBox<String> column = new ComboBox<>();
    private ComboBox<String> key = new ComboBox<>();
    private ComboBox<LightVcfFilter.Connector> connector = new ComboBox<>();
    private TextField value = new TextField();
    private Button delete = new Button("Delete");
    private Button apply = new Button("Apply");
    private final HBox hBox = new HBox(5, column, key, connector, value, apply, delete);


    LightVcfFilterCell(VCFHeader header, LightVcfReader reader) {
        this.reader = reader;
        hBox.setAlignment(Pos.CENTER_LEFT);
        column.getItems().addAll("CHROM", "POS", "ID", "REF", "ALT", "QUAL", "FILTER", "INFO");
        column.valueProperty().addListener(change -> columnChanged());
        key.getItems().addAll(header.getInfoHeaderLines().stream().map(VCFCompoundHeaderLine::getID).collect(Collectors.toList()));
        key.valueProperty().addListener(change -> apply.setDisable(false));
        connector.getItems().addAll(LightVcfFilter.Connector.values());
        connector.valueProperty().addListener(change -> connectorChanged());
        value.textProperty().addListener((observable, oldValue, newValue) -> apply.setDisable(false));
        apply.setOnAction(event -> apply());
        delete.setOnAction(event -> delete());
    }

    private void connectorChanged() {
        value.setDisable(connector.getValue() == LightVcfFilter.Connector.TRUE
                || connector.getValue() == LightVcfFilter.Connector.FALSE);
        apply.setDisable(false);
    }

    private void columnChanged() {
        key.setDisable(!column.getValue().equals("INFO"));
        apply.setDisable(false);
    }

    private void delete() {
        getListView().getItems().remove(getItem());
        reader.filter();
    }

    private void apply() {
        final LightVcfFilter item = getItem();
        item.setColumn(column.getValue());
        item.setKey(key.getValue());
        item.setConnector(connector.getValue());
        item.setValue(value.getText());
//        final String type = item.getType();
//        Object v = ValueUtils.getValue(value.getText(), type);
        reader.filter();
        apply.setDisable(true);
    }

    @Override
    protected void updateItem(LightVcfFilter item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            setText(null);
            setGraphic(hBox);
            column.setValue(item.getColumn());
            key.setValue(item.getKey());
            connector.setValue(item.getConnector());
            value.setText(String.valueOf(item.getValue()));
        }
    }

}
