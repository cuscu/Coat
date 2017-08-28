/*
 * Copyright (c) UICHUIMI 2017
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

package coat.view.lightreader;

import coat.Coat;
import coat.CoatView;
import coat.core.reader.Reader;
import coat.utils.FileManager;
import coat.utils.OS;
import coat.view.graphic.SizableImageView;
import coat.view.lightreader.header.LightHeaderViewController;
import coat.view.lightreader.save.SaveVcfController;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.Options;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.*;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * FXML Controller class
 *
 * @author UICHUIMI
 */
public class LightVcfReader extends VBox implements Reader {


    /**
     * For the TSV saver, to avoid more than 3 decimals
     */
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###",
            DecimalFormatSymbols.getInstance(Locale.US));

    private final static long LIMIT = 100000;

    private final static Set<String> FREQUENCY_IDS = new LinkedHashSet<>(Arrays.asList("AA_F", "EUR_F", "AFR_F",
            "AMR_F", "EA_F", "ASN_F", "AA_MAF", "EUR_MAF", "AFR_MAF", "AMR_MAF", "EA_MAF", "ASN_MAF", "afr_maf",
            "eur_maf", "amr_maf", "ea_maf", "asn_maf", "GMAF", "1KG14", "MINOR_ALLELE_FREQ", "EXAC_ADJ_MAF",
            "EXAC_AFR_MAF", "EXAC_AMR_MAF", "EXAC_EAS_MAF", "EXAC_FIN_MAF", "EXAC_MAF", "EXAC_NFE_MAF",
            "EXAC_OTH_MAF", "EXAC_SAS_MAF", "AFR_AF", "AMR_AF", "EAS_AF", "EUR_AF", "SAS_AF",
            "AA_AF", "EA_AF", "ExAC_AF", "ExAC_Adj_AF", "ExAC_AFR_AF", "ExAC_AMR_AF",
            "ExAC_EAS_AF", "ExAC_FIN_AF", "ExAC_NFE_AF", "ExAC_OTH_AF", "ExAC_SAS_AF"));

    private static final String TSV_EMPTY_VALUE = ".";

    private final LightInfoTable infoTable;
    private final LightVariantsTable variantsTable;
    private final TabPane tabs = new TabPane();
    private final LightSampleTable samplesTableView;
    private final LightSampleFilterView sampleFilterView = new LightSampleFilterView();

    private final SplitPane leftPane = new SplitPane();
    private final SplitPane mainPane = new SplitPane();
    private final SplitPane root = new SplitPane();

    private final ListView<LightVcfFilter> filtersPane = new ListView<>();
    private final List<Button> actions = new LinkedList<>();
    private final Property<String> titleProperty = new SimpleStringProperty();
    private final VCFHeader vcfHeader;
    private final ProgressBar progressBar = new ProgressBar();
    private final Button addFrequencyFilters = new Button(OS.getString("add.frequency.filters"));
    private final Button clearFilters = new Button(OS.getString("clear.all.filters"));
    private final Button addFilter = new Button(OS.getString("add.filter"));
    private final Label progressLabel = new Label();
    private final StackPane stackPane = new StackPane(progressBar, progressLabel);
    private final HBox hBox = new HBox(5, addFilter, clearFilters, addFrequencyFilters, stackPane);
    private ObservableList<VariantContext> variants = FXCollections.observableArrayList();
    private File file;
    private String baseName;
    private Thread thread;
    private AtomicLong numberOfVariants = new AtomicLong();


    public LightVcfReader(File file) throws Exception {
        try (VCFFileReader reader = new VCFFileReader(file, false)) {
            vcfHeader = reader.getFileHeader();
        }
        this.file = file;
        this.samplesTableView = new LightSampleTable(vcfHeader);
        this.variantsTable = new LightVariantsTable(vcfHeader, variants);
        this.infoTable = new LightInfoTable(vcfHeader);
        filtersPane.setCellFactory(param -> new LightVcfFilterCell(vcfHeader, this));
        LightVcfFilter.setVcfHeader(vcfHeader);
//        this.variantsTable.setSampleFilters(sampleFilterView.getFilters());
        this.baseName = file.getName();
        initializeLeftPane();
        initializeThis();
        initializeButtons();
        initializeTabs();
        bindFile();
        loadAndFilter();
    }

    private void initializeLeftPane() {
        leftPane.getItems().addAll(new VBox(variantsTable, hBox));
        leftPane.setDividerPositions(0.75);
        leftPane.setOrientation(Orientation.VERTICAL);
        hBox.setPadding(new Insets(5));
        hBox.setAlignment(Pos.CENTER_LEFT);
        VBox.setVgrow(variantsTable, Priority.ALWAYS);
        HBox.setHgrow(stackPane, Priority.ALWAYS);
        progressBar.setMaxWidth(9999);
        progressLabel.getStyleClass().add("white-text");
        addFrequencyFilters.setOnAction(event -> addFrequencyFilters());
        clearFilters.setOnAction(event -> clearFilters());
        addFilter.setOnAction(event -> filtersPane.getItems().add(new LightVcfFilter("CHROM", null,
                LightVcfFilter.Connector.EQUALS, "1")));

    }

    private void initializeThis() {
        final SplitPane filterPane = new SplitPane(filtersPane, sampleFilterView);
        filterPane.setDividerPositions(0.7);
        root.getItems().addAll(mainPane, filterPane);
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
        final Tab sampleTab = new Tab(OS.getResources().getString("samples"), samplesTableView);
//        samplesPane.setOrientation(Orientation.VERTICAL);
        sampleTab.setClosable(false);
        variantsTable.getVariantProperty().addListener((observable, oldValue, newValue) -> samplesTableView.setVariant(newValue));
        samplesTableView.setVariant(variantsTable.getVariantProperty().get());
        tabs.getTabs().addAll(infoTab, sampleTab);
        sampleFilterView.setSamples(vcfHeader.getGenotypeSamples());
        sampleFilterView.onChange(event -> loadAndFilter());
    }

    @Override
    public Property<String> titleProperty() {
        return titleProperty;
    }

    @Override
    public void saveAs() {
        final File f = FileManager.saveFile(
                OS.getString("select.output.file"), file.getParentFile(),
                file.getName().replace(".vcf", "_filtered.vcf"),
                FileManager.VCF_FILTER, FileManager.TSV_FILTER);
        if (f == null) return;
        if (f.getName().endsWith(".vcf")) saveAsVcf(f);
        else saveAsTsv(f);
    }

    private void saveAsVcf(File f) {
        final List[] toSave = getFieldsAndSamplesToSave();
        if (toSave == null) return;
        final ProgressDialog progressDialog = new ProgressDialog();
        final Stage stage = getProgressStage(progressDialog);
        stopCurrentThread();
        thread = new Thread(() -> {
            saveVcf(f, toSave[0], toSave[1], progressDialog);
            Platform.runLater(stage::close);
        });
        thread.start();
        stage.showAndWait();
    }

    private void saveVcf(File f, List<String> infos, List<String> samples,
                         ProgressDialog progressDialog) {
        if (variants.size() < LIMIT) saveFromCache(f, infos, samples);
        else saveReloading(f, infos, samples, progressDialog);
    }

    private void saveReloading(File f, List<String> infos, List<String> samples, ProgressDialog progressDialog) {
        variants.clear();
        final VCFHeader header = new VCFHeader(vcfHeader.getMetaDataInInputOrder(), samples);
        for (VCFHeaderLine headerLine : vcfHeader.getInfoHeaderLines()) {
            if (!infos.contains(headerLine.getKey()))
                header.getInfoHeaderLines().remove(header.getInfoHeaderLine
                        (headerLine.getKey()));
        }
        final AtomicLong total = new AtomicLong();
        final AtomicLong passed = new AtomicLong();
        try (VariantContextWriter writer = new VariantContextWriterBuilder()
                .setOutputFile(f).unsetOption(Options.INDEX_ON_THE_FLY).build()) {
            Locale.setDefault(Locale.ENGLISH);
            writer.writeHeader(header);
            try (VCFFileReader reader = new VCFFileReader(file, false)) {
                for (VariantContext variantContext : reader) {
                    if (total.incrementAndGet() % 1000 == 0) {
                        if (thread.isInterrupted()) break;
                        progressDialog.update(numberOfVariants.get(), total.get(), passed.get());
                        updateProgressInPlatform(total.get(), passed.get());
                    }
                    if (filterBySample(variantContext) && filterByColumns(variantContext)) {
                        passed.incrementAndGet();
                        if (variants.size() < LIMIT)
                            variants.add(variantContext);
                        saveVariant(infos, writer, variantContext);

                    }
                }
            }
        }
    }

    private void saveFromCache(File f, List<String> infos, List<String> samples) {
        final VCFHeader header = new VCFHeader(vcfHeader.getMetaDataInInputOrder(), samples);
        for (VCFHeaderLine headerLine : vcfHeader.getInfoHeaderLines()) {
            if (!infos.contains(headerLine.getKey()))
                header.getInfoHeaderLines().remove(header.getInfoHeaderLine
                        (headerLine.getKey()));
        }
        try (VariantContextWriter writer = new VariantContextWriterBuilder()
                .setOutputFile(f).unsetOption(Options.INDEX_ON_THE_FLY).build()) {
            Locale.setDefault(Locale.ENGLISH);
            writer.writeHeader(header);
            for (VariantContext variantContext : variants) {
                saveVariant(infos, writer, variantContext);
            }
        }
    }

    private void saveVariant(List<String> infos, VariantContextWriter writer,
                             VariantContext variantContext) {
        final List<String> attributesToRemove = new LinkedList<>();
        for (String id : variantContext.getAttributes().keySet())
            if (!infos.contains(id)) attributesToRemove.add(id);
        final VariantContext variantToSave
                = new VariantContextBuilder(variantContext)
                .rmAttributes(attributesToRemove)
                .make();
        writer.add(variantToSave);
    }


    @NotNull
    private Stage getProgressStage(ProgressDialog progressDialog) {
        final Stage stage = new Stage(StageStyle.UTILITY);
        stage.setScene(new Scene(progressDialog));
        stage.setAlwaysOnTop(true);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(Coat.getStage());
        stage.setResizable(false);
        stage.setTitle("Saving... Please wait");
        return stage;
    }

    private List[] getFieldsAndSamplesToSave() {
        final Stage stage = new Stage();
        final SaveVcfController controller = new SaveVcfController(vcfHeader);
        final Scene scene = new Scene(controller);
        stage.setScene(scene);
        stage.initOwner(Coat.getStage());
        stage.initModality(Modality.WINDOW_MODAL);
        stage.showAndWait();
        return controller.getLists();
    }


    private void stopCurrentThread() {
        try {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void saveAsTsv(File f) {
        final List<String> header = getTsvHeaders();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
            writer.write(String.join("\t", header));
            writer.newLine();
            try (VCFFileReader reader = new VCFFileReader(file, false)) {
                for (VariantContext variantContext : reader)
                    if (filterBySample(variantContext) && filterByColumns(variantContext)) {
                        writer.write(String.join("\t", getTsvFields(variantContext)));
                        writer.newLine();
                    }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getTsvHeaders() {
        final List<String> headers = new LinkedList<>(Arrays.asList("CHROM", "POS", "ID", "REF", "ALT", "QUAL",
                "FILTER"));
        vcfHeader.getInfoHeaderLines().forEach(headerLine -> headers.add(headerLine.getID()));
        for (String sample : vcfHeader.getSampleNamesInOrder()) {
            vcfHeader.getFormatHeaderLines().forEach(vcfFormatHeaderLine -> headers.add(
                    sample + "." + vcfFormatHeaderLine.getID()
            ));
        }
        return headers;
    }

    private List<String> getTsvFields(VariantContext variant) {
        final List<String> fields = new LinkedList<>();
        fields.add(variant.getContig());
        fields.add(variant.getStart() + "");
        fields.add(variant.getID());
        fields.add(variant.getReference().getBaseString());
        fields.add(String.join(",", variant.getAlternateAlleles()
                .stream()
                .map(Allele::getBaseString)
                .collect(Collectors.toList())));
        fields.add(DECIMAL_FORMAT.format(variant.getPhredScaledQual()));
        fields.add(String.join(",", variant.getFilters()));
        vcfHeader.getInfoHeaderLines().forEach(headerLine ->
                fields.add(variant.getAttributeAsString(headerLine.getID(), TSV_EMPTY_VALUE)));
        for (String sample : vcfHeader.getSampleNamesInOrder()) {
            vcfHeader.getFormatHeaderLines().forEach(line -> fields.add(
                    variant.getGenotype(sample).hasAnyAttribute(line.getID())
                            ? variant.getGenotype(sample).getAnyAttribute(line.getID()).toString()
                            : TSV_EMPTY_VALUE));
        }
        return fields;

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
        actions.addAll(Arrays.asList(viewheaders, lfs));
    }

    private Button getViewHeadersButton() {
        Button viewheaders = new Button(OS.getResources().getString("headers"));
        viewheaders.setOnAction(event -> viewHeaders());
        viewheaders.setGraphic(new SizableImageView("coat/img/black/headers.png", SizableImageView.SMALL_SIZE));
        return viewheaders;
    }

    private void viewHeaders() {
        try {
            final FXMLLoader loader = new FXMLLoader(LightHeaderViewController
                    .class.getResource("header-view.fxml"));
            final Parent root = loader.load();
            final LightHeaderViewController controller = loader.getController();
            controller.setHeader(vcfHeader);
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
        lfs.setGraphic(new SizableImageView("coat/img/black/lfs.png", SizableImageView.SMALL_SIZE));
        return lfs;
    }

    private void addLFS() {
        injectLFSHeader();
//        variantSet.getVariants().parallelStream().forEach(LFS::addLFS);
        CoatView.printMessage("LFS tag added", "success");
    }

    /**
     * Inserts LFS header alphabetically.
     */
    private void injectLFSHeader() {
        if (!vcfHeader.hasInfoLine("LFS")) {
            final VCFInfoHeaderLine lfsHeader = new VCFInfoHeaderLine(
                    "LFS", 1, VCFHeaderLineType.Float,
                    "Low frequency codon substitution");
            vcfHeader.getInfoHeaderLines().add(lfsHeader);
        }
    }

    private void bindFile() {
        titleProperty.setValue(baseName);
        infoTable.getVariantProperty().bind(variantsTable.getVariantProperty());
    }

    public void loadAndFilter() {
        stopCurrentThread();
        thread = new Thread(() -> {
            final AtomicLong total = new AtomicLong();
            final AtomicLong passed = new AtomicLong();
            try (VCFFileReader reader = new VCFFileReader(file, false)) {
                variants.clear();
                for (VariantContext variantContext : reader) {
                    if (total.incrementAndGet() % 1000 == 0) {
                        if (thread.isInterrupted()) break;
                        updateProgressInPlatform(total.get(), passed.get());
                    }
                    if (filterBySample(variantContext) && filterByColumns(variantContext)) {
                        passed.incrementAndGet();
                        if (variants.size() < LIMIT)
                            variants.add(variantContext);
                    }
                }
            } catch (Exception ex) {
                CoatView.printMessage(ex.getMessage(), "severe");
            }
            if (total.get() > numberOfVariants.get())
                numberOfVariants.set(total.get());
            updateProgressInPlatform(total.get(), passed.get());
            variantsTable.updateChromosomeComboBox();
        });
        thread.start();
    }

    private void updateProgressInPlatform(long total, long passed) {
        Platform.runLater(() -> {
            final double progress = (double) passed / total;
            progressBar.setProgress(progress);
            progressLabel.setText(String.format("%s/%s (%.2f%%)", passed, total, 100 * progress));
        });
    }

    private void addFrequencyFilters() {
        final TextInputDialog dialog = new TextInputDialog("0.01");
        dialog.setHeaderText("Set max value frequency");
        dialog.setTitle("Max frequency");
        dialog.showAndWait().ifPresent(s -> {
            try {
                final double th = Double.valueOf(s);
                for (VCFInfoHeaderLine info : vcfHeader.getInfoHeaderLines()) {
                    if (FREQUENCY_IDS.contains(info.getID())) {
                        final LightVcfFilter filter = new LightVcfFilter("INFO", info.getID(),
                                LightVcfFilter.Connector.LESS_THAN, th);
                        filtersPane.getItems().add(filter);
                    }
                }
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
        });
        loadAndFilter();
    }

    private boolean filterBySample(VariantContext variant) {
        return sampleFilterView.getFilters().stream()
                .allMatch(sampleFilter -> sampleFilter.filter(variant));
    }

    private boolean filterByColumns(VariantContext variant) {
        return filtersPane.getItems().stream()
                .allMatch(vcfFilter -> vcfFilter.filter(variant));
    }


    private void clearFilters() {
        filtersPane.getItems().clear();
        loadAndFilter();
    }


    private class FastFilter extends VcfParallelReaderFilter {
        FastFilter(File file) {
            super(file);
        }

        @Override
        boolean filter(VariantContext variantContext) {
            return filterByColumns(variantContext) && filterBySample(variantContext);

        }
    }
}
