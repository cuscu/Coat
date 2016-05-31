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

package coat.view.vcfcombiner;

import coat.CoatView;
import coat.utils.FileManager;
import coat.utils.OS;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import vcf.Sample;
import vcf.VcfFile;
import vcf.VcfFileFactory;
import vcf.combine.VcfCombineTask;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by uichuimi on 24/05/16.
 */
public class VcfCombinerController {
    @FXML
    private CheckBox removeVariants;
    @FXML
    private TableColumn<Sample, File> mist;
    @FXML
    private TableColumn<Sample, Sample.Status> status;
    @FXML
    private TableColumn<Sample, Integer> variants;
    @FXML
    private TableColumn<Sample, String> name;
    @FXML
    private TableView<Sample> sampleTable;
    private VcfFile vcfFile;

    @FXML
    private void initialize() {
        name.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getName()));
        variants.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getVcfFile().getVariants().size()));
        status.setCellValueFactory(param -> param.getValue().statusProperty());
        status.setCellFactory(param -> new ComboBoxTableCell<>(Sample.Status.values()));
        mist.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getVcfFile().getMistFile()));
        mist.setCellFactory(param -> new MistCell());
    }

    public void addVcf(ActionEvent actionEvent) {
        final List<File> files = FileManager.openFiles("Select VCF files", FileManager.VCF_FILTER);
        if (files != null) for (File file : files) addSamples(file);
    }

    private void addSamples(File file) {
        final VcfFile vcfFile = VcfFileFactory.createFromFile(file);
        for (String name : vcfFile.getHeader().getSamples()) sampleTable.getItems().add(new Sample(vcfFile, name));
    }

    public void deleteSample(ActionEvent actionEvent) {
        if (!sampleTable.getSelectionModel().isEmpty())
            sampleTable.getItems().remove(sampleTable.getSelectionModel().getSelectedItem());
    }

    public void combine(ActionEvent actionEvent) {
        vcfFile = new VcfCombineTask(sampleTable.getItems(), removeVariants.isSelected()).start();
        CoatView.getCoatView().openVcfFile(vcfFile);
        Logger.getLogger(getClass().getName()).info("Combined " + vcfFile.getVariants().size() + " variants");
    }

    public void save(ActionEvent actionEvent) {
        final File file = FileManager.saveFile("Select save file", FileManager.VCF_FILTER);
        if (file != null) {
            vcfFile.save(file);
            Logger.getLogger(getClass().getName()).info("Saved " + file);
        }
    }

    private class MistCell extends TableCell<Sample, File> {
        final Button button = new Button("...");

        MistCell() {
            button.setOnAction(event -> selectMistFile());
        }

        private void selectMistFile() {
            final File file = FileManager.openFile(OS.getString("choose.file"), FileManager.MIST_FILTER);
            if (file != null) {
                ((Sample) getTableRow().getItem()).getVcfFile().setMistFile(file);
                updateItem(file, false);
            }
        }

        @Override
        protected void updateItem(File file, boolean empty) {
            super.updateItem(file, empty);
            if (!empty) {
                if (file != null) setText(file.getName());
                setGraphic(button);
            } else {
                setText(null);
                setGraphic(null);
            }
        }
    }
}
