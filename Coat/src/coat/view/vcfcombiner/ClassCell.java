package coat.view.vcfcombiner;

import coat.view.graphic.SizableImage;
import javafx.scene.control.ListCell;

/**
 * Simply holds an image taken from coat/img/. The image is the string item plus .png. So image is coat/img/item.png
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ClassCell extends ListCell<String> {

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            setText(item);
            setGraphic(new SizableImage("coat/img/" + item + ".png", SizableImage.SMALL_SIZE));
        } else {
            setText(null);
            setGraphic(null);
        }
    }
}
