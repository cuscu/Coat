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

package coat.view.vcfreader;

import coat.utils.OS;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.jetbrains.annotations.NotNull;
import org.uichuimi.vcf.variant.Variant;
import org.uichuimi.vcf.variant.VcfConstants;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class SampleTable extends TableView<String> {

    private Variant variant;

    public void setVariant(Variant variant) {
        this.variant = variant;
        getItems().clear();
        if (variant == null) return;
        makeColumns(variant);
        getItems().addAll(variant.getHeader().getSamples());
    }

    private void makeColumns(Variant variant) {
        getColumns().clear();
        getColumns().add(getNameColumn());
        final Set<String> usedTags = getUsedTags(variant);
        for (String tag : usedTags) getColumns().add(getTagColumn(tag));
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
        tableColumn.setCellValueFactory(param -> {
            final int i = variant.getHeader().getSamples().indexOf(param.getValue());
            return new SimpleObjectProperty<>(variant.getSampleInfo(i).get(tag));
        });
        return tableColumn;
    }

    private Set<String> getUsedTags(Variant variant) {
        final List<String> tags = variant.getHeader().getIdList("FORMAT");
        final Set<String> usedTags = new LinkedHashSet<>();
        variant.getHeader().getSamples().forEach(sample -> {
            final int i = variant.getHeader().getSamples().indexOf(sample);
            tags.forEach(tag -> {
                final String format = variant.getSampleInfo(i).get(tag);
                if (format != null && !format.equals(VcfConstants.EMPTY_VALUE)) usedTags.add(tag);
            });
        });
        return usedTags;
    }

}
