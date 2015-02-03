/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author UICHUIMI
 */
public class Coat extends Application {

    private static Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("CoatView.fxml"));

        Scene scene = new Scene(root);

        stage.setTitle("COAT");
        stage.setScene(scene);
        stage.show();
        Coat.stage = stage;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    public static void setTitle(String title) {
        if (title != null && !title.isEmpty()) {
            stage.setTitle("COAT - " + title);
        } else {
            stage.setTitle("COAT");
        }
    }

}
