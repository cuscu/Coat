/******************************************************************************
 * Copyright (C) 2015 UICHUIMI                                                *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify it    *
 * under the terms of the GNU General Public License as published by the      *
 * Free Software Foundation, either version 3 of the License, or (at your     *
 * option) any later version.                                                 *
 *                                                                            *
 * This program is distributed in the hope that it will be useful, but        *
 * WITHOUT ANY WARRANTY; without even the implied warranty of                 *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 ******************************************************************************/

package coat.core.vcf.combine;

import coat.core.vcf.Variant;
import coat.core.vcf.VcfFile;
import coat.view.vcfreader.VcfSample;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

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
public class VcfCombineTask extends Task<VcfFile> {

    private ObservableList<VcfSample> vcfSamples;
    private int size;
    private AtomicInteger count = new AtomicInteger();
    private List<VariantStream> streams;

    /**
     * Creates a new VcfCombinerTask with the given list of samples.
     *
     * @param vcfSamples must not be null. Empty list is valid, although useless.
     */
    public VcfCombineTask(ObservableList<VcfSample> vcfSamples) {
        this.vcfSamples = vcfSamples;
    }

    @Override
    protected VcfFile call() throws Exception {
        try {
            createStreams();
            final VariantStream reference = getReferenceStream();
            if (reference == null) return null;
            size = reference.getVariants().size();
            final List<Variant> variants = reference.getVariants().stream().filter(this::filter).collect(Collectors.toList());
            reference.getVcfFile().getVariants().setAll(variants);
            return reference.getVcfFile();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
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
