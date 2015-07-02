/*
 * Copyright (C) 2015 UICHUIMI
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
package coat.view.vcfcombiner;

import coat.CoatView;
import coat.model.tool.Tool;
import coat.model.vcfcombiner.VcfCombiner;
import coat.utils.FileManager;
import coat.utils.OS;
import coat.view.graphic.FileList;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;

import java.io.File;
import java.io.IOException;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class CombineVCF extends Tool {

    @FXML
    private FileList includes;
    @FXML
    private FileList excludes;
    @FXML
    private Button startButton;

    private Property<String> title = new SimpleStringProperty("Combine VCF");

    public CombineVCF() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CombineVCF.fxml"), OS.getResources());
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void initialize() {
        startButton.setOnAction(e -> start());
        includes.setFilters(FileManager.VCF_FILTER);
        excludes.setFilters(FileManager.VCF_FILTER);
    }

    private void start() {
        final File file = FileManager.saveFile("Select ouptut file", FileManager.VCF_FILTER);
        if (file != null) {
            final File[] incl = includes.getFiles().toArray(new File[includes.getFiles().size()]);
            final File[] excl = excludes.getFiles().toArray(new File[excludes.getFiles().size()]);
            final VcfCombiner vcfCombiner = new VcfCombiner(incl, excl, file);
            startButton.setDisable(true);
            vcfCombiner.setOnSucceeded(event -> finished("Intersection finished, output file: " + file, "success"));
            vcfCombiner.setOnFailed(event -> finished("Error with intersection", "error"));
            Platform.runLater(vcfCombiner);
        }
    }

    private void finished(String message, String typeOfMessage) {
        CoatView.printMessage(message, typeOfMessage);
        startButton.setDisable(false);
    }

    @Override
    public Property<String> getTitleProperty() {
        return title;
    }

    @Override
    public void saveAs() {

    }
}
