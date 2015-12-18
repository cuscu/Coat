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

import coat.utils.FileManager;
import coat.utils.OS;
import coat.view.vcfreader.VcfSample;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

import java.io.File;

/**
 * Manages the Mist file TableCell in VcfSampleTableView.
 * <p>
 * TODO: This class was tried to merge with BamTableCell, but code <code>startEdit();commitEdit(file);</code> throws NullPointerException
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
class MistTableCell extends TableCell<VcfSample, File> {

    private final Button button = new Button("...");

    public MistTableCell() {
        button.setOnAction(event -> selectFile());
    }

    private void selectFile() {
        final File file = FileManager.openFile(OS.getString("choose.file"), FileManager.MIST_FILTER);
        if (file != null) ((VcfSample) getTableRow().getItem()).mistFileProperty().setValue(file);
    }

    @Override
    protected void updateItem(File file, boolean empty) {
        super.updateItem(file, empty);
        if (!empty) {
            if (file != null) setText(file.getName());
            setGraphic(button);
        } else {
            setText(null);
            setGraphic(null);
        }
    }
}
