package coat.view.graphic;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Popup;

import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class AutoFillComboBox extends HBox {

    private final TextField editor = new TextField();
    private final Popup popup = new Popup();
    private final ListView<String> list = new ListView<>();

    private ObservableList<String> items = FXCollections.observableArrayList();
    private final Property<String> value = new SimpleObjectProperty<>();

    public AutoFillComboBox() {
        HBox.setHgrow(editor, Priority.ALWAYS);
        setAlignment(Pos.CENTER);

        popup.getContent().add(list);
        popup.setAutoHide(true);

        editor.setOnKeyReleased(this::keyReleased);
        editor.textProperty().addListener((observable, oldValue, newValue) -> filter());

        items.addListener((ListChangeListener<String>) c -> {
            list.getItems().setAll(items);
            this.setWidth(list.getWidth());
        });

        list.setOnKeyReleased(this::listKeyReleased);
        list.setOnMouseReleased(this::mouseClicked);

        final Button button = new Button(null, new SizableImage("coat/img/drop-down-arrow.png", SizableImage.SMALL_SIZE));
        button.setOnAction(event -> show());
        button.getStyleClass().add("graphic-button");

        value.addListener((observable, oldValue, newValue) -> valueChanged(newValue));

        getChildren().addAll(editor, button);
    }

    private void mouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) setValue(list.getSelectionModel().getSelectedItem());
    }

    private void valueChanged(String newValue) {
        editor.setText(newValue);
        editor.end();
        popup.hide();
    }

    private void filter() {
        final String text = editor.getText() == null ? "" : editor.getText().toLowerCase();
        list.getItems().setAll(items.stream().filter(t -> t.toLowerCase().contains(text)).collect(Collectors.toList()));
        show();
    }

    private void listKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) popup.hide();
        else if (event.getCode() == KeyCode.ENTER) setValue(list.getSelectionModel().getSelectedItem());
    }

    private void keyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.DOWN) {
            if (popup.isShowing()) list.requestFocus();
            else show();
        }
    }

    private void show() {
        final Point2D p = editor.localToScene(0.0, 0.0);
        popup.show(editor,
                p.getX() + editor.getScene().getX() + editor.getScene().getWindow().getX(),
                p.getY() + editor.getScene().getY() + editor.getScene().getWindow().getY() + editor.getHeight());
    }

    public ObservableList<String> getItems() {
        return items;
    }

    public String getValue() {
        return value.getValue();
    }

    public void setValue(String value) {
        this.value.setValue(value);
    }

    public void setPromptText(String promptText) {
        editor.setPromptText(promptText);
    }

    public TextField getEditor() {
        return editor;
    }
}
