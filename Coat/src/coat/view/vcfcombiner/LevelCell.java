package coat.view.vcfcombiner;

import coat.view.vcfreader.Sample;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class LevelCell extends TableCell<Sample, Sample.Level> {

    private final ComboBox<Sample.Level> levelComboBox = new ComboBox<>(FXCollections.observableArrayList(Sample.Level.values()));
    private Sample current;

    public LevelCell() {
        levelComboBox.setCellFactory(param -> new ClassCell());
        levelComboBox.setButtonCell(new ClassCell());
    }

    @Override
    protected void updateItem(Sample.Level item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            final Sample sample = (Sample) ((getTableRow() != null) ? getTableRow().getItem() : null);
            if (current != null) levelComboBox.valueProperty().unbindBidirectional(current.getLevelProperty());
            if (sample != null) levelComboBox.valueProperty().bindBidirectional(sample.getLevelProperty());
            current = sample;
            setGraphic(levelComboBox);
        } else {
            setGraphic(null);
        }
    }

}
