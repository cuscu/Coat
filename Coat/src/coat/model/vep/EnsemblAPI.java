/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coat.model.vep;

import coat.json.JSONArray;
import coat.json.JSONException;
import coat.json.JSONObject;
import coat.model.vcf.Variant;
import javafx.concurrent.Task;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author UICHUIMI
 */
public class EnsemblAPI {

    private final static String SERVER = "http://grch37.rest.ensembl.org/vep/";
    private final static String SPECIES = "human";
    private final static String REGION = "region";
    private final static String variantURL = SERVER + SPECIES + "/" + REGION + "/";

    public final static String[] headers = {
            "##INFO=<ID=GENE,Number=1,Type=String,Description=\"Ensemble gene ID\">",
            "##INFO=<ID=FEAT,Number=1,Type=String,Description=\"Ensemble feature ID\">",
            "##INFO=<ID=TYPE,Number=1,Type=String,Description=\"Type of feature (Transcript, RegulatoryFeature, MotifFeature)\">",
            "##INFO=<ID=CONS,Number=1,Type=String,Description=\"Consequence type\">",
            "##INFO=<ID=CDNA,Number=1,Type=Integer,Description=\"Relative position of base pair in cDNA sequence\">",
            "##INFO=<ID=CDS,Number=1,Type=Integer,Description=\"Relative position of base pair in coding sequence\">",
            "##INFO=<ID=PROT,Number=1,Type=Integer,Description=\"Relative position of amino acid in protein\">",
            "##INFO=<ID=AMINO,Number=1,Type=String,Description=\"Amino acid change. Only given if the variation affects the protein-coding sequence\">",
            "##INFO=<ID=COD,Number=1,Type=String,Description=\"The alternative codons\">",
            "##INFO=<ID=DIST,Number=1,Type=String,Description=\"Shortest distance from variant to transcript\">",
            "##INFO=<ID=STR,Number=1,Type=String,Description=\"The DNA strand (1 or -1) on which the transcript/feature lies\">",
            "##INFO=<ID=GNAME,Number=1,Type=String,Description=\"Gene symbol or name\">",
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
            "##INFO=<ID=HIP,Number=0,Type=Flag,Description=\"a flag indicating if the variant falls in a high information position of a transcription factor binding profile (TFBP)\">",
            "##INFO=<ID=MSC,Number=1,Type=String,Description=\"difference in motif score of the reference and variant sequences for the TFBP\">",
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
            "##INFO=<ID=AFR_F,Number=1,Type=String,Description=\"Minor allele and frequency of existing variation in 1000 Genomes Phase 1 combined African population\">",
            "##INFO=<ID=AMR_F,Number=1,Type=String,Description=\"Minor allele and frequency of existing variation in 1000 Genomes Phase 1 combined American population\">",
            "##INFO=<ID=ASN_F,Number=1,Type=String,Description=\"Minor allele and frequency of existing variation in 1000 Genomes Phase 1 combined Asian population\">",
            "##INFO=<ID=EUR_F,Number=1,Type=String,Description=\"Minor allele and frequency of existing variation in 1000 Genomes Phase 1 combined European population\">",
            "##INFO=<ID=AA_F,Number=1,Type=String,Description=\"Minor allele and frequency of existing variant in NHLBI-ESP African American population\">",
            "##INFO=<ID=EA_F,Number=1,Type=String,Description=\"Minor allele and frequency of existing variant in NHLBI-ESP European American population\">",
            "##INFO=<ID=CLIN,Number=1,Type=String,Description=\"Clinical significance of variant from dbSNP\">",
            "##INFO=<ID=BIO,Number=1,Type=String,Description=\"Biotype of transcript or regulatory feature\">",
            "##INFO=<ID=TSL,Number=1,Type=String,Description=\"Transcript support level\">",
            "##INFO=<ID=PUBM,Number=1,Type=String,Description=\"Pubmed ID(s) of publications that cite existing variant\">",
            "##INFO=<ID=SOMA,Number=1,Type=String,Description=\"Somatic status of existing variation(s)\">"
    };

