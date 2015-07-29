/*
 * Copyright (C) 2014 UICHUIMI
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package coat.view.graphic;

import coat.model.vcfreader.Variant;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TableCell;

/**
 * Experiment: I was trying to show only the name of the chromosome in the first visible row.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ChromosomeCell extends TableCell<Variant, String> {

    /**
     * Creates a new index cell that will have "index-cell" css class and will be aligned on the
     * center right.
     */
    public ChromosomeCell() {
        getStyleClass().add("index-cell");
        setAlignment(Pos.CENTER_RIGHT);
        setGraphic(null);
        setBackground(null);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) setText(null);
        else setChromosome(item);
    }

    private void setChromosome(String item) {
        final TableViewSkin skin = (TableViewSkin) getTableView().getSkin();
        final ObservableList kids = skin.getChildren();
        if (kids != null && !kids.isEmpty()) {
            final VirtualFlow flow = (VirtualFlow) kids.get(1);
            if (flow != null) {
                final IndexedCell firstVisibleCell = flow.getFirstVisibleCell();
                if (firstVisibleCell != null) {
                    int first = firstVisibleCell.getIndex();
                    // int last = flow.getLastVisibleCell().getIndex();
                    if (first == getIndex() - 1) setText(item);
                    else setText(null);
                    flow.requestLayout();
                }
            }
        }
    }

}
