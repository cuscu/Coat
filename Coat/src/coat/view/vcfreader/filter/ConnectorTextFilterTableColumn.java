/*
 * Copyright (c) UICHUIMI 2016
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

package coat.view.vcfreader.filter;

import coat.core.vcf.VcfFilter;
import coat.view.graphic.SizableImageView;
import coat.view.vcfreader.VariantsTable;
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
abstract class ConnectorTextFilterTableColumn<S, T> extends FilterTableColumn<S, T> {

    private final Stage stage = new Stage(StageStyle.UTILITY);
    private ConnectorTextFilterController controller;

    ConnectorTextFilterTableColumn(VariantsTable table, String title) {
        super(table, title);
        createFilterMenu();
    }

    private void createFilterMenu() {
        addContextMenu();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("connector-text-filter.fxml"));
            final Parent parent = loader.load();
            controller = loader.getController();
            controller.getApplyButton().setOnAction(event -> updateTable());
            controller.getFilterText().setOnAction(event -> updateTable());
            controller.getConnector().setValue(VcfFilter.Connector.EQUALS);
            controller.getConnector().getItems().setAll(getConnectors());
            controller.getName().setText(getText());
            stage.setScene(new Scene(parent));
            stage.setTitle("Filter " + getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addContextMenu() {
        final ImageView filterIcon = new SizableImageView("coat/img/black/filter.png", SizableImageView.SMALL_SIZE);
        final MenuItem filterMenuItem = new MenuItem("Filter", filterIcon);
        setContextMenu(new ContextMenu(filterMenuItem));
        filterMenuItem.setOnAction(event -> showFilterMenu());
    }

    private void showFilterMenu() {
        if (stage.isShowing()) stage.requestFocus();
        else stage.show();
        controller.getFilterText().requestFocus();
    }

    protected abstract List<VcfFilter.Connector> getConnectors();

    public StringProperty filterTextProperty() {
        return controller.getFilterText().textProperty();
    }

    public Property<VcfFilter.Connector> connectorProperty() {
        return controller.getConnector().valueProperty();
    }

    public String getFilterText() {
        return filterTextProperty().get();
    }

    public VcfFilter.Connector getConnector() {
        return connectorProperty().getValue();
    }

    public void setConnector(VcfFilter.Connector connector) {
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

}
