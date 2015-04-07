/*
 * Copyright (C) 2015 UICHUIMI
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package coat.graphic;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author UICHUIMI
 */
public class WorkspaceView extends BorderPane {

    private final ObservableList<FileView> fileViews = FXCollections.observableArrayList();
    private final TabPane tabPane = new TabPane();
    private final Property<FileView> selectedFileProperty = new SimpleObjectProperty<>();

    public WorkspaceView() {
        fileViews.addListener((ListChangeListener.Change<? extends FileView> c) -> {
            resetView();
            if (c.wasAdded()) {
                FileView addedFileView = c.getAddedSubList().get(0);
                select(addedFileView.getFile());
            }
        });
        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) -> {
                    selectedFileProperty.setValue(null);
                });
    }

    private void resetView() {
        if (fileViews.isEmpty()) {
            clearWorkspace();
        } else if (fileViews.size() == 1) {
            showOnlyOneFile();
        } else {
            showMultipleFiles();
        }
    }

    public ObservableList<FileView> getFileViews() {
        return fileViews;
    }

    private void clearWorkspace() {
        getChildren().clear();
    }

    private void showOnlyOneFile() {
        getChildren().setAll(fileViews.get(0).getView());
    }

    private void showMultipleFiles() {
        tabPane.getTabs().clear();
        fileViews.forEach(fileView -> {
            Tab t = new Tab(fileView.getFile().getName());
            t.setContent(fileView.getView());
            tabPane.getTabs().add(t);
        });
        getChildren().setAll(tabPane);
    }

    public boolean isOpen(File file) {
        return fileViews.stream().anyMatch(fileView -> (fileView.getFile().equals(file)));
    }

    public final void select(File file) {
        if (fileViews.size() > 1) {
            selectTab(file);
        }
    }

    private void selectTab(File file) {
        try {
            int index = getIndexOf(file);
            tabPane.getSelectionModel().select(index);
        } catch (FileNotOpened ex) {
            Logger.getLogger(WorkspaceView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int getIndexOf(File file) throws FileNotOpened {
        for (int i = 0; i < fileViews.size(); i++) {
            if (fileViews.get(i).getFile().equals(file)) {
                return i;
            }
        }
        throw new FileNotOpened();
    }

    public FileView getSelectedFileView() {
        if (fileViews.isEmpty()) {
            return null;
        } else if (fileViews.size() == 1) {
            return fileViews.get(0);
        } else {
            int index = tabPane.getSelectionModel().getSelectedIndex();
            return fileViews.get(index);
        }
    }

}
