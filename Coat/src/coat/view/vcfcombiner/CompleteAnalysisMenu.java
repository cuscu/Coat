package coat.view.vcfcombiner;

import coat.model.tool.Tool;
import coat.model.tool.ToolMenu;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class CompleteAnalysisMenu implements ToolMenu {
    @Override
    public String getName() {
        return "Combine Vcf (enhanced)";
    }

    @Override
    public String getIconPath() {
        return "coat/img/documents_vcf.png";
    }

    @Override
    public Tool getTool() {
        return new CompleteAnalysis();
    }
}
