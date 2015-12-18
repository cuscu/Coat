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

import coat.view.vcfreader.VcfSample;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;

/**
 * Cell for the VcfSampleTableView which shows the level of affection of the sample.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
class LevelComboBoxCell extends TableCell<VcfSample, VcfSample.Level> {

    private final ComboBox<VcfSample.Level> levelComboBox = new ComboBox<>(FXCollections.observableArrayList(VcfSample.Level.values()));
    private VcfSample current;

    public LevelComboBoxCell() {
        levelComboBox.setCellFactory(param -> new LevelCell());
        levelComboBox.setButtonCell(new LevelCell());
    }

    @Override
    protected void updateItem(VcfSample.Level item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            final VcfSample vcfSample = (VcfSample) ((getTableRow() != null) ? getTableRow().getItem() : null);
            if (current != null) levelComboBox.valueProperty().unbindBidirectional(current.levelProperty());
            if (vcfSample != null) levelComboBox.valueProperty().bindBidirectional(vcfSample.levelProperty());
            current = vcfSample;
            setGraphic(levelComboBox);
        } else {
            setGraphic(null);
        }
    }

}
