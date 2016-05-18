/*
 * Copyright (c) UICHUIMI 2016
 *
 * This file is part of Coat.
 *
 * Coat is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package coat.view.poirot.gene;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import poirot.core.Pearl;

/**
 * Created by uichuimi on 18/05/16.
 */
public class GeneCellController {
    @FXML
    private CheckBox geneSelection;

    @FXML
    private Label score;

    @FXML
    private Label distance;

    public void setGene(Pearl item) {
        distance.setText(String.valueOf(item.getDistanceToPhenotype()));
        geneSelection.setText(item.getId());
        score.setText(String.valueOf(item.getScore()));
    }
}
