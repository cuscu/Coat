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
package coat.view.mist;

import coat.model.mist.MistCombiner;
import coat.model.tool.Tool;
import coat.utils.FileManager;
import coat.utils.OS;
import coat.view.graphic.FileList;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.File;
import java.io.IOException;

/**
 * Controller for the Combine MIST Window.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class CombineMIST extends Tool {

    @FXML
    private Label messageLabel;
    @FXML
    private FileList files;
    @FXML
    private Button startButton;

    private Property<String> title = new SimpleObjectProperty<>("Combine Mist");

    public CombineMIST() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CombineMIST.fxml"), OS.getResources());
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        startButton.setOnAction(e -> combine());
        files.setFilters(FileManager.MIST_FILTER);
    }

    private void combine() {
        final File f = getOutput();
        if (f != null) {
            final MistCombiner mistCombinator = new MistCombiner(files.getFiles(), f);
            messageLabel.textProperty().bind(mistCombinator.messageProperty());
            startButton.setDisable(true);
            mistCombinator.setOnSucceeded(this::combinerFinished);
            Platform.runLater(mistCombinator);
        }
    }

    private void combinerFinished(WorkerStateEvent event) {
        messageLabel.textProperty().unbind();
        startButton.setDisable(false);
    }

    private File getOutput() {
        String message = OS.getStringFormatted("select.file", "MIST");
        return FileManager.saveFile(message, FileManager.MIST_FILTER);
    }

    @Override
    public Property<String> getTitleProperty() {
        return title;
    }

    @Override
    public void saveAs() {

    }
}
