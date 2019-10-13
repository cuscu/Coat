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

package org.uichuimi.coat.view.lightreader;

import org.uichuimi.coat.view.vcfreader.Zigosity;
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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by uichuimi on 14/10/16.
 */
public class LightSampleFilterView extends BorderPane {

    private List<LightSampleFilter> sampleFilters = new LinkedList<>();
    private Map<Zigosity, List<CheckBox>> checkBoxes = new LinkedHashMap<>();

    private GridPane gridPane = new GridPane();

    private CheckBox noCall = new CheckBox();
    private CheckBox wild = new CheckBox();
    private CheckBox het = new CheckBox();
    private CheckBox hom = new CheckBox();

    final Object[][] allCheckBoxes = new Object[][]{
            {Zigosity.NO_CALL, noCall},
            {Zigosity.WILD, wild},
            {Zigosity.HET, het},
            {Zigosity.HOM, hom}};

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
        gridPane.add(noCall, 1, 1);
        gridPane.add(wild, 2, 1);
        gridPane.add(het, 3, 1);
        gridPane.add(hom, 4, 1);
        gridPane.setId("sample-filter-table");
        for (int i = 0; i < 5; i++) {
            final ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setHalignment(HPos.CENTER);
            gridPane.getColumnConstraints().add(columnConstraints);
        }
        setCenter(new ScrollPane(gridPane));
        initAllCheckBoxes();
        for (Zigosity zigosity : Zigosity.values())
            checkBoxes.put(zigosity, new LinkedList<>());
    }

    private void initAllCheckBoxes() {
        for (Object[] pair : allCheckBoxes) {
            final Zigosity zigosity = (Zigosity) pair[0];
            final CheckBox box = (CheckBox) pair[1];
            box.setSelected(true);
            box.setOnAction(event -> {
                checkBoxes.get(zigosity).forEach(checkBox -> checkBox
                        .setSelected(box.isSelected()));
                if (box.isSelected())
                    sampleFilters.forEach(filter -> filter.set(zigosity));
                else sampleFilters.forEach(filter -> filter.unset(zigosity));
            });
        }
    }

    public List<LightSampleFilter> getFilters() {
        return sampleFilters;
    }

    public void setSamples(List<String> samples) {
        for (int i = 0; i < samples.size(); i++) {
            final LightSampleFilter sampleFilter = new LightSampleFilter(samples.get(i));
            final CheckBox none = newCheckBox(sampleFilter, Zigosity.NO_CALL);
            final CheckBox wild = newCheckBox(sampleFilter, Zigosity.WILD);
            final CheckBox het = newCheckBox(sampleFilter, Zigosity.HET);
            final CheckBox hom = newCheckBox(sampleFilter, Zigosity.HOM);
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
    private CheckBox newCheckBox(LightSampleFilter filter, Zigosity zigosity) {
        final CheckBox checkBox = new CheckBox();
        checkBox.setSelected(true);
        checkBox.setOnAction(event -> {
            if (checkBox.isSelected()) filter.set(zigosity);
            else filter.unset(zigosity);
            recalculateAll();
        });
        checkBoxes.get(zigosity).add(checkBox);
        return checkBox;
    }

    private void recalculateAll() {
        for (Object[] pair : allCheckBoxes)
            bindAll((Zigosity) pair[0], (CheckBox) pair[1]);
    }

    private void bindAll(Zigosity zigosity, CheckBox checkBox) {
        final boolean allMatch = sampleFilters.stream()
                .allMatch(filter -> filter.has(zigosity));
        final boolean noneMatch = sampleFilters.stream()
                .noneMatch(filter -> filter.has(zigosity));
        checkBox.setSelected(allMatch);
        checkBox.setIndeterminate(!allMatch && !noneMatch);
    }

    public void onChange(EventHandler handler) {
        sampleFilters.forEach(sampleFilter -> sampleFilter.setOnChange(handler));
    }


}
