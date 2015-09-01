package coat.view.vcfcombiner;

import coat.model.vcfreader.Variant;
import coat.view.vcfreader.VcfSample;
import javafx.concurrent.Task;

import java.util.List;

/**
 * This task will score each variant with metadata generated from the bam and mist files related with the samples
 * genetic information.
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 * @author Jacob
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
