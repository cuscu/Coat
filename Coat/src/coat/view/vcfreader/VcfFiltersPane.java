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

import coat.core.poirot.dataset.Instance;
import coat.view.graphic.SizableImageView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VcfFiltersPane extends VBox {

    private ObservableList<Instance> inputVariants = FXCollections.observableArrayList();
    private ObservableList<Instance> outputVariants = FXCollections.observableArrayList();
    private final ListView<InstanceFilter> filterListView = new ListView<>();
    private final Button addFilter = new Button("Filter", new SizableImageView("coat/img/black/add.png", SizableImageView.SMALL_SIZE));
    private final ProgressBar progressBar = new ProgressBar();
    private final Label progressText = new Label();
    private final StackPane progressPane = new StackPane(progressBar, progressText);
    private final HBox buttonsBar = new HBox(5, addFilter, progressPane);


    public void setInputVariants(ObservableList<Instance> variants) {
        this.inputVariants = variants;
        filter();
        getChildren().addAll(buttonsBar, filterListView);
        filterListView.getStyleClass().add("variants-list");
        filterListView.setCellFactory(param -> new InstanceFilterCell(this));
        filterListView.setEditable(true);
        addFilter.setOnAction(event -> addFilter());
        HBox.setHgrow(progressPane, Priority.ALWAYS);
        progressBar.setMaxWidth(9999);
    }

    private void addFilter() {
        filterListView.getItems().add(new InstanceFilter());
    }

    public ObservableList<Instance> getOutputVariants() {
        return outputVariants;
    }

    public void filter() {
        outputVariants.setAll(
                inputVariants.stream()
                        .filter(this::filter)
                        .collect(Collectors.toList()));
        updateProgress();
    }

    private void updateProgress() {
        final double progress = (double) outputVariants.size() / inputVariants.size();
        progressBar.setProgress(progress);
        progressText.setText(String.format("%d/%d (%.2f%%)", outputVariants.size(), inputVariants.size(), (progress * 100)));
    }

    private boolean filter(Instance instance) {
        return filterListView.getItems().stream().allMatch(instanceFilter -> instanceFilter.filter(instance));
    }


}
