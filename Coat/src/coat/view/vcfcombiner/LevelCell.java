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

import coat.view.graphic.SizableImage;
import coat.view.vcfreader.VcfSample;
import javafx.scene.control.ListCell;

/**
 * Cell for ListView that shows the level of affection. The image is taken from coat/img/black/ plus level name plus .png.
 * For instance: HETEROZYGOUS -> coat/img/black/heterozygous.png
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
class LevelCell extends ListCell<VcfSample.Level> {

    @Override
    protected void updateItem(VcfSample.Level item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            setText(item.toString());
            setGraphic(new SizableImage("coat/img/black" + item.name().toLowerCase() + ".png", SizableImage.SMALL_SIZE));
        } else {
            setText(null);
            setGraphic(null);
        }
    }
}
