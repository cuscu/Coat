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

package coat.view.vcfcombiner;

import coat.view.graphic.SizableImageView;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import vcf.combine.Sample;

/**
 * Cell for the VcfSampleTableView which shows the level of affection of the sample.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
class StatusComboBoxCell extends TableCell<Sample, Sample.Status> {

    private final ComboBox<Sample.Status> comboBox = new ComboBox<>(FXCollections.observableArrayList(Sample.Status.values()));

    public StatusComboBoxCell() {
        comboBox.setCellFactory(param -> new StatusCell());
        comboBox.setButtonCell(new StatusCell());
        comboBox.setOnAction(event -> commitEdit(comboBox.getValue()));
    }

    @Override
    protected void updateItem(Sample.Status item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item.toString());
            setGraphic(new SizableImageView("coat/img/black/" + item.name().toLowerCase() + ".png", SizableImageView.SMALL_SIZE));
        }
    }

    @Override
    public void startEdit() {
        super.startEdit();
        comboBox.setValue(getItem());
        setGraphic(comboBox);
        setText(null);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getItem().toString());
        setGraphic(new SizableImageView("coat/img/black/" + getItem().name().toLowerCase() + ".png", SizableImageView
                .SMALL_SIZE));
    }
}
