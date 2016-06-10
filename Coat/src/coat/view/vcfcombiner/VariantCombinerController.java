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
import vcf.VariantSet;
import vcf.VariantSetFactory;
import vcf.VcfHeader;
import vcf.combine.VariantCombinerTask;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by uichuimi on 24/05/16.
 */
public class VariantCombinerController {
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label message;
    @FXML
    private CheckBox removeVariants;
    @FXML
    private TableColumn<Sample, File> mist;
    @FXML
    private TableColumn<Sample, Sample.Status> status;
    @FXML
    private TableColumn<Sample, Long> variants;
    @FXML
    private TableColumn<Sample, String> name;
    @FXML
    private TableView<Sample> sampleTable;

    @FXML
    private void initialize() {
        name.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getName()));
        variants.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        status.setCellValueFactory(param -> param.getValue().statusProperty());
        status.setCellFactory(param -> new ComboBoxTableCell<>(Sample.Status.values()));
        mist.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getMistFile()));
        mist.setCellFactory(param -> new MistCell());
    }

    public void addVcf(ActionEvent actionEvent) {
        final List<File> files = FileManager.openFiles("Select VCF files", FileManager.VCF_FILTER);
        if (files != null) for (File file : files) addSamples(file);
    }

    private void addSamples(File file) {
//        final VariantSet variantSet = VariantSetFactory.createFromFile(file);
        final VcfHeader header = VariantSetFactory.readHeader(file);
        for (String name : header.getSamples()) sampleTable.getItems().add(new Sample(file, name));
    }

    public void deleteSample(ActionEvent actionEvent) {
        if (!sampleTable.getSelectionModel().isEmpty())
            sampleTable.getItems().remove(sampleTable.getSelectionModel().getSelectedItem());
    }

    public void combine(ActionEvent actionEvent) {
        final VariantCombinerTask combinerTask = new VariantCombinerTask(sampleTable.getItems(), removeVariants.isSelected());
        combinerTask.setOnSucceeded(event -> endCombine(combinerTask.getValue()));
        setProgressGUI(combinerTask);
        new Thread(combinerTask).start();
    }

    private void setProgressGUI(VariantCombinerTask combinerTask) {
        message.textProperty().bind(combinerTask.messageProperty());
        progressBar.progressProperty().bind(combinerTask.progressProperty());
        message.setVisible(true);
        progressBar.setVisible(true);
    }

    private void endCombine(VariantSet variantSet) {
        CoatView.getCoatView().openVcfFile(variantSet, null);
        Logger.getLogger(getClass().getName()).info("Combined " + variantSet.getVariants().size() + " variants");
        unsetProgressGUI();
    }

    private void unsetProgressGUI() {
        message.textProperty().unbind();
        progressBar.progressProperty().unbind();
        message.setVisible(false);
        progressBar.setVisible(false);
    }

    private class MistCell extends TableCell<Sample, File> {
        final Button button = new Button("...");

        MistCell() {
            button.setOnAction(event -> selectMistFile());
        }

        private void selectMistFile() {
            final File file = FileManager.openFile(OS.getString("choose.file"), FileManager.MIST_FILTER);
            if (file != null) {
                ((Sample) getTableRow().getItem()).setMistFile(file);
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
