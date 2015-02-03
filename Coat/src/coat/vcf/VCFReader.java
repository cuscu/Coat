/*
 * Copyright (C) 2014 UICHUIMI
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package coat.vcf;

import coat.graphic.IndexCell;
import coat.graphic.NaturalCell;
import coat.graphic.SizableImage;
import coat.utils.FileManager;
import coat.utils.OS;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Main scenario for the TSV Reader View.
 *
 * @author Pascual Lorente Arencibia
 */
public class VCFReader extends SplitPane {

    @FXML
    private TableView<Variant> table;
//    @FXML
//    private VariantGenotype formatBox;
    @FXML
    private VariantInfoTable variantInfo;
    @FXML
    private VBox filtersPane;
    @FXML
    private Button addFilter;
    @FXML
    private Label infoLabel;
    @FXML
    private Button lfs;
    @FXML
    private Button viewHeaders;

    /**
     * The VCF file.
     */
    private final File vcfFile;
    /**
     * Total number of lines.
     */
    private final AtomicInteger totalLines = new AtomicInteger();
    /**
     * Current unfiltered lines.
     */
    private final AtomicInteger lines = new AtomicInteger();

    private final TableColumn<Variant, String> lineNumber = new TableColumn();
    private final TableColumn<Variant, String> chrom = new TableColumn("Chrom");
    private final TableColumn<Variant, String> position = new TableColumn("Position");
    private final TableColumn<Variant, String> variant = new TableColumn("Variant");
    private final TableColumn<Variant, String> rsId = new TableColumn("ID");
    private final TableColumn<Variant, String> qual = new TableColumn("Qual");
    private final TableColumn<Variant, String> filter = new TableColumn("Filter");
    private final List<VariantListener> listeners = new ArrayList();
    /**
     * Label INFO
     */
    private Set<String> infos = new TreeSet();
    private Set<String> headers = new LinkedHashSet();
    private VCFHeader vcfHeader;

