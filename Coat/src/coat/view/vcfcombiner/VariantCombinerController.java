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
import vcf.VariantSet;
import vcf.io.VariantSetFactory;
import vcf.VcfHeader;
import vcf.combine.Sample;
import vcf.combine.VariantCombinerTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

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
        status.setCellFactory(param -> new StatusComboBoxCell());
        mist.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getMistFile()));
        mist.setCellFactory(param -> new MistCell());
    }

    public void addVcf(ActionEvent actionEvent) {
        final List<File> files = FileManager.openFiles("Select VCF files", FileManager.VCF_FILTER);
        if (files != null) for (File file : files) addSamples(file);
    }

    private void addSamples(File file) {
        final VcfHeader header = VariantSetFactory.readHeader(file);
        final long size = getLinesCount(file);
        for (String name : header.getSamples()) {
            final Sample sample = new Sample(file, name, size);
            sampleTable.getItems().add(sample);
            setMistFile(file, name, sample);
        }
    }

    private long getLinesCount(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().filter(line -> !line.startsWith("#")).count();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void setMistFile(File file, String name, Sample sample) {
        final File[] files = file.getParentFile().listFiles((dir, filename)
                -> filename.toLowerCase().matches(name.toLowerCase() + ".*\\.mist"));
        if (files.length > 0) sample.setMistFile(files[0]);
    }

    public void deleteSample(ActionEvent actionEvent) {
        if (!sampleTable.getSelectionModel().isEmpty())
            sampleTable.getItems().remove(sampleTable.getSelectionModel().getSelectedItem());
    }

    public void combine(ActionEvent actionEvent) {
        final VariantCombinerTask combinerTask = new VariantCombinerTask(sampleTable.getItems(), !removeVariants.isSelected());
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
