/******************************************************************************
 * Copyright (C) 2015 UICHUIMI                                                *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify it    *
 * under the terms of the GNU General Public License as published by the      *
 * Free Software Foundation, either version 3 of the License, or (at your     *
 * option) any later version.                                                 *
 *                                                                            *
 * This program is distributed in the hope that it will be useful, but        *
 * WITHOUT ANY WARRANTY; without even the implied warranty of                 *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 ******************************************************************************/

package coat.view.vcfcombiner;

import coat.core.tool.Tool;
import coat.core.vcfcombiner.VcfCombineTask;
import coat.core.vcfreader.Variant;
import coat.core.vcfreader.VcfFile;
import coat.core.vcfreader.VcfSaver;
import coat.utils.FileManager;
import coat.utils.OS;
import coat.view.graphic.SizableImage;
import coat.view.vcfreader.VariantsTable;
import coat.view.vcfreader.VcfSample;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Main panel of the Combine Vcf Tool.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class CombineVcfTool extends Tool {

    private final static FileChooser.ExtensionFilter[] filters = {FileManager.VCF_FILTER};

    private final VcfSampleTableView vcfSampleTableView = new VcfSampleTableView();

    private final VariantsTable variantsTable = new VariantsTable();

    private final Button addFiles = new Button(OS.getString("add.files"), new SizableImage("coat/img/add.png", SizableImage.SMALL_SIZE));
    private final Button combine = new Button(OS.getString("combine"), new SizableImage("coat/img/combine.png", SizableImage.SMALL_SIZE));
    private final Button delete = new Button(OS.getString("delete"), new SizableImage("coat/img/delete.png", SizableImage.SMALL_SIZE));
//    private final Button save = new Button(OS.getString("save"), new SizableImage("coat/img/save.png", SizableImage.SMALL_SIZE));

    private final HBox topButtonsBox = new HBox(5, addFiles, delete, combine);

    private final Label message = new Label();
    private final ProgressBar progressBar = new ProgressBar();
    private final HBox progressPane = new HBox(5, message, progressBar);

    private final Property<String> title = new SimpleStringProperty(OS.getString("combine.vcf"));

    private final ObservableList<Variant> resultVariants = FXCollections.observableArrayList();
    private Task<List<Variant>> combiner;

    public CombineVcfTool() {
        configureRoot();
        configureButtonsPane();
        configureSampleTable();
        configureVariantsTable();
    }

    private void configureRoot() {
        getChildren().addAll(topButtonsBox, vcfSampleTableView, variantsTable, progressPane);
        setPadding(new Insets(10));
        setSpacing(5);
        HBox.setHgrow(progressBar, Priority.ALWAYS);
        progressBar.setMaxWidth(9999);
        progressBar.setVisible(false);
    }

    private void configureButtonsPane() {
        topButtonsBox.getChildren().stream().map(node -> (Button) node).forEach(this::setTopButton);
//        save.setOnAction(event -> saveAs());
        addFiles.setOnAction(event -> addFiles());
        combine.setOnAction(event -> combine());
        delete.setOnAction(event -> deleteFile());
    }

    private void setTopButton(Button button) {
        button.setMaxWidth(9999);
        HBox.setHgrow(button, Priority.ALWAYS);
        button.setPadding(new Insets(10));
    }

    private void configureSampleTable() {
        VBox.setVgrow(vcfSampleTableView, Priority.ALWAYS);
        vcfSampleTableView.getSelectionModel().selectedItemProperty().addListener((obs, prev, current) ->
                delete.setDisable(current == null));
        delete.setDisable(true);
    }

    private void configureVariantsTable() {
        variantsTable.setVariants(resultVariants);
    }

    private void addFiles() {
        final List<File> f = FileManager.openFiles(OS.getString("select.files"), filters);
        if (f != null) f.stream()
                .filter(this::notInSampleTable)
                .map(VcfSample::new)
                .forEach(vcfSampleTableView.getItems()::add);
    }

    private boolean notInSampleTable(File file) {
        return vcfSampleTableView.getItems().stream().noneMatch(vcfSample -> vcfSample.getVcfFile().equals(file));
    }

    private void deleteFile() {
        vcfSampleTableView.getItems().remove(vcfSampleTableView.getSelectionModel().getSelectedItem());
    }

    @Override
    public void saveAs() {
        final File file = FileManager.saveFile("Select ouptut file", FileManager.VCF_FILTER);
        if (file != null) {
            final VcfSample referenceSample = getReferenceSample(vcfSampleTableView.getItems());
            if (referenceSample != null) {
                VcfSaver saver = new VcfSaver(new VcfFile(referenceSample.getVcfFile()), file, resultVariants);
                saver.invoke();
            }
        }
    }

    @Override
    public Property<String> titleProperty() {
        return title;
    }

    /**
     * Stops current combining thread and starts a new process
     */
    private void combine() {
        stopCombiner();
        prepareGUI();
        startCombiner();
    }

    private void stopCombiner() {
        if (combiner != null) combiner.cancel(true);
    }

    private void prepareGUI() {
        Platform.runLater(() -> {
            message.setText(OS.getString("combining") + "...");
//            save.setDisable(true);
            combine.setDisable(true);
            progressBar.setVisible(true);
        });
    }

    private void startCombiner() {
        combiner = new VcfCombineTask(vcfSampleTableView.getItems());
        combiner.setOnSucceeded(event -> combinerSucceeded());
        progressBar.progressProperty().bind(combiner.progressProperty());
        new Thread(combiner).start();
    }

    private void combinerSucceeded() {
        restoreGUI();
        resultVariants.setAll(combiner.getValue());
    }

    private void restoreGUI() {
        Platform.runLater(() -> {
            message.setText(OS.getStringFormatted("commom.variants", resultVariants.size()));
//            save.setDisable(false);
            combine.setDisable(false);
            progressBar.progressProperty().unbind();
            progressBar.setVisible(false);
        });
    }

    private VcfSample getReferenceSample(ObservableList<VcfSample> vcfSamples) {
        try {
            return vcfSamples.stream().filter(sample -> !sample.getLevel().equals(VcfSample.Level.UNAFFECTED)).findFirst().get();
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

}
