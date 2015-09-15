package coat.view.poirot;

import coat.model.poirot.*;
import coat.model.poirot.databases.HGNCDatabase;
import coat.model.poirot.databases.OmimDatabase;
import coat.model.tool.Tool;
import coat.model.vcfreader.Variant;
import coat.model.vcfreader.VcfFile;
import coat.utils.FileManager;
import coat.utils.OS;
import coat.view.graphic.IndexCell;
import coat.view.graphic.SizableImage;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotView extends Tool {

    private final HBox content = new HBox();
    private final TextArea phenotypeList = new TextArea();
    private final Button start = new Button(OS.getResources().getString("start"), new SizableImage("coat/img/start.png", SizableImage.SMALL_SIZE));
    private final Label message = new Label();
    private final TextField file = new TextField();
    private final Button browse = new Button();

    private final VBox inputPane = new VBox(5, new HBox(file, browse), phenotypeList, start, message);

    private final GraphView graphView = new GraphView();

    private final VBox graphVBox = new VBox(graphView);
    private final VBox infoBox = new VBox();
    private final StackPane stackPane = new StackPane(graphVBox, infoBox);

    private final TableView<Pearl> pearlTableView = new TableView<>();
    private final TableColumn<Pearl, String> scoreColumn = new TableColumn<>("Score");
    private final TableColumn<Pearl, Integer> indexColumn = new TableColumn<>("*");
    private final TableColumn<Pearl, Integer> distanceColumn = new TableColumn<>("Dist");
    private final TableColumn<Pearl, String> nameColumn = new TableColumn<>("Name");


    private final Button reload = new Button("Reload graph");
    private final ToggleButton repeat = new ToggleButton("Show panel");
    private final HBox buttons = new HBox(5, repeat, reload);

    private final VBox listPane = new VBox(5, pearlTableView, buttons);

    private List<String> genes = new ArrayList<>();
    private Property<String> title = new SimpleStringProperty("Poirot");


    public PoirotView() {
        file.getStyleClass().add("fancy-text-field");
        file.setTooltip(new Tooltip("Input VCF file"));
        file.setPromptText("Input VCF file");
        HBox.setHgrow(file, Priority.ALWAYS);
        browse.setGraphic(new SizableImage("coat/img/folder.png", SizableImage.SMALL_SIZE));
        browse.getStyleClass().add("graphic-button");
        phenotypeList.setText("schizophrenia");
        initializeThis();
        initializeInputPane();
        initializeListPane();
        initializeGraphView();
    }

    private void initializeThis() {
        getChildren().add(content);
        VBox.setVgrow(content, Priority.ALWAYS);
        VBox.setVgrow(browse, Priority.ALWAYS);
        content.setSpacing(5);
        content.setPadding(new Insets(5, 5, 0, 5));
        content.getChildren().addAll(inputPane);
    }

    private void initializeInputPane() {
        initializePhenotypeList();
        initializeStartButton();
        initializeFileInput();
    }

    private void initializeFileInput() {
        file.setEditable(false);
        browse.setMaxWidth(9999);
        browse.setOnAction(event -> {
            final File file = FileManager.openFile(this.file, "Select file", FileManager.VCF_FILTER);
            if (file != null) title.setValue("Poirot (" + file.getName() + ")");
        });
    }

    private void initializeListPane() {
        initializeReloadButton();
        initializeRepeatButton();
        initializePearlListView();
    }

    private void initializePhenotypeList() {
        phenotypeList.setPromptText("Phenotypes: one per line");
        VBox.setVgrow(phenotypeList, Priority.ALWAYS);
    }

    private void initializeStartButton() {
        start.setOnAction(event -> start());
        start.setMaxWidth(9999);
        start.setPadding(new Insets(10));
    }

    private void initializeReloadButton() {
        reload.setGraphic(new SizableImage("coat/img/update.png", SizableImage.SMALL_SIZE));
        HBox.setHgrow(reload, Priority.ALWAYS);
        reload.setMaxWidth(9999);
        reload.setPadding(new Insets(10));
        reload.setOnAction(event -> reload());
    }

    private void initializeRepeatButton() {
        repeat.setGraphic(new SizableImage("coat/img/form.png", SizableImage.SMALL_SIZE));
        repeat.setMaxWidth(9999);
        HBox.setHgrow(repeat, Priority.ALWAYS);
        repeat.setPadding(new Insets(10));
        repeat.selectedProperty().addListener((observable, oldValue, selected) -> repeat.setText((selected) ? "Hide panel" : "Show panel"));
        repeat.selectedProperty().addListener((observable, oldValue, selected) -> {
            if (!selected) content.getChildren().remove(inputPane);
            else if (!content.getChildren().contains(inputPane)) content.getChildren().add(0, inputPane);
        });
    }

    private void initializePearlListView() {
        VBox.setVgrow(pearlTableView, Priority.ALWAYS);
        pearlTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        pearlTableView.getColumns().addAll(indexColumn, distanceColumn, scoreColumn, nameColumn);
        pearlTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        final MenuItem menuItem = new MenuItem("Copy");
        final ContextMenu menu = new ContextMenu(menuItem);
        pearlTableView.setContextMenu(menu);
        menuItem.setOnAction(event -> copy());
        distanceColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getDistanceToPhenotype()));
        scoreColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(String.format("%.2f", param.getValue().getScore())));
        nameColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getName()));
        indexColumn.setCellFactory(param -> new IndexCell());
    }

    private void copy() {
        final StringBuilder builder = new StringBuilder();
        pearlTableView.getSelectionModel().getSelectedItems()
                .forEach(pearl -> builder
                        .append(pearl.getName()).append("\t")
                        .append(String.format("%.2f",pearl.getScore())).append("\t")
                        .append(pearl.getDistanceToPhenotype()).append("\n"));
        final ClipboardContent content = new ClipboardContent();
        content.putString(builder.toString());
        Clipboard.getSystemClipboard().setContent(content);
    }

    private void initializeGraphView() {
        HBox.setHgrow(stackPane, Priority.ALWAYS);
        graphVBox.widthProperty().addListener((observable, oldValue, newValue) -> graphView.setWidth(newValue.doubleValue()));
        graphVBox.heightProperty().addListener((observable, oldValue, newValue) -> graphView.setHeight(newValue.doubleValue()));
        graphView.setManaged(false);
        graphView.getSelectedPearlProperty().addListener((observable, oldValue, pearl) -> selected(pearl));
        graphView.getSelectedRelationship().addListener((observable, oldValue, relationship) -> selected(relationship));
        StackPane.setAlignment(infoBox, Pos.BOTTOM_LEFT);
        infoBox.setMaxWidth(USE_PREF_SIZE);
        infoBox.setMaxHeight(USE_PREF_SIZE);
        infoBox.getStyleClass().add("graph-info-box");
    }

    private void selected(GraphRelationship relationship) {
        if (relationship != null) {
            for (PearlRelationship pearlRelationship : relationship.getRelationships()) {
                infoBox.getChildren().add(new Label(pearlRelationship.getProperties().toString()));
            }
        } else if (graphView.getSelectedPearlProperty().getValue() == null) infoBox.getChildren().clear();
    }

    private void selected(Pearl pearl) {
        infoBox.getChildren().clear();
        if (pearl != null) {
            if (pearl.getType().equals("gene")) showGeneDescription(pearl);
            else showNonGeneDescription(pearl);
        }
    }

    private void showNonGeneDescription(Pearl pearl) {
        infoBox.getChildren().add(new Label(pearl.getProperties().toString()));
    }

    private void showGeneDescription(Pearl pearl) {
        final String symbol = pearl.getName();
        String description = HGNCDatabase.getName(symbol);
        if (description == null) {
            final List<DatabaseEntry> entries = OmimDatabase.getEntries(symbol);
            if (!entries.isEmpty()) description = entries.get(0).getField(1);
        }
        infoBox.getChildren().add(new Label(symbol + "(" + description + ")"));
        if (pearl.getType().equals("gene")) {
            final String url = "http://v4.genecards.org/cgi-bin/carddisp.pl?gene=" + pearl.getName();
            final Hyperlink hyperlink = new Hyperlink("GeneCards");
            hyperlink.setOnAction(event -> new Thread(() -> {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }).start());
            infoBox.getChildren().add(hyperlink);
        }
        final List<Variant> variants = (List<Variant>) pearl.getProperties().get("variants");
        if (variants != null)
            for (Variant variant : variants) infoBox.getChildren().add(new Label(simplified(variant)));
    }

    private String simplified(Variant variant) {
        double af = Double.valueOf((String) variant.getInfos().get("AF"));
        String value = String.format("%s:%d %s/%s AF=%.1f", variant.getChrom(), variant.getPos(), variant.getRef(), variant.getAlt(), af);
        final String bio = (String) variant.getInfos().get("BIO");
        if (bio != null) value += " BIO=" + bio;
        final String cons = (String) variant.getInfos().get("CONS");
        if (cons != null) value += " CONS=" + cons;
        return value;
    }

    private void start() {
        final List<String> phenotypes = Arrays.asList(phenotypeList.getText().split("\n"));
        if (!file.getText().isEmpty()) {
            final VcfFile vcfFile = new VcfFile(new File(file.getText()));
            genes.clear();
            vcfFile.getVariants().stream().map(variant -> (String) variant.getInfos().get("GNAME")).filter(name -> name != null).distinct().forEach(genes::add);
            final Task<PearlDatabase> task = new PoirotAnalysis(vcfFile.getVariants(), phenotypes);
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
        content.getChildren().remove(inputPane);
        if (!content.getChildren().contains(listPane)) content.getChildren().addAll(listPane, stackPane);
    }

    private void createGraph(PearlDatabase database) {
        if (database != null) {
            final List<Pearl> candidates = getCandidates(database);
            pearlTableView.getItems().setAll(candidates);
            Collections.sort(pearlTableView.getItems(), (p1, p2) -> {
                final int compare = Double.compare(p2.getScore(), p1.getScore());
                return (compare != 0) ? compare : p1.getName().compareTo(p2.getName());
            });
        }
    }

    private List<Pearl> getCandidates(PearlDatabase database) {
        return genes.stream().map(gene -> database.getPearl(gene, "gene")).
                filter(pearl -> pearl != null).
                collect(Collectors.toList());
    }

    private void reload() {
        infoBox.getChildren().clear();
        graphView.setRootGenes(pearlTableView.getSelectionModel().getSelectedItems());
    }

    @Override
    public Property<String> titleProperty() {
        return title;
    }

}