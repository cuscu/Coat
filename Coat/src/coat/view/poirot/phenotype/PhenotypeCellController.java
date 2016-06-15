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

package coat.view.poirot.phenotype;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

/**
 * Created by uichuimi on 10/06/16.
 */
public class PhenotypeCellController {
    @FXML
    private TextField name;
    @FXML
    private TextField score;
    @FXML
    private CheckBox selected;

    public void selected(ActionEvent actionEvent) {

    }

    public TextField getName() {
        return name;
    }

    public CheckBox getSelected() {
        return selected;
    }

    public TextField getScore() {
        return score;
    }
}
