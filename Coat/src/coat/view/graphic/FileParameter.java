package coat.view.graphic;

import coat.utils.FileManager;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class FileParameter extends HBox {

    private final String title;
    private final Property<File> file = new SimpleObjectProperty<>();
    private List<FileChooser.ExtensionFilter> filters = new ArrayList<>();

    public FileParameter(String title) {
        this.title = title;

        final Button button = getButton();
        final TextField textField = getTextField(title);

        file.addListener((observable, oldValue, newValue) -> textField.setText(newValue.getAbsolutePath()));

        HBox.setHgrow(textField, Priority.ALWAYS);
        getChildren().addAll(textField, button);

    }

    private Button getButton() {
        final Button button = new Button(null, new SizableImage("coat/img/folder.png", SizableImage.SMALL_SIZE));
        button.setOnAction(event -> openFile());
        button.getStyleClass().add("graphic-file-button");
        return button;
    }

    private TextField getTextField(String title) {
        final TextField textField = new TextField();
        textField.setEditable(false);
        textField.setPromptText(title);
        textField.setTooltip(new Tooltip(title));
        textField.getStyleClass().add("fancy-text-field");
        return textField;
    }

    private void openFile() {
        final File f = FileManager.openFile("Select " + title, filters);
        if (f != null) file.setValue(f);
    }

    public List<FileChooser.ExtensionFilter> getFilters() {
        return filters;
    }

    public Property<File> fileProperty() {
        return file;
    }
}
