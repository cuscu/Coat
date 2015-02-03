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
package coat.tsv;

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
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

/**
 * A pane that shows a Tab Separated Values file.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class TSVReader extends SplitPane {

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

    private final File file;
    private String[] headers;

    private AtomicInteger totalLines = new AtomicInteger();
    private AtomicInteger currentLines = new AtomicInteger();

    /**
     * Creates a new TSVReader.
     *
     * @param file the file to read
     */
    public TSVReader(File file) {
        this.file = file;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TSVReader.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
        }
        loadFile();
    }

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

}
