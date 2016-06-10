/*
 * Copyright (c) UICHUIMI 2016
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

package coat.view.poirot.phenotype;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import poirot.core.Pearl;
import poirot.core.PearlGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by uichuimi on 17/05/16.
 */
public class PhenotypeListController {
    private final static Image LENS = new Image("coat/img/black/search.png");
    private final static Image CLEAR = new Image("coat/img/black/cancel.png");


    @FXML
    private ComboBox<SortBy> sortBy;
    @FXML
    private ListView phenotypeList;
    @FXML
    private TableColumn<Phenotype, String> name;
    @FXML
    private TableColumn<Phenotype, String> score;
    @FXML
    private TableColumn<Phenotype, Boolean> select;
    @FXML
    private ImageView searchImage;
    @FXML
    private TextField searchBox;
    @FXML
    private TableView<Phenotype> phenotypeTable;
    @FXML
    private ToggleGroup group;
    private PearlGraph graph;

    private List<Phenotype> phenotypes = new ArrayList<>();
    private ObservableList<Pearl> selectedPhenotypes = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        searchBox.textProperty().addListener((observable, oldValue, newValue) -> {
            searchImage.setImage(newValue.isEmpty() ? LENS : CLEAR);
            filter();
        });
        searchImage.setOnMouseClicked(event -> searchBox.setText(""));
        searchImage.setFitWidth(16);
        searchImage.setImage(LENS);
        name.setCellValueFactory(param -> new SimpleObjectProperty(param.getValue().pearl.getProperties().get("name")));
        score.setCellValueFactory(param -> new SimpleObjectProperty<>(String.format("%.3f", param.getValue().pearl.getScore())));
        select.setCellValueFactory(param -> param.getValue().selected);
        select.setCellFactory(param -> new CheckBoxTableCell<>());
        group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> filter());
        sortBy.getItems().setAll(SortBy.values());
    }

    private void filter() {
        final RadioButton toggle = (RadioButton) group.getSelectedToggle();
        final String searchText = searchBox.getText().toLowerCase();
        phenotypeTable.getItems().setAll(getFilterStream(toggle.getText())
                .filter(phenotype -> phenotype.pearl.getProperties().get("name").toString().toLowerCase().contains(searchText))
                .collect(Collectors.toList()));
        phenotypeTable.sort();
    }

    private Stream<Phenotype> getFilterStream(String selectedType) {
        if (selectedType.equals("All")) return phenotypes.stream();
        if (selectedType.equals("Selected"))
            return phenotypes.stream().filter(phenotype -> phenotype.selected.getValue());
        if (selectedType.equals("Diseases"))
            return phenotypes.stream().filter(phenotype -> phenotype.pearl.getType() == Pearl.Type.DISEASE);
        if (selectedType.equals("Tissues"))
            return phenotypes.stream().filter(phenotype -> phenotype.pearl.getType() == Pearl.Type.TISSUE);
        return Stream.empty();
    }

    private void updateQuantities() {
        selectedPhenotypes.setAll(phenotypes.stream()
                .filter(phenotype -> phenotype.selected.getValue())
                .map(phenotype -> phenotype.pearl)
                .collect(Collectors.toList()));
    }

    public void setPearlGraph(PearlGraph graph) {
        this.graph = graph;
        graph.getPearls(Pearl.Type.DISEASE).stream()
                .filter(pearl -> pearl.getProperties().containsKey("name")).forEach(pearl -> phenotypes.add(new Phenotype(pearl, false)));
        graph.getPearls(Pearl.Type.TISSUE).stream()
                .filter(pearl -> pearl.getProperties().containsKey("name")).forEach(pearl -> phenotypes.add(new Phenotype(pearl, false)));
        filter();
    }

    public ObservableList<Pearl> selectedPhenotypes() {
        return selectedPhenotypes;
    }

    public ObservableList<Phenotype> phenotypes() {
        return phenotypeTable.getItems();
    }

    public void sort() {
        switch (sortBy.getValue()) {
            case NAME_ASCENDING:
                Collections.sort(phenotypeTable.getItems(), (o1, o2) -> {
                    String p1 = (String) o1.pearl.getProperties().get("name");
                    String p2 = (String) o2.pearl.getProperties().get("name");
                    return p1.compareTo(p2);
                });
                break;
            case NAME_DESCENDING:
                Collections.sort(phenotypeTable.getItems(), (o1, o2) -> {
                    String p1 = (String) o1.pearl.getProperties().get("name");
                    String p2 = (String) o2.pearl.getProperties().get("name");
                    return p1.compareTo(p2);
                });
                break;
            case SCORE_ASCENDING:
                Collections.sort(phenotypeTable.getItems(),
                        (o1, o2) -> Double.compare(o1.pearl.getScore(), o2.pearl.getScore()));
                break;
            case SCORE_DESCENDING:
                Collections.sort(phenotypeTable.getItems(),
                        (o1, o2) -> Double.compare(o2.pearl.getScore(), o1.pearl.getScore()));
                break;
        }
//        phenotypeTable.sort();
    }

    private enum SortBy {
        NAME_ASCENDING {
            @Override
            public String toString() {
                return "Name (A to Z)";
            }
        },
        NAME_DESCENDING {
            @Override
            public String toString() {
                return "Name (Z to A)";
            }
        },
        SCORE_ASCENDING {
            @Override
            public String toString() {
                return "Score (0 to 1)";
            }
        },
        SCORE_DESCENDING {
            @Override
            public String toString() {
                return "Score (1 to 0)";
            }
        }
    }

    private class Phenotype {
        private Pearl pearl;
        private Property<Boolean> selected = new SimpleBooleanProperty(false);

        private Phenotype(Pearl pearl, boolean selected) {
            this.pearl = pearl;
            this.selected.setValue(selected);
            this.selected.addListener((observable, oldValue, newValue) -> updateQuantities());
        }
    }
}
