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

import coat.CoatView;
import coat.model.mist.MistCombinator;
import coat.utils.FileManager;
import coat.utils.OS;
import coat.view.graphic.FileList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.File;

/**
 * Controller for the Combine MIST Window.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class CombineMIST {

    @FXML
    private FileList files;
    @FXML
    private Button startButton;

    /**
     * Initializes the controller class.
     */
    @FXML
    private void initialize() {
        startButton.setOnAction(e -> combine());
        files.setFilters(FileManager.MIST_FILTER);
    }

    private void combine() {
        File f = getOutput();
        if (f != null){
            final MistCombinator mistCombinator = new MistCombinator(files.getFiles(), f);
            mistCombinator.setOnSucceeded(event -> {
                String message = OS.getStringFormatted("combine.mist.success", f, event.getSource().getValue());
                CoatView.printMessage(message, "success");
            });
            mistCombinator.run();
        }
    }

    private File getOutput() {
        String message = OS.getStringFormatted("select.file", "MIST");
        return FileManager.saveFile(message, FileManager.MIST_FILTER);
    }
}
