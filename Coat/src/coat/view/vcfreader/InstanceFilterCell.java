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

package coat.view.vcfreader;

import coat.core.vcf.VcfFilter;
import coat.view.graphic.AutoFillComboBox;
import coat.view.graphic.SizableImage;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.Collections;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class InstanceFilterCell extends ListCell<InstanceFilter> {

    private final ComboBox<String> fieldBox = new ComboBox<>();
    private final ComboBox<VcfFilter.Connector> connectorBox = new ComboBox<>();
    private final AutoFillComboBox valueBox = new AutoFillComboBox();

    private final SizableImage circleIcon = new SizableImage("coat/img/circle.png", SizableImage.SMALL_SIZE);
    private final SizableImage noCircleIcon = new SizableImage("coat/img/nocircle.png", SizableImage.SMALL_SIZE);


    private final Separator separator = new Separator(Orientation.HORIZONTAL);
    private final Button cancel = new Button(null, new SizableImage("coat/img/cancel.png", SizableImage.SMALL_SIZE));
    private final Button ok = new Button(null, new SizableImage("coat/img/accept.png", SizableImage.SMALL_SIZE));

    private final Button delete = new Button(null, new SizableImage("coat/img/delete.png", SizableImage.SMALL_SIZE));
    private final Button strict = new Button(null, new SizableImage("coat/img/circle.png", SizableImage.SMALL_SIZE));
    private final HBox buttonsBox = new HBox(delete, strict);

    private final HBox box = new HBox(5, fieldBox, connectorBox, valueBox, separator, ok);

    private final VcfFiltersPane filtersPane;

    public InstanceFilterCell(VcfFiltersPane filtersPane) {
        this.filtersPane = filtersPane;
        connectorBox.setItems(FXCollections.observableArrayList(VcfFilter.Connector.values()));
        fieldBox.setOnAction(event -> updateValueList());
        valueBox.setOnAction(event -> commitEdit(getItem()));

        setGraphicTextGap(5);
        setButtons();
    }

    private void setButtons() {
        setOkButton();
        setDeleteButton();
        setCancelButton();
        setStrictButton();
        setSeparator();
    }

    private void setSeparator() {
        HBox.setHgrow(separator, Priority.ALWAYS);
        separator.setVisible(false);
    }

    private void setCancelButton() {
        cancel.setOnAction(event -> cancelEdit());
    }

    private void setDeleteButton() {
        delete.setOnAction(event -> {
            getListView().getItems().remove(getItem());
            filtersPane.filter();
        });
        delete.getStyleClass().add("graphic-button");
    }

    private void setStrictButton() {
        strict.getStyleClass().add("graphic-button");
        strict.setOnAction(event -> {
            getItem().setStrict(!getItem().isStrict());
            strict.setGraphic(getItem().isStrict() ? noCircleIcon : circleIcon);
        });
    }

    private void setOkButton() {
        ok.getStyleClass().add("graphic-button");
        ok.setOnAction(event -> commitEdit(getItem()));
    }


    @Override
    protected void updateItem(InstanceFilter filter, boolean empty) {
        super.updateItem(filter, empty);
        if (!empty) {
            toPassive(filter);
        } else {
            setText(null);
            setGraphic(null);
        }
    }

    @Override
    public void startEdit() {
        super.startEdit();
        fieldBox.setValue(getItem().getField());
        connectorBox.setValue(getItem().getConnector());
        valueBox.setValue(getItem().getValue());
        fieldBox.getItems().setAll(filtersPane.getOutputVariants().get(0).getDataset().getColumnNames());
        updateValueList();
        setGraphic(box);
        setText(null);
    }

    @Override
    public void commitEdit(InstanceFilter filter) {
        super.commitEdit(filter);
        filter.setField(fieldBox.getValue());
        filter.setConnector(connectorBox.getValue());
        filter.setValue(valueBox.getText());
        toPassive(filter);
        filtersPane.filter();
    }


    private void toPassive(InstanceFilter filter) {
        setText(filter.getField() + " " + filter.getConnector() + " " + filter.getValue());
        setGraphic(buttonsBox);
    }


    private void updateValueList() {
        valueBox.getItems().clear();
        final String field = fieldBox.getValue();
        final int index = filtersPane.getOutputVariants().get(0).getDataset().indexOf(field);
        if (index >= 0) {
            filtersPane.getOutputVariants().stream()
                    .map(instance -> (String) instance.getField(index))
                    .distinct()
                    .forEach(o -> valueBox.getItems().add(o));
            Collections.sort(valueBox.getItems());
        }
    }
}
