package coat.view.vcfcombiner;

import coat.model.tool.Tool;
import coat.model.tool.ToolMenu;
import coat.utils.OS;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class CombineVcfMenu implements ToolMenu {
    @Override
    public String getName() {
        return OS.getString("combine.vcf");
    }

    @Override
    public String getIconPath() {
        return "coat/img/documents_vcf.png";
    }

    @Override
    public Tool getTool() {
        return new CombineVCF();
    }
}
