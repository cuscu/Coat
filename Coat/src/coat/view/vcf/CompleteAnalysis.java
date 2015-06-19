package coat.view.vcf;

import coat.model.vcf.VCFCombiner;
import coat.utils.FileManager;
import coat.utils.OS;
import coat.view.graphic.SizableImage;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class CompleteAnalysis extends VBox {

    private final static ListView<Sample> samples = new ListView<>();
    private final static Button add = new Button(null, new SizableImage("coat/img/add.png", SizableImage.MEDIUM_SIZE));
    private FileChooser.ExtensionFilter[] filters = {FileManager.VCF_FILTER};

    private final static Button delete = new Button(null, new SizableImage("coat/img/delete.png", SizableImage.MEDIUM_SIZE));
    private final static HBox actionButtons = new HBox(delete);
    private final static Button start = new Button(OS.getResources().getString("start"));


    private final static StackPane listStackPane = new StackPane(samples, add);


    public CompleteAnalysis() {
        configureAddButton();
        configureButtonsPane();
        getChildren().addAll(listStackPane, new HBox(start));
        VBox.setVgrow(listStackPane, Priority.ALWAYS);
        setPadding(new Insets(10));
        setSpacing(5);
        samples.setCellFactory(param -> new SampleCell());
        samples.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) listStackPane.getChildren().remove(actionButtons);
            else if (!listStackPane.getChildren().contains(actionButtons))
                listStackPane.getChildren().add(1, actionButtons);
        });
        start.setOnAction(event -> start());
        start.setPadding(new Insets(10));
        start.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(start, Priority.ALWAYS);

    }

    private void configureAddButton() {
        StackPane.setAlignment(add, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(add, new Insets(10));
        add.getStyleClass().add("graphic-button");
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
        final Task combiner = new VCFCombiner(samples.getItems(), file);
        final Thread thread = new Thread(combiner);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
