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

import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

/**
 * Created by uichuimi on 14/10/16.
 */
public class SampleFilterCell extends ListCell<SampleFilter> {

    private CheckBox v = new CheckBox();
    private CheckBox wild = new CheckBox();
    private CheckBox het = new CheckBox();
    private CheckBox hom = new CheckBox();
    private HBox hBox = new HBox(v, wild, het, hom);

    SampleFilterCell() {
        setContentDisplay(ContentDisplay.RIGHT);
        setGraphicTextGap(15);
    }

    @Override
    protected void updateItem(SampleFilter item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            setGraphic(hBox);
            v.setSelected(item.has(Zigosity.NO_CALL));
            wild.setSelected(item.has(Zigosity.WILD));
            het.setSelected(item.has(Zigosity.HET));
            hom.setSelected(item.has(Zigosity.HOM));
            setText(item.getSample());
            bind(v, Zigosity.NO_CALL);
            bind(wild, Zigosity.WILD);
            bind(het, Zigosity.HET);
            bind(hom, Zigosity.HOM);
        }
    }

    private void bind(CheckBox checkBox, Zigosity zigosity) {
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) getItem().set(zigosity);
            else getItem().unset(zigosity);
            checkBox.setTooltip(new Tooltip(zigosity.name()));
        });
    }

}
