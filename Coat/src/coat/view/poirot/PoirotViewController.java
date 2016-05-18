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
import coat.view.poirot.sample.SampleListController;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import poirot.core.PearlGraph;
import poirot.core.PearlGraphFactory;

import java.io.File;
import java.util.logging.Logger;

/**
 * Created by uichuimi on 16/05/16.
 */
public class PoirotViewController {

    private final static Logger log = Logger.getLogger(PoirotViewController.class.getName());
    private PearlGraph fromPoirotCore;

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

    @FXML
    private void initialize() {
        log.info("Loading Poirot core...");
        final Task<PearlGraph> graphTask = new Task<PearlGraph>() {
            @Override
            protected PearlGraph call() throws Exception {
                return PearlGraphFactory.createFromPoirotCore(new File("config/poirot-brain.txt.gz"));
            }
        };
        graphTask.setOnSucceeded(event -> coreloaded(graphTask.getValue()));
        new Thread(graphTask).start();

    }

    private void coreloaded(PearlGraph value) {
        log.info("Core loaded");
        phenotypesController.setPearlGraph(value);
        genesController.setPearlGraph(value);
    }
}
