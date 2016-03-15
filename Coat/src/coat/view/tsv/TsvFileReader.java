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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coat.view.tsv;

import coat.core.reader.Reader;
import coat.utils.FileManager;
import coat.utils.OS;
import coat.view.graphic.IndexCell;
import coat.view.graphic.NaturalCell;
import coat.view.graphic.SizableImage;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author UICHUIMI
 */
public class TsvFileReader extends VBox implements Reader {

    private final File file;
    private final TableView<String[]> table = new TableView<>();
    private final VBox filtersPane = new VBox();
    private final Button addFilter = new Button("Filter", new SizableImage("coat/img/black/new.png", SizableImage.SMALL_SIZE));
    private final Label infoLabel = new Label();
    private final VBox bottomVBox = new VBox(5, infoLabel, filtersPane, addFilter);
    private final SplitPane mainPane = new SplitPane(table, bottomVBox);

    private final AtomicInteger totalLines = new AtomicInteger();
    private final AtomicInteger currentLines = new AtomicInteger();
    private Property<String> titleProperty = new SimpleStringProperty();

    private String[] headers = null;

    public TsvFileReader(File file) {
        this.file = file;
        titleProperty.setValue(file.getName());
        mainPane.setOrientation(Orientation.VERTICAL);
        mainPane.setDividerPositions(0.75);
        SplitPane.setResizableWithParent(bottomVBox, false);
        getChildren().setAll(mainPane);
        VBox.setVgrow(mainPane, Priority.ALWAYS);
        initialize();
        loadFile();
    }

    private void initialize() {
        addFilter.setOnAction(event -> {
            final TSVFilterPane filterPane = new TSVFilterPane(Arrays.asList(headers));
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
            // skip # lines
            String line = reader.readLine();
            while (line.startsWith("#")) {
                if (line.startsWith("#chrom")){
                    headers = line.substring(1).split("\t");
                    break;
                }
                line = reader.readLine();
            }
            if (headers == null) headers = line.split("\t");
            generateColumns();
            reader.lines().forEachOrdered(line1 -> table.getItems().add(line1.split("\t")));
            totalLines.set(table.getItems().size());
            currentLines.set(totalLines.get());
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
        }
        setInfo();
    }

    private void generateColumns() {
        TableColumn<String[], String> in = new TableColumn<>();
        in.setCellFactory(column -> new IndexCell());
        table.getColumns().add(in);
        for (int i = 0; i < headers.length; i++) {
            final int index = i;
            TableColumn<String[], String> tc = new TableColumn<>(headers[i]);
            tc.setCellValueFactory(param -> new SimpleStringProperty(param.getValue()[index]));
            tc.setCellFactory(column -> new NaturalCell());
            table.getColumns().add(tc);
        }
        table.getColumns().get(1).getStyleClass().add("first-column");
    }

    private void filter() {
        table.getItems().clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();
            reader.lines().forEachOrdered(line -> {
                String[] row = line.split("\t");
                boolean accepted = true;
                for (Node node : filtersPane.getChildren()) {
                    final TSVFilterPane pane = (TSVFilterPane) node;
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
        for (int i = 0; i < uniques.length; i++) uniques[i] = new TreeSet<>();
        table.getItems().stream().forEach(line -> {
            for (int i = 0; i < line.length; i++) uniques[i].add(line[i]);
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
//            table.getColumns().get(i + 1).setText(columnNames[i] + "\n" + uniques[i].size());
        }
    }

    @Override
    public Property<String> titleProperty() {
        return titleProperty;
    }

    /**
     * Ask user to open a file.
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
     * Exports columnNames and table.getItems to the given file.
     *
     * @param output the output file
     */
    private void exportTo(File output) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output)))) {
            writer.println(OS.asString(headers));
            table.getItems().forEach(line -> writer.println(OS.asString(line)));
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
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
