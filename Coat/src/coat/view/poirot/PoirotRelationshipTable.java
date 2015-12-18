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

import coat.core.poirot.PearlRelationship;
import coat.utils.OS;
import coat.view.graphic.NaturalCell;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotRelationshipTable extends TableView<PearlRelationship> {

    // "database", "id", "source", "target", "method", "type", "score"

    public PoirotRelationshipTable() {
        getColumns().addAll(getDatabaseColumn(), getIdColumn(), getMethodColumn(), getTypeColumn(), getScoreColumn());
        setMaxHeight(200);
    }

    private TableColumn<PearlRelationship, String> getDatabaseColumn() {
        final TableColumn<PearlRelationship, String> database = new TableColumn<>(OS.getString("database"));
        database.setCellFactory(param -> new NaturalCell<>());
        database.setCellValueFactory(param -> new SimpleObjectProperty<>((String) param.getValue().getProperties().get("database")));
        return database;
    }

    private TableColumn<PearlRelationship, String> getIdColumn() {
        final TableColumn<PearlRelationship, String> id = new TableColumn<>(OS.getString("id"));
        id.setCellFactory(param -> new NaturalCell<>());
        id.setCellValueFactory(param -> new SimpleObjectProperty<>((String) param.getValue().getProperties().get("id")));
//        id.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().toString()));
        return id;
    }

    private TableColumn<PearlRelationship, String> getMethodColumn() {
        final TableColumn<PearlRelationship, String> method = new TableColumn<>(OS.getString("method"));
        method.setCellFactory(param -> new NaturalCell<>());
        method.setCellValueFactory(param -> new SimpleObjectProperty<>((String) param.getValue().getProperties().get("method")));
        return method;
    }

    private TableColumn<PearlRelationship, String> getTypeColumn() {
        final TableColumn<PearlRelationship, String> method = new TableColumn<>(OS.getString("type"));
        method.setCellFactory(param -> new NaturalCell<>());
        method.setCellValueFactory(param -> new SimpleObjectProperty<>((String) param.getValue().getProperties().get("type")));
        return method;
    }

    private TableColumn<PearlRelationship, String> getScoreColumn() {
        final TableColumn<PearlRelationship, String> method = new TableColumn<>(OS.getString("score"));
        method.setCellFactory(param -> new NaturalCell<>());
        method.setCellValueFactory(param -> new SimpleObjectProperty<>((String) param.getValue().getProperties().get("score")));
        return method;
    }
}
