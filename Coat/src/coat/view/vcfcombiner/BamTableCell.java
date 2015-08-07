package coat.view.vcfcombiner;

import coat.utils.FileManager;
import coat.utils.OS;
import coat.view.vcfreader.Sample;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

import java.io.File;

/**
 * Manages the Mist file TableCell in SampleTableView.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class BamTableCell extends TableCell<Sample, File> {

    final Button button = new Button("...");

    public BamTableCell() {
        button.setOnAction(event -> selectFile());
    }

    private void selectFile() {
        final File file = FileManager.openFile(OS.getString("choose.file"), FileManager.BAM_FILTER);
        if (file != null) ((Sample) getTableRow().getItem()).getBamFileProperty().setValue(file);
    }

    @Override
    protected void updateItem(File file, boolean empty) {
        super.updateItem(file, empty);
        if (!empty) {
            if (file != null) setText(file.getName());
            setGraphic(button);
        } else {
            setText(null);
            setGraphic(null);
        }
    }
}
