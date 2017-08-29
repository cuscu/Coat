/*
 * Copyright (c) UICHUIMI 2017
 *
 * This file is part of Coat.
 *
 * Coat is free software:
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Coat.
 *
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */

package coat.core.vep;

import coat.json.JSONArray;
import coat.json.JSONException;
import coat.json.JSONObject;
import coat.utils.OS;
import javafx.concurrent.Task;
import org.jetbrains.annotations.NotNull;
import vcf.ComplexHeaderLine;
import vcf.Variant;
import vcf.VariantSet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VepAnnotator extends Task<Boolean> {

    public static final String[][] DEFAULT_HEADERS = new String[][]{
            {"GENE", "1", "String", "Ensemble gene ID"},
            {"FEAT", "1", "String", "Ensemble feature ID"},
            {"TYPE", "1", "String", "Type of feature (Transcript", " RegulatoryFeature", " MotifFeature)"},
            {"CONS", "1", "String", "Consequence type"},
            {"CDNA", "1", "Integer", "Relative position of base pair in cDNA sequence"},
            {"CDS", "1", "Integer", "Relative position of base pair in coding sequence"},
            {"PROT", "1", "Integer", "Relative position of amino acid in protein"},
            {"AMINO", "1", "String", "Amino acid change. Only given if the variation affects the protein-coding sequence"},
            {"COD", "1", "String", "The alternative codons"},
            {"DIST", "1", "Integer", "Shortest distance from vcf to transcript"},
            {"STR", "1", "String", "The DNA strand (1 or -1) on which the transcript/feature lies"},
            {"SYMBOL", "1", "String", "Gene symbol or name"},
            {"SRC", "1", "String", "The source of the gene symbol"},
            {"ENSP", "1", "String", "Ensembl protein identifier of the affected transcript"},
            {"SWPR", "1", "String", "UniProtKB/Swiss-Prot identifier of protein product"},
            {"TRBL", "1", "String", "UniProtKB/TrEMBL identifier of protein product"},
            {"UNI", "1", "String", "UniParc identifier of protein product"},
            {"HGVSc", "1", "String", "HGVS coding sequence name"},
            {"HGVSp", "1", "String", "HGVS protein sequence name"},
            {"SIFTs", "1", "Float", "SIFT score"},
            {"SIFTp", "1", "String", "SIFT prediction"},
            {"PPHs", "1", "Float", "Polyphen score"},
            {"PPHp", "1", "String", "Polyphen prediction"},
            {"POLY", "1", "String", "PolyPhen prediction and/or score"},
            {"MTFN", "1", "String", "source and identifier of a transcription factor binding profile aligned at this position"},
            {"MTFP", "1", "String", "relative position of the variation in the aligned TFBP"},
            {"HIP", "0", "Flag", "a flag indicating if the vcf falls in a high information position of a transcription factor binding profile (TFBP)"},
            {"MSC", "1", "String", "difference in motif score of the reference and vcf sequences for the TFBP"},
            {"CLLS", "1", "String", "List of cell types and classifications for regulatory feature"},
            {"CANON", "0", "Flag", "Transcript is denoted as the canonical transcript for this gene"},
            {"CCDS", "1", "String", "CCDS identifer for this transcript", " where applicable"},
            {"INTR", "1", "String", "Intron number (out of total number)"},
            {"EXON", "1", "String", "Exon number (out of total number)"},
            {"DOM", "1", "String", "the source and identifer of any overlapping protein domains"},
            {"IND", "1", "String", "Individual name"},
            {"ZYG", "1", "String", "Zygosity of individual genotype at this locus"},
            {"SV", "1", "String", "IDs of overlapping structural variants"},
            {"FRQ", "1", "String", "Frequencies of overlapping variants used in filtering"},
            {"GMAF", "1", "Float", "Minor allele and frequency of existing variation in 1000 Genomes Phase 1"},
            {"AFR_MAF", "1", "Float", "Minor allele and frequency of existing variation in 1000 Genomes Phase 1 " +
                    "combined African population"},
            {"AMR_MAF", "1", "Float", "Minor allele and frequency of existing variation in 1000 Genomes Phase 1 combined American population"},
            {"ASN_MAF", "1", "Float", "Minor allele and frequency of existing variation in 1000 Genomes Phase 1 combined Asian population"},
            {"EUR_MAF", "1", "Float", "Minor allele and frequency of existing variation in 1000 Genomes Phase 1 combined European population"},
            {"AA_MAF", "1", "Float", "Minor allele and frequency of existing vcf in NHLBI-ESP African American population"},
            {"EA_MAF", "1", "Float", "Minor allele and frequency of existing vcf in NHLBI-ESP European American population"},
            {"CLIN", "1", "String", "Clinical significance of vcf from dbSNP"},
            {"BIO", "1", "String", "Biotype of transcript or regulatory feature"},
            {"TSL", "1", "String", "Transcript support level"},
            {"PUBM", "1", "String", "Pubmed ID(s) of publications that cite existing vcf"},
            {"SOMA", "1", "String", "Somatic status of existing variation(s)"}
    };
    public static final int MAX_VARIANTS = 1000;

    static {


    }

    private final VariantSet variantSet;
    private List<Variant> variants;

    public VepAnnotator(VariantSet variantSet) {
        this.variantSet = variantSet;
        this.variants = new ArrayList<>(variantSet.getVariants());
    }

    private static Variant getVariant(List<Variant> variants, String[] input) {
        return variants.stream()
                .filter(variant -> variant.getPosition() == Integer.valueOf(input[1]) && variant.getChrom().equals(input[0]))
                .findFirst().orElse(null);
    }

    private static synchronized void incorporateData(Variant variant, JSONObject json) {
//        final Map<String, String> map = vcf.getProperty();
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
     * @param variant   target vcf
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
//                        result += String.valueOf(terms.getProperty(i)) + ",";
//                if (!result.isEmpty()) {
//                    result = result.substring(0, result.length() - 1);
//                    target.put(targetKey, result);
//                }
            }
        }
    }

    @Override
    protected Boolean call() throws Exception {
        injectVEPHeaders();
        return annotateVariants();

    }

    private void injectVEPHeaders() {
        Arrays.stream(DEFAULT_HEADERS).forEach(info -> {
            final Map<String, String> map = new LinkedHashMap<>();
            map.put("ID", info[0]);
            map.put("Number", info[1]);
            map.put("Type", info[2]);
            map.put("Description", info[3]);
            variantSet.getHeader().getHeaderLines()
                    .add(new ComplexHeaderLine("INFO", map));
        });
//        Arrays.stream(HEADERS).forEach(variantSet.getHeader()::addHeader);
    }

    private boolean annotateVariants() {
//        variantSet.setChanged(true);
        final List<Integer> starts = getStarts();
        final AtomicInteger total = new AtomicInteger();
        starts.parallelStream().forEachOrdered(start -> {
            try {
                System.out.println(start + "-" + (start + MAX_VARIANTS));
                annotate(start);
            } catch (Exception e) {
                e.printStackTrace();
            }
            updateProgress(total.addAndGet(MAX_VARIANTS), variantSet.getVariants().size());
            updateMessage(total.toString() + " " + OS.getString("variants"));
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
}
