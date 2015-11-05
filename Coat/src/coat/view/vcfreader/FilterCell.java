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

import coat.core.vcf.Variant;
import coat.core.vcf.VcfFilter;
import coat.utils.OS;
import coat.view.graphic.AutoFillComboBox;
import coat.view.graphic.SizableImage;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
class FilterCell extends ListCell<VcfFilter> {

    private final static int SPACING = 2;

    private final ComboBox<VcfFilter.Field> field = new ComboBox<>();
    //    private final ComboBox<String> info = new ComboBox<>();
    private final AutoFillComboBox info = new AutoFillComboBox();
    private final ComboBox<VcfFilter.Connector> connector = new ComboBox<>();
    //    private final TextField value = new TextField();
    private final AutoFillComboBox value = new AutoFillComboBox();
    private final HBox filterBox = new HBox(SPACING, field, info, connector, value);

    private final Separator invisibleActiveSeparator = new Separator(Orientation.HORIZONTAL);
    private final Button accept = new Button(null, new SizableImage("coat/img/accept.png", SizableImage.SMALL_SIZE));
    private final Button cancel = new Button(null, new SizableImage("coat/img/cancel.png", SizableImage.SMALL_SIZE));
    private final HBox activeBox = new HBox(SPACING, filterBox, invisibleActiveSeparator, accept, cancel);

    private final Label passiveInfo = new Label();

    private final SizableImage circle = new SizableImage("coat/img/circle.png", SizableImage.SMALL_SIZE);
    private final SizableImage nocircle = new SizableImage("coat/img/nocircle.png", SizableImage.SMALL_SIZE);
    private final SizableImage viewImg = new SizableImage("coat/img/view.png", SizableImage.SMALL_SIZE);
    private final SizableImage noview = new SizableImage("coat/img/noview.png", SizableImage.SMALL_SIZE);
    private final SizableImage deleteImg = new SizableImage("coat/img/delete.png", SizableImage.SMALL_SIZE);

    private final ToggleButton strict = new ToggleButton(null, circle);
    private final ToggleButton view = new ToggleButton(null, viewImg);
    private final Button delete = new Button(null, deleteImg);

    private final Separator invisiblePassiveSeparator = new Separator(Orientation.HORIZONTAL);

    private final HBox passiveBox = new HBox(SPACING, passiveInfo, invisiblePassiveSeparator, strict, view, delete);

    private final List<String> infoItems = new ArrayList<>();

    private VcfFilter currentFilter;
    private ObservableList<Variant> variants;
    private ObservableList<String> infos;

    public FilterCell(ObservableList<Variant> variants, ObservableList<String> infos) {
        this.variants = variants;
        this.infos = infos;
        infos.addListener((ListChangeListener< String>) c -> updateInfoList());
        initializeThis();
        initializeActiveBox();
        initializePassiveBox();
        initializeButtonActions();
        updateInfoList();
    }

    private void initializeThis() {
        setEditable(true);
        setText(null);
    }

    private void initializeActiveBox() {
        HBox.setHgrow(invisibleActiveSeparator, Priority.ALWAYS);
        invisibleActiveSeparator.setVisible(false);
        activeBox.setAlignment(Pos.CENTER);
        initializeInfoBox();
        initializeConnectorBox();
        initializeFieldBox();
        value.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) reloadValues();
        });
    }

    private void reloadValues() {
        final VcfFilter.Field fieldValue = field.getValue();
        if (fieldValue == VcfFilter.Field.CHROMOSOME) {
            final List<String> valueList = variants.stream().map(Variant::getChrom).distinct().collect(Collectors.toList());
            Collections.sort(valueList);
            value.getItems().setAll(valueList);
        } else if (fieldValue == VcfFilter.Field.REF) {
            final List<String> valueList = variants.stream().map(Variant::getRef).distinct().collect(Collectors.toList());
            Collections.sort(valueList);
            value.getItems().setAll(valueList);
        } else if (fieldValue == VcfFilter.Field.INFO) {
            final String infoValue = info.getText();
            if (infoValue != null) {
                final List<String> valueList = variants.stream()
                        .map(variant -> variant.getInfo(infoValue))
                        .filter(val -> val != null)
                        .distinct()
                        .collect(Collectors.toList());
                Collections.sort(valueList);
                value.getItems().setAll(valueList);
            }
        }

    }

    private void initializeInfoBox() {
        info.setPromptText(OS.getResources().getString("info"));
        info.setDisable(true);
//        setSmartInfoBox();
    }

//    private void setSmartInfoBox() {
//        info.setEditable(true);
//        info.getEditor().setOnKeyReleased(event -> displayMatchingInfos());
//        final ChangeListener<Boolean> listener = (observable, oldValue, newValue) -> {
//            if (newValue) displayMatchingInfos();
//        };
//        info.getEditor().focusedProperty().addListener(listener);
//    }

