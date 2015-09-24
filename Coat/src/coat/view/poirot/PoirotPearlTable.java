package coat.view.poirot;

import coat.model.poirot.Pearl;
import coat.view.graphic.IndexCell;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotPearlTable extends  TableView<Pearl> {

//    private final TableView<Pearl> pearlTableView = new TableView<>();
    private final TableColumn<Pearl, String> scoreColumn = new TableColumn<>("Score");
    private final TableColumn<Pearl, Integer> indexColumn = new TableColumn<>("*");
    private final TableColumn<Pearl, Integer> distanceColumn = new TableColumn<>("Dist");
    private final TableColumn<Pearl, String> nameColumn = new TableColumn<>("Name");


    PoirotPearlTable() {
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        getColumns().addAll(indexColumn, distanceColumn, scoreColumn, nameColumn);
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        final MenuItem menuItem = new MenuItem("Copy");
        final ContextMenu menu = new ContextMenu(menuItem);
        setContextMenu(menu);
        menuItem.setOnAction(event -> copy());
        distanceColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getDistanceToPhenotype()));
        scoreColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(String.format("%.2f", param.getValue().getScore())));
        nameColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getName()));
        indexColumn.setCellFactory(param -> new IndexCell());

    }

    private void copy() {
        final StringBuilder builder = new StringBuilder();
        getSelectionModel().getSelectedItems()
                .forEach(pearl -> builder
                        .append(pearl.getName()).append("\t")
                        .append(String.format("%.2f", pearl.getScore())).append("\t")
                        .append(pearl.getDistanceToPhenotype()).append("\n"));
        final ClipboardContent content = new ClipboardContent();
        content.putString(builder.toString());
        Clipboard.getSystemClipboard().setContent(content);
    }
}
