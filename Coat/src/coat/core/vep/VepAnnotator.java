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

package coat.core.vep;

import coat.json.JSONArray;
import coat.json.JSONException;
import coat.json.JSONObject;
import javafx.concurrent.Task;
import org.jetbrains.annotations.NotNull;
import vcf.Variant;
import vcf.VcfFile;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VepAnnotator extends Task<Boolean> {

    public final static String[] HEADERS = {
            "##INFO=<ID=GENE,Number=1,Type=String,Description=\"Ensemble gene ID\">",
            "##INFO=<ID=FEAT,Number=1,Type=String,Description=\"Ensemble feature ID\">",
            "##INFO=<ID=TYPE,Number=1,Type=String,Description=\"Type of feature (Transcript, RegulatoryFeature, MotifFeature)\">",
            "##INFO=<ID=CONS,Number=1,Type=String,Description=\"Consequence type\">",
            "##INFO=<ID=CDNA,Number=1,Type=Integer,Description=\"Relative position of base pair in cDNA sequence\">",
            "##INFO=<ID=CDS,Number=1,Type=Integer,Description=\"Relative position of base pair in coding sequence\">",
            "##INFO=<ID=PROT,Number=1,Type=Integer,Description=\"Relative position of amino acid in protein\">",
            "##INFO=<ID=AMINO,Number=1,Type=String,Description=\"Amino acid change. Only given if the variation affects the protein-coding sequence\">",
            "##INFO=<ID=COD,Number=1,Type=String,Description=\"The alternative codons\">",
            "##INFO=<ID=DIST,Number=1,Type=String,Description=\"Shortest distance from vcf to transcript\">",
            "##INFO=<ID=STR,Number=1,Type=String,Description=\"The DNA strand (1 or -1) on which the transcript/feature lies\">",
            "##INFO=<ID=SYMBOL,Number=1,Type=String,Description=\"Gene symbol or name\">",
            "##INFO=<ID=SRC,Number=1,Type=String,Description=\"The source of the gene symbol\">",
            "##INFO=<ID=ENSP,Number=1,Type=String,Description=\"Ensembl protein identifier of the affected transcript\">",
            "##INFO=<ID=SWPR,Number=1,Type=String,Description=\"UniProtKB/Swiss-Prot identifier of protein product\">",
            "##INFO=<ID=TRBL,Number=1,Type=String,Description=\"UniProtKB/TrEMBL identifier of protein product\">",
            "##INFO=<ID=UNI,Number=1,Type=String,Description=\"UniParc identifier of protein product\">",
            "##INFO=<ID=HGVSc,Number=1,Type=String,Description=\"HGVS coding sequence name\">",
            "##INFO=<ID=HGVSp,Number=1,Type=String,Description=\"HGVS protein sequence name\">",
            "##INFO=<ID=SIFTs,Number=1,Type=String,Description=\"SIFT score\">",
            "##INFO=<ID=SIFTp,Number=1,Type=String,Description=\"SIFT prediction\">",
            "##INFO=<ID=PPHs,Number=1,Type=String,Description=\"Polyphen score\">",
            "##INFO=<ID=PPHp,Number=1,Type=String,Description=\"Polyphen prediction\">",
            "##INFO=<ID=POLY,Number=1,Type=String,Description=\"PolyPhen prediction and/or score\">",
            "##INFO=<ID=MTFN,Number=1,Type=String,Description=\"source and identifier of a transcription factor binding profile aligned at this position\">",
            "##INFO=<ID=MTFP,Number=1,Type=String,Description=\"relative position of the variation in the aligned TFBP\">",
            "##INFO=<ID=HIP,Number=0,Type=Flag,Description=\"a flag indicating if the vcf falls in a high information position of a transcription factor binding profile (TFBP)\">",
            "##INFO=<ID=MSC,Number=1,Type=String,Description=\"difference in motif score of the reference and vcf sequences for the TFBP\">",
            "##INFO=<ID=CLLS,Number=1,Type=String,Description=\"List of cell types and classifications for regulatory feature\">",
            "##INFO=<ID=CANON,Number=0,Type=Flag,Description=\"Transcript is denoted as the canonical transcript for this gene\">",
            "##INFO=<ID=CCDS,Number=1,Type=String,Description=\"CCDS identifer for this transcript, where applicable\">",
            "##INFO=<ID=INTR,Number=1,Type=String,Description=\"Intron number (out of total number)\">",
            "##INFO=<ID=EXON,Number=1,Type=String,Description=\"Exon number (out of total number)\">",
            "##INFO=<ID=DOM,Number=1,Type=String,Description=\"the source and identifer of any overlapping protein domains\">",
            "##INFO=<ID=IND,Number=1,Type=String,Description=\"Individual name\">",
            "##INFO=<ID=ZYG,Number=1,Type=String,Description=\"Zygosity of individual genotype at this locus\">",
            "##INFO=<ID=SV,Number=1,Type=String,Description=\"IDs of overlapping structural variants\">",
            "##INFO=<ID=FRQ,Number=1,Type=String,Description=\"Frequencies of overlapping variants used in filtering\">",
            "##INFO=<ID=GMAF,Number=1,Type=String,Description=\"Minor allele and frequency of existing variation in 1000 Genomes Phase 1\">",
            "##INFO=<ID=AFR_MAF,Number=1,Type=String,Description=\"Minor allele and frequency of existing variation in 1000 Genomes Phase 1 combined African population\">",
            "##INFO=<ID=AMR_MAF,Number=1,Type=String,Description=\"Minor allele and frequency of existing variation in 1000 Genomes Phase 1 combined American population\">",
            "##INFO=<ID=ASN_MAF,Number=1,Type=String,Description=\"Minor allele and frequency of existing variation in 1000 Genomes Phase 1 combined Asian population\">",
            "##INFO=<ID=EUR_MAF,Number=1,Type=String,Description=\"Minor allele and frequency of existing variation in 1000 Genomes Phase 1 combined European population\">",
            "##INFO=<ID=AA_MAF,Number=1,Type=String,Description=\"Minor allele and frequency of existing vcf in NHLBI-ESP African American population\">",
            "##INFO=<ID=EA_MAF,Number=1,Type=String,Description=\"Minor allele and frequency of existing vcf in NHLBI-ESP European American population\">",
            "##INFO=<ID=CLIN,Number=1,Type=String,Description=\"Clinical significance of vcf from dbSNP\">",
            "##INFO=<ID=BIO,Number=1,Type=String,Description=\"Biotype of transcript or regulatory feature\">",
            "##INFO=<ID=TSL,Number=1,Type=String,Description=\"Transcript support level\">",
            "##INFO=<ID=PUBM,Number=1,Type=String,Description=\"Pubmed ID(s) of publications that cite existing vcf\">",
            "##INFO=<ID=SOMA,Number=1,Type=String,Description=\"Somatic status of existing variation(s)\">"
    };
    public static final int MAX_VARIANTS = 1000;
    private final VcfFile vcfFile;
    private List<Variant> variants;

    public VepAnnotator(VcfFile vcfFile) {
        this.vcfFile = vcfFile;
        this.variants = new ArrayList<>(vcfFile.getVariants());
    }

    public VepAnnotator(List<Variant> variants) {
        this.variants = (variants);
        vcfFile = variants.get(0).getVcfFile();
    }

    @Override
    protected Boolean call() throws Exception {
        injectVEPHeaders();
        return annotateVariants();

    }

    private void injectVEPHeaders() {
        Arrays.stream(HEADERS).forEach(vcfFile.getHeader()::addHeader);
    }

    private boolean annotateVariants() {
        vcfFile.setChanged(true);
        final List<Integer> starts = getStarts();
        final AtomicInteger total = new AtomicInteger();
        starts.parallelStream().forEachOrdered(start -> {
            try {
                System.out.println(start + "-" + (start + MAX_VARIANTS));
                annotate(start);
            } catch (Exception e) {
                e.printStackTrace();
            }
//            updateProgress(total.addAndGet(MAX_VARIANTS), vcfFile.getVariants().size());
//            updateMessage(total.toString() + " variants annotated");
        });
        return true;
    }

    @NotNull
    private List<Integer> getStarts() {
        final List<Integer> starts = new ArrayList<>();
        for (int j = 0; j < variants.size(); j += MAX_VARIANTS) starts.add(j);
        return starts;
    }


    private void annotate(int from) throws Exception {
        int to = from + MAX_VARIANTS;
        if (to >= variants.size()) to = variants.size();
        final List<Variant> subList = variants.subList(from, to);
        final String response = makeHttpRequest(subList);
        annotateVariants(response, subList);
    }

    private String makeHttpRequest(List<Variant> variants) throws MalformedURLException {
        final URL url = new URL("http://grch37.rest.ensembl.org/vep/human/region");
        final Map<String, String> requestMap = getRequestMap();
        final JSONObject message = getJsonMessage(variants);
        return Web.httpRequest(url, requestMap, message);
    }

    private JSONObject getJsonMessage(List<Variant> variants) {
        final JSONArray array = getJsonVariantArray(variants);
        final JSONObject message = new JSONObject();
        // {"variants":array}
        message.put("variants", array);
        return message;
    }

    private JSONArray getJsonVariantArray(List<Variant> variants) {
        // Translate list into JSON
        // ["1 156897 156897 A/C","2 3547966 3547968 TCC/T"]
        final JSONArray array = new JSONArray();
        for (Variant v : variants) {
            int start = v.getPosition();
            int end = v.getPosition() + v.getRef().length() - 1;
            // 1 156897 156897 A/C
            // 2 3547966 3547968 TCC/T
            array.put(String.format("%s %d %d %s/%s", v.getChrom(), start, end, v.getRef(), v.getAlt()));
        }
        return array;
    }

    private Map<String, String> getRequestMap() {
        final Map<String, String> requestMap = new HashMap<>();
        requestMap.put("Content-Type", "application/json");
        return requestMap;
    }

    private void annotateVariants(String response, List<Variant> variants) {
        if (response != null) {
            JSONArray json = new JSONArray(response);
            mapVepInfo(json, variants);
        }
    }

    private void mapVepInfo(JSONArray json, List<Variant> variants) {
        // To go faster, as Vep does not guarantee the order of variants, I will copy the list of variants
        // Then, each located vcf will be removed from list
        final List<Variant> copy = new LinkedList<>(variants);
        for (int i = 0; i < json.length(); i++) {
//            System.out.println(copy.hashCode() + " " + i);
            try {
                JSONObject object = json.getJSONObject(i);
                // 1 156897 156897 A/C
                String[] input = ((String) object.get("input")).split(" ");
                Variant variant = getVariant(copy, input);
                if (variant != null) {
                    incorporateData(variant, object);
                    copy.remove(variant);
                }
            } catch (JSONException | NumberFormatException ex) {
                ex.printStackTrace();

            }
        }
    }

    private static Variant getVariant(List<Variant> variants, String[] input) {
        return variants.stream()
                .filter(variant -> variant.getPosition() == Integer.valueOf(input[1]) && variant.getChrom().equals(input[0]))
                .findFirst().orElse(null);
    }

    private static synchronized void incorporateData(Variant variant, JSONObject json) {
//        final Map<String, String> map = vcf.get();
        addFrequencyData(variant, json);
        addTranscriptConsequences(variant, json);
        addIntergenicConsequences(variant, json);
//        vcf.set(map);
    }

    private static void addFrequencyData(Variant variant, JSONObject json) {
    /*
     - "id":"temp"
     - "input":"1 1534738 1534738 G/A"
     - "assembly_name":"GRCh37"
     - "seq_region_name":"13"
     - "start":77999693
     - "end":77999693
     - "strand":1
     - "allele_string":"A/G"
     - "most_severe_consequence" : "intergenic_variant"
     - "intergenic_consequences" : [ array ]
     - "colocated_variants" : [ array ]
     - "transcript_consequences" : [ array ]
     */
        /*
         First: "colocated_variants" : [ array ]
         - "id":"rs1536074"
         - "seq_region_name":"9"
         - "start":17721795
         - "end":17721795
         - "somatic":0
         - "strand":1

         - "minor_allele":"T"
         - "allele_string":"T/A"
         - "minor_allele_freq":0.0023

         - "aa_maf":0
         - "ea_maf":0.002285
         - "amr_maf":0.25
         - "asn_maf":0.07
         - "afr_maf":0.03
         - "eur_maf":0.99
         - "ea_allele":"T"
         - "aa_allele":"T"
         - "amr_allele":"G"
         - "afr_allele":"G"
         - "eur_allele":"G"
         - "asn_allele":"G"
        */
        try {
            JSONArray variants = json.getJSONArray("colocated_variants");
            // Only take the first one
            JSONObject var = variants.getJSONObject(0);

            // ID goes in the VCF id field
            String id = var.getString("id");
            if (variant.getId().equals("."))
                variant.setId(id);

//            GMAF|AFR_MAF|AMR_MAF|EAS_MAF|EUR_MAF|SAS_MAF|AA_MAF|EA_MAF
            findAndPut(var, "minor_allele_freq", variant, "GMAF", Double.class);
            findAndPut(var, "amr_maf", variant, "AMR_MAF", Double.class);
            findAndPut(var, "asn_maf", variant, "ASN_MAF", Double.class);
            findAndPut(var, "eur_maf", variant, "EUR_MAF", Double.class);
            findAndPut(var, "afr_maf", variant, "AFR_MAF", Double.class);
            findAndPut(var, "ea_maf", variant, "EA_MAF", Double.class);
            findAndPut(var, "aa_maf", variant, "AA_MAF", Double.class);
        } catch (JSONException ex) {
            // No colocated_variants
        }
    }

    private static void addTranscriptConsequences(Variant variant, JSONObject json) {
    /*
     Second: "transcript_consequences" : [ array ]
     - "variant_allele":"A"
     - "strand":1

     - "gene_id":"ENSG00000107295"
     - "gene_symbol_source":"HGNC"
     - "gene_symbol":"SH3GL2"
     - "biotype":"protein_coding"

     - "hgnc_id":10831

     - "transcript_id":"ENST00000380607"
     - "codons":"Ccc/Tcc"
     - "amino_acids":"P/S"
     - "protein_start":220
     - "protein_end":220
     - "cds_start":658
     - "cds_end":658
     - "cdna_start":495
     - "cdna_end":739
     - "distance":4425

     - "sift_score":0
     - "sift_prediction":"deleterious"
     - "polyphen_score":0.81
     - "polyphen_prediction":"possibly_damaging"

     - "consequence_terms":[ array ]
     -- "intron_variant","downstream_gene_variant"
     */
        try {

            JSONArray cons = json.getJSONArray("transcript_consequences");
            // Only take the first one
            JSONObject first = cons.getJSONObject(0);

            findAndPut(first, "gene_symbol", variant, "SYMBOL", String.class);
            findAndPut(first, "gene_id", variant, "GENE", String.class);
            findAndPut(first, "distance", variant, "DIST", Integer.class);
            findAndPut(first, "biotype", variant, "BIO", String.class);
            findAndPut(first, "transcript_id", variant, "FEAT", String.class);
            findAndPut(first, "codons", variant, "COD", String.class);
            findAndPut(first, "amino_acids", variant, "AMINO", String.class);
            // Unnecessary
//            findAndPut(first, "protein_start", v, "PROTS", Integer.class);
//            findAndPut(first, "protein_end", v, "PROTE", Integer.class);
//            findAndPut(first, "cds_start", v, "CDSS", Integer.class);
//            findAndPut(first, "cds_end", v, "CDSE", Integer.class);
//            findAndPut(first, "cdna_start", v, "CDNAS", Integer.class);
//            findAndPut(first, "cdna_end", v, "CDNAE", Integer.class);
            findAndPut(first, "sift_score", variant, "SIFTs", Double.class);
            findAndPut(first, "sift_prediction", variant, "SIFTp", String.class);
            findAndPut(first, "polyphen_score", variant, "PPHs", Double.class);
            findAndPut(first, "polyphen_prediction", variant, "PPHp", String.class);
            findAndPutArray(first, "consequence_terms", variant, "CONS", String.class);

        } catch (JSONException e) {
            // NO transcript_consequences
        }
    }

    private static void addIntergenicConsequences(Variant variant, JSONObject json) {
    /*
     Third try: intergenic consequences:[array]
     - "variant_allele":"G"
     - "consequence_terms" : [ array ]
     -- "intergenic_variant"
     */
        try {
            JSONArray cons = json.getJSONArray("intergenic_consequences");
            findAndPut(cons.getJSONObject(0), "consequence_terms", variant, "CONS", String.class);
        } catch (JSONException e) {
            // NO intergenic_consequences

        }
    }

    /**
     * Checks if sourceKey is present in the source JSONObject. In that case, reads a classType
     * object and puts it into target vcf with targetKey.
     *
     * @param source    source JSONObject
     * @param sourceKey key in the source JSONObject
     * @param variant       target vcf
     * @param targetKey key in the target vcf
     * @param classType type of value in source JSONObject
     */
    private static void findAndPut(JSONObject source, String sourceKey, Variant variant,
                                   String targetKey, Class classType) {
        if (source.containsKey(sourceKey))
            if (classType == String.class)
                variant.getInfo().set(targetKey, source.getString(sourceKey));
            else if (classType == Integer.class)
                variant.getInfo().set(targetKey, source.getInt(sourceKey) + "");
            else if (classType == Double.class)
                variant.getInfo().set(targetKey, source.getDouble(sourceKey) + "");
            else if (classType == Boolean.class)
                variant.getInfo().set(targetKey, source.getBoolean(sourceKey) + "");
            else if (classType == Long.class)
                variant.getInfo().set(targetKey, source.getLong(sourceKey) + "");
            else
                variant.getInfo().set(targetKey, String.valueOf(source.get(sourceKey)));
    }

    private static void findAndPutArray(JSONObject source, String sourceKey, Variant target,
                                        String targetKey, Class classType) {
        if (source.containsKey(sourceKey)) {
            JSONArray terms = source.getJSONArray(sourceKey);
            if (terms.length() > 0) {
                final StringBuilder builder = new StringBuilder(terms.get(0).toString());
                for (int i = 1; i < terms.length(); i++) builder.append(",").append(terms.get(i).toString());
                target.getInfo().set(targetKey, builder.toString());
//                String result = "";
//                for (int i = 0; i < terms.length(); i++)
//                    if (classType == String.class)
//                        result += terms.getString(i) + ",";
//                    else if (classType == Integer.class)
//                        result += terms.getInt(i) + ",";
//                    else if (classType == Double.class)
//                        result += terms.getDouble(i) + ",";
//                    else if (classType == Boolean.class)
//                        result += terms.getBoolean(i) + ",";
//                    else if (classType == Long.class)
//                        result += terms.getLong(i) + ",";
//                    else
//                        result += String.valueOf(terms.get(i)) + ",";
//                if (!result.isEmpty()) {
//                    result = result.substring(0, result.length() - 1);
//                    target.put(targetKey, result);
//                }
            }
        }
    }
}
