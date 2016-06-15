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

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;

import java.io.IOException;

/**
 * Created by uichuimi on 10/06/16.
 */
public class PhenotypeCell extends ListCell<PhenotypeListController.Phenotype> {

    private PhenotypeCellController controller;
    private Parent parent;


    PhenotypeCell() {
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("phenotype-cell.fxml"));
            parent = loader.load();
            controller = loader.getController();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void updateItem(PhenotypeListController.Phenotype item, boolean empty) {
        unbind();
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            setGraphic(parent);
            bind();
        }
    }

    private void bind() {
        controller.getName().setText((String) getItem().getPearl().getProperty("name"));
        controller.getSelected().selectedProperty().bindBidirectional(getItem().selectedProperty());
        controller.getScore().setText(String.format("%.3f", getItem().getPearl().getScore()));
    }

    private void unbind() {
        if (getItem() != null) controller.getSelected().selectedProperty().unbindBidirectional(getItem().selectedProperty());
    }
}
