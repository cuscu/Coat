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

import coat.model.reader.Reader;
import coat.model.tool.Tool;
import coat.model.tool.ToolMenu;
import coat.model.vcfreader.VcfFile;
import coat.utils.FileManager;
import coat.utils.OS;
import coat.view.graphic.MemoryPane;
import coat.view.graphic.SizableImage;
import coat.view.mist.CombineMistMenu;
import coat.view.poirot.PoirotMenu;
import coat.view.tsv.TsvFileReader;
import coat.view.vcfcombiner.CombineVcfMenu;
import coat.view.vcfcombiner.CompleteAnalysisMenu;
import coat.view.vcfreader.VcfReader;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class CoatView {

    @FXML
    private Menu toolsMenu;
    @FXML
    private MemoryPane memoryPane;
    @FXML
    private MenuItem openFileMenu;
    @FXML
    private MenuItem saveFileMenu;
    @FXML
    private BorderPane root;
    @FXML
    private MenuBar menu;
    @FXML
    private Label info;

    private final List<ToolMenu> toolsMenuClasses = new ArrayList<>();

    {
        toolsMenuClasses.add(new CombineMistMenu());
        toolsMenuClasses.add(new CombineVcfMenu());
        toolsMenuClasses.add(new CompleteAnalysisMenu());
        toolsMenuClasses.add(new PoirotMenu());
    }

    private static VBox bigConsole = new VBox();

    private WebView webView = new WebView();

    private Menu customMenu;

    private static Label staticInfo;

    private final static DateFormat df = new SimpleDateFormat("HH:mm:ss");

    private final TabPane workspace = new TabPane();

    private final static List<String> AVAILABLE_MESSAGE_TYPES = Arrays.asList("info", "error", "success", "warning");

    public void initialize() {
        createToolsMenu();
        root.setCenter(workspace);
        staticInfo = info;
        staticInfo.setOnMouseClicked(event -> showBigConsole());
        // Listen whe user clicks on a tab, or opens a file
        workspace.getSelectionModel().selectedItemProperty().addListener((obs, old, current) -> tabSelected(current));
        // By default is disabled
        saveFileMenu.setDisable(true);
        assignMenuIcons();
        printMessage(System.getProperty("user.dir"), "info");
    }

    private void createToolsMenu() {
        toolsMenuClasses.forEach(toolMenu -> {
            final MenuItem menuItem = new MenuItem(toolMenu.getName(), new SizableImage(toolMenu.getIconPath(), SizableImage.SMALL_SIZE));
            toolsMenu.getItems().add(menuItem);
            menuItem.setOnAction(event -> show(toolMenu));
        });
    }

    private void tabSelected(Tab current) {
        // Remove custom menu if it exists
        menu.getMenus().remove(customMenu);
        if (current != null) {
            // Try to load custom tab
            Reader reader = (Reader) current.getUserData();
            if (reader == null) return;

            if (reader.getActions() != null) {
                // Load buttons in the custom tab
                final FlowPane pane = new FlowPane();
                pane.getChildren().setAll(reader.getActions());
                customMenu = new Menu(reader.getActionsName());
                reader.getActions().forEach(button -> customMenu.getItems().add(getMenuItem(button)));
                menu.getMenus().add(customMenu);
            }
            // Activate save file
            saveFileMenu.setDisable(false);

            Coat.setTitle(reader.getTitle().getValue());
        } else
            // If no tab is selected, save button should be disabled
            saveFileMenu.setDisable(true);
    }

    private void show(ToolMenu toolMenu) {
        final Tool tool = toolMenu.getTool();
        final Tab tab = new Tab();
        tab.setContent(tool);
        tab.textProperty().bind(tool.getTitleProperty());
        workspace.getTabs().add(tab);
        workspace.getSelectionModel().select(tab);
    }

    private void startWeb() {
        workspace.getTabs().add(new Tab("HTML", webView));
        // path in current directory for WebEngine.load()
        final String html = "Coat/graph/index.html";
        final URI uri = Paths.get(html).toAbsolutePath().toUri();

        webView.getEngine().load(uri.toString());

    }

    private MenuItem getMenuItem(Button button) {
        MenuItem menuItem = new MenuItem(button.getText());
        menuItem.setOnAction(button.getOnAction());
        menuItem.setGraphic(button.getGraphic());
        return menuItem;
    }

    private void assignMenuIcons() {
        openFileMenu.setGraphic(new SizableImage("coat/img/open.png", SizableImage.SMALL_SIZE));
        saveFileMenu.setGraphic(new SizableImage("coat/img/save.png", SizableImage.SMALL_SIZE));
    }

    @FXML
    private void openAFile(ActionEvent event) {
        File f = FileManager.openFile(OS.getResources().getString("choose.file"),
                FileManager.VCF_FILTER, FileManager.MIST_FILTER, FileManager.TSV_FILTER);
        if (f != null) openFileInWorkspace(f);
    }

    @FXML
    private void saveAs(ActionEvent event) {
        if (!workspace.getSelectionModel().isEmpty()) {
            // workspace.getSelectionModel.getSelectedItem is a Tab
            // tab.getUserData is a VCFReader controller
            Reader reader = (Reader) workspace.getSelectionModel().getSelectedItem().getUserData();
            if (reader != null) reader.saveAs();
        }
    }

    public static void printMessage(String message, String level) {
        final String date = df.format(new Date());
        final Label label = new Label();
        final String type = level.toLowerCase();
        staticInfo.getStyleClass().clear();
        setMessageLabel(message, date, staticInfo, type);
        setMessageLabel(message, date, label, type);
        bigConsole.getChildren().add(label);
    }

    private static void setMessageLabel(String message, String date, Label label, String type) {
        if (AVAILABLE_MESSAGE_TYPES.contains(type)) {
            label.setText(date + ": " + message);
            label.setGraphic(new SizableImage("coat/img/" + type + ".png", SizableImage.SMALL_SIZE));
            label.getStyleClass().add(type + "-label");
        } else label.setText(date + " (" + type + "): " + message);
    }

    private void openFileInWorkspace(File f) {
        if (isOpenInWorkspace(f)) selectTabInWorkspace(f);
        else addFileTabToWorkspace(f);
    }

    private boolean isOpenInWorkspace(File f) {
        return workspace.getTabs().stream().anyMatch(tab -> tab.getUserData() != null && tab.getUserData().equals(f));
    }

    private void selectTabInWorkspace(File f) {
        workspace.getTabs().stream().filter(tab -> (tab.getUserData().equals(f))).forEach(tab
                -> workspace.getSelectionModel().select(tab));
    }

    private void addFileTabToWorkspace(File f) {
        if (isVCF(f)) openVCFInWorkspace(f);
        else if (isTSV(f)) openTSVInWorkspace(f);
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
        printMessage("Loading " + f, "info");
        Task<VcfFile> vcfLoader = new Task<VcfFile>() {
            @Override
            protected VcfFile call() throws Exception {
                return new VcfFile(f);
            }
        };
        vcfLoader.setOnSucceeded(event -> {
            VcfFile vcfFile = vcfLoader.getValue();
            VcfReader vcfReader = new VcfReader(vcfFile);
            addReaderToWorkspace(vcfReader, vcfReader);
            printMessage(f + " loaded", "success");
        });
        new Thread(vcfLoader).start();
    }

    private void addTSVReaderToWorkspace(File f) throws IOException {
        TsvFileReader tsvFileReader = new TsvFileReader(f);
        addReaderToWorkspace(tsvFileReader, tsvFileReader);
    }

    private void addReaderToWorkspace(Reader reader, Node content) {
        Tab t = new Tab();
        t.textProperty().bind(reader.getTitle());
        t.setContent(content);
        t.setUserData(reader);
        // Add and select tab
        workspace.getTabs().add(t);
        workspace.getSelectionModel().select(t);
    }

    private void showBigConsole() {
        final ScrollPane pane = new ScrollPane(bigConsole);
        Scene scene = new Scene(pane);
        Stage stage = new Stage();
        stage.setWidth(600);
        stage.setHeight(600);
        stage.setTitle("Messages");
        stage.centerOnScreen();
        scene.getStylesheets().addAll("coat/main.css");
        stage.setScene(scene);
        stage.show();
    }
}
