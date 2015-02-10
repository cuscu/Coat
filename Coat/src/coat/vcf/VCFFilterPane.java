/*
 * Copyright (C) 2014 UICHUIMI
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package coat.vcf;

import coat.graphic.SizableImage;
import coat.utils.OS;
import java.util.Collections;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Graphical wrapper of a VCFFilter. (uhmmm, wrappers, gnom gnom).
 *
 * @author Lorente Arencibia, Pascual <pasculorente@gmail.com>
 */
public class VCFFilterPane extends VBox {

    /**
     * The label when the filter is not being edited.
     */
    private final Label staticInfo = new Label();
    /**
     * Field (CHROM, POS, FILTER...) combo box.
     */
    private final ComboBox<VCFFilter.Field> field = new ComboBox();
    /**
     * Connector (contains, less than...) combo box.
     */
    private final ComboBox<VCFFilter.Connector> connector = new ComboBox();
    /**
     * Info fields combo box.
     */
    private final ComboBox<String> info = new ComboBox();
    /**
     * The value textField.
     */
    private final TextField value = new TextField();
    /**
     * Accept button.
     */
    private final Button accept = new Button(null, new SizableImage("coat/img/accept.png", SizableImage.SMALL_SIZE));
    /**
     * Cancel button.
     */
    private final Button cancel = new Button(null, new SizableImage("coat/img/cancel.png", SizableImage.SMALL_SIZE));
    /**
     * Delete button.
     */
    private final Button delete = new Button(null, new SizableImage("coat/img/delete.png", SizableImage.SMALL_SIZE));
    /**
     * Button to enable/disable filter.
     */
    private final Button view = new Button(null, new SizableImage("coat/img/view.png", SizableImage.SMALL_SIZE));
    /**
     * Button to set strictness.
     */
    private final Button strict = new Button(null, new SizableImage("coat/img/circle.png", SizableImage.SMALL_SIZE));
    /**
     * Event to fire when filter is updated. Since a single filter does not have privileges on
     * firing whole table to be filtered, main window will override this value.
     */
    private EventHandler onUpdate;
    /**
     * Event to fire when filter is deleted. Since a single filter does not have privileges on
     * delete itself, main window will override this value.
     */
    private EventHandler onDelete;
    /**
     * Editing mode view.
     */
    private final HBox activePane = new HBox(field, info, connector, value, accept, cancel);
    /**
     * The related filter.
     */
    private final VCFFilter filter;

    /**
     * Creates a new VCFFilterPane.
     *
     * @param infos the infos list
     */
    public VCFFilterPane(List<String> infos) {
        filter = new VCFFilter();

        initializeFieldBox();
        initializeInfoBox(infos);
        initializeConnectorBox();
        initializeValueTextField();
        initializeButtons();

        staticInfo.setText(OS.getResources().getString("click.filter"));

        setOnMouseClicked(e -> startEdit());
        getStyleClass().add("filter-box");

        // Start in passive mode
        startEdit();
    }

    private void initializeFieldBox() {
        // Fields are constants
        field.getItems().setAll(VCFFilter.Field.values());
        field.setPromptText(OS.getResources().getString("field"));
        // Detect when the info field is selected to activate INFO combo box
        field.setOnAction(e -> {
            if (field.getSelectionModel().getSelectedItem() == VCFFilter.Field.INFO) {
                info.setDisable(false);
            } else {
                info.setDisable(true);
            }
            value.requestFocus();
        });
        info.setDisable(true);
    }

    private void initializeInfoBox(List<String> infos) {
        Collections.sort(infos);
        info.getItems().setAll(infos);
        info.setPromptText(OS.getResources().getString("info"));
    }

    private void initializeConnectorBox() {
        connector.setPromptText(OS.getResources().getString("connector"));
        connector.getItems().setAll(VCFFilter.Connector.values());
        connector.valueProperty().addListener((obs, old, current)
                -> value.setDisable(current == VCFFilter.Connector.NOT_PRESENT
                        || current == VCFFilter.Connector.PRESENT));

    }

    private void initializeValueTextField() {
        // Force textfield to have always the focus
        info.setOnAction(e -> value.requestFocus());
        connector.setOnAction(e -> value.requestFocus());
        value.setOnAction(e -> accept());
        value.setOnKeyReleased(e -> {
            // I think this is not necessary (it is called with setOnAction)
            if (e.getCode() == KeyCode.ENTER) {
                accept();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                toPassive();
            }
        });
        HBox.setHgrow(value, Priority.SOMETIMES);
    }

