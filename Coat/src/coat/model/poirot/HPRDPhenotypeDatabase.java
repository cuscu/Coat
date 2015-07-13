package coat.model.poirot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class HPRDPhenotypeDatabase {

    private static final Map<String, Integer> status = new HashMap<>();

    private static Map<String, List<String>> phenotypeGenes;

    public static List<String> getRelatedGenes(String phenotype) {
        if (phenotypeGenes == null) loadDatabases();
        return phenotypeGenes.getOrDefault(phenotype, new ArrayList<>());
    }

    public static List<String> getPhenotypes(String name) {
        if (phenotypeGenes == null) loadDatabases();
        final String lowerCasedName = name.toLowerCase();
        return phenotypeGenes.keySet().stream().filter(disease -> disease.toLowerCase().contains(lowerCasedName)).collect(Collectors.toList());
    }

    private static void loadDatabases() {
        phenotypeGenes = new HashMap<>();
        loadDiseases();
        loadExpressions();
    }

    private static void loadExpressions() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(HPRDPhenotypeDatabase.class.getResourceAsStream("hprd-phenotypes.tsv.gz"))))) {
            reader.lines().forEach(HPRDPhenotypeDatabase::addExpression);
            System.out.println(status);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadDiseases() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(HPRDPhenotypeDatabase.class.getResourceAsStream("hprd-diseases.tsv.gz"))))) {
            reader.lines().forEach(HPRDPhenotypeDatabase::addDisease);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addDisease(String line) {
        /*
            0 hprd_id   00003
            1 geneSymbol	ALDH2
            2 refseq_id	NP_000681.2
            3 disease_name  Alcohol sensitivity, acute
            4 reference_id  16440063,6582480,4065146,2987944,7593603,8903321,10780266,15654505,10627091,16046871
         */
        final String[] row = line.split("\t");
        final String gene = row[1];
        final String disease = row[3];
        List<String> genes = phenotypeGenes.get(disease);
        if (genes == null) {
            genes = new ArrayList<>();
            phenotypeGenes.put(disease, genes);
        }
        genes.add(gene);
    }

    private static void addExpression(String line) {
        /*
            0 hprd_id   00004
            1 refseq_id NP_000682.3
            2 geneSymbol    ALDH3A1
            3 expression_term   Stomach
            4 status    General
            5 reference_id  1737758
         */
        final String[] row = line.split("\t");
        final String gene = row[2];
        final String expression = row[3];
        List<String> genes = phenotypeGenes.get(expression);
        if (genes == null) {
            genes = new ArrayList<>();
            phenotypeGenes.put(expression, genes);
        }
        genes.add(gene);
        final Integer count = status.getOrDefault(row[4], 0);
        status.put(row[4], count + 1);
    }
}
