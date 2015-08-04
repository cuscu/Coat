package coat.view.vcfcombiner;

import coat.model.vcfreader.Variant;
import coat.view.vcfreader.Sample;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VariantStream {

    private final Sample sample;
    private List<String> chromosomeOrder;
    private BufferedReader reader;
    private Variant variant;

    public VariantStream(Sample sample) {
        this.sample = sample;
        try {
            reader = new BufferedReader(new FileReader(sample.getFile()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        loadFirstVariant();
    }

    public VariantStream(Sample sample, List<String> chromosomeOrder) {
        this.sample = sample;
        this.chromosomeOrder = chromosomeOrder;
    }

    private void loadFirstVariant() {
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#")) {
                    variant = new Variant(line);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Sample getSample() {
        return sample;
    }

    public List<Variant> getVariants() {
        try (BufferedReader reader = new BufferedReader(new FileReader(sample.getFile()))) {
            return reader.lines().filter(line -> !line.startsWith("#")).map(Variant::new).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean filter(Variant variant) {
        Variant variant1 = this.variant;
        while (variant1 != null) {
            final int compareTo = compareVariants(variant, variant1);
            // The variant is present
            if (compareTo == 0) return checkZygotic(variant1);
                // The variant is lower
            else if (compareTo < 0) variant1 = nextVariant();
                // The variant is higher
            else break;
        }
        // The variant is not present
        return sample.getLevel() == Sample.Level.UNAFFECTED;
    }

    private int compareVariants(Variant variant, Variant variant1) {
        if (chromosomeOrder == null) return variant1.compareTo(variant);
        else return compare(variant, variant1);
    }

    private int compare(Variant variant, Variant variant1) {
        final int chromIndex1 = chromosomeOrder.indexOf(variant.getChrom());
        final int chromIndex2 = chromosomeOrder.indexOf(variant1.getChrom());
        // Variants with non-standard chromosomes go to the end
        if (chromIndex1 != -1 && chromIndex2 == -1) return -1;
        if (chromIndex1 == -1 && chromIndex2 != -1) return 1;
        // Non-standard chromosomes are ordered alphabetically
        int compare = (chromIndex1 == -1)
                ? variant.getChrom().compareTo(variant1.getChrom())
                : Integer.compare(chromIndex1, chromIndex2);
        if (compare != 0) return compare;
        return Integer.compare(variant.getPos(), variant1.getPos());
    }

    private String shortVariant(Variant variant) {
        return variant.getChrom() + ":" + variant.getPos() + " " + variant.getInfos().get("AF");
    }

    private boolean checkZygotic(Variant variant) {
        if (sample.getLevel() == Sample.Level.UNAFFECTED) return false;
        if (sample.getLevel() == Sample.Level.AFFECTED) return true;
        final String AF = (String) variant.getInfos().get("AF");
        if (AF.equals("0.500")) return sample.getLevel() == Sample.Level.HETEROZYGOUS;
        else return sample.getLevel() == Sample.Level.HOMOZYGOUS;
    }


    private Variant nextVariant() {
        if (variant == null) return null;
        try {
            final Variant v = variant;
            final String line = reader.readLine();
            variant = (line == null) ? null : new Variant(line);
            return v;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
