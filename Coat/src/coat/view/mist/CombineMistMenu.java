package coat.view.mist;

import coat.model.tool.Tool;
import coat.model.tool.ToolMenu;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class CombineMistMenu implements ToolMenu {


    @Override
    public String getName() {
        return "Combine Mist";
    }

    @Override
    public Tool getTool() {
        return new CombineMIST();
    }

    @Override
    public String getIconPath() {
        return "coat/img/documents_mist.png";
    }
}
