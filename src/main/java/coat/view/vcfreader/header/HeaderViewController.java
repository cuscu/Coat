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

package coat.view.vcfreader.header;

import coat.utils.OS;
import coat.view.graphic.NaturalCell;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import org.jetbrains.annotations.NotNull;
import org.uichuimi.vcf.header.ComplexHeaderLine;
import org.uichuimi.vcf.header.VcfHeader;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by uichuimi on 28/06/16.
 */
public class HeaderViewController {

    public static final int FIXED_CELL_SIZE = 40;
    @FXML
    private Accordion accordion = new Accordion();

    @FXML
    private void initialize() {
    }

    public void setHeader(VcfHeader header) {
        addSingleHeaders(header);
        addComplexHeaders(header);
    }

    private void addSingleHeaders(VcfHeader header) {
        final TableView<String[]> tableView = createTableView(Arrays.asList(OS.getString("name"), OS.getString("value")));
        header.getSimpleHeaders().forEach(line -> tableView.getItems()
                .add(new String[]{line.getKey(), line.getValue()}));
        styleTable(tableView);
        accordion.getPanes().add(new TitledPane(OS.getString("single.headers"), tableView));
    }

    private void addComplexHeaders(VcfHeader header) {
        header.getComplexLines().forEach((domain, map) -> {
            final List<String> columns = map.values().stream()
                    .flatMap(headerLine -> headerLine.getValue().keySet().stream())
                    .distinct()
                    .collect(Collectors.toList());
            final TableView<String[]> tableView = createTableView(columns);
            final List<String[]> rows = map.values().stream()
                    .map(headerLine -> toRow(headerLine, columns))
                    .collect(Collectors.toList());
            tableView.getItems().setAll(rows);
            styleTable(tableView);
            accordion.getPanes().add(new TitledPane(domain, tableView));
        });
    }

    private String[] toRow(ComplexHeaderLine line, List<String> cols) {
        final String[] row = new String[cols.size()];
        for (int i = 0; i < cols.size(); i++)
            row[i] = line.getValue().getOrDefault(cols.get(i), "");
        return row;
    }

    private void styleTable(TableView<String[]> tableView) {
        tableView.setEditable(true);
        tableView.setFixedCellSize(FIXED_CELL_SIZE);
        tableView.setPrefHeight(FIXED_CELL_SIZE * (tableView.getItems().size() + 2));
        tableView.setMinHeight(FIXED_CELL_SIZE * (tableView.getItems().size() + 2));
        tableView.setMaxHeight(FIXED_CELL_SIZE * (tableView.getItems().size() + 2));
    }

    @NotNull
    private TableView<String[]> createTableView(Collection<String> columnNames) {
        final TableView<String[]> tableView = new TableView<>();
        int i = 0;
        for (String name : columnNames) {
            final TableColumn<String[], String> column = new TableColumn<>(name);
            final int index = i++;
            column.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()[index]));
            column.setCellFactory(param -> new NaturalCell<>());
            tableView.getColumns().add(column);
        }
        return tableView;
    }

    @NotNull
    private Set<String> getAllKeys(List<Map<String, String>> mapList) {
        final Set<String> keys = new LinkedHashSet<>();
        mapList.forEach(map -> keys.addAll(map.keySet()));
        return keys;
    }
}
