package coat.view.vcfreader;

import coat.view.vcfcombiner.ClassCell;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * View of a sample. A combobox with the level of affection (unaffected, heterocygous, homocygous or affected),
 * and a file name.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class SampleCell extends ListCell<Sample> {

    private ComboBox<Sample.Level> filter = new ComboBox<>();

    private Sample current;


    public SampleCell() {
        filter.setCellFactory(param -> new ClassCell());
        filter.getItems().addAll(Sample.Level.values());
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
            advancedTextLabel();
            setGraphic(filter);
            filter.getSelectionModel().select(item.getLevel());
        } else {
            setText(null);
            setGraphic(null);
        }
    }

    private void advancedTextLabel() {
        try (BufferedReader reader = new BufferedReader(new FileReader(current.getFile()))) {
            final long numberOfVariants = reader.lines().filter(line -> !line.startsWith("#")).count();
            setText(current.getFile().getAbsolutePath() + " (" + numberOfVariants + " variants)");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


