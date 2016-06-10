/******************************************************************************
 * Copyright (C) 2015 UICHUIMI                                                *
 * *
 * This program is free software: you can redistribute it and/or modify it    *
 * under the terms of the GNU General Public License as published by the      *
 * Free Software Foundation, either version 3 of the License, or (at your     *
 * option) any later version.                                                 *
 * *
 * This program is distributed in the hope that it will be useful, but        *
 * WITHOUT ANY WARRANTY; without even the implied warranty of                 *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 * *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 ******************************************************************************/

package coat;

import coat.utils.OS;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Coat extends Application {

    private static final Properties properties = new Properties();
    private static Stage stage;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    public static void setTitle(String title) {
        if (title != null && !title.isEmpty()) stage.setTitle("COAT - " + title);
        else stage.setTitle("COAT");
    }

    public static Stage getStage() {
        return stage;
    }

    public static Properties getProperties() {
        return properties;
    }

    @Override
    public void start(Stage stage) throws Exception {
        loadProperties();
        final Parent root = FXMLLoader.load(getClass().getResource("CoatView.fxml"), OS.getResources());
        final Scene scene = new Scene(root);

        stage.getIcons().add(new Image("coat/img/black/lfs.png"));
        stage.setTitle("COAT");
        stage.setScene(scene);
        stage.show();

        Coat.stage = stage;
    }

    private void loadProperties() {
        final File file = new File(OS.getConfigPath(), "coat.properties");
        try {
            if (file.exists())
                properties.load(new FileInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        savePropertiesOnExit(file);
    }

    private void savePropertiesOnExit(File file) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    properties.store(new FileOutputStream(file), null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
