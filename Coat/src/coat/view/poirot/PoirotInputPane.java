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
import coat.core.poirot.PoirotGraphAnalysis;
import coat.core.vcfreader.VcfFile;
import coat.utils.FileManager;
import coat.view.graphic.FileParameter;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Used to select the input VCF file and the list of phenotypes.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotInputPane extends VBox {

    private final FileParameter inputVcf = new FileParameter("Input VCF");
    private final PhenotypeSelector phenotypeSelector = new PhenotypeSelector();
    private final ProgressIndicator loading = new ProgressIndicator();
    private final StackPane phenotypeSelectorStackPane = new StackPane(phenotypeSelector, loading);

    private Property<PearlDatabase> database = new SimpleObjectProperty<>();

    PoirotInputPane() {
        inputVcf.getFilters().add(FileManager.VCF_FILTER);
        inputVcf.fileProperty().addListener((observable, oldValue, file) -> loadGraph(file));
        phenotypeSelector.setDisable(true);
        loading.setVisible(false);
        getChildren().addAll(inputVcf, phenotypeSelectorStackPane);
        setSpacing(5);
    }

    private void loadGraph(File file) {
        final VcfFile vcfFile = new VcfFile(file);
        final PoirotGraphAnalysis analysis = new PoirotGraphAnalysis(vcfFile.getVariants());
        phenotypeSelector.setDisable(true);
        loading.setVisible(true);
        StackPane.setMargin(loading, new Insets(20, 20, 20, 20));
        analysis.setOnSucceeded(event -> fileLoaded(analysis));
        new Thread(analysis).start();
    }

    private void fileLoaded(PoirotGraphAnalysis analysis) {
        final PearlDatabase pearlDatabase = analysis.getValue();
        database.setValue(pearlDatabase);
        final List<String> list = pearlDatabase.getPearls("phenotype").stream().map(Pearl::getName).collect(Collectors.toList());
        phenotypeSelector.setPhenotypes(list);
        phenotypeSelector.setDisable(false);
        loading.setVisible(false);
    }

    public Property<PearlDatabase> databaseProperty() {
        return database;
    }

    public PearlDatabase getDatabase() {
        return database.getValue();
    }

    public FileParameter getInputVcf() {
        return inputVcf;
    }

    public ObservableList<String> getSelectedPhenotypes() {
        return phenotypeSelector.getSelectedPhenotypes();
    }
}

