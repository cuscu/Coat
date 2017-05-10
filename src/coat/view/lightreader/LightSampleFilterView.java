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

import coat.view.vcfreader.Zigosity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by uichuimi on 14/10/16.
 */
public class LightSampleFilterView extends BorderPane {

    private ObservableList<LightSampleFilter> sampleFilters = FXCollections.observableArrayList();

    private GridPane gridPane = new GridPane();

    private CheckBox allNoCall = new CheckBox();
    private CheckBox allWild = new CheckBox();
    private CheckBox allHet = new CheckBox();
    private CheckBox allHom = new CheckBox();

    public LightSampleFilterView() {
        gridPane.setAlignment(Pos.CENTER);
//        gridPane.setHgap(5);
//        gridPane.setVgap(5);
        gridPane.add(new Label("Sample"), 0, 0);
        gridPane.add(new Label("No call"), 1, 0);
        gridPane.add(new Label("Wild"), 2, 0);
        gridPane.add(new Label("Het"), 3, 0);
        gridPane.add(new Label("Hom"), 4, 0);
        gridPane.add(new Label("ALL"), 0, 1);
        gridPane.add(allNoCall, 1, 1);
        gridPane.add(allWild, 2, 1);
        gridPane.add(allHet, 3, 1);
        gridPane.add(allHom, 4, 1);
        gridPane.setId("sample-filter-table");
        for (int i = 0; i < 5; i++) {
            final ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setHalignment(HPos.CENTER);
            gridPane.getColumnConstraints().add(columnConstraints);
        }
        setCenter(new ScrollPane(gridPane));
    }

    public ObservableList<LightSampleFilter> getFilters() {
        return sampleFilters;
    }

    public void setSamples(List<String> samples) {
        for (int i = 0; i < samples.size(); i++) {
            final LightSampleFilter sampleFilter = new LightSampleFilter(samples.get(i));
            final CheckBox none = bind(sampleFilter, Zigosity.NO_CALL);
            final CheckBox wild = bind(sampleFilter, Zigosity.WILD);
            final CheckBox het = bind(sampleFilter, Zigosity.HET);
            final CheckBox hom = bind(sampleFilter, Zigosity.HOM);
            final int row = i + 2;
            gridPane.add(new Label(samples.get(i)), 0, row);
            gridPane.add(none, 1, row);
            gridPane.add(wild, 2, row);
            gridPane.add(het, 3, row);
            gridPane.add(hom, 4, row);
            sampleFilters.add(sampleFilter);
            final RowConstraints constraints = new RowConstraints();
            constraints.setValignment(VPos.CENTER);
            gridPane.getRowConstraints().add(constraints);
        }
    }

    @NotNull
    private CheckBox bind(LightSampleFilter sampleFilter, Zigosity zigosity) {
        final CheckBox checkBox = new CheckBox();
        checkBox.setSelected(true);
        checkBox.setOnAction(event -> {
            if (checkBox.isSelected()) sampleFilter.set(zigosity);
            else sampleFilter.unset(zigosity);
//            recalculateAll();
        });
        return checkBox;
    }

    private void recalculateAll() {
        final Object[][] list = new Object[][]{
                {Zigosity.NO_CALL, allNoCall},
                {Zigosity.WILD, allWild},
                {Zigosity.HET, allHet},
                {Zigosity.HOM, allHom}};
        for (Object[] pair : list) {
            bindAll((Zigosity) pair[0], (CheckBox) pair[1]);
        }


    }

    private void bindAll(Zigosity zigosity, CheckBox checkBox) {
        final boolean allMatch = sampleFilters.stream()
                .allMatch(filter -> filter.has(zigosity));
        if (allMatch) checkBox.setSelected(true);
        else {
            final boolean noneMatch = sampleFilters.stream()
                    .noneMatch(filter -> filter.has(zigosity));
            if (noneMatch) checkBox.setSelected(false);
            else allNoCall.setIndeterminate(true);
        }
    }

    public void onChange(EventHandler handler) {
        sampleFilters.forEach(sampleFilter -> sampleFilter.setOnChange(handler));
    }


}
