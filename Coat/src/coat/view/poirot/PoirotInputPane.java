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

import coat.core.poirot.GraphFactory;
import coat.core.poirot.Pearl;
import coat.core.poirot.PearlGraph;
import coat.core.poirot.graph.GraphEvaluator;
import coat.core.vcf.VcfFile;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to select the input VCF file and the list of phenotypes.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotInputPane extends VBox {

    private final PhenotypeSelector phenotypeSelector = new PhenotypeSelector();
    private final ProgressIndicator loading = new ProgressIndicator();

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
            final VcfFile vcfFile = new VcfFile(file);
            System.out.println("Variants loaded");
            final GraphFactory analysis = new GraphFactory(vcfFile.getVariants());
            analysis.setOnSucceeded(event -> fileLoaded(analysis));
            new Thread(analysis).start();
        }).start();
    }

    private void fileLoaded(GraphFactory analysis) {
        final PearlGraph pearlDatabase = analysis.getValue();
        database.setValue(pearlDatabase);
        new GraphEvaluator(pearlDatabase).run();
        final List<Pearl> list = new ArrayList<>();
        pearlDatabase.getPearls(Pearl.Type.EXPRESSION).stream().forEach(list::add);
        pearlDatabase.getPearls(Pearl.Type.DISEASE).stream().forEach(list::add);
        phenotypeSelector.setPhenotypes(list);
        phenotypeSelector.setDisable(false);
        loading.setVisible(false);
    }

    public PearlGraph getDatabase() {
        return database.getValue();
    }

    public List<Pearl> getSelectedPhenotypes() {
        return phenotypeSelector.getSelectedPhenotypes();
    }

    public void setFile(File file) {
        loadGraph(file);
    }
}

