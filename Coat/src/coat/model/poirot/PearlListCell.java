package coat.model.poirot;

import javafx.scene.control.ListCell;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PearlListCell extends ListCell<Pearl> {

    @Override
    protected void updateItem(Pearl pearl, boolean empty) {
        super.updateItem(pearl, empty);
        setText(empty ? null : String.format("[%d, %.1f] %s", pearl.getDistanceToPhenotype(), pearl.getScore(), pearl.getName()));
    }
}
