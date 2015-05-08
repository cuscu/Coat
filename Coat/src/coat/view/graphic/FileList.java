package coat.view.graphic;

import coat.utils.FileManager;
import coat.utils.OS;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;


/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class FileList extends StackPane {

    private final ListView<File> listView = new ListView<>();
    private final Button add = new Button(null, new SizableImage("coat/img/new.png", SizableImage.MEDIUM_SIZE));
    private final Button delete = new Button(null, new SizableImage("coat/img/delete.png", SizableImage.MEDIUM_SIZE));
    private final MenuItem deleteFile = new MenuItem(OS.getResources().getString("delete"));
    private final ContextMenu contextMenu = new ContextMenu(deleteFile);
    private FileChooser.ExtensionFilter[] filters = {FileManager.ALL_FILTER};


    public FileList() {
        initializeContextMenu();
        initializeButtons();
        initializeListView();
        configureButtonPositions();
        getChildren().addAll(listView, add, delete);
    }

    private void configureButtonPositions() {
        StackPane.setAlignment(add, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(add, new Insets(10));
        StackPane.setAlignment(delete, Pos.BOTTOM_LEFT);
        StackPane.setMargin(delete, new Insets(10));
    }

    private void initializeContextMenu() {
        deleteFile.setGraphic(new SizableImage("coat/img/delete.png", SizableImage.SMALL_SIZE));
        deleteFile.setOnAction(event -> listView.getItems().remove(listView.getSelectionModel().getSelectedItem()));
        listView.setContextMenu(contextMenu);
    }

    private void initializeButtons() {
        add.setOnAction(e -> addInclude());
        add.getStyleClass().add("graphic-button");
        delete.setOnAction(event -> listView.getItems().remove(listView.getSelectionModel().getSelectedItem()));
        delete.getStyleClass().add("graphic-button");
        // Delete button disappears when no file selected
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue)
                -> delete.setVisible(newValue != null));
        delete.setVisible(false);
    }

    private void initializeListView() {
        listView.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.DELETE)
                listView.getItems().remove(listView.getSelectionModel().getSelectedItem());
        });
    }

    private void addInclude() {
        List<File> f = FileManager.openFiles(OS.getResources().getString("select.files"), filters);
        if (f != null) listView.getItems().addAll(f);
    }

    public ObservableList<File> getFiles() {
        return listView.getItems();
    }

    public void setFilters(FileChooser.ExtensionFilter... filters) {
        this.filters = filters;
    }
}
