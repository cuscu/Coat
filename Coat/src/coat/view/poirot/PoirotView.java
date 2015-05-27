package coat.view.poirot;

import coat.model.poirot.Pearl;
import coat.model.poirot.PearlDatabase;
import coat.model.poirot.PoirotAnalysis;
import coat.utils.OS;
import coat.view.graphic.SizableImage;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
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

    private final String html = System.getProperty("user.dir") + "/Coat/graph/index.html";
    private final URI uri = Paths.get(html).toAbsolutePath().toUri();
    private final File graphFile = new File(System.getProperty("user.dir"), "Coat/graph/data/data.json");

    private final TextArea geneList = new TextArea();
    private final TextArea phenotypeList = new TextArea();
    private final Button start = new Button(OS.getResources().getString("start"),
            new SizableImage("coat/img/start.png", SizableImage.MEDIUM_SIZE));
    private final TextArea outputArea = new TextArea();
    private final VBox vBox = new VBox(5, geneList, phenotypeList, start, outputArea);

    private WebView webView = new WebView();

    private ListView<Pearl> pearlListView = new ListView<>();
    private Button reload = new Button("Reload");
    private VBox centre = new VBox(5, pearlListView, reload);

    private PearlDatabase database;

    public PoirotView() {
        VBox.setVgrow(geneList, Priority.ALWAYS);
        geneList.setPromptText("Genes: one per line");
        phenotypeList.setPromptText("Phenotypes: one per line");
        start.setOnAction(event -> start());
        outputArea.setEditable(false);
        VBox.setVgrow(pearlListView, Priority.ALWAYS);
        reload.setOnAction(event -> reload());
        pearlListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        pearlListView.setCellFactory(param -> new TextFieldListCell<>(new StringConverter<Pearl>() {
            @Override
            public String toString(Pearl pearl) {
                return String.format("(%d)%s", pearl.getWeight(), pearl.getName());
            }

            @Override
            public Pearl fromString(String string) {
                return null;
            }
        }));
        HBox.setHgrow(webView, Priority.ALWAYS);
        getChildren().addAll(vBox, centre, webView);
    }

    private void start() {
        final List<String> genes = Arrays.asList(geneList.getText().split("\n"));
        final List<String> phenotypes = Arrays.asList(phenotypeList.getText().split("\n"));
        Task<PearlDatabase> task = new PoirotAnalysis(genes, phenotypes);
        task.messageProperty().addListener((observable, oldValue, newValue) -> {
            outputArea.appendText(newValue + "\n");
        });
        outputArea.clear();
        start.setDisable(true);
        new Thread(task).start();
        task.setOnSucceeded(event -> end(task.getValue()));

    }

    private void end(PearlDatabase database) {
        case2(database);
    }

    private void case2(PearlDatabase database) {
        if (database != null) {
            if (database.nodes() <= 50) save(graphFile, database);
            else graphFile.delete();
            webView.getEngine().load(uri.toString());
            final List<Pearl> candidates = Arrays.stream(geneList.getText().split("\n")).
                    map(gene -> database.getPearl(gene, "gene")).
                    filter(pearl -> pearl != null).
                    collect(Collectors.toList());
            pearlListView.getItems().setAll(candidates);
            Collections.sort(pearlListView.getItems(), (p1, p2) -> {
                final int compare = Integer.compare(p1.getWeight(), p2.getWeight());
                return (compare != 0) ? compare : p1.getName().compareTo(p2.getName());
            });
            this.database = database;
        }
        start.setDisable(false);
    }

    private void save(File output, PearlDatabase database) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
            writer.write(database.toJson());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reload() {
        reload2();
    }

    private void reload2() {
        if (database != null) {
            database.subgraph(graphFile, pearlListView.getSelectionModel().getSelectedItems());
            webView.getEngine().reload();
        }

    }
}
