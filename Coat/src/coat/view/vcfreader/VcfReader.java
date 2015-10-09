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
package coat.view.vcfreader;

import coat.CoatView;
import coat.model.reader.Reader;
import coat.model.vcfreader.*;
import coat.model.vep.EnsemblAPI;
import coat.utils.FileManager;
import coat.utils.OS;
import coat.view.graphic.SizableImage;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;

/**
 * FXML Controller class
 *
 * @author UICHUIMI
 */
public class VcfReader extends VBox implements Reader {

    private final InfoTable infoTable = new InfoTable();
    private final VariantsTable variantsTable = new VariantsTable();
    private final FilterList filterList = new FilterList();
    private final TabPane tabs = new TabPane();

    private final SplitPane leftPane = new SplitPane();
    private final SplitPane mainPane = new SplitPane();

    private final VcfFile vcfFile;

    private final List<Button> actions = new LinkedList<>();
    private final Property<String> titleProperty = new SimpleStringProperty();

    public VcfReader(VcfFile vcfFile) {
        this.vcfFile = vcfFile;
        initializeLeftPane();
        initializeThis();
        initializeButtons();
        initializeTabs();
        bindFile();
    }

    private void initializeLeftPane() {
        leftPane.getItems().addAll(variantsTable, filterList);
        leftPane.setDividerPositions(0.75);
        SplitPane.setResizableWithParent(filterList, false);
        leftPane.setOrientation(Orientation.VERTICAL);
    }

    private void initializeThis() {
        mainPane.getItems().addAll(leftPane, tabs);
        mainPane.setDividerPositions(0.75);
        SplitPane.setResizableWithParent(tabs, false);
        mainPane.setOrientation(Orientation.HORIZONTAL);
        getChildren().setAll(mainPane);
        VBox.setVgrow(mainPane, Priority.ALWAYS);
    }

    private void initializeTabs() {
        Tab infoTab = new Tab(OS.getResources().getString("properties"), infoTable);
        infoTab.setClosable(false);
        tabs.getTabs().addAll(infoTab);
    }

    @Override
    public Property<String> getTitle() {
        return titleProperty;
    }

    @Override
    public void saveAs() {
        File output = FileManager.saveFile("Select output file", vcfFile.getFile().getParentFile(),
                vcfFile.getFile().getName(), FileManager.VCF_FILTER, FileManager.TSV_FILTER);
        List<Variant> toSaveVariants = new ArrayList<>(filterList.getOutputVariants());
        if (output != null)
            if (output.getName().endsWith(".vcf")) new VcfSaver(vcfFile, output, toSaveVariants).invoke();
            else new TsvSaver(vcfFile, output, toSaveVariants).invoke();
    }

    @Override
    public List<Button> getActions() {
        return actions;
    }

    @Override
    public String getActionsName() {
        return "VCF";
    }

    private void initializeButtons() {
        Button viewheaders = getViewHeadersButton();
        Button lfs = getLfsButton();
        Button vep = getVepButton();
        Button statsButton = getStatsButton();
        actions.addAll(Arrays.asList(viewheaders, vep, lfs, statsButton));
    }

    private Button getViewHeadersButton() {
        Button viewheaders = new Button(OS.getResources().getString("headers"));
        viewheaders.setOnAction(event -> viewHeaders());
        viewheaders.setGraphic(new SizableImage("coat/img/headers.png", SizableImage.SMALL_SIZE));
        return viewheaders;
    }

    private void viewHeaders() {
        TextArea area = new TextArea();
        vcfFile.getUnformattedHeaders().forEach(header -> area.appendText(header + "\n"));
        area.setEditable(false);
        area.setWrapText(true);
        area.home();
        Scene scene = new Scene(area);
        Stage stage = new Stage();
        stage.setWidth(600);
        stage.setHeight(600);
        stage.setTitle(vcfFile.getFile().getName());
        stage.centerOnScreen();
        stage.setScene(scene);
        stage.show();
    }

    private Button getLfsButton() {
        Button lfs = new Button("LFS");
        lfs.setOnAction(event -> addLFS());
        lfs.setGraphic(new SizableImage("coat/img/lfs.png", SizableImage.SMALL_SIZE));
        return lfs;
    }

    private void addLFS() {
        injectLFSHeader();
        vcfFile.getVariants().parallelStream().forEach(LFS::addLFS);
        CoatView.printMessage("LFS tag added", "success");
    }

    /**
     * Inserts LFS header alphabetically.
     */
    private void injectLFSHeader() {
        // Let's check if LFS header is already stored
        for (Map<String, String> map : vcfFile.getInfos()) if (map.get("ID").equals("LFS")) return;
        final String lfsInfo = "##INFO=<ID=LFS,Number=1,Type=Integer,Description=\"Low frequency codon substitution\">";
        vcfFile.addInfoLines(lfsInfo);
    }

    private Button getVepButton() {
        Button vep = new Button("VEP");
        vep.setGraphic(new SizableImage("coat/img/vep_logo.png", SizableImage.SMALL_SIZE));
        vep.setOnAction(e -> addVep());
        return vep;
    }

    private void addVep() {
        injectVEPHeaders();
        Task task = EnsemblAPI.vepAnnotator(vcfFile.getVariants());
        task.setOnSucceeded(event -> CoatView.printMessage(vcfFile.getVariants().size() + " variants annotated", "success"));
        task.setOnFailed(event -> CoatView.printMessage("something wrong", "error"));
        task.messageProperty().addListener((obs, old, current) -> CoatView.printMessage(current, "info"));
        CoatView.printMessage("Annotating variants...", "info");
        new Thread(task).start();
    }

    private void injectVEPHeaders() {
        vcfFile.addInfoLines(EnsemblAPI.headers);
    }

    private Button getStatsButton() {
        Button statsButton = new Button("View stats");
        statsButton.setGraphic(new SizableImage("coat/img/stats.png", SizableImage.SMALL_SIZE));
        statsButton.setOnAction(event -> showStats());
        return statsButton;
    }

    private void showStats() {
        VcfStats vcfStats = new VcfStats(vcfFile);
        StatsReader statsReader = new StatsReader(vcfStats);
        Scene scene = new Scene(statsReader);
        Stage stage = new Stage();
        stage.setWidth(600);
        stage.setHeight(600);
        stage.setTitle(vcfFile.getFile().getName());
        stage.centerOnScreen();
        stage.setScene(scene);
        stage.show();
    }

    private void bindFile() {
        titleProperty.setValue(vcfFile.getFile().getName());
        infoTable.setInfos(vcfFile.getInfos());
        infoTable.getVariantProperty().bind(variantsTable.getVariantProperty());
        filterList.setInputVariants(vcfFile.getVariants());
        filterList.setInfos(vcfFile.getInfos());
        variantsTable.setVariants(filterList.getOutputVariants());
    }
}