    private void initializeButtons() {
        accept.setOnAction(e -> accept());
        cancel.setOnAction(e -> toPassive());
        delete.setOnAction(e -> delete());
        view.setOnAction(e -> alternateView());
        strict.setOnAction(e -> alternateStrictness());

        accept.setTooltip(new Tooltip(OS.getResources().getString("accept")));
        cancel.setTooltip(new Tooltip(OS.getResources().getString("cancel.changes")));
        delete.setTooltip(new Tooltip(OS.getResources().getString("delete.filter")));
        view.setTooltip(new Tooltip(OS.getResources().getString("enable.disable.filter")));
        strict.setTooltip(new Tooltip(OS.getResources().getString("strict.non.strict")));
    }

    /**
     * Get the filter associated.
     *
     * @return the VCFFilter
     */
    public VCFFilter getFilter() {
        return filter;
    }

    /**
     * User clicked in accept.
     */
    private void accept() {
        // Set the filter
        filter.setField(field.getSelectionModel().getSelectedItem());
        filter.setConnector(connector.getSelectionModel().getSelectedItem());
        filter.setValue(value.getText());
        filter.setSelectedInfo(info.getValue());
        // Go to passive mode
        toPassive();
        // Fire onUpdate
        if (onUpdate != null) {
            onUpdate.handle(new ActionEvent());
        }
    }

    /**
     * Show the passive state: only text and buttons.
     */
    private void toPassive() {
        getChildren().clear();

        // The separator right aligns buttons
        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setVisible(false);
        HBox.setHgrow(separator, Priority.ALWAYS);

        HBox box = new HBox(staticInfo, separator, strict, view, delete);
        box.setAlignment(Pos.CENTER);
        getChildren().setAll(box);
        setStaticInfo();
    }

    /**
     * Enables field selectors and textField.
     */
    private void startEdit() {
        getChildren().setAll(activePane);
        value.requestFocus();
    }

    /**
     * Sets the string inside staticInfo. Example: CHROMOSOME is equals to 7
     */
    private void setStaticInfo() {
        String f = (filter.getField() == VCFFilter.Field.INFO)
                ? filter.getSelectedInfo() : filter.getField().name();
        String v = "";
        // v == "" if connector is present or not_present
        // v == " [empty]" if value is null or ""
        // v == value otherwise
        if (filter.getConnector() != VCFFilter.Connector.PRESENT
                && filter.getConnector() != VCFFilter.Connector.NOT_PRESENT) {
            v = " " + (filter.getValue() == null || filter.getValue().isEmpty()
                    ? "[empty]" : filter.getValue());
        }
        staticInfo.setText(f + " " + filter.getConnector() + v);
    }

    /**
     * Sets what happens when user changes something in the filter. Usually refilter.
     *
     * @param onUpdate the method to call when the user updates the filter
     */
    public void setOnUpdate(EventHandler onUpdate) {
        this.onUpdate = onUpdate;
    }

    /**
     * Sets what happens when user clicks on delete. Usually remove from user view.
     *
     * @param onDelete the method to call when user deletes the filter
     */
    public void setOnDelete(EventHandler onDelete) {
        this.onDelete = onDelete;
    }

    /**
     * Oh, user clicked on delete.
     */
    private void delete() {
        if (onDelete != null) {
            onDelete.handle(new ActionEvent());
        }
    }

    /**
     * Change the view icon and the active flag of filter.
     */
    private void alternateView() {
        boolean act = filter.isEnabled();
        filter.setEnabled(!act);
        view.setGraphic(act ? new SizableImage("coat/img/noview.png", 16)
                : new SizableImage("coat/img/view.png", 16));
        if (onUpdate != null) {
            onUpdate.handle(new ActionEvent());
        }
    }

    /**
     * Change the strictness flag and the tag icon.
     */
    private void alternateStrictness() {
        boolean isStrict = filter.isStrict();
        filter.setStrict(!isStrict);
        strict.setGraphic(isStrict ? new SizableImage("coat/img/nocircle.png", SizableImage.SMALL_SIZE)
                : new SizableImage("coat/img/circle.png", SizableImage.SMALL_SIZE));
        if (onUpdate != null) {
            onUpdate.handle(new ActionEvent());
        }
    }
}
