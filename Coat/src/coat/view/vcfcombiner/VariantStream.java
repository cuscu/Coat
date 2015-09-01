package coat.view.vcfcombiner;

import coat.model.vcfreader.Variant;
import coat.view.vcfreader.VcfSample;

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

    private final VcfSample vcfSample;
    private BufferedReader reader;
    private Variant variant;

    public VariantStream(VcfSample vcfSample) {
        this.vcfSample = vcfSample;
        try {
            reader = new BufferedReader(new FileReader(vcfSample.getVcfFile()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        loadFirstVariant();
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

    public VcfSample getVcfSample() {
        return vcfSample;
    }

    public List<Variant> getVariants() {
        try (BufferedReader reader = new BufferedReader(new FileReader(vcfSample.getVcfFile()))) {
            return reader.lines().filter(line -> !line.startsWith("#")).map(Variant::new).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean filter(Variant variant) {
        Variant currentVariant = this.variant;
        while (currentVariant != null) {
            final int compareTo = currentVariant.compareTo(variant);
            // The variant is present
            if (compareTo == 0) return checkZygotic(currentVariant);
                // Current variant is lower
            else if (compareTo < 0) currentVariant = nextVariant();
                // Current variant is higher
            else break;
        }
        // The variant is not present
        return vcfSample.getLevel() == VcfSample.Level.UNAFFECTED;
    }

    private boolean checkZygotic(Variant variant) {
        if (vcfSample.getLevel() == VcfSample.Level.UNAFFECTED) return false;
        if (vcfSample.getLevel() == VcfSample.Level.AFFECTED) return true;
        final String AF = (String) variant.getInfos().get("AF");
        if (AF.equals("0.500")) return vcfSample.getLevel() == VcfSample.Level.HETEROZYGOUS;
        else return vcfSample.getLevel() == VcfSample.Level.HOMOZYGOUS;
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
