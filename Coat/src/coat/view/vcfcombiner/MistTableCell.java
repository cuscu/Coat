package coat.view.vcfcombiner;

import coat.utils.FileManager;
import coat.utils.OS;
import coat.view.vcfreader.VcfSample;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

import java.io.File;

/**
 * Manages the Mist file TableCell in VcfSampleTableView.
 * <p>
 * TODO: This class was tried to merge with BamTableCell, but code <code>startEdit();commitEdit(file);</code> throws NullPointerException
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
class MistTableCell extends TableCell<VcfSample, File> {

    private final Button button = new Button("...");

    public MistTableCell() {
        button.setOnAction(event -> selectFile());
    }

    private void selectFile() {
        final File file = FileManager.openFile(OS.getString("choose.file"), FileManager.MIST_FILTER);
        if (file != null) ((VcfSample) getTableRow().getItem()).mistFileProperty().setValue(file);
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
