package coat.view.vcfcombiner;

import coat.view.vcfreader.VcfSample;
import javafx.scene.control.cell.CheckBoxTableCell;

/**
 * Created by Pascual on 14/08/2015.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class SampleCheckBoxTableCell extends CheckBoxTableCell<VcfSample, Boolean> {

    @Override
    public void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            final VcfSample vcfSample = (VcfSample) getTableRow().getItem();
            if (vcfSample != null) setText(vcfSample.getFile().getName());
        } else setText(null);
    }
}
