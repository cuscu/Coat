package coat.view.vcfcombiner;

import coat.view.vcfreader.VcfSample;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;

/**
 * Cell for the VcfSampleTableView which shows the level of affection of the sample.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class LevelComboBoxCell extends TableCell<VcfSample, VcfSample.Level> {

    private final ComboBox<VcfSample.Level> levelComboBox = new ComboBox<>(FXCollections.observableArrayList(VcfSample.Level.values()));
    private VcfSample current;

    public LevelComboBoxCell() {
        levelComboBox.setCellFactory(param -> new LevelCell());
        levelComboBox.setButtonCell(new LevelCell());
    }

    @Override
    protected void updateItem(VcfSample.Level item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            final VcfSample vcfSample = (VcfSample) ((getTableRow() != null) ? getTableRow().getItem() : null);
            if (current != null) levelComboBox.valueProperty().unbindBidirectional(current.levelProperty());
            if (vcfSample != null) levelComboBox.valueProperty().bindBidirectional(vcfSample.levelProperty());
            current = vcfSample;
            setGraphic(levelComboBox);
        } else {
            setGraphic(null);
        }
    }

}
