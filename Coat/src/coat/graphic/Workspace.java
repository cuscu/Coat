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
public class Workspace extends BorderPane {

    private final ObservableList<FileView> fileViews = FXCollections.observableArrayList();
    private final TabPane tabPane = new TabPane();

    public Workspace() {
        fileViews.addListener((ListChangeListener.Change<? extends FileView> c) -> {
            if (fileViews.isEmpty()) {
                clearWorkspace();
            } else if (fileViews.size() == 1) {
                showOnlyOneFile();
            } else {
                showMultipleFiles();
            }
        });
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
    }

}
