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
package coat.combinevcf;

import coat.CoatView;
import coat.graphic.SizableImage;
import coat.utils.FileManager;
import java.io.File;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

/**
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class CombineVCF {

    @FXML
    private ListView<File> includes;
    @FXML
    private Button addInclude;
    @FXML
    private ListView<File> excludes;
    @FXML
    private Button addExclude;
    @FXML
    private Button startButton;
    @FXML
    private TextField output;

    @FXML
    private void initialize() {
        includes.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.DELETE)
                includes.getItems().remove(includes.getSelectionModel().getSelectedItem());
        });
        excludes.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.DELETE)
                excludes.getItems().remove(excludes.getSelectionModel().getSelectedItem());
        });
        addInclude.setOnAction(e -> addInclude());
        addExclude.setOnAction(e -> addExclude());
        startButton.setOnAction(e -> start());
        startButton.setGraphic(new SizableImage("coat/img/start.png", 32));
        addExclude.setGraphic(new SizableImage("coat/img/new.png", 32));
        addInclude.setGraphic(new SizableImage("coat/img/new.png", 32));
    }

    @FXML
    private void selectOutput(ActionEvent event) {
        FileManager.saveFile(output, "Select ouptut file", FileManager.VCF_FILTER);
    }

    private void addInclude() {
        List<File> f = FileManager.openFiles("Select VCF", FileManager.VCF_FILTER);
        if (f != null)
            includes.getItems().addAll(f);
    }

    private void addExclude() {
        List<File> f = FileManager.openFiles("Select VCF", FileManager.VCF_FILTER);
        if (f != null)
            excludes.getItems().addAll(f);
    }

    private void start() {
        File intersection = Combinator.combine(includes.getItems(), excludes.getItems(), output.getText());
        if (intersection != null)
            CoatView.printMessage("Intersection finished, output file: " + intersection, "success");
        else
            CoatView.printMessage("Error with intersection", "error");
    }

}
