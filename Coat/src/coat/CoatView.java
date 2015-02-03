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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
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
    private Menu vcfMenu;
    @FXML
    private MenuItem openMenu;
    @FXML
    private MenuItem saveMenu;
    @FXML
    private MenuItem exitMenu;
    @FXML
    private MenuItem headersMenu;
    @FXML
    private MenuItem lfsMenu;

    public void initialize() {
        // Listen whe user clicks on a tab, or opens a file
        workspace.getSelectionModel().selectedItemProperty().addListener((obs, old, current) -> {
            File f = (File) current.getUserData();
            // Change title of application
            Coat.setTitle(f.getName());
            // Show/hide VCF menu
            if (f.getName().endsWith(".vcf")) {
                vcfMenu.setVisible(true);
            } else {
                vcfMenu.setVisible(false);
            }

        });
        assignMenuIcons();
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
                t.setContent(new VCFReader(f));
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
            if (workspace.getSelectionModel().getSelectedItem().getContent().getClass().equals(VCFReader.class)) {
                VCFReader reader = (VCFReader) workspace.getSelectionModel().getSelectedItem().getContent();
                reader.saveAs();
            } else {
                TSVReader reader = (TSVReader) workspace.getSelectionModel().getSelectedItem().getContent();
                reader.saveAs();
            }
        }
    }

    @FXML
    private void exit(ActionEvent event) {
    }

    private void assignMenuIcons() {
        exitMenu.setGraphic(new SizableImage("coat/img/exit.png", SizableImage.SMALL_SIZE));
        openMenu.setGraphic(new SizableImage("coat/img/open.png", SizableImage.SMALL_SIZE));
        saveMenu.setGraphic(new SizableImage("coat/img/save.png", SizableImage.SMALL_SIZE));
    }

}
