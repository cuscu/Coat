/*
 * Copyright (c) UICHUIMI 2017
 *
 * This file is part of Coat.
 *
 * Coat is free software:
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Coat.
 *
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.uichuimi.coat.view.vcfreader;

import org.uichuimi.coat.CoatView;
import org.uichuimi.coat.core.reader.Reader;
import org.uichuimi.coat.core.vcf.LFS;
import org.uichuimi.coat.core.vcf.TsvSaver;
import org.uichuimi.coat.core.vcf.VcfStats;
import org.uichuimi.coat.utils.FileManager;
import org.uichuimi.coat.utils.OS;
import org.uichuimi.coat.view.graphic.SizableImageView;
import org.uichuimi.coat.view.vcfreader.header.HeaderViewController;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.uichuimi.vcf.header.InfoHeaderLine;
import org.uichuimi.vcf.io.VariantWriter;
import org.uichuimi.vcf.variant.Variant;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 * FXML Controller class
 *
 * @author UICHUIMI
 */
public class VcfReader extends VBox implements Reader {

    private static int NEW_VCF = 0;

    private final InfoTable infoTable = new InfoTable();
    private final VariantsTable variantsTable;
    private final TabPane tabs = new TabPane();
    private final SampleTable samplesTableView = new SampleTable();
    private final SampleFilterView sampleFilterView = new SampleFilterView();
    private final SplitPane samplesPane = new SplitPane(samplesTableView);

    private final SplitPane leftPane = new SplitPane();
    private final SplitPane mainPane = new SplitPane();
    private final SplitPane root = new SplitPane();

    private final ListView<VcfFilter> filtersPane = new ListView<>();

    private final List<Button> actions = new LinkedList<>();
    private final Property<String> titleProperty = new SimpleStringProperty();
    private final List<Variant> variants;
    private File file;
    private String baseName;

    public VcfReader(List<Variant> variants, File file) {
        this.variants = variants;
        this.file = file;
        this.variantsTable = new VariantsTable(variants);
        this.variantsTable.setFilters(filtersPane.getItems());
        filtersPane.setCellFactory(param -> new VcfFilterCell(variants.get(0).getHeader(), variantsTable));
        VcfFilter.setVcfHeader(variants.get(0).getHeader());
        this.variantsTable.setSampleFilters(sampleFilterView.getFilters());
        this.baseName = file != null ? file.getName() : "New vcf " + NEW_VCF++;
        initializeLeftPane();
        initializeThis();
        initializeButtons();
        initializeTabs();
        bindFile();
    }

    private void initializeLeftPane() {
        leftPane.getItems().addAll(variantsTable);
        leftPane.setDividerPositions(0.75);
        leftPane.setOrientation(Orientation.VERTICAL);
    }

    private void initializeThis() {
        root.getItems().addAll(mainPane, new SplitPane(filtersPane, sampleFilterView));
        root.setOrientation(Orientation.VERTICAL);
        mainPane.getItems().addAll(leftPane, tabs);
        mainPane.setDividerPositions(0.75);
        SplitPane.setResizableWithParent(tabs, false);
        mainPane.setOrientation(Orientation.HORIZONTAL);
        getChildren().setAll(root);
        VBox.setVgrow(root, Priority.ALWAYS);
    }

    private void initializeTabs() {
        final Tab infoTab = new Tab(OS.getResources().getString("properties"), infoTable);
        infoTab.setClosable(false);
        final Tab sampleTab = new Tab(OS.getResources().getString("samples"), samplesPane);
        samplesPane.setOrientation(Orientation.VERTICAL);
        sampleTab.setClosable(false);
        variantsTable.getVariantProperty().addListener((observable, oldValue, newValue) -> samplesTableView.setVariant(newValue));
        samplesTableView.setVariant(variantsTable.getVariantProperty().get());
        tabs.getTabs().addAll(infoTab, sampleTab);
        sampleFilterView.setSamples(variants.get(0).getHeader().getSamples());
        sampleFilterView.onChange(event -> variantsTable.filter());
    }

