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

import coat.CoatView;
import coat.graphic.IndexCell;
import coat.graphic.NaturalCell;
import coat.graphic.SizableImage;
import coat.reader.Reader;
import coat.utils.FileManager;
import coat.utils.OS;
import coat.vep.EnsemblAPI;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VcfFileReader extends Reader {

    /**
     * The variants' table.
     */
    @FXML
    private TableView<Variant> table;
    /**
     * Where filters' views live.
     */
    @FXML
    private VBox filtersPane;
    /**
     * Button to add a new filter.
     */
    @FXML
    private Button addFilter;
    /**
     * The label that indicates the number of lines and the percentage filtered.
     */
    @FXML
    private Label infoLabel;
    /**
     * The chromosome indicator/selector.
     */
    @FXML
    private ComboBox<String> chromosome;
    /**
     * The position indicator/selector.
     */
    @FXML
    private TextField pos;
    /**
     * Total number of lines.
     */
    private final AtomicInteger totalLines = new AtomicInteger();
    /**
     * Column that indicates the line number in the variants' table.
     */
    @FXML
    private TableColumn<Variant, String> lineNumber;
    /**
     * Chromosome column of the variants' table.
     */
    @FXML
    private TableColumn<Variant, String> chrom;
    /**
     * Position column of the variants' table.
     */
    @FXML
    private TableColumn<Variant, String> position;
    /**
     * Ref->Alt column of the variants' table.
     */
    @FXML
    private TableColumn<Variant, String> variant;
    /**
     * ID column of the variants' table.
     */
    @FXML
    private TableColumn<Variant, String> rsId;
    /**
     * Quality column in the variants' table.
     */
    @FXML
    private TableColumn<Variant, String> qual;
    /**
     * Filters column in the variants' table.
     */
    @FXML
    private TableColumn<Variant, String> filter;
    /**
     * The table with the info values.
     */
    @FXML
    private TableView<Info> infoTable;
    /**
     * Name column in info table.
     */
    @FXML
    private TableColumn<Info, String> name;
    /**
     * Value column in info table.
     */
    @FXML
    private TableColumn<Info, String> value;
    /**
     * Description column in info table.
     */
    @FXML
    private TableColumn<Info, String> description;
    /**
     * List of info ids to use in filters.
     */
    private final Set<String> infos = new TreeSet();
    /**
     * Contains the headers metaprocessed.
     */
    private VCFHeader vcfHeader;

    private final List<Button> actions = new LinkedList();

    /**
     * Initializes the controller class.
     */
    public void initialize() {
        initializeVariantsTable();
        initializeInfoTable();
        initializeButtons();

        addFilter.setGraphic(new SizableImage("coat/img/new.png", SizableImage.SMALL_SIZE));
        addFilter.setOnAction(e -> addFilter());

        // Avoid non digit character
        pos.setOnKeyTyped(event -> {
            if (!Character.isDigit(event.getCharacter().charAt(0))) {
                event.consume();
            }
        });
        pos.setOnAction(event -> selectVariant());

        chromosome.setOnAction(event -> selectVariant());
        chromosome.setEditable(true);

    }

    private void initializeVariantsTable() {
        table.setSortPolicy(view -> false);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setEditable(false);

        // Values
        chrom.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getChrom()));
        position.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPos() + ""));
        variant.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getRef()
                + "->" + param.getValue().getAlt()));
        rsId.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getId()));
        filter.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilter()));
        qual.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getQual() + ""));

        // Factories
        lineNumber.setCellFactory(param -> new IndexCell());
        chrom.setCellFactory(column -> new NaturalCell());
        position.setCellFactory(column -> new NaturalCell());
        variant.setCellFactory(column -> new NaturalCell());
        rsId.setCellFactory(column -> new NaturalCell());
        filter.setCellFactory(column -> new NaturalCell());
        qual.setCellFactory(column -> new NaturalCell());
        table.getSelectionModel().selectedItemProperty().addListener(e -> variantChanged());
    }

    private void initializeInfoTable() {
        infoTable.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        infoTable.setEditable(false);
        infoTable.setSelectionModel(null);
        name.setCellValueFactory(new PropertyValueFactory("name"));
        value.setCellValueFactory(new PropertyValueFactory("value"));
        description.setCellValueFactory(new PropertyValueFactory("description"));
        name.setCellFactory(param -> new NaturalCell());
        value.setCellFactory(param -> new NaturalCell());
        description.setCellFactory(param -> new NaturalCell());
    }

    /**
     * Reads the source file and populates the table. Everything is cleared before doing this:
     * filters, table content, headers. It is like resetting.
     */
    private void loadFile() {
        clearView();
        readFile();
        updateInfo();
        table.getSelectionModel().select(0);
    }

    /**
     * Clears headers, table and filters.
     */
    private void clearView() {
        // Clear header
        vcfHeader = new VCFHeader(file);
        // Clear table
        table.getItems().clear();
        totalLines.set(0);
        // Clear filters
        infos.clear();
        filtersPane.getChildren().clear();
    }

    /**
     * Reads the file and populate the table.
     */
    private void readFile() {
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            in.lines().forEachOrdered(line -> {
                if (line.startsWith("#")) {
                    vcfHeader.add(line);
                } else {
                    table.getItems().add(new Variant(line));
                    totalLines.incrementAndGet();
                }
            });
            // Fulfill infos
            vcfHeader.getInfos().forEach(map -> infos.add(map.get("ID")));
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns the opened file.
     *
     * @return the opened file
     */
    @Override
    public File getFile() {
        return file;
    }

    /**
     * Set the source VCF file.
     *
     * @param vcfFile new file to use with the VCFReader
     */
    public void setVcfFile(File vcfFile) {
        this.file = vcfFile;
        loadFile();

    }

    /**
     * This is called when a variant is selected in the table
     */
    private void variantChanged() {
        Variant v = table.getSelectionModel().getSelectedItem();
        infoTable.getItems().clear();
        if (v != null) {
            pos.setText(v.getPos() + "");
            // Silently change chromosome, avoiding it to fire a selection
            chromosome.setOnAction(null);
            chromosome.setValue(v.getChrom());
            chromosome.setOnAction(event -> selectVariant());

            // Update info table
            v.getInfos().forEach((key, val) -> {
                // Search the description in headers
                String desc = "";
                for (Map<String, String> property : vcfHeader.getInfos()) {
                    if (property.get("ID").equals(key)) {
                        desc = property.getOrDefault("Description", "");
                        break;
                    }
                }
                // For flag properties
                if (val == null) {
                    val = "yes";
                }
                infoTable.getItems().add(new Info(key, val, desc));
            });
        }
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
        // Kepp the las selected variant
        String chrm = chromosome.getValue();
        String p = pos.getText();
        // Clear table
        table.getItems().clear();
        // Read file again, but do not process headers, only the variants
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            in.lines().forEachOrdered(line -> {
                if (!line.startsWith("#")) {
                    final Variant v = new Variant(line);
                    if (filter(v)) {
                        table.getItems().add(v);
                    }
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        updateInfo();
        // Reselect last selected variat
        chromosome.setValue(chrm);
        pos.setText(p);
        selectVariant();
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

    /**
     * Updates the number of variants in the table (in infoLabel) and the chromosome selector.
     */
    private void updateInfo() {
        int lines = table.getItems().size();
        final double percentage = lines * 100.0 / totalLines.get();
        infoLabel.setText(String.format("%,d / %,d (%.2f%%)", lines, totalLines.get(),
                percentage));
        Set<String> chrs = new LinkedHashSet();
        table.getItems().forEach(v -> chrs.add(v.getChrom()));
        chromosome.setItems(FXCollections.observableArrayList(chrs));

    }

    /**
     * Ask user to select a save File (VCF or TSV) and exports variants in the current table.
     */
    @Override
    public void saveAs() {
        File output = FileManager.saveFile("Select output file", file.getParentFile(),
                file.getName(), FileManager.VCF_FILTER, FileManager.TSV_FILTER);
        if (output != null) {
            if (output.getName().endsWith(".vcf")) {
                exportToVCF(output);
            } else {
                exportToTSV(output);
            }
//            File json = new File(output.getAbsolutePath().replace(".vcf", ".json"));
            // Too big files, cause header repeats for every variant
            //VCF2JSON.vcf2Json(file, json);
        }
    }

    /**
     * Save current variants into output with VCF format.
     *
     * @param output the file to write variants
     */
    private void exportToVCF(File output) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output)))) {
            vcfHeader.getHeaders().forEach(writer::println);
            table.getItems().forEach(writer::println);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addLFS() {
        injectLFSHeader();
        table.getItems().parallelStream().forEach(LFS::addLFS);
    }

    public void viewHeaders() {
        TextArea area = new TextArea();
        vcfHeader.getHeaders().forEach(header -> area.appendText(header + "\n"));
        area.setEditable(false);
        area.setWrapText(true);
        area.home();
        Scene scene = new Scene(area);
        Stage stage = new Stage();
        stage.setWidth(600);
        stage.setHeight(600);
        stage.setTitle(file.getName());
        stage.centerOnScreen();
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Inserts LFS header alphabetically.
     */
    private void injectLFSHeader() {
        // Let's check if LFS header is already stored
        for (Map<String, String> map : vcfHeader.getInfos()) {
            if (map.get("ID").equals("LFS")) {
                return;
            }
        }
        // Insert LFS header
        final String lfsInfo = "##INFO=<ID=LFS,Number=1,Type=Integer,Description=\"Low frequency codon substitution\">";
        vcfHeader.add(lfsInfo);
        infos.add("LFS");
    }

    /**
     * Exports the current table to a TSV file.
     *
     * @param output the destination file
     */
    private void exportToTSV(File output) {
        final int length = infos.size() + 7;
        final String[] head = new String[length];
        // Fixed columns
        head[0] = "CHROM";
        head[1] = "POS";
        head[2] = "ID";
        head[3] = "REF";
        head[4] = "ALT";
        head[5] = "QUAL";
        head[6] = "FILTER";
        int i = 7;
        // INFO columns
        for (String info : infos) {
            head[i++] = info;
        }
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output)))) {
            writer.println(OS.asString("\t", head));
            table.getItems().forEach(var -> {
                String[] line = new String[length];
                // Fixed columns
                line[0] = var.getChrom();
                line[1] = String.valueOf(var.getPos());
                line[2] = var.getId();
                line[3] = var.getRef();
                line[4] = var.getAlt();
                line[5] = String.format("%.4f", var.getQual());
                line[6] = var.getFilter();
                // Empty values are represented with a dot "."
                for (int k = 7; k < line.length; k++) {
                    line[k] = ".";
                }
                var.getInfos().forEach((key, val) -> {
                    int index = -1;
                    // Iterate infos to locate the position of key
                    int j = 0;
                    for (String info : infos) {
                        if (info.equals(key)) {
                            index = j;
                            break;
                        }
                        j++;
                    }
                    if (index != -1) {
                        // val == null when key is located, but has no value (a flag)
                        line[index + 7] = (val == null) ? "yes" : val;
                    }
                });
                writer.println(OS.asString("\t", line));
            });
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Focus on the variant indicated by chromosome and pos, only if they contain not null and not
     * empty values. If there is no variant in the given position, it focus on the next variant.
     */
    private void selectVariant() {
        if (chromosome.getValue() != null && !pos.getText().isEmpty()) {
            String c = chromosome.getValue();
            int i = Integer.valueOf(pos.getText());
            for (Variant v : table.getItems()) {
                if (v.getChrom().equals(c) && v.getPos() >= i) {
                    table.getSelectionModel().select(v);
                    table.scrollTo(v);
                    break;
                }
            }
        }
    }

    @Override
    public List<Button> getActions() {
        return actions;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
        loadFile();
    }

    private void initializeButtons() {
        // View headers button
        Button viewheaders = new Button(OS.getResources().getString("headers"));
        viewheaders.setTooltip(new Tooltip(OS.getResources().getString("view.headers")));
        viewheaders.setOnAction(event -> viewHeaders());
        viewheaders.setGraphic(new SizableImage("coat/img/headers.png", SizableImage.MEDIUM_SIZE));
        viewheaders.getStyleClass().add("graphic-button");
        viewheaders.setContentDisplay(ContentDisplay.TOP);
        actions.add(viewheaders);

        // Add lfs button
        Button lfs = new Button("LFS");
        lfs.setTooltip(new Tooltip(OS.getResources().getString("add.lfs")));
        lfs.setOnAction(event -> addLFS());
        lfs.setGraphic(new SizableImage("coat/img/lfs.png", SizableImage.MEDIUM_SIZE));
        lfs.getStyleClass().add("graphic-button");
        lfs.setContentDisplay(ContentDisplay.TOP);
        actions.add(lfs);

        // Add vep button
        Button vep = new Button("VEP");
        vep.setTooltip(new Tooltip("Add info from Ensembl VEP"));
        vep.setGraphic(new SizableImage("coat/img/vep_logo.png", SizableImage.MEDIUM_SIZE));
        vep.setOnAction(e -> addVep());
        vep.getStyleClass().add("graphic-button");
        vep.setContentDisplay(ContentDisplay.TOP);
        actions.add(vep);
    }

    @Override
    public String getActionsName() {
        return "VCF";
    }

    private void addVep() {
        injectVEPHeaders();
        Task task = EnsemblAPI.vepAnnotator(table.getItems());
        task.setOnSucceeded(event
                -> CoatView.printMessage(table.getItems().size()
                        + " variants annotated", "success"));
        task.setOnFailed(event -> CoatView.printMessage("something wrong", "error"));
        task.messageProperty().addListener((obs, old, current)
                -> CoatView.printMessage(current, "info"));
        CoatView.printMessage("Annotating variants...", "info");
        new Thread(task).start();
    }

    private void injectVEPHeaders() {
        // Add headers to vcfHeader
        Arrays.stream(EnsemblAPI.headers).forEach(vcfHeader::add);
        // Update infos
        vcfHeader.getInfos().forEach(inf -> infos.add(inf.get("ID")));
    }

}
