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
