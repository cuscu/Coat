package coat.view.vcfcombiner;

import coat.view.vcfreader.Sample;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;

/**
 * Cell for the SampleTableView which shows the level of affection of the sample.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class LevelComboBoxCell extends TableCell<Sample, Sample.Level> {

    private final ComboBox<Sample.Level> levelComboBox = new ComboBox<>(FXCollections.observableArrayList(Sample.Level.values()));
    private Sample current;

    public LevelComboBoxCell() {
        levelComboBox.setCellFactory(param -> new LevelCell());
        levelComboBox.setButtonCell(new LevelCell());
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
