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

package org.uichuimi.coat.view;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class TestApp extends Application {

    private Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        primaryStage.show();
        testOne();
    }

    public void setParent(Parent parent) {
        final Scene scene = new Scene(parent);
        scene.getStylesheets().add("default.css");
        stage.setScene(scene);
    }

    public void testOne() {

    }

}
