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

package org.uichuimi.coat.view.vcfreader.filter;

import org.uichuimi.coat.utils.OS;
import org.uichuimi.coat.view.graphic.SizableImageView;
import org.uichuimi.coat.view.vcfreader.VariantsTable;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.List;

/**
 * Use for filters that relies on a TextField as user input. This class provides a convenient window to write text
 * and select a VcfFilter.Connector.
 * <p>
 * Created by uichuimi on 1/07/16.
 */
public abstract class ConnectorTextFilterTableColumn<S, T> extends FilterTableColumn<S, T> {

    private final Stage stage = new Stage(StageStyle.UTILITY);
    private ConnectorTextFilterController controller;

    ConnectorTextFilterTableColumn(VariantsTable table, String title) {
        super(table, title);
        createFilterMenu();
    }

    private void createFilterMenu() {
        addContextMenu();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/connector-text-filter.fxml"), OS.getResources());
            final Parent parent = loader.load();
            controller = loader.getController();
            controller.getApplyButton().setOnAction(event -> updateTable());
            controller.getFilterText().setOnAction(event -> updateTable());
            controller.getConnector().setValue(Connector.EQUALS);
            controller.getConnector().getItems().setAll(getConnectors());
            controller.getName().setText(getText());
            stage.setScene(new Scene(parent));
            stage.setTitle(getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addContextMenu() {
        final ImageView filterIcon = new SizableImageView("/img/black/filter.png", SizableImageView.SMALL_SIZE);
        final MenuItem filterMenuItem = new MenuItem(OS.getString("filter"), filterIcon);
        setContextMenu(new ContextMenu(filterMenuItem));
        filterMenuItem.setOnAction(event -> showFilterMenu());
    }

    private void showFilterMenu() {
        if (stage.isShowing()) stage.requestFocus();
        else stage.show();
        controller.getFilterText().requestFocus();
    }

    protected abstract List<Connector> getConnectors();

    private StringProperty filterTextProperty() {
        return controller.getFilterText().textProperty();
    }

    private Property<Connector> connectorProperty() {
        return controller.getConnector().valueProperty();
    }

    String getFilterText() {
        return filterTextProperty().get();
    }

    public Connector getConnector() {
        return connectorProperty().getValue();
    }

    public void setConnector(Connector connector) {
        controller.getConnector().setValue(connector);
    }

    public void setFilterValue(String value) {
        controller.getFilterText().setText(value);
    }

    public boolean isStrict() {
        return controller.getStrict().isSelected();
    }

    @Override
    public void clear() {
        controller.getFilterText().clear();
    }

    /**
     * The type of relation between the pass value and the field value.
     */
    public enum Connector {

        /**
         * Equals to (String or natural number).
         */
        EQUALS {
            @Override
            public String toString() {
                return OS.getResources().getString("equals.to");
            }

        },
        /**
         * Contains (String)
         */
        CONTAINS {
            @Override
            public String toString() {
                return OS.getResources().getString("contains");
            }
        },
        /**
         * Greater than (number).
         */
        GREATER {
            @Override
            public String toString() {
                return OS.getResources().getString("greater.than");
            }

        },
        /**
         * Less than (number).
         */
        LESS {
            @Override
            public String toString() {
                return OS.getResources().getString("less.than");
            }

        },
        /**
         * Regular expression (String).
         */
        MATCHES {
            @Override
            public String toString() {
                return OS.getResources().getString("matches");
            }

        },
        /**
         * Different (String, ¿number?).
         */
        DIFFERS {
            @Override
            public String toString() {
                return OS.getResources().getString("differs.from");
            }

        }
    }
}
