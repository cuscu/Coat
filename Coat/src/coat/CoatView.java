/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coat;

import coat.graphic.SizableImage;
import coat.tsv.TSVReader;
import coat.utils.FileManager;
import coat.utils.OS;
import coat.vcf.VCFReader;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 *
 * @author Lorente Arencibia, Pascual <pasculorente@gmail.com>
 */
public class CoatView {

    @FXML
    private TabPane workspace;
    @FXML
    private Button openFile;
    @FXML
    private Button saveFile;
    @FXML
    private Button viewHeaders;
    @FXML
    private Button addLFS;
    @FXML
    private Tab vcfTab;
    @FXML
    private TabPane menu;

    public void initialize() {
        // Listen whe user clicks on a tab, or opens a file
        workspace.getSelectionModel().selectedItemProperty().addListener((obs, old, current) -> {
            File f = (File) current.getUserData();
            // Change title of application
            Coat.setTitle(f.getName());
            // Show/hide VCF menu
            if (f.getName().endsWith(".vcf")) {
                if (!menu.getTabs().contains(vcfTab)) {
                    menu.getTabs().add(vcfTab);
                }
            } else {
                menu.getTabs().remove(vcfTab);
            }
        });
        menu.getTabs().remove(vcfTab);
        assignMenuIcons();
    }

    private void assignMenuIcons() {
        openFile.setGraphic(new SizableImage("coat/img/open.png", SizableImage.MEDIUM_SIZE));
        saveFile.setGraphic(new SizableImage("coat/img/save.png", SizableImage.MEDIUM_SIZE));
        viewHeaders.setGraphic(new SizableImage("coat/img/headers.png", SizableImage.MEDIUM_SIZE));
        addLFS.setGraphic(new SizableImage("coat/img/lfs.png", SizableImage.MEDIUM_SIZE));

    }

    @FXML
    private void open(ActionEvent event) {
        File f = FileManager.openFile(OS.getResources().getString("choose.file"),
                FileManager.VCF_FILTER, FileManager.MIST_FILTER,
                FileManager.TSV_FILTER, FileManager.ALL_FILTER);
        if (f != null) {
            // Check if the file is already opened
            for (Tab t : workspace.getTabs()) {
                if (t.getUserData().equals(f)) {
                    workspace.getSelectionModel().select(t);
                    return;
                }
            }
            if (f.getName().endsWith(".vcf")) {
                // VCF files
                Tab t = new Tab(f.getName());
                t.setUserData(f);
                t.setContent(createVCFReader(f));
                workspace.getTabs().add(t);
                workspace.getSelectionModel().select(t);
            } else if (f.getName().endsWith(".mist") || f.getName().endsWith(".tsv")) {
                // MIST and TSV files
                Tab t = new Tab(f.getName());
                t.setUserData(f);
                t.setContent(new TSVReader(f));
                workspace.getTabs().add(t);
                workspace.getSelectionModel().select(t);
            }
        }
    }

    @FXML
    private void saveAs(ActionEvent event) {
        if (!workspace.getSelectionModel().isEmpty()) {
            if (workspace.getSelectionModel().getSelectedItem().getContent().getUserData().getClass().equals(VCFReader.class)) {
                VCFReader reader = (VCFReader) workspace.getSelectionModel().getSelectedItem().getContent().getUserData();
                reader.saveAs();
            } else {
                TSVReader reader = (TSVReader) workspace.getSelectionModel().getSelectedItem().getContent().getUserData();
                reader.saveAs();
            }
        }
    }

    @FXML
    private void viewHeaders(ActionEvent event) {
        if (!workspace.getSelectionModel().isEmpty()) {
            if (workspace.getSelectionModel().getSelectedItem().getContent().getUserData().getClass().equals(VCFReader.class)) {
                VCFReader reader = (VCFReader) workspace.getSelectionModel().getSelectedItem().getContent().getUserData();
                reader.viewHeaders();
            }
        }
    }

    @FXML
    private void addLFS(ActionEvent event) {
        if (!workspace.getSelectionModel().isEmpty()) {
            if (workspace.getSelectionModel().getSelectedItem().getContent().getUserData().getClass().equals(VCFReader.class)) {
                VCFReader reader = (VCFReader) workspace.getSelectionModel().getSelectedItem().getContent().getUserData();
                reader.addLFS();
            }
        }

    }

    /**
     * Returns a node that contains the main node of a new VCFReader, where the userData is the
     * controller.
     *
     * @param f
     * @return
     */
    private Node createVCFReader(File f) {
        FXMLLoader loader = new FXMLLoader(VCFReader.class.getResource("VCFReader.fxml"), OS.getResources());
        try {
            Node n = loader.load();
            // Associate file to the controller
            VCFReader controller = loader.getController();
            controller.setVcfFile(f);
            // Include controller as node's userData property
            n.setUserData(controller);
            return n;
        } catch (IOException ex) {
            Logger.getLogger(CoatView.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
