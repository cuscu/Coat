/*
 * Copyright (c) UICHUIMI 2017
 *
 * This file is part of Coat.
 *
 * Coat is free software:
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Coat.
 *
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Arrays;
import java.util.List;


/**
 * This StackPane shows a list of files. New files can be added with 'add' button on the BOTTOM_RIGHT corner.
 * User can add multiple Files at once. To delete a file, user can press the 'Delete' button or 'Del' key when
 * File is selected.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class FileList extends StackPane {

    private final ListView<File> listView = new ListView<>();
    private final Button add = new Button(null, new SizableImageView("img/black/add.png", SizableImageView.MEDIUM_SIZE));
    private final Button delete = new Button(null, new SizableImageView("img/black/delete.png", SizableImageView.MEDIUM_SIZE));
    private final MenuItem deleteFile = new MenuItem(OS.getString("delete"));
    private final ContextMenu contextMenu = new ContextMenu(deleteFile);
    private FileChooser.ExtensionFilter[] filters = {FileManager.ALL_FILTER};

    public FileList() {
        allowDragAndDrop();
        initializeContextMenu();
        initializeButtons();
        initializeListView();
        configureButtonPositions();
        getChildren().addAll(listView, add, delete);
    }

    private void allowDragAndDrop() {
        setOnDragEntered(this::dragEntered);
        setOnDragExited(this::dragExited);
        setOnDragOver(this::acceptDrag);
        setOnDragDropped(this::dropFiles);
    }

    private void dragEntered(DragEvent event) {
        listView.getStyleClass().add("drop-entered");
    }

    private void dragExited(DragEvent dragEvent) {
        listView.getStyleClass().remove("drop-entered");
    }

    private void acceptDrag(DragEvent event) {
        if (event.getGestureSource() != this && event.getDragboard().hasString())
            event.acceptTransferModes(TransferMode.LINK);
        event.consume();
    }

    private void dropFiles(DragEvent event) {
        final List<File> files = event.getDragboard().getFiles();
        files.stream().
                filter(file -> !getFiles().contains(file)).
                filter(this::matchesAnyExtension).
                forEach(file -> getFiles().add(file));
    }

    private boolean matchesAnyExtension(File file) {
        return Arrays.stream(filters).
                anyMatch(filter -> filter.getExtensions().stream().
                        anyMatch(extension -> file.getName().endsWith(extension.replace("*", ""))));
    }

    private void configureButtonPositions() {
        StackPane.setAlignment(add, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(add, new Insets(10));
        StackPane.setAlignment(delete, Pos.BOTTOM_LEFT);
        StackPane.setMargin(delete, new Insets(10));
    }

    private void initializeContextMenu() {
        deleteFile.setGraphic(new SizableImageView("img/black/delete.png", SizableImageView.SMALL_SIZE));
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
