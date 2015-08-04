package coat.view.poirot;

import coat.model.tool.Tool;
import coat.model.tool.ToolMenu;
import coat.utils.OS;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotMenu implements ToolMenu {
    @Override
    public String getName() {
        return OS.getResources().getString("poirot.analysis");
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
