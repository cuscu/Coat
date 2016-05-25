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

import coat.utils.FileManager;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;
import java.util.Observable;

/**
 * Created by uichuimi on 16/05/16.
 */
public class SampleListController {
    @FXML
    private VBox editing;
    @FXML
    private SampleEditingController editingController;
    @FXML
    private ListView<Sample> sampleList;


    @FXML
    private void initialize() {
        sampleList.setCellFactory(param -> new SampleCell());
        sampleList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            editingController.setSample(newValue, sampleList);
            editing.setDisable(newValue == null);
        });
        editing.setDisable(true);
        editingController.setOnDelete(event -> sampleList.getItems().remove(sampleList.getSelectionModel().getSelectedItem()));
    }

    public void addSample(ActionEvent actionEvent) {
        final List<File> files = FileManager.openFiles("Select VCF file", FileManager.VCF_FILTER);
        if (files != null) files.forEach(file -> sampleList.getItems().add(new Sample(file)));
    }

    public List<Sample> getSamples() {
        return sampleList.getItems();
    }

    public ObservableList<Sample> samples() {
        return sampleList.getItems();
    }
}
