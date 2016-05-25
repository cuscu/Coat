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

package coat.view.vcfreader;

import coat.utils.OS;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import vcf.Variant;

import java.util.List;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class SampleTable extends TableView<String[]> {


    public void setVariant(Variant variant) {
        getItems().clear();
        if (variant == null) return;
        final List<String> sampleNames = variant.getVcfFile().getHeader().getSamples();
        final List<String> formats = variant.getVcfFile().getHeader().getIdList("FORMAT");
        for (int sampleIndex = 0; sampleIndex < sampleNames.size(); sampleIndex++) {
            final String[] row = new String[formats.size() + 1];
            row[0] = sampleNames.get(sampleIndex);
            for (int i = 0; i < formats.size(); i++) row[i + 1] = variant.getSampleInfo().getFormat(sampleIndex, formats.get(i));
            getItems().add(row);
        }
    }

    public void setColumns(List<String> columns) {
        getColumns().clear();
        TableColumn<String[], String> name = new TableColumn<>(OS.getString("name"));
        name.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()[0]));
        getColumns().add(name);
        for (int i = 0; i < columns.size(); i++) {
            final TableColumn<String[], String> tableColumn = new TableColumn<>(columns.get(i));
            final int index = i;
            tableColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()[index + 1]));
            getColumns().add(tableColumn);
        }
    }
}
