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

import coat.combinevcf.CombineVCF;
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
import javafx.scene.layout.BorderPane;
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
    BorderPane root;

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

    private final TabPane workspace = new TabPane();

    public void initialize() {
        root.setCenter(workspace);
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
                if (reader == null || reader.getActions() == null) {
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
    private void openAFile(ActionEvent event) {
        File f = FileManager.openFile(OS.getResources().getString("choose.file"),
                FileManager.VCF_FILTER, FileManager.MIST_FILTER, FileManager.TSV_FILTER);
        if (f != null) {
//            workspace1.open(f);
            openFileInWorkspace(f);
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
        try {
            FXMLLoader loader = new FXMLLoader(CombineVCF.class.getResource("CombineVCF.fxml"), OS.getResources());
            Tab t = new Tab("Combine VCF");
            t.setContent(loader.load());
            workspace.getTabs().add(t);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void openFileInWorkspace(File f) {
        if (isOpenInWorkspace(f)) {
            selectTabInWorkspace(f);
        } else {
            addFileTabToWorkspace(f);
        }

    }

    private boolean isOpenInWorkspace(File f) {
        return workspace.getTabs().stream().anyMatch(tab -> (tab.getUserData().equals(f)));
    }

    private void selectTabInWorkspace(File f) {
        workspace.getTabs().stream().filter(tab -> (tab.getUserData().equals(f))).forEach(tab
                -> workspace.getSelectionModel().select(tab));
    }

    private void addFileTabToWorkspace(File f) {
        if (isVCF(f)) {
            openVCFInWorkspace(f);
        } else if (isTSV(f)) {
            openTSVInWorkspace(f);
        }
    }

    private boolean isVCF(File f) {
        return f.getName().endsWith(".vcf");
    }

    private void openVCFInWorkspace(File f) {
        try {
            addVCFReaderToWorkspace(f);
        } catch (IOException ex) {
            Logger.getLogger(CoatView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean isTSV(File f) {
        return f.getName().endsWith(".mist") || f.getName().endsWith(".tsv");
    }

    private void openTSVInWorkspace(File f) {
        try {
            addTSVReaderToWorkspace(f);
        } catch (IOException ex) {
            Logger.getLogger(CoatView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addVCFReaderToWorkspace(File f) throws IOException {
        FXMLLoader loader = new FXMLLoader(VcfFileReader.class.getResource("VcfFileReader.fxml"),
                OS.getResources());
        loader.load();
        // Load file
        Reader reader = loader.getController();
        reader.setFile(f);
        addReaderToWorkspace(loader);

    }

    private void addTSVReaderToWorkspace(File f) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                TsvFileReader.class.getResource("TsvFileReader.fxml"),
                OS.getResources());
        loader.load();
        // Load file
        Reader reader = loader.getController();
        reader.setFile(f);
        addReaderToWorkspace(loader);
    }

    private void addReaderToWorkspace(FXMLLoader loader) {
        Reader reader = loader.getController();
        Tab t = new Tab(reader.getFile().getName());
        t.setContent(loader.getRoot());
        t.setUserData(reader);
        // Add and select tab
        workspace.getTabs().add(t);
        workspace.getSelectionModel().select(t);
    }
}
