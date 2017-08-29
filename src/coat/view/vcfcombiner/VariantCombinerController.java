/*
 * Copyright (c) UICHUIMI 2017
 *
 * This file is part of Coat.
 *
 * Coat is free software:
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Coat.
 *
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */

package coat.view.vcfcombiner;

import coat.utils.FileManager;
import coat.utils.OS;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vcf.Genotype;
import vcf.Variant;
import vcf.VcfHeader;
import vcf.combine.Sample;
import vcf.io.VariantSetFactory;
import vcf.io.VariantSetReaderList;
import vcf.io.VariantSetWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
    private TableColumn<Sample, Genotype> status;
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
        status.setCellValueFactory(param -> new SimpleObjectProperty<>(param
                .getValue().getGenotype()));
        status.setCellFactory(param -> new StatusComboBoxCell());
        mist.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getMistFile()));
        mist.setCellFactory(param -> new MistCell());
    }

    public void addVcf() {
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
//        if (files.length > 0) sample.setMistFile(files[0]);
    }

    public void deleteSample() {
        if (!sampleTable.getSelectionModel().isEmpty())
            sampleTable.getItems().remove(sampleTable.getSelectionModel().getSelectedItem());
    }

    public void combine() {
        final List<File> files = getFiles();
        if (files == null) return;
        final File output = getOutput();
        if (output == null) return;
        final Runnable combinerTask = getCombiner(files, output);
        new Thread(combinerTask).start();
    }

    @NotNull
    private Runnable getCombiner(List<File> files, File output) {
        return () -> {
            message.setVisible(true);
            progressBar.setVisible(true);
            Platform.runLater(() -> progressBar.setProgress(-1));
            try (VariantSetReaderList reader = new VariantSetReaderList(files);
                 VariantSetWriter writer = new VariantSetWriter(output)) {
                writer.setHeader(reader.getMergedHeader());
                writer.writeHeader();
                final AtomicLong counter = new AtomicLong();
                while (reader.hasNext()) {
                    final Variant variant = reader.nextMerged();
                    if (counter.incrementAndGet() % 1000 == 0)
                        updateProgress(counter, variant);
                    if (filter(variant))
                        writer.write(variant);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            message.setVisible(false);
            progressBar.setVisible(false);
        };
    }

    @Nullable
    private File getOutput() {
        return FileManager.saveFile("Select ouput file",
                FileManager.VCF_FILTER);
    }

    @Nullable
    private List<File> getFiles() {
        return sampleTable.getItems().stream()
                .map(Sample::getFile)
                .distinct().collect(Collectors.toList());
    }

    private void updateProgress(AtomicLong counter, Variant variant) {
        Platform.runLater(() -> message.setText(String.format("%12d %s",
                counter.get(), variant.getCoordinate())));
    }

    private boolean filter(Variant variant) {
        for (Sample sample : sampleTable.getItems()) {
            final Genotype selected = sample.getGenotype();
            final Genotype predicted = variant.getSampleInfo().getGenotype(sample.getName());
            if (predicted != selected)
                return false;
        }
        return true;
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
