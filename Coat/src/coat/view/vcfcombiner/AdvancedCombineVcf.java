package coat.view.vcfcombiner;

import coat.model.tool.Tool;
import coat.model.vcfcombiner.VcfCombiner;
import coat.model.vcfreader.Variant;
import coat.model.vcfreader.VcfFile;
import coat.model.vcfreader.VcfSaver;
import coat.utils.FileManager;
import coat.utils.OS;
import coat.view.graphic.SizableImage;
import coat.view.vcfreader.Sample;
import coat.view.vcfreader.SampleCell;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class AdvancedCombineVcf extends Tool {

    private final ListView<Sample> samples = new ListView<>();
    private final Button add = new Button(OS.getResources().getString("add.files"), new SizableImage("coat/img/add.png", SizableImage.SMALL_SIZE));
    private final FileChooser.ExtensionFilter[] filters = {FileManager.VCF_FILTER};

    private final Button delete = new Button(null, new SizableImage("coat/img/delete.png", SizableImage.MEDIUM_SIZE));
    private final HBox actionButtons = new HBox(delete);

    private final Label message = new Label();
    private final Button save = new Button(OS.getResources().getString("save"));

    private final StackPane listStackPane = new StackPane(samples);
    private Property<String> title = new SimpleStringProperty("Combine VCF(enhanced)");
    private ChangeListener<? super Sample.Level> sampleLevelListener = (observable, oldValue, newValue) -> combine();
    private List<Variant> resultVariants;
    private Thread thread = null;

    public AdvancedCombineVcf() {
        configureAddButton();
        configureButtonsPane();
        getChildren().addAll(add, listStackPane, message, save);
        VBox.setVgrow(listStackPane, Priority.SOMETIMES);
        VBox.setVgrow(add, Priority.ALWAYS);
        setPadding(new Insets(10));
        setSpacing(5);
        samples.setCellFactory(param -> new SampleCell());
        samples.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) listStackPane.getChildren().remove(actionButtons);
            else if (!listStackPane.getChildren().contains(actionButtons))
                listStackPane.getChildren().add(1, actionButtons);
        });
        samples.getItems().addListener(this::listChanged);
        save.setOnAction(event -> saveAs());
        save.setPadding(new Insets(10));
        save.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(save, Priority.ALWAYS);

    }


    private void configureAddButton() {
//        StackPane.setAlignment(add, Pos.BOTTOM_RIGHT);
//        StackPane.setMargin(add, new Insets(10));
//        add.getStyleClass().add("graphic-button");
        add.setMaxWidth(9999);
        HBox.setHgrow(add, Priority.ALWAYS);
        add.setOnAction(event -> addFiles());
    }

    private void addFiles() {
        List<File> f = FileManager.openFiles(OS.getResources().getString("select.files"), filters);
        if (f != null) f.forEach(file -> samples.getItems().addAll(new Sample(file)));
    }

    private void configureButtonsPane() {
        configureDeleteButton();
        StackPane.setMargin(actionButtons, new Insets(10));
        StackPane.setAlignment(actionButtons, Pos.BOTTOM_LEFT);
        actionButtons.setMaxWidth(0);
        actionButtons.setMaxHeight(0);
        actionButtons.setSpacing(5);
        actionButtons.setAlignment(Pos.CENTER_LEFT);
    }

    private void configureDeleteButton() {
        delete.getStyleClass().add("graphic-button");
        delete.setOnAction(event -> deleteFile());
    }

    private void deleteFile() {
        samples.getItems().remove(samples.getSelectionModel().getSelectedItem());
    }

    private void start() {
        final File file = FileManager.saveFile("Select ouptut file", FileManager.VCF_FILTER);
        if (file != null) launchCombiner(file);

    }

    private void launchCombiner(File file) {
        final Task combiner = new VcfCombiner(samples.getItems(), file);
        Platform.runLater(combiner);
    }

    @Override
    public Property<String> getTitleProperty() {
        return title;
    }

    @Override
    public void saveAs() {
        final File file = FileManager.saveFile("Select ouptut file", FileManager.VCF_FILTER);
        if (file != null) {
            final Sample referenceSample = getReferenceSample(samples.getItems());
            if (referenceSample != null) {
                VcfSaver saver = new VcfSaver(new VcfFile(referenceSample.getFile()), file, resultVariants);
                saver.invoke();
            }
        }
        ;

    }

    private void listChanged(ListChangeListener.Change<? extends Sample> c) {
        if (c.next()) {
            if (c.wasRemoved()) {
                c.getRemoved().forEach(sample -> sample.getLevelProperty().removeListener(sampleLevelListener));
                combine();
            } else {
                c.getAddedSubList().forEach(sample -> sample.getLevelProperty().addListener(sampleLevelListener));
                combine();
            }
        }
    }

    /**
     * Stops current combining thread and starts a new process
     */
    private void combine() {
        System.gc();
        if (thread != null && thread.isAlive()) thread.interrupt();
        thread = new Thread(() -> combine(samples.getItems()));
        thread.start();
    }

    private void combine(ObservableList<Sample> samples) {
        final List<VariantStream> streams = samples.stream().map(VariantStream::new).collect(Collectors.toList());
        final VariantStream reference = getReferenceStream(streams);
        if (reference != null) {
            Platform.runLater(() -> {
                message.setText("Combining...");
                save.setDisable(true);
            });
            resultVariants = null;
            System.gc();
            resultVariants = reference.getVariants().stream().filter(variant -> streams.stream().allMatch(stream -> stream.filter(variant))).collect(Collectors.toList());
            Platform.runLater(() -> {
                message.setText(resultVariants.size() + " common variants");
                save.setDisable(false);
            });
        }
    }

    private VariantStream getReferenceStream(List<VariantStream> streams) {
        for (VariantStream stream : streams)
            if (!stream.getSample().getLevel().equals(Sample.Level.UNAFFECTED)) return stream;
        return null;
    }

    private Sample getReferenceSample(ObservableList<Sample> samples) {
        for (Sample sample : samples) if (!sample.getLevel().equals(Sample.Level.UNAFFECTED)) return sample;
        return null;
    }
}
