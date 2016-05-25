/*
 * Copyright (c) UICHUIMI 2016
 *
 * This file is part of Coat.
 *
 * Coat is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package coat.view.poirot;

import coat.view.poirot.gene.GeneListController;
import coat.view.poirot.phenotype.PhenotypeListController;
import coat.view.poirot.sample.Sample;
import coat.view.poirot.sample.SampleListController;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import poirot.core.Pearl;
import poirot.core.PearlGraph;
import poirot.core.PearlGraphFactory;
import poirot.view.GraphEvaluator;
import poirot.view.GraphView;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * Created by uichuimi on 16/05/16.
 */
public class PoirotViewController {

    private final static Logger log = Logger.getLogger(PoirotViewController.class.getName());
    @FXML private PoirotInfo poirotInfo;
    @FXML
    private StackPane graphViewContainer;
    @FXML
    private GraphView graphView;
    @FXML
    private Button updateButton;
    @FXML
    private CheckBox autoUpdate;
    @FXML
    private VBox samples;
    @FXML
    private SampleListController samplesController;
    @FXML
    private VBox phenotypes;
    @FXML
    private PhenotypeListController phenotypesController;
    @FXML
    private VBox genes;
    @FXML
    private GeneListController genesController;

    private PearlGraph pearlGraph;

    @FXML
    private void initialize() {
        log.info("Loading Poirot core...");
        final Task<PearlGraph> graphTask = new Task<PearlGraph>() {
            @Override
            protected PearlGraph call() throws Exception {
                return PearlGraphFactory.createFromPoirotCore(new File("config/poirot-brain.txt.gz"));
            }
        };
        graphTask.setOnSucceeded(event -> coreLoaded(graphTask.getValue()));
        new Thread(graphTask).start();
        samplesController.samples().addListener((ListChangeListener<Sample>) c -> evaluatePhenotypes());
        phenotypesController.selectedPhenotypes().addListener((ListChangeListener<Pearl>) c -> evaluateGenes());
        graphViewContainer.widthProperty().addListener((observable, oldValue, newValue) -> graphView.setWidth(newValue.doubleValue()));
        graphViewContainer.heightProperty().addListener((observable, oldValue, newValue) -> graphView.setHeight(newValue.doubleValue()));
        graphView.setManaged(false);
        graphView.selectedItemProperty().addListener((observable, oldValue, item) -> {
            poirotInfo.setItem(item);
        });
    }

    private void coreLoaded(PearlGraph pearlGraph) {
        this.pearlGraph = pearlGraph;
        log.info("Core loaded");
        phenotypesController.setPearlGraph(pearlGraph);
        genesController.setPearlGraph(pearlGraph);
    }

    private void evaluatePhenotypes() {
        pearlGraph.clearVariants();
        log.info("Adding variants");
        samplesController.getSamples().forEach(sample -> pearlGraph.addVariants(sample.getVcfFile()));
        log.info("Evaluating graph");
        new GraphEvaluator(pearlGraph).run();
        phenotypesController.sort();
        graphView.clear();
    }

    private void evaluateGenes() {
        log.info("Evaluating graph");
        new GraphEvaluator(pearlGraph, phenotypesController.selectedPhenotypes()).run();
        genesController.sort();
        graphView.clear();
    }

    public void update(ActionEvent actionEvent) {
        final List<Pearl> selectedGenes = genesController.getSelectedGenes();
        graphView.setRootGenes(selectedGenes);
    }
}
