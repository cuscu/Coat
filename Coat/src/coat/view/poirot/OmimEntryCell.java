package coat.view.poirot;

import javafx.scene.control.ListCell;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
class OmimEntryCell extends ListCell<OmimPhenotype> {

    @Override
    protected void updateItem(OmimPhenotype item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) setText(null);
        else setText(String.format("%d %s (%d)", item.getNumber(), item.getName(), item.getMappingKey()));
    }
}
