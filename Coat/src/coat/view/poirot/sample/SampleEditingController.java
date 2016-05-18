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

package coat.view.poirot.sample;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.stream.Collectors;

/**
 * Created by uichuimi on 16/05/16.
 */
public class SampleEditingController {
    @FXML
    private ComboBox<Sample> mother;
    @FXML
    private ComboBox<Sample> father;
    @FXML
    private ComboBox<Sample.Sex> sex;
    @FXML
    private ComboBox<Boolean> affected;
    @FXML
    private Label name;
    private Sample sample;

    private Property<EventHandler<ActionEvent>> onDelete = new SimpleObjectProperty<>();

    @FXML
    private void initialize() {
        sex.setItems(FXCollections.observableArrayList(Sample.Sex.values()));
        sex.setValue(Sample.Sex.FEMALE);
        affected.getItems().addAll(Boolean.FALSE, Boolean.TRUE);
        affected.setValue(true);
        mother.setCellFactory(param -> new AncestorCell());
        mother.setButtonCell(new AncestorCell());
        father.setCellFactory(param -> new AncestorCell());
        father.setButtonCell(new AncestorCell());
    }

    public void delete(ActionEvent actionEvent) {
        onDelete.getValue().handle(actionEvent);
    }

    public void setSample(Sample sample, ListView<Sample> sampleList) {
        unbind();
        this.sample = sample;
        setAncestorLists(sample, sampleList);
        bind();
    }

    private void setAncestorLists(Sample sample, ListView<Sample> sampleList) {
        mother.getItems().setAll(sampleList.getItems().stream()
                .filter(sample1 -> sample1.getSex() == Sample.Sex.FEMALE)
                .filter(sample1 -> sample1 != sample)
                .collect(Collectors.toList()));
        father.getItems().setAll(sampleList.getItems().stream()
                .filter(sample1 -> sample1.getSex() == Sample.Sex.MALE)
                .filter(sample1 -> sample1 != sample)
                .collect(Collectors.toList()));
    }

    private void unbind() {
        if (sample != null) {
            mother.valueProperty().unbindBidirectional(sample.motherProperty());
            father.valueProperty().unbindBidirectional(sample.fatherProperty());
            sex.valueProperty().unbindBidirectional(sample.sexProperty());
            name.textProperty().unbindBidirectional(sample.nameProperty());
            affected.valueProperty().unbindBidirectional(sample.affectedProperty());
        }
    }

    private void bind() {
        if (sample != null) {
            mother.valueProperty().bindBidirectional(sample.motherProperty());
            father.valueProperty().bindBidirectional(sample.fatherProperty());
            sex.valueProperty().bindBidirectional(sample.sexProperty());
            name.textProperty().bindBidirectional(sample.nameProperty());
            affected.valueProperty().bindBidirectional(sample.affectedProperty());
        }
    }

    void setOnDelete(EventHandler<ActionEvent> handler) {
        onDelete.setValue(handler);
    }

    private class AncestorCell extends ListCell<Sample> {
        @Override
        protected void updateItem(Sample item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty ? null : item.getName());
        }
    }
}
