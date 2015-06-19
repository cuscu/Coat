package coat.view.vcf;

import coat.view.graphic.SizableImage;
import javafx.scene.control.ListCell;

/**
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
