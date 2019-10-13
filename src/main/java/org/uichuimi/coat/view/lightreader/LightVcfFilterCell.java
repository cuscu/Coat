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

package org.uichuimi.coat.view.lightreader;

import org.uichuimi.coat.view.lightreader.LightVcfFilter.Connector;
import htsjdk.variant.vcf.VCFCompoundHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineType;
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
    private VCFHeader header;
    private ComboBox<String> column = new ComboBox<>();
    private ComboBox<String> key = new ComboBox<>();
    private ComboBox<Connector> connector = new ComboBox<>();
    private TextField value = new TextField();
    private Button delete = new Button("Delete");
    private Button apply = new Button("Apply");
    private final HBox hBox = new HBox(5, column, key, connector, value, apply, delete);


    LightVcfFilterCell(VCFHeader header, LightVcfReader reader) {
        this.header = header;
        this.reader = reader;
        hBox.setAlignment(Pos.CENTER_LEFT);
        column.getItems().addAll("CHROM", "POS", "ID", "REF", "ALT", "QUAL", "FILTER", "INFO");
        column.valueProperty().addListener(change -> columnChanged());
        key.getItems().addAll(header.getInfoHeaderLines().stream().map(VCFCompoundHeaderLine::getID).collect(Collectors.toList()));
        key.valueProperty().addListener(change -> keyChanged());
        connector.getItems().addAll(Connector.values());
        connector.valueProperty().addListener(change -> connectorChanged());
        value.textProperty().addListener((observable, oldValue, newValue) -> valueChanged());
        value.setOnAction(event -> apply());
        apply.setOnAction(event -> apply());
        delete.setOnAction(event -> delete());
    }

    private void valueChanged() {
        apply.setDisable(false);
    }

    private void keyChanged() {
        apply.setDisable(false);
        updateConnector();
    }

    private void connectorChanged() {
        value.setDisable(connector.getValue() == Connector.TRUE
                || connector.getValue() == Connector.FALSE);
        apply.setDisable(false);
    }

    private void columnChanged() {
        key.setDisable(!column.getValue().equals("INFO"));
        apply.setDisable(false);
        updateConnector();
    }

    private void updateConnector() {
        switch (type()) {
            case Integer:
            case Float:
                connector.getItems().setAll(Connector.EQUALS, Connector.IS_NOT,
                        Connector.LESS_THAN, Connector.MORE_THAN,
                        Connector.TRUE, Connector.FALSE);
                break;
            case Flag:
                connector.getItems().setAll(Connector.TRUE,
                        Connector.FALSE, Connector.IS_NOT);
                break;
            case String:
            case Character:
            default:
                connector.getItems().setAll(Connector.EQUALS,
                        Connector.CONTAINS, Connector.IS_NOT, Connector.TRUE,
                        Connector.FALSE);
                break;
        }
        connector.setValue(connector.getItems().get(0));
    }

    private VCFHeaderLineType type() {
        if (column.getValue().equals("INFO")) {
            if (key.getValue() == null) return VCFHeaderLineType.String;
            return header.getInfoHeaderLine(key.getValue()).getType();
        } else {
            switch (column.getValue()) {
                case "CHROM":
                case "FILTER":
                case "REF":
                case "ALT":
                case "ID":
                    return VCFHeaderLineType.String;
                case "POS":
                    return VCFHeaderLineType.Integer;
                case "QUAL":
                    return VCFHeaderLineType.Float;
            }
        }
        return VCFHeaderLineType.String;
    }

    private void delete() {
        getListView().getItems().remove(getItem());
        reader.loadAndFilter();
    }

    private void apply() {
        final LightVcfFilter item = getItem();
        item.setColumn(column.getValue());
        item.setKey(key.getValue());
        item.setConnector(connector.getValue());
        item.setValue(value.getText());
//        final String type = item.getType();
//        Object v = ValueUtils.getValue(value.getText(), type);
        reader.loadAndFilter();
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
            apply.setDisable(true);
        }
    }

}