    @Override
    public Property<String> titleProperty() {
        return titleProperty;
    }

    @Override
    public void saveAs() {
        File output = file == null
                ? FileManager.saveFile(OS.getString("select.output.file"), FileManager.VCF_FILTER, FileManager
                .TSV_FILTER)
                : FileManager.saveFile(OS.getString("select.output.file"), file.getParentFile(), file.getName(),
                FileManager.VCF_FILTER, FileManager.TSV_FILTER);
        final TreeSet<Variant> toSaveVariants = new TreeSet<>(variantsTable.getFilteredVariants());
        if (output != null) {
            if (output.getName().endsWith(".vcf"))
                saveAsVcf(output, toSaveVariants);
            else new TsvSaver(output, toSaveVariants).invoke();
            this.file = output;
            baseName = output.getName();
            titleProperty.setValue(baseName);
        }
    }

    private void saveAsVcf(File output, TreeSet<Variant> toSaveVariants) {
        try (VariantWriter writer = new VariantWriter(output)) {
            writer.setHeader(toSaveVariants.iterator().next().getHeader());
            for (Variant variant : toSaveVariants) writer.write(variant);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        Button statsButton = getStatsButton();
        actions.addAll(Arrays.asList(viewheaders, lfs, statsButton));
    }

    private Button getViewHeadersButton() {
        Button viewheaders = new Button(OS.getResources().getString("headers"));
        viewheaders.setOnAction(event -> viewHeaders());
        viewheaders.setGraphic(new SizableImageView("/img/black/headers.png", SizableImageView.SMALL_SIZE));
        return viewheaders;
    }

    private void viewHeaders() {
        try {
            final FXMLLoader loader = new FXMLLoader(HeaderViewController.class.getResource("/fxml/header-view.fxml"));
            final Parent root = loader.load();
            final HeaderViewController controller = loader.getController();
            controller.setHeader(variants.get(0).getHeader());
            final Scene scene = new Scene(root);
            final Stage stage = new Stage();
            stage.setWidth(600);
            stage.setHeight(600);
            stage.setTitle(baseName);
            stage.centerOnScreen();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Button getLfsButton() {
        final Button lfs = new Button("LFS");
        lfs.setOnAction(event -> addLFS());
        lfs.setGraphic(new SizableImageView("/img/black/lfs.png", SizableImageView.SMALL_SIZE));
        return lfs;
    }

    private void addLFS() {
        injectLFSHeader();
        variants.parallelStream().forEach(LFS::addLFS);
        CoatView.printMessage("LFS tag added", "success");
    }

    /**
     * Inserts LFS header alphabetically.
     */
    private void injectLFSHeader() {
        if (!variants.get(0).getHeader().getIdList("INFO").contains("LFS")) {
            variants.get(0).getHeader().getHeaderLines()
                    .add(new InfoHeaderLine("LFS", "1", "Float", "Low frequency codon substitution"));
        }
    }

    private Button getStatsButton() {
        Button statsButton = new Button(OS.getString("view.stats"));
        statsButton.setGraphic(new SizableImageView("/img/black/stats.png", SizableImageView.SMALL_SIZE));
        statsButton.setOnAction(event -> showStats());
        return statsButton;
    }

    private void showStats() {
        VcfStats vcfStats = new VcfStats(variants);
        StatsReader statsReader = new StatsReader(vcfStats);
        Scene scene = new Scene(statsReader);
        Stage stage = new Stage();
        stage.setWidth(600);
        stage.setHeight(600);
        stage.setTitle(baseName);
        stage.centerOnScreen();
        stage.setScene(scene);
        stage.show();
    }

    private void bindFile() {
        titleProperty.setValue(baseName);
        infoTable.getVariantProperty().bind(variantsTable.getVariantProperty());
//        variantSet.changedProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue) Platform.runLater(() -> titleProperty.setValue(baseName + "*"));
//            else titleProperty.setValue(baseName);
//        });
    }

}
