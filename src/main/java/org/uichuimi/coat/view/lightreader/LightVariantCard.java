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

import htsjdk.variant.variantcontext.VariantContext;
import javafx.geometry.Insets;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextArea;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
class LightVariantCard extends TableCell<VariantContext, VariantContext> {

    private final TextArea area = new TextArea();

    public LightVariantCard() {
        setText(null);
//        area.setMinWidth(0);
//        area.setMinHeight(0);
        area.setPrefHeight(24);
        area.setBackground(null);
        area.setEditable(false);
        area.setPadding(new Insets(0));
        area.setOnMouseClicked(event -> getTableView().getSelectionModel().select(getIndex()));
        area.styleProperty().bind(styleProperty());
    }

    @Override
    protected void updateItem(VariantContext variant, boolean empty) {
        super.updateItem(variant, empty);
        area.clear();
        if (empty || variant == null)
            setGraphic(null);
        else {
            final String codon = variant.getAttributeAsString("COD", null);
            if (codon == null)
                setValues(variant.getReference().getBaseString(), variant.getAltAlleleWithHighestAlleleCount().toString());
            else {
                final String [] cods = codon.split("[-/]");
                final String amino = variant.getAttributeAsString("AMINO", null);
                if (amino == null)
                    setValues(cods[0], cods[1]);
                else {
                    String [] aminos = amino.split("[-/]");
                    setValues(cods[0] + " (" + aminos[0] + ")",
                            cods[1] + " (" + (aminos.length > 1 ? aminos[1] : aminos[0]) + ")");
                }
            }
            setGraphic(area);
        }
    }

    private void setValues(String ref, String alt) {
        area.clear();
        area.appendText(ref);
        area.appendText("\n");
        area.appendText(alt);
    }


}