    public static void addVepInfo(List<Variant> variants) {
        // List must be split into 1000 variants' fragments
        for (int i = 0; i < variants.size(); i += 1000)
            try {
                // http://grch37.rest.ensembl.org/vep/human/region
                final URL url = new URL("http://grch37.rest.ensembl.org/vep/human/region");

                // Put json request
                Map<String, String> hea = new HashMap<>();
                hea.put("Content-Type", "application/json");

                // Translate list into JSON
                // ["1 156897 156897 A/C","2 3547966 3547968 TCC/T"]
                JSONArray array = new JSONArray();
                for (int j = i; j < i + 1000 && j < variants.size(); j++) {
                    Variant v = variants.get(j);
                    int start = v.getPos();
                    int end = v.getPos() + v.getRef().length() - 1;
                    // 1 156897 156897 A/C
                    // 2 3547966 3547968 TCC/T
                    array.put(String.format("%s %d %d %s/%s", v.getChrom(), start, end, v.getRef(), v.getAlt()));
                }
                // {"variants":array}
                JSONObject message = new JSONObject();
                message.put("variants", array);

                // Make internet request
                String response = Web.httpRequest(url, hea, message);

                if (response != null) {
                    JSONArray json = new JSONArray(response);
                    mapVepInfo(json, variants);
//                    System.out.println(json.toString(2));
                }
            } catch (MalformedURLException ex) {
                Logger.getLogger(EnsemblAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    public static Task<Boolean> vepAnnotator(List<Variant> variants) {
        Task<Boolean> task;
        task = new Task<Boolean>() {

            @Override
            protected Boolean call() throws Exception {
                // List must be split into 1000 variants' fragments
                for (int i = 0; i < variants.size(); i += 1000) {
                    updateMessage(i + " variants annotated");
                    updateProgress(i, variants.size());
                    try {
                        // http://grch37.rest.ensembl.org/vep/human/region
                        final URL url = new URL("http://grch37.rest.ensembl.org/vep/human/region");

                        // Put json request
                        Map<String, String> hea = new HashMap<>();
                        hea.put("Content-Type", "application/json");

                        // Translate list into JSON
                        // ["1 156897 156897 A/C","2 3547966 3547968 TCC/T"]
                        JSONArray array = new JSONArray();
                        for (int j = i; j < i + 1000 && j < variants.size(); j++) {
                            Variant v = variants.get(j);
                            int start = v.getPos();
                            int end = v.getPos() + v.getRef().length() - 1;
                            // 1 156897 156897 A/C
                            // 2 3547966 3547968 TCC/T
                            array.put(String.format("%s %d %d %s/%s",
                                    v.getChrom(), start, end, v.getRef(), v.getAlt()));
                        }
                        // {"variants":array}
                        JSONObject message = new JSONObject();
                        message.put("variants", array);

                        // Make internet request
                        String response = Web.httpRequest(url, hea, message);
                        if (response != null) {
                            JSONArray json = new JSONArray(response);
                            new Thread(() -> mapVepInfo(json, variants)).start();
//                    System.out.println(json.toString(2));
                        }
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(EnsemblAPI.class.getName()).log(Level.SEVERE, null, ex);
                        return false;
                    }
                }
                return true;
            }
        };
        return task;
    }

    /**
     * Incorporates vep icarus to variant.
     *
     * @param v variant
     */
    public static void addVepInfo(Variant v) {
        try {
            // Generate address
            // http://grch37.rest.ensembl.org/vep/human/region/chr:start-end/allele
            int start = v.getPos();
            int end = v.getPos() + v.getRef().length() - 1;
            String region = v.getChrom() + ":" + start + "-" + end;
            URL url = new URL(variantURL + region + "/" + v.getAlt());

            // Put json request
            Map<String, String> hea = new HashMap<>();
            hea.put("content-type", "application/json");

            // Connect to the internet
            String response = Web.httpRequest(url, hea, null);

            // Add icarus to variant
            if (response != null) {
                JSONArray json = new JSONArray(response);
                incorporateData(v, json.getJSONObject(0));
            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(EnsemblAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void incorporateData(Variant variant, JSONObject json) {
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

            findAndPut(var, "minor_allele_freq", variant, "GMAF", Double.class);
            findAndPut(var, "amr_maf", variant, "AMR_F", Double.class);
            findAndPut(var, "asn_maf", variant, "ASN_F", Double.class);
            findAndPut(var, "eur_maf", variant, "EUR_F", Double.class);
            findAndPut(var, "afr_maf", variant, "AFR_F", Double.class);
            findAndPut(var, "ea_maf", variant, "EA_F", Double.class);
            findAndPut(var, "aa_maf", variant, "AA_F", Double.class);

        } catch (JSONException ex) {
            // No colocated_variants
        }

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

            findAndPut(first, "gene_symbol", variant, "GNAME", String.class);
            findAndPut(first, "gene_id", variant, "GENE", String.class);
            findAndPut(first, "distance", variant, "DIST", Integer.class);
            findAndPut(first, "biotype", variant, "BIO", String.class);
            findAndPut(first, "transcript_id", variant, "FEAT", String.class);
            findAndPut(first, "codons", variant, "COD", String.class);
            findAndPut(first, "amino_acids", variant, "AA", String.class);
            // Unnecesary
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

    private static void mapVepInfo(JSONArray json, List<Variant> variants) {
        // To go faster, I will copy the list of variants
        // Then, each located variant will be removed from list
        List<Variant> copy = new LinkedList<>(variants);
        for (int i = 0; i < json.length(); i++)
            try {
                // Check similar variant
                JSONObject object = json.getJSONObject(i);
                // 1 156897 156897 A/C
                String[] input = ((String) object.get("input")).split(" ");
                // Look for the variant
                for (Variant variant : copy)
                    if (variant.getChrom().equals(input[0]) && variant.getPos() == Integer.valueOf(input[1])) {
                        incorporateData(variant, object);
                        // Remove variant from list
                        copy.remove(variant);
                        break;
                    }
            } catch (JSONException | NumberFormatException ex) {
                Logger.getLogger(EnsemblAPI.class.getName()).
                        log(Level.SEVERE, json.get(i).toString(), ex);

            }
    }

    /**
     * Checks if sourceKey is present in the source JSONObject. In that case, reads a classType
     * object and puts it into target variant with targetKey.
     *
     * @param source    source JSONObject
     * @param sourceKey key in the source JSONObject
     * @param target    target variant
     * @param targetKey key in the target variant
     * @param classType type of value in source JSONObject
     */
    private static void findAndPut(JSONObject source, String sourceKey, Variant target,
                                   String targetKey, Class classType) {
        if (source.containsKey(sourceKey))
            if (classType == String.class)
                target.getInfos().put(targetKey, source.getString(sourceKey));
            else if (classType == Integer.class)
                target.getInfos().put(targetKey, source.getInt(sourceKey) + "");
            else if (classType == Double.class)
                target.getInfos().put(targetKey, source.getDouble(sourceKey) + "");
            else if (classType == Boolean.class)
                target.getInfos().put(targetKey, source.getBoolean(sourceKey) + "");
            else if (classType == Long.class)
                target.getInfos().put(targetKey, source.getLong(sourceKey) + "");
            else
                target.getInfos().put(targetKey, String.valueOf(source.get(sourceKey)));
    }

    private static void findAndPutArray(JSONObject source, String sourceKey, Variant target,
                                        String targetKey, Class classType) {
        if (source.containsKey(sourceKey)) {
            JSONArray terms = source.getJSONArray(sourceKey);
            if (terms.length() > 0) {
                String result = "";
                for (int i = 0; i < terms.length(); i++)
                    if (classType == String.class)
                        result += terms.getString(i) + ",";
                    else if (classType == Integer.class)
                        result += terms.getInt(i) + ",";
                    else if (classType == Double.class)
                        result += terms.getDouble(i) + ",";
                    else if (classType == Boolean.class)
                        result += terms.getBoolean(i) + ",";
                    else if (classType == Long.class)
                        result += terms.getLong(i) + ",";
                    else
                        result += String.valueOf(terms.get(i)) + ",";
                if (!result.isEmpty()) {
                    result = result.substring(0, result.length() - 1);
                    target.getInfos().put(targetKey, result);
                }
            }
        }
    }
}
