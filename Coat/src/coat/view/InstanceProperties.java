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

package coat.view;

import coat.core.poirot.dataset.Instance;
import coat.view.vcfreader.Info;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class InstanceProperties extends VBox {

    private final TableView<Info> tableView = new TableView<>();

    InstanceProperties() {
        getChildren().addAll(tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        createColumns();
    }

    private void createColumns() {
        final TableColumn<Info, String> title = new TableColumn<>("Name");
        final TableColumn<Info, String> value = new TableColumn<>("Value");
        title.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getName()));
        value.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getValue()));
        tableView.getColumns().addAll(title, value);
    }

    public void setInstance(Instance instance) {
        tableView.getItems().clear();
        if (instance != null) {
            final List<String> columnNames = new ArrayList<>(instance.getDataset().getColumnNames());
            Collections.sort(columnNames);
            tableView.getItems().setAll(
                    columnNames.stream()
                            .map(name -> new Info(name, (String) instance.getField(name), null))
                            .collect(Collectors.toList()));
        }
    }

}