//    private void displayMatchingInfos() {
//        String text = info.getEditor().getText();
//        List<String> validCells = infoItems.stream().filter(s -> s.toLowerCase().contains(text.toLowerCase())).collect(Collectors.toList());
//        info.getItems().setAll(validCells);
//        info.show();
//    }

    private void initializeConnectorBox() {
        connector.setPromptText(OS.getResources().getString("connector"));
        connector.getItems().setAll(VcfFilter.Connector.values());
        connector.valueProperty().addListener((obs, old, current)
                -> value.setDisable(current == VcfFilter.Connector.NOT_PRESENT
                || current == VcfFilter.Connector.PRESENT));
        connector.setOnAction(event -> value.requestFocus());
    }

    private void initializeFieldBox() {
        field.getItems().setAll(VcfFilter.Field.values());
        field.setPromptText(OS.getResources().getString("field"));
        // Detect when the info field is selected to activate INFO combo box
        field.setOnAction(e -> fieldSelected());
    }

    private void fieldSelected() {
        info.setDisable(field.getSelectionModel().getSelectedItem() != VcfFilter.Field.INFO);
        value.requestFocus();
    }

    private void initializePassiveBox() {
        passiveBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(invisiblePassiveSeparator, Priority.ALWAYS);
        invisiblePassiveSeparator.setVisible(false);
    }

    private void initializeButtonActions() {
        cancel.setOnAction(event -> cancelEdit());
        delete.setOnAction(event -> delete());
        value.setOnAction(event -> commit());
        accept.setOnAction(event -> commit());

        strict.selectedProperty().addListener((observable, oldValue, selected) -> strict.setGraphic(selected ? circle : nocircle));
        view.selectedProperty().addListener((observable, oldValue, selected) -> view.setGraphic(selected ? viewImg : noview));

        setButtonsStyle();
    }

    private void setButtonsStyle() {
        accept.getStyleClass().add("graphic-button");
        cancel.getStyleClass().add("graphic-button");
        strict.getStyleClass().add("graphic-button");
        view.getStyleClass().add("graphic-button");
        delete.getStyleClass().add("graphic-button");
    }

    private void updateInfoList() {
        info.getItems().setAll(infos.stream().sorted().collect(Collectors.toList()));
    }

    private void delete() {
        getListView().getItems().remove(currentFilter);
    }

    @Override
    protected void updateItem(VcfFilter vcfFilter, boolean empty) {
        super.updateItem(vcfFilter, empty);
        if (empty) setGraphic(null);
        else showPassiveView(vcfFilter);
    }

    private void showPassiveView(VcfFilter vcfFilter) {
        if (currentFilter != null) unbindButtons();
        currentFilter = vcfFilter;
        bindButtons();
        toPassive();
    }

    private void unbindButtons() {
        strict.selectedProperty().unbindBidirectional(currentFilter.getStrictProperty());
        view.selectedProperty().unbindBidirectional(currentFilter.getEnabledProperty());
    }

    private void bindButtons() {
        strict.selectedProperty().bindBidirectional(currentFilter.getStrictProperty());
        view.selectedProperty().bindBidirectional(currentFilter.getEnabledProperty());
    }

    private void toPassive() {
        updateStaticInfo();
        setGraphic(passiveBox);
    }

    private void bind(VcfFilter vcfFilter) {
        field.setValue(vcfFilter.getField());
        info.setValue(vcfFilter.getSelectedInfo());
        connector.setValue(vcfFilter.getConnector());
        value.setText(vcfFilter.getValue());
    }

    private void updateStaticInfo() {
        if (filterIsValid()) setPassiveInfoText();
        else passiveInfo.setText(OS.getResources().getString("click.filter"));
    }

    private boolean filterIsValid() {
        return currentFilter != null
                && currentFilter.getConnector() != null
                && currentFilter.getField() != null
                && !(currentFilter.getField() == VcfFilter.Field.INFO
                && (currentFilter.getSelectedInfo() == null));
    }

    private void setPassiveInfoText() {
        String element = getElement();
        String relation = currentFilter.getConnector().toString();
        String val = getValue();
        passiveInfo.setText(element + " " + relation + " " + val);
    }

    private String getValue() {
        final boolean valueNotNecessary = Arrays.asList(VcfFilter.Connector.PRESENT, VcfFilter.Connector.NOT_PRESENT).contains(currentFilter.getConnector());
        return valueNotNecessary ? "" : getValueString();
    }

    private String getValueString() {
        return currentFilter.getValue() == null || currentFilter.getValue().isEmpty() ? "[empty]" : currentFilter.getValue();
    }

    private String getElement() {
        return currentFilter.getField() == VcfFilter.Field.INFO
                ? currentFilter.getSelectedInfo()
                : currentFilter.getField().name();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        toPassive();
    }

    @Override
    public void startEdit() {
        super.startEdit();
        bind(currentFilter);
        setGraphic(activeBox);
    }

    @Override
    public void commitEdit(VcfFilter ignored) {
        super.commitEdit(ignored);
        currentFilter.setValue(value.getText());
        currentFilter.setSelectedInfo(info.getText());
        currentFilter.setConnector(connector.getValue());
        currentFilter.setField(field.getValue());
        toPassive();
    }

    private void commit() {
        commitEdit(currentFilter);
    }
}
