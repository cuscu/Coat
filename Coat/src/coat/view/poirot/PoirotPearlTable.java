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

package coat.view.poirot;

import coat.core.poirot.Pearl;
import coat.view.graphic.IndexCell;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotPearlTable extends TableView<Pearl> {

    PoirotPearlTable() {
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        final TableColumn<Pearl, String> scoreColumn = new TableColumn<>("Score");
        final TableColumn<Pearl, Integer> distanceColumn = new TableColumn<>("Dist");
        final TableColumn<Pearl, Integer> indexColumn = new TableColumn<>("*");
        final TableColumn<Pearl, String> nameColumn = new TableColumn<>("Name");
        getColumns().addAll(indexColumn, distanceColumn, scoreColumn, nameColumn);
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        final MenuItem menuItem = new MenuItem("Copy");
        final ContextMenu menu = new ContextMenu(menuItem);
        setContextMenu(menu);
        menuItem.setOnAction(event -> copy());
        distanceColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getDistanceToPhenotype()));
        scoreColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(String.format("%.2f", param.getValue().getScore())));
        nameColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getName()));
        indexColumn.setCellFactory(param -> new IndexCell());

    }

    private void copy() {
        final StringBuilder builder = new StringBuilder();
        getSelectionModel().getSelectedItems()
                .forEach(pearl -> builder
                        .append(pearl.getName()).append("\t")
                        .append(String.format("%.2f", pearl.getScore())).append("\t")
                        .append(pearl.getDistanceToPhenotype()).append("\n"));
        final ClipboardContent content = new ClipboardContent();
        content.putString(builder.toString());
        Clipboard.getSystemClipboard().setContent(content);
    }
}
