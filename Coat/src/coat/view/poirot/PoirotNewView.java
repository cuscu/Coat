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

package coat.view.poirot;

import coat.core.tool.Tool;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

/**
 * Created by uichuimi on 16/05/16.
 */
public class PoirotNewView extends Tool {

    private static PoirotNewView TOOL;
    private Property<String> title = new SimpleObjectProperty<>("Poirot");

    private PoirotNewView() {
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("poirot-view.fxml"));
        try {
            final Parent parent = loader.load();
            getChildren().setAll(parent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Tool getView() {
        if (TOOL == null) TOOL = new PoirotNewView();
        return TOOL;
    }

    @Override
    public Property<String> titleProperty() {
        return title;
    }


}
