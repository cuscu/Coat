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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by uichuimi on 14/10/16.
 */
public class SampleFilterView extends BorderPane {

    private ObservableList<SampleFilter> sampleFilters = FXCollections.observableArrayList();

    private GridPane gridPane = new GridPane();

    public SampleFilterView() {
        gridPane.setAlignment(Pos.CENTER);
//        gridPane.setHgap(5);
//        gridPane.setVgap(5);
        gridPane.add(new Label("Sample"), 0, 0);
        gridPane.add(new Label("None"), 1, 0);
        gridPane.add(new Label("Wild"), 2, 0);
        gridPane.add(new Label("Het"), 3, 0);
        gridPane.add(new Label("Hom"), 4, 0);
        gridPane.setId("sample-filter-table");
        for (int i = 0; i < 5; i++) {
            final ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setHalignment(HPos.CENTER);
            gridPane.getColumnConstraints().add(columnConstraints);
        }
        setCenter(gridPane);
    }

    public ObservableList<SampleFilter> getFilters() {
        return sampleFilters;
    }

    public void setSamples(List<String> samples) {
        for (int i = 0; i < samples.size(); i++) {
            final SampleFilter sampleFilter = new SampleFilter(samples.get(i));
            final CheckBox none = bind(sampleFilter, Zigosity.NO_CALL);
            final CheckBox wild = bind(sampleFilter, Zigosity.WILD);
            final CheckBox het = bind(sampleFilter, Zigosity.HET);
            final CheckBox hom = bind(sampleFilter, Zigosity.HOM);
            gridPane.add(new Label(samples.get(i)), 0, i + 1);
            gridPane.add(none, 1, i + 1);
            gridPane.add(wild, 2, i + 1);
            gridPane.add(het, 3, i + 1);
            gridPane.add(hom, 4, i + 1);
            sampleFilters.add(sampleFilter);
            final RowConstraints constraints = new RowConstraints();
            constraints.setValignment(VPos.CENTER);
            gridPane.getRowConstraints().add(constraints);
        }
    }

    @NotNull
    private CheckBox bind(SampleFilter sampleFilter, Zigosity zigosity) {
        final CheckBox checkBox = new CheckBox();
        checkBox.setSelected(true);
        checkBox.setOnAction(event -> {
            if (checkBox.isSelected()) sampleFilter.set(zigosity);
            else sampleFilter.unset(zigosity);
        });
        return checkBox;
    }

    public void onChange(EventHandler handler) {
        sampleFilters.forEach(sampleFilter -> sampleFilter.setOnChange(handler));
    }


}
