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

package coat.core.poirot;

import coat.core.poirot.dataset.biogrid.BioGridRelator;
import coat.core.poirot.dataset.biogrid.Relator;
import coat.core.poirot.dataset.hgnc.HGNCDatabase;
import coat.core.poirot.dataset.hprd.HPRDExpressionRelator;
import coat.core.poirot.dataset.hprd.HPRDRelator;
import coat.core.poirot.dataset.mentha.MenthaRelator;
import coat.core.poirot.dataset.omim.OmimRelator;
import coat.core.vcfreader.Variant;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Task (a long task) that analyzes a VCF file and a list of phenotype key words and returns a PearlDatabase with the
 * gene-gene-phenotype graph associated to the variants.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotGraphAnalysis extends Task<PearlDatabase> {

    public static final int CYCLES = 2;

    private final Relator[] relators = new Relator[]{
            new OmimRelator(),
            new HPRDExpressionRelator(),
            new BioGridRelator(),
            new MenthaRelator(),
            new HPRDRelator()
    };

    private final List<Variant> variants;

    private final PearlDatabase database = new PearlDatabase();
    private final List<Pearl> leafGenes = new ArrayList<>();
    private int processed;
    private int round;
    private int total;
    private final static List<String> GENE_BLACKLIST = Arrays.asList("UBC");

    /**
     * Creates a new PoirotAnalysis task, ready to be inserted in a Thread, or launched with <code>Platform</code>
     *
     * @param variants the list of variants
     */
    public PoirotGraphAnalysis(List<Variant> variants) {
        this.variants = variants;
    }

    @Override
    protected PearlDatabase call() throws Exception {
        addInitialGenes();
        expandGraph();
        return database;
    }


    private void addInitialGenes() {
        variants.forEach(variant -> {
            final String gene = (String) variant.getInfos().get("GNAME");
            if (gene != null) addVariantGeneToDatabase(variant, gene);
        });
        leafGenes.addAll(database.getPearls("gene"));
    }

    private void addVariantGeneToDatabase(Variant variant, String gene) {
        final String GENE = HGNCDatabase.getStandardSymbol(gene);
        final Pearl genePearl = database.getOrCreate(GENE, "gene");
        genePearl.getProperties().putIfAbsent("variants", new ArrayList<>());
        final List<Variant> geneVariants = (List<Variant>) genePearl.getProperties().get("variants");
        geneVariants.add(variant);
    }

    private void expandGraph() {
        for (int i = 0; i < CYCLES; i++) {
            final List<Pearl> pearlsToExpand = new ArrayList<>(leafGenes);
            leafGenes.clear();
            resetProgress(i, pearlsToExpand.size());
            pearlsToExpand.forEach(this::expand);
        }
    }

    private void resetProgress(int i, int total) {
        processed = 0;
        round = i;
        this.total = total;
    }

    private void expand(Pearl pearl) {
        printProgress();
        connect(pearl);
    }

    private void printProgress() {
        if (++processed % 100 == 0)
            updateMessage(String.format("Round %d of %d, %d/%d genes processed", round, 2, processed, total));

    }

    private void connect(Pearl pearl) {
        Arrays.stream(relators).forEach(relator -> relator.expand(pearl, database));
    }


    public static boolean notInBlacklist(String symbol) {
        return !GENE_BLACKLIST.contains(symbol);
    }

}
