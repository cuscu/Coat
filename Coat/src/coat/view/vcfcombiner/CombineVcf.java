package coat.view.vcfcombiner;

import coat.model.tool.Tool;
import coat.model.vcfreader.Variant;
import coat.model.vcfreader.VcfFile;
import coat.model.vcfreader.VcfSaver;
import coat.utils.FileManager;
import coat.utils.OS;
import coat.view.graphic.SizableImage;
import coat.view.vcfreader.Sample;
import coat.view.vcfreader.VariantsTable;
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
public class CombineVcf extends Tool {

    private final static FileChooser.ExtensionFilter[] filters = {FileManager.VCF_FILTER};

    private final SampleTableView sampleTableView = new SampleTableView();

    private VariantsTable variantsTable = new VariantsTable();

    private final Button addFiles = new Button(OS.getString("add.files"), new SizableImage("coat/img/add.png", SizableImage.SMALL_SIZE));
    private final Button combine = new Button(OS.getString("combine"), new SizableImage("coat/img/combine.png", SizableImage.SMALL_SIZE));
    private final Button delete = new Button(OS.getString("delete"), new SizableImage("coat/img/delete.png", SizableImage.SMALL_SIZE));
    private final Button save = new Button(OS.getString("save"), new SizableImage("coat/img/save.png", SizableImage.SMALL_SIZE));

    private final HBox topButtonsBox = new HBox(5, addFiles, delete, combine, save);

    private final Label message = new Label();
    private final ProgressBar progressBar = new ProgressBar();
    private final HBox progressPane = new HBox(5, message, progressBar);

    private Property<String> title = new SimpleStringProperty(OS.getString("combine.vcf"));

    private final ObservableList<Variant> resultVariants = FXCollections.observableArrayList();
    private Task<List<Variant>> combiner;

    public CombineVcf() {
        configureRoot();
        configureButtonsPane();
        configureSampleTable();
        configureVariantsTable();
    }

    private void configureRoot() {
        getChildren().addAll(topButtonsBox, sampleTableView, variantsTable, progressPane);
        setPadding(new Insets(10));
        setSpacing(5);
        HBox.setHgrow(progressBar, Priority.ALWAYS);
        progressBar.setMaxWidth(9999);
        progressBar.setVisible(false);
    }

    private void configureButtonsPane() {
        topButtonsBox.getChildren().stream().map(node -> (Button) node).forEach(this::setTopButton);
        save.setOnAction(event -> saveAs());
        addFiles.setOnAction(event -> addFiles());
        combine.setOnAction(event -> combine());
        delete.setOnAction(event -> deleteFile());
    }

    private void configureSampleTable() {
        VBox.setVgrow(sampleTableView, Priority.ALWAYS);

        sampleTableView.getSelectionModel().selectedItemProperty().addListener((obs, prev, current) ->
                delete.setDisable(current == null));
        delete.setDisable(true);
    }

    private void setTopButton(Button button) {
        button.setMaxWidth(9999);
        HBox.setHgrow(button, Priority.ALWAYS);
        button.setPadding(new Insets(10));
    }

    private void configureVariantsTable() {
        variantsTable.setVariants(resultVariants);
    }

    private void addFiles() {
        List<File> f = FileManager.openFiles(OS.getString("select.files"), filters);
        if (f != null) f.forEach(file -> sampleTableView.getItems().addAll(new Sample(file)));
    }

    private void deleteFile() {
        sampleTableView.getItems().remove(sampleTableView.getSelectionModel().getSelectedItem());
    }

    @Override
    public Property<String> getTitleProperty() {
        return title;
    }

    @Override
    public void saveAs() {
        final File file = FileManager.saveFile("Select ouptut file", FileManager.VCF_FILTER);
        if (file != null) {
            final Sample referenceSample = getReferenceSample(sampleTableView.getItems());
            if (referenceSample != null) {
                VcfSaver saver = new VcfSaver(new VcfFile(referenceSample.getFile()), file, resultVariants);
                saver.invoke();
            }
        }
    }

    /**
     * Stops current combining thread and starts a new process
     */
    private void combine() {
        if (combiner != null) combiner.cancel(true);
        combiner = new VcfCombineTask(sampleTableView.getItems());
        combiner.setOnSucceeded(event -> combinerSucceeded());
        progressBar.progressProperty().bind(combiner.progressProperty());
        Platform.runLater(this::prepareGUI);
        progressBar.setVisible(true);
        new Thread(combiner).start();
    }

    private void combinerSucceeded() {
        Platform.runLater(this::restoreGUI);
        resultVariants.setAll(combiner.getValue());
        progressBar.progressProperty().unbind();
        progressBar.setVisible(false);
    }

    private void prepareGUI() {
        message.setText(OS.getString("combining") + "...");
        save.setDisable(true);
        combine.setDisable(true);
    }

    private void restoreGUI() {
        message.setText(OS.getStringFormatted("commom.variants", resultVariants.size()));
        save.setDisable(false);
        combine.setDisable(false);
    }

    private Sample getReferenceSample(ObservableList<Sample> samples) {
        try {
            return samples.stream().filter(sample -> !sample.getLevel().equals(Sample.Level.UNAFFECTED)).findFirst().get();
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

}
