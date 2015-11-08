/******************************************************************************
 * Copyright (C) 2015 UICHUIMI                                                *
 * *
 * This program is free software: you can redistribute it and/or modify it    *
 * under the terms of the GNU General Public License as published by the      *
 * Free Software Foundation, either version 3 of the License, or (at your     *
 * option) any later version.                                                 *
 * *
 * This program is distributed in the hope that it will be useful, but        *
 * WITHOUT ANY WARRANTY; without even the implied warranty of                 *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 * *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 ******************************************************************************/

package coat.view.poirot;

import coat.core.poirot.Pearl;
import coat.utils.OS;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Pane to select input phenotypes. There are two controls: a list with the selected phenotypes and an autoFillComboBox
 * to add new phenotypes. Phenotypes must be loaded using <code>setPhenotypes</code>
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
class PhenotypeSelector extends VBox {

    private final static String OPTION_ALL = OS.getString("all").toUpperCase();
    private final static String OPTION_SELECTED = OS.getString("selected.plural").toUpperCase();
    private final static String OPTION_EXPRESSION = OS.getString("expression").toUpperCase();
    private final static String OPTION_DISEASE = OS.getString("disease").toUpperCase();

    private final static String[] menuOptions = {
            OPTION_ALL, OPTION_SELECTED, OPTION_DISEASE, OPTION_EXPRESSION
    };

    private final ListView<String> leftMenu = new ListView<>();
    private final TextField searchBox = new TextField();
    private final ImageView searchIcon = new ImageView("coat/img/search.png");


    private final TableView<Phenotype> phenotypeTable = new TableView<>();
    private List<Phenotype> phenotypes = new ArrayList<>();

    public PhenotypeSelector() {
        initializeLeftMenu();
        initializePhenotypeTable();
        final HBox phenotypeMenu = new HBox(leftMenu, phenotypeTable);
        final StackPane searchPane = getSearchPane();
        setSpacing(5);
        VBox.setVgrow(phenotypeMenu, Priority.ALWAYS);
        getChildren().addAll(searchPane, phenotypeMenu);
    }

    private void initializeLeftMenu() {
        leftMenu.getItems().addAll(menuOptions);
        leftMenu.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selected) -> filter());
        leftMenu.getStyleClass().add("custom-list");
        leftMenu.getSelectionModel().select(OPTION_ALL);
    }

    private StackPane getSearchPane() {
        searchBox.textProperty().addListener((observable, oldValue, newValue) -> filter());
        searchBox.getStyleClass().add("phenotype-search-box");
        searchBox.setPromptText(OS.getString("search"));
        searchBox.setAlignment(Pos.CENTER);
        StackPane searchPane = new StackPane(searchBox, searchIcon);
        StackPane.setAlignment(searchIcon, Pos.CENTER_RIGHT);
        return searchPane;
    }

    private void initializePhenotypeTable() {
        final TableColumn<Phenotype, Boolean> selected = getSelectedColumn();
        final TableColumn<Phenotype, String> name = getNameColumn();
        final TableColumn<Phenotype, Double> score = getScoreColumn();
        phenotypeTable.getColumns().addAll(selected, score, name);
        phenotypeTable.setEditable(true);
        phenotypeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        HBox.setHgrow(phenotypeTable, Priority.ALWAYS);
    }

    private TableColumn<Phenotype, String> getNameColumn() {
        final TableColumn<Phenotype, String> name = new TableColumn<>(OS.getString("name"));
        name.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().pearl.getName()));
        name.setPrefWidth(400);
        return name;
    }

    private TableColumn<Phenotype, Boolean> getSelectedColumn() {
        final TableColumn<Phenotype, Boolean> selected = new TableColumn<>(OS.getString("selected.singular"));
        selected.setCellValueFactory(param -> param.getValue().selected);
        selected.setCellFactory(param -> new CheckBoxTableCell<>());
        selected.setPrefWidth(50);
        return selected;
    }

    private TableColumn<Phenotype, Double> getScoreColumn() {
        final TableColumn<Phenotype, Double> score = new TableColumn<>(OS.getString("score"));
        score.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().pearl.getScore()));
        score.setCellFactory(param -> new TextFieldTableCell<>(new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                return String.format("%.3f", object);
            }

            @Override
            public Double fromString(String string) {
                return Double.valueOf(string);
            }
        }));
        score.setEditable(false);
        return score;
    }

    private void filter() {
        final String selectedType = leftMenu.getSelectionModel().getSelectedItem();
        final String searchValue = searchBox.getText().toLowerCase();
        final List<Phenotype> list = getFilteredPhenotypes(selectedType, searchValue);
        phenotypeTable.getItems().setAll(list);
        phenotypeTable.sort();
    }

    private List<Phenotype> getFilteredPhenotypes(String selectedType, String searchValue) {
        if (selectedType.equals(OPTION_ALL)) {
            return phenotypes.stream()
                    .filter(phenotype -> phenotype.pearl.getName().toLowerCase().contains(searchValue))
                    .collect(Collectors.toList());
        } else if (selectedType.equals(OPTION_SELECTED)) {
            return phenotypes.stream()
                    .filter(phenotype -> phenotype.selected.getValue())
                    .collect(Collectors.toList());
        } else if (selectedType.equals(OPTION_DISEASE)) {
            return phenotypes.stream()
                    .filter(phenotype -> phenotype.pearl.getType() == Pearl.Type.DISEASE)
                    .filter(phenotype -> phenotype.pearl.getName().toLowerCase().contains(searchValue))
                    .collect(Collectors.toList());
        } else if (selectedType.equals(OPTION_EXPRESSION)) {
            return phenotypes.stream()
                    .filter(phenotype -> phenotype.pearl.getType() == Pearl.Type.EXPRESSION)
                    .filter(phenotype -> phenotype.pearl.getName().toLowerCase().contains(searchValue))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public List<Pearl> getSelectedPhenotypes() {
        return phenotypeTable.getItems().stream()
                .filter(phenotype -> phenotype.selected.getValue())
                .map(phenotype -> phenotype.pearl)
                .collect(Collectors.toList());
    }

    public void setPhenotypes(List<Pearl> list) {
        phenotypes = list.stream().map(pearl -> new Phenotype(pearl, false)).collect(Collectors.toList());
        filter();
    }

    private class Phenotype {
        private Pearl pearl;
        private Property<Boolean> selected = new SimpleBooleanProperty(false);

        private Phenotype(Pearl pearl, boolean selected) {
            this.pearl = pearl;
            this.selected.setValue(selected);
        }
    }
}
