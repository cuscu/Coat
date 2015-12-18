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

package coat.view.vcfcombiner;

import coat.utils.OS;
import coat.view.vcfreader.VcfSample;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.File;
import java.util.Arrays;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
class VcfSampleTableView extends TableView<VcfSample> {

    VcfSampleTableView(){
        final TableColumn<VcfSample, VcfSample.Level> levelColumn = new TableColumn<>(OS.getString("level"));
//        final TableColumn<Sample, String> nameColumn = new TableColumn<>(OS.getString("name"));
        final TableColumn<VcfSample, Long> numberOfVariantsColumn = new TableColumn<>(OS.getString("variants"));
        final TableColumn<VcfSample, Boolean> enableColumn = new TableColumn<>(OS.getString("name"));
        final TableColumn<VcfSample, File> bamFileColumn = new TableColumn<>(OS.getString("bam.file"));
        final TableColumn<VcfSample, File> mistFileColumn = new TableColumn<>(OS.getString("mist.file"));

        getColumns().addAll(Arrays.asList(enableColumn, numberOfVariantsColumn, mistFileColumn));
        setEditable(true);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);

        enableColumn.setCellValueFactory(param -> param.getValue().enabledProperty());
        enableColumn.setCellFactory(param -> new SampleCheckBoxTableCell());
        enableColumn.getStyleClass().add("text-column");
        enableColumn.setPrefWidth(200);

        numberOfVariantsColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getNumberOfVariants()));
        numberOfVariantsColumn.getStyleClass().add("text-column");

        levelColumn.setCellValueFactory(param -> param.getValue().levelProperty());
        levelColumn.setCellFactory(param -> new LevelComboBoxCell());
        levelColumn.setPrefWidth(200);

        bamFileColumn.setCellValueFactory(param -> param.getValue().bamFileProperty());
        bamFileColumn.setCellFactory(param -> new BamTableCell());
        bamFileColumn.setPrefWidth(200);

        mistFileColumn.setCellValueFactory(param -> param.getValue().mistFileProperty());
        mistFileColumn.setCellFactory(param -> new MistTableCell());
        mistFileColumn.setPrefWidth(200);
    }

}
