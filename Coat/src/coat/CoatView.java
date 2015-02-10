/*
 * Copyright (C) 2014 UICHUIMI
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
package coat;

import coat.graphic.SizableImage;
import coat.mist.CombineMIST;
import coat.reader.Reader;
import coat.tsv.TsvFileReader;
import coat.utils.FileManager;
import coat.utils.OS;
import coat.vcf.VcfFileReader;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class CoatView {

    @FXML
    private TabPane workspace;
    @FXML
    private Button openFile;
    @FXML
    private Button saveFile;
    @FXML
    private TabPane menu;
    @FXML
    private Button combineVCF;
    @FXML
    private Button combineMIST;
    @FXML
    private Label info;
    @FXML
    private HBox infoBox;

    private Tab customTab;

    private static Label staticInfo;
    private static HBox staticInfoBox;
    private final static DateFormat df = new SimpleDateFormat("HH:mm:ss");

    public void initialize() {
        staticInfo = info;
        staticInfoBox = infoBox;
        // Listen whe user clicks on a tab, or opens a file
        workspace.getSelectionModel().selectedItemProperty().addListener((obs, old, current) -> {
            // Remove custom tab if it exists
            if (menu.getTabs().contains(customTab)) {
                menu.getTabs().remove(customTab);
            }
            if (current != null) {
                // Try to load custom tab
                Reader reader = (Reader) current.getUserData();
                if (reader.getActions() == null) {
                    return;
                }
                // Load buttons in the custom tab
                FlowPane pane = new FlowPane();
                pane.getChildren().setAll(reader.getActions());

                customTab = new Tab(reader.getActionsName());
                customTab.setClosable(false);
                customTab.setContent(pane);
                menu.getTabs().add(customTab);
                menu.getSelectionModel().select(customTab);
                Coat.setTitle(reader.getFile().getName());

                // Activate save file
                saveFile.setDisable(false);
            } else {
                // If no tab is selected, save button should be disabled
                saveFile.setDisable(true);
            }
        });
        // By default is disabled
        saveFile.setDisable(true);
        assignMenuIcons();
    }

    private void assignMenuIcons() {
        openFile.setGraphic(new SizableImage("coat/img/open.png", SizableImage.MEDIUM_SIZE));
        saveFile.setGraphic(new SizableImage("coat/img/save.png", SizableImage.MEDIUM_SIZE));
        combineVCF.setGraphic(new SizableImage("coat/img/documents_vcf.png", SizableImage.MEDIUM_SIZE));
        combineMIST.setGraphic(new SizableImage("coat/img/documents_mist.png", SizableImage.MEDIUM_SIZE));
    }

    /**
     * When user clicks on open button.
     *
     * @param event
     */
    @FXML
    private void open(ActionEvent event) {
        File f = FileManager.openFile(OS.getResources().getString("choose.file"),
                FileManager.VCF_FILTER, FileManager.MIST_FILTER, FileManager.TSV_FILTER);
        if (f != null) {
            // Check if the file is already opened
            for (Tab t : workspace.getTabs()) {
                if (t.getUserData().equals(f)) {
                    workspace.getSelectionModel().select(t);
                    return;
                }
            }

            // Load corresponding View
            FXMLLoader loader = null;
            if (f.getName().endsWith(".vcf")) {
                try {
                    loader = new FXMLLoader(
                            VcfFileReader.class.getResource("VcfFileReader.fxml"),
                            OS.getResources());
                    loader.load();
                } catch (IOException ex) {
                    Logger.getLogger(CoatView.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }
            } else if (f.getName().endsWith(".mist") || f.getName().endsWith(".tsv")) {
                try {
                    loader = new FXMLLoader(
                            TsvFileReader.class.getResource("TsvFileReader.fxml"),
                            OS.getResources());
                    loader.load();
                } catch (IOException ex) {
                    Logger.getLogger(CoatView.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }
            }
            if (loader == null) {
                return;
            }
            // Load file
            Reader reader = loader.getController();
            reader.setFile(f);
            // Create tab
            Tab t = new Tab(f.getName());
            t.setContent(loader.getRoot());
            t.setUserData(reader);
            // Add and select tab
            workspace.getTabs().add(t);
            workspace.getSelectionModel().select(t);
        }
    }

    @FXML
    private void saveAs(ActionEvent event) {
        if (!workspace.getSelectionModel().isEmpty()) {
            // workspace.getSelectionModel.getSelectedItem is a Tab
            // tab.getUserData is a VCFReader controller
            Reader reader = (Reader) workspace.getSelectionModel().getSelectedItem().getUserData();
            if (reader != null) {
                reader.saveAs();
            }
        }
    }

    @FXML
    private void combineVCF(ActionEvent event) {
        System.out.println("Combine VCF");
    }

    @FXML
    private void combineMIST(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(CombineMIST.class
                    .getResource("CombineMIST.fxml"), OS.getResources());
            Parent p = loader.load();
            Scene scene = new Scene(p);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(OS.getResources().getString("combine.mist"));
            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(CoatView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void printMessage(String message, String level) {
        String date = df.format(new Date());
        staticInfo.getStyleClass().clear();
        staticInfoBox.getStyleClass().clear();
        String type = level.toLowerCase();
        if (type.equals("info") || type.equals("success") || type.equals("warning")
                || type.equals("error")) {
            staticInfo.setText(date + ": " + message);
            staticInfo.getStyleClass().add(type + "-label");
            staticInfoBox.getStyleClass().add(type + "-box");
            staticInfo.setGraphic(new SizableImage("coat/img/" + type + ".png",
                    SizableImage.SMALL_SIZE));
        } else {
            staticInfo.setText(date + " (" + level + "): " + message);
        }
    }
}
