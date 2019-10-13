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

package org.uichuimi.coat.view.lightreader.header;

import htsjdk.variant.vcf.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Created by uichuimi on 28/06/16.
 */
public class LightHeaderViewController {


    @FXML
    private TableView<VCFIDHeaderLine> idTable;
    @FXML
    private TableView<VCFHeaderLine> otherTable;
    @FXML
    private TableView<VCFContigHeaderLine> contigTable;
    @FXML
    private TableView<VCFFilterHeaderLine> filterTable;
    @FXML
    private TableView<VCFFormatHeaderLine> formatTable;
    @FXML
    private TableView<VCFInfoHeaderLine> infoTable;

    @FXML
    private void initialize() {
        initInfoTable();
        initFormatTable();
        initFilterTable();
        initContigTable();
        initOtherTable();
        initIDTable();
    }

    private void initInfoTable() {
        final TableColumn<VCFInfoHeaderLine, String> id
                = new TableColumn<>("ID");
        id.setCellValueFactory(param
                -> new SimpleObjectProperty<>(param.getValue().getID()));
        final TableColumn<VCFInfoHeaderLine, VCFHeaderLineType> type
                = new TableColumn<>("Type");
        type.setCellValueFactory(param
                -> new SimpleObjectProperty<>(param.getValue().getType()));
        final TableColumn<VCFInfoHeaderLine, String> countType
                = new TableColumn<>("Count");
        countType.setCellValueFactory(param -> new SimpleObjectProperty<>(
                String.valueOf(param.getValue().getCountType() == VCFHeaderLineCount.INTEGER
                        ? param.getValue().getCount()
                        : param.getValue().getCountType())));
        final TableColumn<VCFInfoHeaderLine, String> description
                = new TableColumn<>("Description");
        description.setCellValueFactory(param ->
                new SimpleObjectProperty<>(param.getValue().getDescription()));
        infoTable.getColumns().setAll(id, type, countType, description);
    }

    private void initFormatTable() {
        final TableColumn<VCFFormatHeaderLine, String> id
                = new TableColumn<>("ID");
        id.setCellValueFactory(param
                -> new SimpleObjectProperty<>(param.getValue().getID()));
        final TableColumn<VCFFormatHeaderLine, VCFHeaderLineType> type
                = new TableColumn<>("Type");
        type.setCellValueFactory(param
                -> new SimpleObjectProperty<>(param.getValue().getType()));
        final TableColumn<VCFFormatHeaderLine, String> countType
                = new TableColumn<>("Count");
        countType.setCellValueFactory(param -> new SimpleObjectProperty<>(
                String.valueOf(param.getValue().getCountType() == VCFHeaderLineCount.INTEGER
                        ? param.getValue().getCount()
                        : param.getValue().getCountType())));
        final TableColumn<VCFFormatHeaderLine, String> description
                = new TableColumn<>("Description");
        description.setCellValueFactory(param ->
                new SimpleObjectProperty<>(param.getValue().getDescription()));
        formatTable.getColumns().setAll(id, type, countType, description);
    }

    private void initFilterTable() {
        final TableColumn<VCFFilterHeaderLine, String> id
                = new TableColumn<>("ID");
        id.setCellValueFactory(param
                -> new SimpleObjectProperty<>(param.getValue().getID()));
        final TableColumn<VCFFilterHeaderLine, String> description
                = new TableColumn<>("Description");
        description.setCellValueFactory(param ->
                new SimpleObjectProperty<>(param.getValue().getDescription()));
        filterTable.getColumns().setAll(id, description);
    }

    private void initContigTable() {
        final TableColumn<VCFContigHeaderLine, String> id = new TableColumn<>
                ("ID");
        id.setCellValueFactory(param -> new SimpleObjectProperty<>(param
                .getValue().getID()));
        final TableColumn<VCFContigHeaderLine, Integer> length
                = new TableColumn<>("length");
        length.setCellValueFactory(param -> new SimpleObjectProperty<>(param
                .getValue().getSAMSequenceRecord().getSequenceLength()));
        contigTable.getColumns().setAll(id, length);

    }

    private void initOtherTable() {
        final TableColumn<VCFHeaderLine, String> id =
                new TableColumn<>("Key");
        id.setCellValueFactory(param -> new SimpleObjectProperty<>(param
                .getValue().getKey()));
        final TableColumn<VCFHeaderLine, String> value =
                new TableColumn<>("Value");
        id.setCellValueFactory(param -> new SimpleObjectProperty<>(param
                .getValue().getValue()));
        otherTable.getColumns().setAll(id, value);
    }

    private void initIDTable() {
        final TableColumn<VCFIDHeaderLine, String> id =
                new TableColumn<>("ID");
        id.setCellValueFactory(param -> new SimpleObjectProperty<>(param
                .getValue().getID()));
        final TableColumn<VCFIDHeaderLine, String> value =
                new TableColumn<>("Value");
        id.setCellValueFactory(param -> new SimpleObjectProperty<>(param
                .getValue().toString()));
        idTable.getColumns().setAll(id, value);
    }

    public void setHeader(VCFHeader header) {
        infoTable.getItems().setAll(header.getInfoHeaderLines());
        formatTable.getItems().setAll(header.getFormatHeaderLines());
        filterTable.getItems().setAll(header.getFilterLines());
        contigTable.getItems().setAll(header.getContigLines());
        otherTable.getItems().setAll(header.getOtherHeaderLines());
        idTable.getItems().setAll(header.getIDHeaderLines());
    }

}
