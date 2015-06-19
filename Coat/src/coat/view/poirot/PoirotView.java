package coat.view.poirot;

import coat.CoatView;
import coat.model.poirot.Pearl;
import coat.model.poirot.PearlDatabase;
import coat.model.poirot.PoirotAnalysis;
import coat.utils.OS;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.util.StringConverter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotView extends HBox {

    //    private final String html = "graph/cyto.html";
    private final String html = "graph/index.html";
    private final URI uri = Paths.get(html).toAbsolutePath().toUri();
    private final File graphFile = new File("graph/data/data.json");
    private final File cytoFile = new File("graph/data/test.js");

    private final TextArea geneList = new TextArea();
    private final TextArea phenotypeList = new TextArea();
    private final Button start = new Button(OS.getResources().getString("start"));
    private final Label message = new Label();
    private final VBox vBox = new VBox(5, geneList, phenotypeList, start, message);

    private final WebView webView = new WebView();
    private final GraphView graphView = new GraphView();
    private final ScrollPane scrollPane = new ScrollPane(graphView);

    private ListView<Pearl> pearlListView = new ListView<>();
    private Button reload = new Button("Reload graph");
    private ToggleButton repeat = new ToggleButton("Show panel");
    private VBox centre = new VBox(5, pearlListView, reload, repeat);

    private PearlDatabase database;

    public PoirotView() {
        setSpacing(5);
        setPadding(new Insets(5, 5, 0, 5));
        VBox.setVgrow(geneList, Priority.ALWAYS);
        geneList.setPromptText("Genes: one per line");
        phenotypeList.setPromptText("Phenotypes: one per line");
        start.setOnAction(event -> start());
        start.setMaxWidth(9999);
        start.setPadding(new Insets(10));
        reload.setMaxWidth(9999);
        reload.setPadding(new Insets(10));
        repeat.setMaxWidth(9999);
        repeat.setPadding(new Insets(10));
        repeat.selectedProperty().addListener((observable, oldValue, selected) -> {
            if (selected) {
                if (!getChildren().contains(vBox)) getChildren().add(0, vBox);
                repeat.setText("Hide panel");
            } else {
                getChildren().remove(vBox);
                repeat.setText("Show panel");
            }
        });
        VBox.setVgrow(pearlListView, Priority.ALWAYS);
        reload.setOnAction(event -> reload());
        pearlListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        pearlListView.setCellFactory(param -> new TextFieldListCell<>(new StringConverter<Pearl>() {
            @Override
            public String toString(Pearl pearl) {
                return String.format("[%d] %s", pearl.getWeight(), pearl.getName());
            }

            @Override
            public Pearl fromString(String string) {
                return null;
            }
        }));
        HBox.setHgrow(webView, Priority.ALWAYS);
//        HBox.setHgrow(graphView, Priority.ALWAYS);
        getChildren().addAll(vBox);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        scrollPane.widthProperty().addListener((observable, oldValue, newValue) -> graphView.setWidth(newValue.doubleValue()));
        scrollPane.heightProperty().addListener((observable, oldValue, newValue) -> graphView.setHeight(newValue.doubleValue()));
    }

    private void start() {
        final List<String> genes = Arrays.asList(geneList.getText().split("\n"));
        final List<String> phenotypes = Arrays.asList(phenotypeList.getText().split("\n"));
        Task<PearlDatabase> task = new PoirotAnalysis(genes, phenotypes);
        message.textProperty().bind(task.messageProperty());
        start.setDisable(true);
        new Thread(task).start();
        task.setOnSucceeded(event -> end(task.getValue()));

    }

    private void end(PearlDatabase database) {
        start.setDisable(false);
        message.textProperty().unbind();
        message.setText("Done");
        repeat.setSelected(false);
        getChildren().remove(vBox);
//        if (!getChildren().contains(centre)) getChildren().addAll(centre, webView);
        if (!getChildren().contains(centre)) getChildren().addAll(centre, scrollPane);
//        graphView.setDatabase(database);
        createGraph(database);
    }

    private void createGraph(PearlDatabase database) {
        if (database != null) {
            CoatView.printMessage("warning", uri.getPath());
            CoatView.printMessage("warning", graphFile.getAbsolutePath());
            if (database.pearls() <= 50) save(graphFile, database);
            else graphFile.delete();
            webView.getEngine().load(uri.toString());
            final List<Pearl> candidates = Arrays.stream(geneList.getText().split("\n")).
                    map(gene -> database.getPearl(gene, "gene")).
                    filter(pearl -> pearl != null).
                    collect(Collectors.toList());
            pearlListView.getItems().setAll(candidates);
            graphView.setCandidates(pearlListView.getItems());
            database.subgraphCyto(cytoFile, pearlListView.getItems());
            Collections.sort(pearlListView.getItems(), (p1, p2) -> {
                final int compare = Integer.compare(p1.getWeight(), p2.getWeight());
                return (compare != 0) ? compare : p1.getName().compareTo(p2.getName());
            });
            this.database = database;
        }
    }

    private void save(File output, PearlDatabase database) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
            writer.write(database.toJson());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reload() {
        if (database != null) {
            database.subgraph(graphFile, pearlListView.getSelectionModel().getSelectedItems());
            database.subgraphCyto(cytoFile, pearlListView.getSelectionModel().getSelectedItems());
            graphView.setCandidates(pearlListView.getSelectionModel().getSelectedItems());
            webView.getEngine().reload();
            CoatView.printMessage("info", uri.getPath());
        }

    }
}