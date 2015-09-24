package coat.view.poirot;

import coat.model.poirot.Pearl;
import coat.model.poirot.PearlDatabase;
import coat.model.poirot.PoirotGraphAnalysis;
import coat.model.vcfreader.VcfFile;
import coat.utils.FileManager;
import coat.view.graphic.FileParameter;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
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

    private Property<PearlDatabase> database = new SimpleObjectProperty<>();

    PoirotInputPane() {
        inputVcf.getFilters().add(FileManager.VCF_FILTER);
        inputVcf.fileProperty().addListener((observable, oldValue, file) -> loadGraph(file));
        phenotypeSelector.setDisable(true);
        getChildren().addAll(inputVcf, phenotypeSelector);
        setSpacing(5);
    }

    private void loadGraph(File file) {
        final VcfFile vcfFile = new VcfFile(file);
        final PoirotGraphAnalysis analysis = new PoirotGraphAnalysis(vcfFile.getVariants());
        phenotypeSelector.setDisable(true);
        analysis.setOnSucceeded(event -> fileLoaded(analysis));
        new Thread(analysis).start();
    }

    private void fileLoaded(PoirotGraphAnalysis analysis) {
        final PearlDatabase pearlDatabase = analysis.getValue();
        database.setValue(pearlDatabase);
        final List<String> list = pearlDatabase.getPearls("phenotype").stream().map(Pearl::getName).collect(Collectors.toList());
        phenotypeSelector.setPhenotypes(list);
        phenotypeSelector.setDisable(false);
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