    /**
     * Creates a new VCFTable to read the vcfFile.
     *
     * @param vcfFile the VCF file
     */
    public VCFReader(File vcfFile) {
        this.vcfFile = vcfFile;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("VCFReader.fxml"), OS.getResources());
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(VCFReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize() {
        addFilter.setGraphic(new SizableImage("coat/img/new.png", SizableImage.SMALL_SIZE));
        addFilter.setOnAction(e -> addFilter());
        table.setSortPolicy(view -> false);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(lineNumber, chrom, position, rsId, variant, qual, filter);
        table.setEditable(true);
        chrom.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getChrom()));
        position.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPos() + ""));
        variant.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getRef()
                + "->" + param.getValue().getAlt()));
        rsId.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getId()));
        filter.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilter()));
        qual.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getQual() + ""));
        lineNumber.setCellFactory(param -> new IndexCell());
        chrom.setCellFactory(column -> new NaturalCell());
        position.setCellFactory(column -> new NaturalCell());
        variant.setCellFactory(column -> new NaturalCell());
        rsId.setCellFactory(column -> new NaturalCell());
        filter.setCellFactory(column -> new NaturalCell());
        qual.setCellFactory(column -> new NaturalCell());

        table.getSelectionModel().selectedItemProperty().addListener(e -> updateVariant());
        addListener(variantInfo);
        loadFile();
        lfs.setOnAction(event -> getLfsInfo());
        viewHeaders.setOnAction(event -> viewHeader());

    }

    private void loadFile() {
        vcfHeader = new VCFHeader(vcfFile);
        table.getItems().clear();
        totalLines.set(0);
        headers.clear();
        infos.clear();
        try (BufferedReader in = new BufferedReader(new FileReader(vcfFile))) {
            in.lines().forEachOrdered(line -> {
                if (line.startsWith("#")) {
                    addHeader(line);
                } else {
                    table.getItems().add(toVariant(line));
                    totalLines.incrementAndGet();
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(VCFReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        lines.set(totalLines.get());
        updateInfo();
    }

    /**
     * Returns the opened file.
     *
     * @return the opened file
     */
    public File getFile() {
        return vcfFile;
    }

    private Variant toVariant(String t) {
        return new Variant(t);
    }

    /**
     * Fills the bottom table.
     */
    private void updateVariant() {
        Variant v = table.getSelectionModel().getSelectedItem();
        // Call listeners
        listeners.forEach(t -> t.variantChanged(v, vcfHeader));
    }

    /**
     * Adds a listener that will listen for changes in the variant selected.
     *
     * @param listener the listener to add
     */
    public void addListener(VariantListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Nothing to do here.
     *
     * @param listener the listener to remove
     */
    public void removeListener(VariantListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Adds a
     */
    private void addFilter() {
        VCFFilterPane filterPane = new VCFFilterPane(new ArrayList(infos));
        filterPane.setOnUpdate(e -> filter());
        filterPane.setOnDelete(e -> {
            filtersPane.getChildren().remove(filterPane);
            filter();
        });
        filtersPane.getChildren().add(filterPane);

    }

    /**
     * Runs across the variants filtering them.
     */
    private void filter() {
        table.getItems().clear();
        lines.set(0);
        try (BufferedReader in = new BufferedReader(new FileReader(vcfFile))) {
            in.lines().forEachOrdered(line -> {
                if (!line.startsWith("#")) {
                    final Variant v = new Variant(line);
                    if (filter(v)) {
                        table.getItems().add(v);
                        lines.incrementAndGet();
                    }
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(VCFReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateInfo();
    }

    /**
     *
     * @param variant
     * @return true if variant pass all filters.
     */
    private boolean filter(Variant variant) {
        boolean pass = true;
        for (Node pane : filtersPane.getChildren()) {
            VCFFilter f = ((VCFFilterPane) pane).getFilter();
            if (f.getConnector() != null && f.getField() != null && !f.filter(variant)) {
                pass = false;
                break;
            }
        }
        return pass;
    }

    private void updateInfo() {
        final double percentage = lines.get() * 100.0 / totalLines.get();
        infoLabel.setText(String.format("%,d / %,d (%.2f%%)", lines.get(), totalLines.get(),
                percentage));

    }

    private void addHeader(String line) {
        headers.add(line);
        if (line.startsWith("##INFO=<")) {
            // ##INFO=<ID=DP,...
            String[] row = line.substring(8).split(",");
            // ID=DP
            String name = row[0].split("=")[1];
            infos.add(name);
        }
    }

    public void saveAs() {
        File output = FileManager.saveFile("Select output file", vcfFile.getParentFile(),
                vcfFile.getName(), FileManager.VCF_FILTER, FileManager.TSV_FILTER);
        if (output != null) {
            if (output.getName().endsWith(".vcf")) {
                exportTo(output);
            } else {
                exportToTSV(output);
            }
//            File json = new File(output.getAbsolutePath().replace(".vcf", ".json"));
            // Too big files, cause header repeats for every variant
            //VCF2JSON.vcf2Json(vcfFile, json);
        }
    }

    private void exportTo(File output) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output)))) {
            headers.forEach(writer::println);
            table.getItems().forEach(writer::println);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void getLfsInfo() {
        injectLFSHeasder();
        // Store filters status and deactivate all of them
        boolean[] enabled = new boolean[filtersPane.getChildren().size()];
        for (int i = 0; i < enabled.length; i++) {
            VCFFilterPane pane = (VCFFilterPane) filtersPane.getChildren().get(i);
            enabled[i] = pane.getFilter().isEnabled();
            pane.getFilter().setEnabled(false);
        }
        // Load all variants
        filter();
        // Apply LFS.
        table.getItems().parallelStream().forEach(LFS::addLFS);
        // Export to file
        exportTo(vcfFile);
        // Restore filters
        for (int i = 0; i < enabled.length; i++) {
            VCFFilterPane pane = (VCFFilterPane) filtersPane.getChildren().get(i);
            pane.getFilter().setEnabled(enabled[i]);
        }
        filter();
        lfs.setVisible(false);
    }

    private void viewHeader() {
        TextArea area = new TextArea();
        headers.forEach(header -> area.appendText(header + "\n"));
        area.setEditable(false);
        Scene scene = new Scene(area);
        Stage stage = new Stage();
        stage.setTitle(vcfFile.getName());
        stage.centerOnScreen();
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Inserts LFS header alphabetically.
     */
    private void injectLFSHeasder() {
        // Insert LFS header
        final String lfsInfo = "##INFO=<ID=LFS,Number=1,Type=Integer,Description=\"Low frequency codon substitution\">";
        if (!headers.contains(lfsInfo)) {
            Set<String> copy = new LinkedHashSet();
            boolean inserted = false;
            for (String h : headers) {
                if (!inserted && h.startsWith("##INFO=<") && h.compareTo(lfsInfo) > 0) {
                    copy.add(lfsInfo);
                }
                copy.add(h);
            }
            headers = copy;
            infos.add("LFS");
            vcfHeader.getInfos().add(vcfHeader.parseInfo(lfsInfo));
        }
    }

    private void exportToTSV(File output) {
        final int length = infos.size() + 7;
        final String[] head = new String[length];
        head[0] = "CHROM";
        head[1] = "POS";
        head[2] = "ID";
        head[3] = "REF";
        head[4] = "ALT";
        head[5] = "QUAL";
        head[6] = "FILTER";
        int i = 7;
        for (String info : infos) {
            head[i++] = info;
        }
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output)))) {
            writer.println(OS.asString("\t", head));
            table.getItems().forEach(var -> {
                String[] line = new String[length];
                line[0] = var.getChrom();
                line[1] = String.valueOf(var.getPos());
                line[2] = var.getId();
                line[3] = var.getRef();
                line[4] = var.getAlt();
                line[5] = String.format("%.4f", var.getQual());
                line[6] = var.getFilter();
                for (int k = 7; k < line.length; k++) {
                    line[k] = ".";
                }
                var.getInfos().forEach((key, value) -> {
                    int pos = -1;
                    int j = 0;
                    for (String info : infos) {
                        if (info.equals(key)) {
                            pos = j;
                            break;
                        }
                        j++;
                    }
                    if (pos != -1) {
                        line[pos + 7] = (value == null) ? "yes" : value;
                    }
                });
                writer.println(OS.asString("\t", line));
            });
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

}
