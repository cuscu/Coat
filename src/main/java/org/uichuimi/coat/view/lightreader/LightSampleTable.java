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
import htsjdk.variant.vcf.VCFCompoundHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.jetbrains.annotations.NotNull;
import org.uichuimi.coat.utils.OS;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class LightSampleTable extends TableView<String> {

    private VariantContext variant;
    private VCFHeader vcfHeader;

    public LightSampleTable(VCFHeader vcfHeader) {
        this.vcfHeader = vcfHeader;
        getItems().setAll(vcfHeader.getGenotypeSamples());
    }

    public void setVariant(VariantContext variant) {
        this.variant = variant;
        if (variant == null) return;
        makeColumns(variant);
    }

    private void makeColumns(VariantContext variant) {
        getColumns().clear();
        getColumns().add(getNameColumn());
        final Set<String> usedTags = getUsedTags(variant);
        for (String tag : usedTags) getColumns().add(getTagColumn(tag));
        getColumns().add(getTypeColumn());
    }

    @NotNull
    private TableColumn<String, String> getNameColumn() {
        final TableColumn<String, String> name = new TableColumn<>(OS.getString("name"));
        name.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        return name;
    }

    @NotNull
    private TableColumn<String, String> getTagColumn(String tag) {
        final TableColumn<String, String> tableColumn = new TableColumn<>(tag);
        if (tag.equals("GT")) {
            tableColumn.setCellValueFactory(param
                    -> new SimpleObjectProperty<>(variant.getGenotype(param.getValue()).getGenotypeString()));
        } else tableColumn.setCellValueFactory(param
                -> new SimpleObjectProperty<>(String.valueOf(variant.getGenotype(param.getValue()).getAnyAttribute(tag))));
        return tableColumn;
    }

    private TableColumn<String, String> getTypeColumn() {
        final TableColumn<String, String> column = new TableColumn<>(OS.getString("type"));
        column.setCellValueFactory(param -> new SimpleObjectProperty<>(variant.getGenotype(param.getValue())
                .getType().toString()));

        return column;
    }

    private Set<String> getUsedTags(VariantContext variant) {
        final List<String> tags = vcfHeader.getFormatHeaderLines().stream().map(VCFCompoundHeaderLine::getID).collect(Collectors.toList());
        final Set<String> usedTags = new LinkedHashSet<>();
        vcfHeader.getGenotypeSamples().forEach(sample -> {
            tags.forEach(tag -> {
                if (variant.getGenotype(sample).hasAnyAttribute(tag)
                        && !variant.getGenotype(sample).getAnyAttribute(tag).equals("."))
                    usedTags.add(tag);
            });
        });
        return usedTags;
    }

}
