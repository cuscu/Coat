/******************************************************************************
 * Copyright (C) 2015 UICHUIMI                                                *
 * *
 * This program is free software: you can redistribute it and/or modify it    *
 * under the terms of the GNU General Public License as published by the      *
 * Free Software Foundation, either version 3 of the License, or (at your     *
 * option) any later version.                                                 *
 * *
 * This program is distributed in the hope that it will be useful, but        *
 * WITHOUT ANY WARRANTY; without even the implied warranty of                 *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 * *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 ******************************************************************************/

package coat.view.poirot;

import coat.core.tool.Tool;
import coat.utils.OS;
import coat.view.graphic.SizableImageView;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import poirot.core.Pearl;
import poirot.core.PearlGraph;
import poirot.core.PoirotGraphEvaluator;
import poirot.view.GraphEvaluator;
import poirot.view.GraphView;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Complete view of the poirot tool. It is made of three elements:
 * <ol>
 * <li>An input panel, where user selects the input file and the associated phenotypes.</li>
 * <li>A list of affected geneTable, scored by relevance. User will select which geneTable to show in the canvas.</li>
 * <li>A canvas, where a graph with the relationships are shown.</li>
 * </ol>
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotView extends Tool {

    private final PoirotInputPane poirotInputPane = new PoirotInputPane();
    private final Button start = new Button(OS.getString("show").toUpperCase(), new SizableImageView("coat/img/white/arrow-right.png", SizableImageView.SMALL_SIZE));
    private final Label message = new Label();
    private final VBox inputPane = new VBox(5, poirotInputPane, start, message);

    private final PoirotInfo poirotInfo = new PoirotInfo();

    private final PoirotPearlTable poirotPearlTable = new PoirotPearlTable();

    private final GraphView graphView = new GraphView();
    private final StackPane stackPane = new StackPane(graphView);
    private final Button reload = new Button(OS.getString("reload").toUpperCase(), new SizableImageView("coat/img/white/poirot.png", SizableImageView.SMALL_SIZE));
    private final Button back = new Button(OS.getString("back").toUpperCase(), new SizableImageView("coat/img/white/arrow-left.png", SizableImageView.SMALL_SIZE));
    private final VBox listPane = new VBox(5, back, poirotPearlTable, reload);
    private SplitPane graphSplitPane = new SplitPane(stackPane, poirotInfo);
    private final HBox graphHBox = new HBox(listPane, graphSplitPane);

    private Property<String> title = new SimpleStringProperty("Poirot");
    private File file;

    PoirotView(File file) {
        this.file = file;
        title.setValue("Poirot (" + file.getName() + ")");
        initializeThis();
        initializeInputPane();
        initializeListPane();
        initializeGraphView();
    }

    private void initializeThis() {
        getChildren().setAll(inputPane);
        setSpacing(5);
        setPadding(new Insets(5, 5, 5, 5));
        VBox.setVgrow(inputPane, Priority.ALWAYS);
        VBox.setVgrow(graphHBox, Priority.ALWAYS);

    }

    private void initializeInputPane() {
        initializeStartButton();
        initializeFileInput();
    }

    private void initializeFileInput() {
        VBox.setVgrow(poirotInputPane, Priority.ALWAYS);
        poirotInputPane.setFile(file);
        poirotPearlTable.setOnMouseClicked(event -> {
            if (event.getClickCount() >= 2) reload();
        });
    }

    private void initializeListPane() {
        initializeReloadButton();
        initializeBackButton();
        initializePearlListView();
    }

    private void initializeStartButton() {
        start.setOnAction(event -> start());
        start.setMaxWidth(9999);
        start.setPadding(new Insets(10));
        start.setContentDisplay(ContentDisplay.RIGHT);
    }

    private void initializeReloadButton() {
        reload.setMaxWidth(9999);
        reload.setPadding(new Insets(10));
        reload.setOnAction(event -> reload());
        HBox.setHgrow(reload, Priority.ALWAYS);
    }

    private void initializeBackButton() {
        back.setOnAction(event -> {
            final PoirotGraphEvaluator evaluator = new PoirotGraphEvaluator(poirotInputPane.getDatabase());
            evaluator.setOnSucceeded(event1 -> {
                getChildren().setAll(inputPane);
                poirotInfo.clearView();
            });
            new Thread(evaluator).start();
        });
        back.setPadding(new Insets(10));
        back.setMaxWidth(9999);
    }

    private void initializePearlListView() {
        VBox.setVgrow(poirotPearlTable, Priority.ALWAYS);
    }

    private void initializeGraphView() {
        HBox.setHgrow(graphSplitPane, Priority.ALWAYS);
        graphSplitPane.setOrientation(Orientation.VERTICAL);
        graphSplitPane.setDividerPositions(0.7);
        SplitPane.setResizableWithParent(poirotInfo, false);
        stackPane.widthProperty().addListener((observable, oldValue, newValue) -> graphView.setWidth(newValue.doubleValue()));
        stackPane.heightProperty().addListener((observable, oldValue, newValue) -> graphView.setHeight(newValue.doubleValue()));
        VBox.setVgrow(stackPane, Priority.ALWAYS);
        graphView.setManaged(false);
        graphView.selectedItemProperty().addListener((observable, oldValue, item) -> poirotInfo.setItem(item));
    }

    private void start() {
        final List<Pearl> phenotypes = poirotInputPane.getSelectedPhenotypes();
        final PearlGraph database = poirotInputPane.getDatabase();
        PoirotGraphEvaluator evaluator = new PoirotGraphEvaluator(database, phenotypes);
        evaluator.setOnSucceeded(event -> end(database));
        poirotPearlTable.getItems().clear();
        graphView.clear();
        start.setDisable(true);
        message.setText("Analyzing");
        message.textProperty().bind(evaluator.messageProperty());
        new Thread(evaluator).start();
    }

    private void end(PearlGraph database) {
        createGraph(database);
        toGraphView();
    }

    private void toGraphView() {
        start.setDisable(false);
        message.textProperty().unbind();
        message.setText("Done");
        getChildren().setAll(graphHBox);
        graphView.setRootGenes(poirotPearlTable.getItems().subList(0, 1));
    }

    private void createGraph(PearlGraph database) {
        final List<Pearl> candidates = getCandidates(database);
        poirotPearlTable.getItems().setAll(candidates);
        Collections.sort(poirotPearlTable.getItems(), (p1, p2) -> {
            final int compare = Double.compare(p2.getScore(), p1.getScore());
            return (compare != 0) ? compare : p1.getId().compareTo(p2.getId());
        });
    }

    private List<Pearl> getCandidates(PearlGraph database) {
        return database.getPearls(Pearl.Type.GENE).stream()
                .filter(pearl -> pearl.getProperties().containsKey("variants"))
                .filter(pearl -> pearl.getDistanceToPhenotype() > 0)
                .collect(Collectors.toList());
    }

    private void reload() {
        graphView.setRootGenes(poirotPearlTable.getSelectionModel().getSelectedItems());
        poirotInfo.clearView();
    }

    @Override
    public Property<String> titleProperty() {
        return title;
    }

}