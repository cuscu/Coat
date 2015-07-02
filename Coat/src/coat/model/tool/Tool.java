package coat.model.tool;

import javafx.beans.property.Property;
import javafx.scene.layout.VBox;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public abstract class Tool extends VBox {

    public abstract Property<String> getTitleProperty();

    /**
     * Implement this method if you want that something happens when user clicks on File->Save as...
     */
    public void saveAs() {

    }

}
