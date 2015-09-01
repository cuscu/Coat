package coat.model.vcfcombiner;

import coat.model.vcfreader.Variant;
import coat.view.vcfcombiner.VariantStream;
import coat.view.vcfreader.VcfSample;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Task that performs the Combine process of several Vcf files. Files must be enclosed in Sample objects,
 * to determine how each one participates.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VcfCombineTask extends Task<List<Variant>> {

    private ObservableList<VcfSample> vcfSamples;
    private int size;
    private AtomicInteger count = new AtomicInteger();
    List<VariantStream> streams;

    /**
     * Creates a new VcfCombinerTask with the given list of samples.
     *
     * @param vcfSamples must not be null. Empty list is valid, although useless.
     */
    public VcfCombineTask(ObservableList<VcfSample> vcfSamples) {
        this.vcfSamples = vcfSamples;
    }

    @Override
    protected List<Variant> call() throws Exception {
        createStreams();
        final VariantStream reference = getReferenceStream();
        if (reference == null) return Collections.emptyList();
        size = reference.getVariants().size();
        return reference.getVariants().stream().filter(this::filter).collect(Collectors.toList());
    }

    private void createStreams() {
        streams = vcfSamples.stream()
                .filter(sample -> sample.enabledProperty().getValue())
                .map(VariantStream::new).collect(Collectors.toList());
    }

    private boolean filter(Variant variant) {
        updateProgress(count.incrementAndGet(), size);
        return streams.stream().allMatch(stream -> stream.filter(variant));
    }

    private VariantStream getReferenceStream() {
        try {
            return streams.stream().filter(stream -> !stream.getVcfSample().getLevel().equals(VcfSample.Level.UNAFFECTED)).findFirst().get();
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

}
