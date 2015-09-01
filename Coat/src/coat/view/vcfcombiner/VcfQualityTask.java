package coat.view.vcfcombiner;

import coat.model.vcfreader.Variant;
import coat.view.vcfreader.VcfSample;
import javafx.concurrent.Task;

import java.util.List;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VcfQualityTask extends Task<List<Variant>> {

    private final List<Variant> variants;
    private final List<VcfSample> samples;

    public VcfQualityTask(List<Variant> variants, List<VcfSample> samples) {
        this.variants = variants;
        this.samples = samples;
    }


    @Override
    protected List<Variant> call() throws Exception {
        return variants;
    }

}
