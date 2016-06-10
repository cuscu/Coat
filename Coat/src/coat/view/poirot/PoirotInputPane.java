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

import coat.utils.OS;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import poirot.core.Pearl;
import poirot.core.PearlGraph;
import poirot.core.PearlGraphFactory;
import poirot.core.PoirotGraphEvaluator;
import vcf.VariantSet;
import vcf.VariantSetFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to select the input VCF file and the list of phenotypes.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
class PoirotInputPane extends VBox {

    private final PhenotypeSelector phenotypeSelector = new PhenotypeSelector();
    private final ProgressBar loading = new ProgressBar();

    private Property<PearlGraph> database = new SimpleObjectProperty<>();

    PoirotInputPane() {
        phenotypeSelector.setDisable(true);
        loading.setVisible(false);
        final StackPane phenotypeSelectorStackPane = new StackPane(phenotypeSelector, loading);
        VBox.setVgrow(phenotypeSelectorStackPane, Priority.ALWAYS);
        getChildren().addAll(phenotypeSelectorStackPane);
        setSpacing(5);
    }

    private void loadGraph(File file) {
        phenotypeSelector.setDisable(true);
        loading.setVisible(true);
        StackPane.setMargin(loading, new Insets(20, 20, 20, 20));
        new Thread(() -> {
            System.out.println("Loading variants");
            final VariantSet variantSet = VariantSetFactory.createFromFile(file);
            System.out.println("Variants loaded");
            final PearlGraph graph = PearlGraphFactory.createFromPoirotCore(new File(OS.getConfigPath(), "poirot-brain.txt.gz"));
            graph.addVariants(variantSet);
            fileLoaded(graph);
        }).start();
    }

    private void fileLoaded(PearlGraph pearlGraph) {
        database.setValue(pearlGraph);
        final PoirotGraphEvaluator evaluator = new PoirotGraphEvaluator(pearlGraph);
        evaluator.setOnSucceeded(event -> initialEvaluationSucceeded(pearlGraph));
        bindProgress(evaluator);
        new Thread(evaluator).start();
    }

    private void bindProgress(Task task) {
        loading.setVisible(true);
        loading.progressProperty().bind(task.progressProperty());
    }

    private void initialEvaluationSucceeded(PearlGraph pearlGraph) {
        final List<Pearl> list = new ArrayList<>();
        pearlGraph.getPearls(Pearl.Type.TISSUE).stream().forEach(list::add);
        pearlGraph.getPearls(Pearl.Type.DISEASE).stream().forEach(list::add);
        phenotypeSelector.setPhenotypes(list);
        phenotypeSelector.setDisable(false);
        unbindProgress();
    }

    private void unbindProgress() {
        loading.setVisible(false);
        loading.progressProperty().unbind();
    }

    public PearlGraph getDatabase() {
        return database.getValue();
    }

    List<Pearl> getSelectedPhenotypes() {
        return phenotypeSelector.getSelectedPhenotypes();
    }

    public void setFile(File file) {
        loadGraph(file);
    }
}
