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
package coat.view.vcf;

import coat.CoatView;
import coat.view.graphic.FileList;
import coat.model.vcf.Combinator;
import coat.view.graphic.SizableImage;
import coat.utils.FileManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.File;

/**
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class CombineVCF {

    @FXML
    private FileList includes;
    @FXML
    private FileList excludes;
    @FXML
    private Button startButton;
    @FXML
    private TextField output;

    @FXML
    private void initialize() {
        startButton.setOnAction(e -> start());
        startButton.setGraphic(new SizableImage("coat/img/start.png", SizableImage.MEDIUM_SIZE));
        startButton.setDisable(true);
        includes.setFilters(FileManager.VCF_FILTER);
        excludes.setFilters(FileManager.VCF_FILTER);
    }

    @FXML
    private void selectOutput(ActionEvent event) {
        final File file = FileManager.saveFile(output, "Select ouptut file", FileManager.VCF_FILTER);
        if (file != null) startButton.setDisable(false);
    }

    private void start() {
        File intersection = Combinator.combine(includes.getFiles(), excludes.getFiles(), output.getText());
        if (intersection != null)
            CoatView.printMessage("Intersection finished, output file: " + intersection, "success");
        else
            CoatView.printMessage("Error with intersection", "error");
    }

}
