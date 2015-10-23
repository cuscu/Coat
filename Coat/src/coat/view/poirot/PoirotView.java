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

package coat.view.poirot;

import coat.core.poirot.Pearl;
import coat.core.poirot.PearlDatabase;
import coat.core.poirot.dataset.Dataset;
import coat.core.poirot.dataset.Instance;
import coat.core.poirot.dataset.hgnc.HGNCDatabase;
import coat.core.poirot.dataset.omim.OmimDatasetLoader;
import coat.core.poirot.graph.GraphEvaluator;
import coat.core.tool.Tool;
import coat.core.vcfreader.Variant;
import coat.utils.OS;
import coat.view.graphic.SizableImage;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Complete view of the poirot tool. It is made of three elements:
 * <ol>
 * <li>An input panel, where user selects the input file and the associated phenotypes.</li>
 * <li>A list of affected genes, scored by relevance. User will select which genes to show in the canvas.</li>
 * <li>A canvas, where a graph with the relationships are shown.</li>
 * </ol>
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotView extends Tool {

    private final HBox content = new HBox();

    private final PoirotInputPane poirotInputPane = new PoirotInputPane();
    private final PoirotPearlTable poirotPearlTable = new PoirotPearlTable();
    private final GraphView graphView = new GraphView();

    private final Button start = new Button(OS.getResources().getString("start"), new SizableImage("coat/img/start.png", SizableImage.SMALL_SIZE));
    private final Label message = new Label();

    private final VBox inputPane = new VBox(5, poirotInputPane, start, message);
    private final VBox graphVBox = new VBox(graphView);
    private final VBox infoBox = new VBox();
    private final StackPane stackPane = new StackPane(graphVBox, infoBox);

    private final Button reload = new Button("Reload graph", new SizableImage("coat/img/poirot.png", SizableImage.SMALL_SIZE));
    private final ToggleButton repeat = new ToggleButton("Show panel", new SizableImage("coat/img/form.png", SizableImage.SMALL_SIZE));
    private final HBox buttons = new HBox(5, repeat, reload);

    private final VBox listPane = new VBox(5, poirotPearlTable, buttons);

    private Property<String> title = new SimpleStringProperty("Poirot");
    private Dataset omimDataset = null;
//    private PearlDatabase database;

    public PoirotView() {
        initializeThis();
        initializeInputPane();
        initializeListPane();
        initializeGraphView();
    }

    private void initializeThis() {
        getChildren().add(content);
        VBox.setVgrow(content, Priority.ALWAYS);
        content.setSpacing(5);
        content.setPadding(new Insets(5, 5, 5, 5));
        content.getChildren().addAll(inputPane);
    }

    private void initializeInputPane() {
        initializeStartButton();
        initializeFileInput();
    }

    private void initializeFileInput() {
        poirotInputPane.getInputVcf().fileProperty().addListener((observable1, oldValue1, file) -> {
            title.setValue("Poirot (" + file.getName() + ")");
            poirotPearlTable.getItems().clear();
            graphView.clear();
        });
        poirotInputPane.getSelectedPhenotypes().addListener((ListChangeListener<String>) c
                -> start.setDisable(poirotInputPane.getSelectedPhenotypes().isEmpty()));
        poirotPearlTable.setOnMouseClicked(event -> {
            if (event.getClickCount() >= 2) reload();
        });
    }

    private void initializeListPane() {
        initializeReloadButton();
        initializeRepeatButton();
        initializePearlListView();
    }

    private void initializeStartButton() {
        start.setOnAction(event -> start());
        start.setMaxWidth(9999);
        start.setPadding(new Insets(10));
        start.setDisable(true);
    }

    private void initializeReloadButton() {
        reload.setMaxWidth(9999);
        reload.setPadding(new Insets(10));
        reload.setOnAction(event -> reload());
        HBox.setHgrow(reload, Priority.ALWAYS);
    }

    private void initializeRepeatButton() {
        repeat.setMaxWidth(9999);
        repeat.setPadding(new Insets(10));
        repeat.selectedProperty().addListener((observable, oldValue, selected) -> repeat.setText((selected) ? "Hide panel" : "Show panel"));
        repeat.selectedProperty().addListener((observable, oldValue, selected) -> {
            if (!selected) content.getChildren().remove(inputPane);
            else if (!content.getChildren().contains(inputPane)) content.getChildren().add(0, inputPane);
        });
        HBox.setHgrow(repeat, Priority.ALWAYS);
    }

    private void initializePearlListView() {
        VBox.setVgrow(poirotPearlTable, Priority.ALWAYS);
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
            relationship.getRelationships().forEach(pearlRelationship
                    -> infoBox.getChildren().add(new Label(pearlRelationship.getProperties().toString())));
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
        String description = getDescription(symbol);
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

    private String getDescription(String symbol) {
        String description = HGNCDatabase.getName(symbol);
        if (description == null) {
            loadOmimDataset();
            final List<Instance> instances = omimDataset.getInstances(symbol, 0);
            if (!instances.isEmpty()) description = (String) instances.get(0).getField(1);
        }
        return description;
    }

    private void loadOmimDataset() {
        if (omimDataset == null) {
            try {
                final OmimDatasetLoader loader = new OmimDatasetLoader();
                loader.run();
                omimDataset = loader.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
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
        final List<String> phenotypes = poirotInputPane.getSelectedPhenotypes();
        final PearlDatabase database = poirotInputPane.getDatabase();
        final GraphEvaluator graphEvaluator = new GraphEvaluator(database, phenotypes);
        graphEvaluator.setOnSucceeded(event -> end(database));
        poirotPearlTable.getItems().clear();
        graphView.clear();
        start.setDisable(true);
        message.setText("Analyzing");
        message.textProperty().bind(graphEvaluator.messageProperty());
        new Thread(graphEvaluator).start();
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
            poirotPearlTable.getItems().setAll(candidates);
            Collections.sort(poirotPearlTable.getItems(), (p1, p2) -> {
                final int compare = Double.compare(p2.getScore(), p1.getScore());
                return (compare != 0) ? compare : p1.getName().compareTo(p2.getName());
            });
        }
    }

    private List<Pearl> getCandidates(PearlDatabase database) {
        return database.getPearls("gene").stream()
                .filter(pearl -> pearl.getProperties().containsKey("variants"))
                .filter(pearl -> pearl.getDistanceToPhenotype() > 0)
                .collect(Collectors.toList());
    }

    private void reload() {
        infoBox.getChildren().clear();
        graphView.setRootGenes(poirotPearlTable.getSelectionModel().getSelectedItems());
    }

    @Override
    public Property<String> titleProperty() {
        return title;
    }

}