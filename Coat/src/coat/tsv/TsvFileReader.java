/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coat.tsv;

import coat.graphic.IndexCell;
import coat.graphic.NaturalCell;
import coat.graphic.SizableImage;
import coat.reader.Reader;
import coat.utils.FileManager;
import coat.utils.OS;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

/**
 *
 * @author UICHUIMI
 */
public class TsvFileReader extends Reader {

    @FXML
    private TableView<String[]> table;
    @FXML
    private VBox filtersPane;
    @FXML
    private Button addFilter;
    @FXML
    private Label infoLabel;
    @FXML
    private Button export;

    private String[] headers;

    private final AtomicInteger totalLines = new AtomicInteger();
    private final AtomicInteger currentLines = new AtomicInteger();
    private final static List<Button> actions = new LinkedList();

    @FXML
    private void initialize() {
        addFilter.setGraphic(new SizableImage("coat/img/new.png", SizableImage.SMALL_SIZE));
        export.setGraphic(new SizableImage("coat/img/save.png", SizableImage.SMALL_SIZE));
        //table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        addFilter.setOnAction(event -> {
            TSVFilterPane filterPane = new TSVFilterPane(Arrays.asList(headers));
            filterPane.setOnAccept(e -> filter());
            filterPane.setOnDelete(e -> {
                filtersPane.getChildren().remove(filterPane);
                filter();
            });
            filtersPane.getChildren().add(filterPane);
        });
    }

    private void loadFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            headers = reader.readLine().split("\t");
            generateColumns();
            reader.lines().forEachOrdered(line -> table.getItems().add(line.split("\t")));
            totalLines.set(table.getItems().size());
            currentLines.set(totalLines.get());
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
        }
        setInfo();
    }

    private void generateColumns() {
        TableColumn<String[], String> in = new TableColumn();
        in.setCellFactory(column -> new IndexCell());
        table.getColumns().add(in);
        for (int i = 0; i < headers.length; i++) {
            final int index = i;
            TableColumn<String[], String> tc = new TableColumn(headers[i]);
            tc.setCellValueFactory(param -> new SimpleStringProperty(param.getValue()[index]));
            tc.setCellFactory(column -> new NaturalCell());
            table.getColumns().add(tc);
        }
    }

    private void filter() {
        table.getItems().clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();
            reader.lines().forEachOrdered(line -> {
                String[] row = line.split("\t");
                boolean accepted = true;
                for (Node node : filtersPane.getChildren()) {
                    TSVFilterPane pane = (TSVFilterPane) node;
                    if (!pane.getFilter().filter(row)) {
                        accepted = false;
                        break;
                    }
                }
                if (accepted) {
                    table.getItems().add(row);
                }
            });
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
        }
        currentLines.set(table.getItems().size());
        setInfo();
    }

    private void setInfo() {
        double percentage = 100.0 * currentLines.get() / totalLines.get();
        infoLabel.setText(String.format("%,d / %,d (%.2f%%)", currentLines.get(), totalLines.get(),
                percentage));
        Set<String>[] uniques = new Set[headers.length];
        for (int i = 0; i < uniques.length; i++) {
            uniques[i] = new TreeSet<>();
        }
        table.getItems().stream().forEach(line -> {
            for (int i = 0; i < line.length; i++) {
                uniques[i].add(line[i]);
            }
        });
        for (int i = 0; i < headers.length; i++) {
            // Skip index column
            VBox box = (VBox) table.getColumns().get(i + 1).getGraphic();
            if (box == null) {
                Label name = new Label(table.getColumns().get(i + 1).getText());
                Label size = new Label(uniques[i].size() + "");
                box = new VBox(name, size);
                box.setAlignment(Pos.CENTER);
                table.getColumns().get(i + 1).setGraphic(box);
                table.getColumns().get(i + 1).setText(null);
            }
            Label size = (Label) box.getChildren().get(1);
            size.setText(uniques[i].size() + "");
//            table.getColumns().get(i + 1).setText(headers[i] + "\n" + uniques[i].size());
        }
    }

    /**
     * Ask user to open a file.
     *
     */
    @Override
    public void saveAs() {
        File output = FileManager.saveFile("Select output file", file.getParentFile(),
                file.getName(), FileManager.ALL_FILTER);
        if (output != null) {
            exportTo(output);
        }
    }

    /**
     * Exports headers and table.getItems to the given file.
     *
     * @param output the output file
     */
    private void exportTo(File output) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output)))) {
            writer.println(OS.asString("\t", headers));
            table.getItems().forEach(line -> writer.println(OS.asString("\t", line)));
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setFile(File file) {
        this.file = file;
        loadFile();
    }

    @Override
    public List<Button> getActions() {
        return null;
    }

    @Override
    public String getActionsName() {
        return "TSV";
    }

}
