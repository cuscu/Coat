package coat.view.vcfreader;

import coat.view.vcfcombiner.ClassCell;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

/**
 * View of a sample. A combobox with the level of affection (unaffected, heterocygous, homocygous or affected),
 * and a file name.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class SampleCell extends ListCell<Sample> {

    private ComboBox<String> filter = new ComboBox<>();

    private Sample current;

    public SampleCell() {
        filter.setCellFactory(param -> new ClassCell());
        filter.getItems().addAll("unaffected", "heterocygous", "homocygous", "affected");
        filter.setOnAction(event -> levelChanged());
        filter.setButtonCell(new ClassCell());
    }

    private void levelChanged() {
        if (current != null) current.setLevel(filter.getSelectionModel().getSelectedItem());
    }

    @Override
    protected void updateItem(Sample item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            current = item;
            setText(item.getFile().getAbsolutePath());
            setGraphic(filter);
            filter.getSelectionModel().select(item.getLevel());
        } else {
            setText(null);
            setGraphic(null);
        }
    }
}
