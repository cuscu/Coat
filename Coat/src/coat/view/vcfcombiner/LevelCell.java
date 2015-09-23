package coat.view.vcfcombiner;

import coat.view.graphic.SizableImage;
import coat.view.vcfreader.VcfSample;
import javafx.scene.control.ListCell;

/**
 * Cell for ListView that shows the level of affection. The image is taken from coat/img/ plus level name plus .png.
 * For instance: HETEROZYGOUS -> coat/img/heterozygous.png
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
class LevelCell extends ListCell<VcfSample.Level> {

    @Override
    protected void updateItem(VcfSample.Level item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            setText(item.toString());
            setGraphic(new SizableImage("coat/img/" + item.name().toLowerCase() + ".png", SizableImage.SMALL_SIZE));
        } else {
            setText(null);
            setGraphic(null);
        }
    }
}
