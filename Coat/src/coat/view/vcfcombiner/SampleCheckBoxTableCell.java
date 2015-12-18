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
import javafx.scene.control.cell.CheckBoxTableCell;

/**
 * Created by Pascual on 14/08/2015.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class SampleCheckBoxTableCell extends CheckBoxTableCell<VcfSample, Boolean> {

    @Override
    public void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            final VcfSample vcfSample = (VcfSample) getTableRow().getItem();
            if (vcfSample != null) setText(vcfSample.getVcfFile().getName());
        } else setText(null);
    }
}
