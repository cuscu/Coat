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

package coat.view.poirot.sample;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by uichuimi on 16/05/16.
 */
class SampleCell extends ListCell<Sample> {

    private final static Logger log = Logger.getLogger(SampleCell.class.getName());
    private Parent parent;
    private SampleController controller;
    private final ChangeListener<Sample.Sex> sexChangeListener = (observable, oldValue, newValue) -> updateIcon();
    private final ChangeListener<Boolean> affectedChangeListener = (observable, oldValue, newValue) -> updateIcon();
    private final ChangeListener<Sample> fatherChangeListener = (observable, oldValue, newValue) -> controller.fatherProperty().set(newValue.getName());
    private final ChangeListener<Sample> motherChangeListener = (observable, oldValue, newValue) -> controller.motherProperty().set(newValue.getName());

    SampleCell() {
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        try {
            parent = loader.load();
            controller = loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void updateItem(Sample item, boolean empty) {
        unbind();
        super.updateItem(item, empty);
        if (!empty) {
            setGraphic(parent);
            controller.nameProperty().bind(item.nameProperty());
            item.fatherProperty().addListener(fatherChangeListener);
            item.motherProperty().addListener(motherChangeListener);
            item.sexProperty().addListener(sexChangeListener);
            item.affectedProperty().addListener(affectedChangeListener);
            updateIcon();
        } else {
            setGraphic(null);
        }
    }

    private void unbind() {
        if (getItem() != null) {
            controller.nameProperty().unbind();
            controller.fatherProperty().unbind();
            controller.motherProperty().unbind();
            getItem().sexProperty().removeListener(sexChangeListener);
            getItem().affectedProperty().removeListener(affectedChangeListener);
            getItem().motherProperty().removeListener(motherChangeListener);
            getItem().fatherProperty().removeListener(fatherChangeListener);
        }
    }

    private void updateIcon() {
        final SampleController.IconType iconType = getItem().getSex() == Sample.Sex.FEMALE
                ? getItem().isAffected()
                ? SampleController.IconType.FEMALE_AFFECTED
                : SampleController.IconType.FEMALE_UNAFFECTED
                : getItem().isAffected()
                ? SampleController.IconType.MALE_AFFECTED
                : SampleController.IconType.MALE_UNAFFECTED;
        controller.iconProperty().setValue(iconType.getIcon());

    }

}
