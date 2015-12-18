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

package coat.view.graphic;

import coat.Coat;
import coat.utils.OS;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ThresholdDialog {

    public static final String DEFAULT_THRESHOLD = "0.01";

    final static AtomicReference<String> ret = new AtomicReference<>(null);
    final static TextField textField = new TextField(DEFAULT_THRESHOLD);
    final static Button accept = new Button(OS.getResources().getString("accept"));
    final static Button cancel = new Button(OS.getResources().getString("cancel"));
    final static HBox box = new HBox(5, textField, accept, cancel);
    final static Scene scene = new Scene(box);
    final static Stage stage = new Stage(StageStyle.UNIFIED);
    private static final EventHandler<ActionEvent> acceptHandler = event -> {
        ret.set(textField.getText());
        stage.close();
    };
    static {
        box.setPadding(new Insets(5));
        initializeStage();
        initializeActions();
    }

    private static void initializeActions() {
        textField.setOnAction(acceptHandler);
        accept.setOnAction(acceptHandler);
        cancel.setOnAction(event -> stage.close());
    }

    private static void initializeStage() {
        stage.setTitle(OS.getResources().getString("threshold"));
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setAlwaysOnTop(true);
        stage.initOwner(Coat.getStage());
    }

    public static String askThresholdToUser() {
        ret.set(null);
        textField.setText(DEFAULT_THRESHOLD);
        stage.showAndWait();
        return ret.get();
    }
}
