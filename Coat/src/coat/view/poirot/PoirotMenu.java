package coat.view.poirot;

import coat.model.tool.Tool;
import coat.model.tool.ToolMenu;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotMenu implements ToolMenu {
    @Override
    public String getName() {
        return "Poriot analysis";
    }

    @Override
    public String getIconPath() {
        return "coat/img/poirot.png";
    }

    @Override
    public Tool getTool() {
        return new PoirotView();
    }
}
