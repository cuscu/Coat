package coat.view.vcfcombiner;

import coat.model.vcfreader.Variant;
import coat.view.vcfreader.Sample;
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

    private ObservableList<Sample> samples;
    private int size;
    private AtomicInteger count = new AtomicInteger();
    List<VariantStream> streams;

    /**
     * Creates a new VcfCombinerTask with the given list of samples.
     *
     * @param samples must not be null. Empty list is valid, although useless.
     */
    public VcfCombineTask(ObservableList<Sample> samples) {
        this.samples = samples;
    }

    @Override
    protected List<Variant> call() throws Exception {
        streams = samples.stream().filter(sample -> sample.getEnabledProperty().getValue()).map(VariantStream::new).collect(Collectors.toList());
        final VariantStream reference = getReferenceStream();
        if (reference == null) return Collections.emptyList();
        size = reference.getVariants().size();
        return reference.getVariants().stream().filter(this::filter).collect(Collectors.toList());
    }

    private boolean filter(Variant variant) {
        updateProgress(count.incrementAndGet(), size);
        return streams.stream().allMatch(stream -> stream.filter(variant));
    }

    private VariantStream getReferenceStream() {
        try {
            return streams.stream().filter(stream -> !stream.getSample().getLevel().equals(Sample.Level.UNAFFECTED)).findFirst().get();
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

}
