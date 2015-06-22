package coat.view.poirot;

import coat.model.poirot.*;
import coat.model.vcf.VcfFile;
import coat.utils.FileManager;
import coat.utils.OS;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotView extends HBox {


    private final TextArea geneList = new TextArea();
    private final TextArea phenotypeList = new TextArea();
    private final Button start = new Button(OS.getResources().getString("start"));
    private final Label message = new Label();
    private final TextField file = new TextField();
    private final Button browse = new Button(OS.getResources().getString("browse"));
    private final HBox fileBox = new HBox(5,file, browse);

    private final VBox inputPane = new VBox(5, fileBox, geneList, phenotypeList, start, message);

    private final GraphView graphView = new GraphView();

    private final VBox graphVBox = new VBox(graphView);
    private final Label info = new Label();
    private final StackPane stackPane = new StackPane(graphVBox, info);

    private final ListView<Pearl> pearlListView = new ListView<>();
    private final Button reload = new Button("Reload graph");
    private final ToggleButton repeat = new ToggleButton("Show panel");

    private final VBox listPane = new VBox(5, pearlListView, reload, repeat);

    private List<String> genes = new ArrayList<>();


    public PoirotView() {
        initializeThis();
        initializeInputPane();
        initializeListPane();
        initializeGraphView();
    }

    private void initializeThis() {
        setSpacing(5);
        setPadding(new Insets(5, 5, 0, 5));
        getChildren().addAll(inputPane);
    }

    private void initializeInputPane() {
        initializeGeneList();
        initializePhenotypeList();
        initializeStartButton();
        initializeFileInput();
    }

    private void initializeFileInput() {
        HBox.setHgrow(file, Priority.ALWAYS);
        file.setEditable(false);
        browse.setOnAction(event -> FileManager.openFile(file, "Select file", FileManager.VCF_FILTER));
    }

    private void initializeListPane() {
        initializeReloadButton();
        initializeRepeatButton();
        initializePearlListView();
    }

    private void initializePhenotypeList() {
        phenotypeList.setPromptText("Phenotypes: one per line");
    }

    private void initializeGeneList() {
        VBox.setVgrow(geneList, Priority.ALWAYS);
        geneList.setPromptText("Genes: one per line");
    }

    private void initializeStartButton() {
        start.setOnAction(event -> start());
        start.setMaxWidth(9999);
        start.setPadding(new Insets(10));
    }

    private void initializeReloadButton() {
        reload.setMaxWidth(9999);
        reload.setPadding(new Insets(10));
        reload.setOnAction(event -> reload());
    }

    private void initializeRepeatButton() {
        repeat.setMaxWidth(9999);
        repeat.setPadding(new Insets(10));
        repeat.selectedProperty().addListener((observable, oldValue, selected) -> repeat.setText((selected) ? "Hide panel" : "Show panel"));
        repeat.selectedProperty().addListener((observable, oldValue, selected) -> {
            if (!selected) getChildren().remove(inputPane);
            else if (!getChildren().contains(inputPane)) getChildren().add(0, inputPane);
        });
    }

    private void initializePearlListView() {
        VBox.setVgrow(pearlListView, Priority.ALWAYS);
        pearlListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        pearlListView.setCellFactory(param1 -> new PearlListCell());
    }

    private void initializeGraphView() {
        HBox.setHgrow(stackPane, Priority.ALWAYS);
        graphVBox.widthProperty().addListener((observable, oldValue, newValue) -> graphView.setWidth(newValue.doubleValue()));
        graphVBox.heightProperty().addListener((observable, oldValue, newValue) -> graphView.setHeight(newValue.doubleValue()));
        graphView.setManaged(false);
        graphView.getSelectedPearlProperty().addListener((observable, oldValue, pearl) -> selected(pearl));
        StackPane.setAlignment(info, Pos.BOTTOM_LEFT);
    }

    private void selected(Pearl pearl) {
        String text = String.valueOf(pearl);
        for (String key : pearl.getProperties().keySet()) {
            text += String.format(";%s=%s", key, pearl.getProperties().getOrDefault(key, ""));
        }
        info.setText(text);
    }

    private void start() {
        final List<String> phenotypes = Arrays.asList(phenotypeList.getText().split("\n"));
        if (!file.getText().isEmpty()) {
            final VcfFile vcfFile = new VcfFile(new File(file.getText()));
            genes.clear();
            vcfFile.getVariants().stream().map(variant -> (String) variant.getInfos().get("GNAME")).filter(name -> name != null).distinct().forEach(genes::add);
            final Task<PearlDatabase> task = new PoirotAnalysis2(vcfFile.getVariants(), phenotypes);
            message.textProperty().bind(task.messageProperty());
            start.setDisable(true);
            new Thread(task).start();
            task.setOnSucceeded(event -> end(task.getValue()));
        }else {
            genes = Arrays.asList(geneList.getText().split("\n"));
            final Task<PearlDatabase> task = new PoirotAnalysis(genes, phenotypes);
            message.textProperty().bind(task.messageProperty());
            start.setDisable(true);
            new Thread(task).start();
            task.setOnSucceeded(event -> end(task.getValue()));
        }
    }

    private void end(PearlDatabase database) {
        toGraphView();
        createGraph(database);
    }

    private void toGraphView() {
        start.setDisable(false);
        repeat.setSelected(false);
        message.textProperty().unbind();
        message.setText("Done");
        getChildren().remove(inputPane);
        if (!getChildren().contains(listPane)) getChildren().addAll(listPane, stackPane);
    }

    private void createGraph(PearlDatabase database) {
        if (database != null) {
            final List<Pearl> candidates = getCandidates(database);
            pearlListView.getItems().setAll(candidates);
            Collections.sort(pearlListView.getItems(), (p1, p2) -> {
                final int compare = Integer.compare(p1.getWeight(), p2.getWeight());
                return (compare != 0) ? compare : p1.getName().compareTo(p2.getName());
            });
        }
    }

    private List<Pearl> getCandidates(PearlDatabase database) {
        return genes.stream().map(gene -> database.getPearl(gene, "gene")).
                filter(pearl -> pearl != null).
                collect(Collectors.toList());
    }

    private void save(File output, PearlDatabase database) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
            writer.write(database.toJson());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reload() {
        graphView.setCandidates(pearlListView.getSelectionModel().getSelectedItems());

    }
}