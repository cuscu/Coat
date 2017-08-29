/*
 * Copyright (c) UICHUIMI 2017
 *
 * This file is part of Coat.
 *
 * Coat is free software:
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Coat.
 *
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */

package coat.view.vcfreader.filter;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Created by uichuimi on 1/07/16.
 */
public class ConnectorTextFilterController {
    @FXML
    private CheckBox strict;
    @FXML
    private Button applyButton;
    @FXML
    private ComboBox<ConnectorTextFilterTableColumn.Connector> connector;

    @FXML
    private Label name;

    @FXML
    private TextField filterText;

    public TextField getFilterText() {
        return filterText;
    }

    public ComboBox<ConnectorTextFilterTableColumn.Connector> getConnector() {
        return connector;
    }

    public Button getApplyButton() {
        return applyButton;
    }

    public Label getName() {
        return name;
    }

    public void clear(ActionEvent actionEvent) {
        filterText.clear();
    }

    public CheckBox getStrict() {
        return strict;
    }
}
