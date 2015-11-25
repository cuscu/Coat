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

package coat.core.poirot.dataset;

import coat.utils.OS;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipInputStream;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class DatabaseGenerator {

    private final static File BIOGRIDFILE = new File("/home/unidad03/PoirotDatabases/BIOGRID_GENE_TO_GENE.txt.gz");
    private final static File HPRD = new File("/home/unidad03/PoirotDatabases/HPRD_GENE_TO_GENE.txt.gz");
    private final static File HPRD_EXPRESSION = new File("/home/unidad03/PoirotDatabases/HPRD_GENE_TO_PHENOTYPE.txt.gz");
    private final static File HPRD_DISEASE = new File("/home/unidad03/PoirotDatabases/HPRD_GENE_TO_DISEASE.tsv.gz");
    private final static File MENTHA = new File("/home/unidad03/PoirotDatabases/MENTHA_GENE_TO_GENE.gz");
    private final static File OMIM = new File("/home/unidad03/PoirotDatabases/OMIM_GENE_TO_PHENOTYPE.txt.gz");
    private final static File PROTEIN_ATLAS = new File("/home/unidad03/PoirotDatabases/PROTEINATLAS_GENE_TO_DISEASE.csv.gz");


    public static final String EMPTY = "-";

    private final List<String[]> lines = new ArrayList<>();
    private final Map<String, String[]> index = new HashMap<>();
    private final File geneToGene = new File("gene-to-gene.tsv.gz");
    private final File geneToExpression = new File("gene-to-expression.tsv.gz");
    private final File geneToDisease = new File("gene-to-disease.tsv.gz");


    public void start() {
        createGeneToGene();
        lines.clear();
        createGeneToDisease();
        lines.clear();
        createGeneToExpression();
    }


    private void createGeneToGene() {
        addBioGridEntries();
        addHPRDEntries();
        addMenthaEntries();
        saveToFile(lines, geneToGene);
    }

    private void createGeneToDisease() {
        addOmimEntries();
        addHPRDDiseaseEntries();
        saveToFile(lines, geneToDisease);
    }

    private void addHPRDDiseaseEntries() {
        try (BufferedReader reader = getGzipReader(HPRD_DISEASE)) {
            reader.lines()
                    .map(line -> line.split("\t"))
                    .map(HPRDDiseaseParser::getEntry)
                    .forEach(this::addGeneToGeneLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createGeneToExpression() {
        addHPRDExpressionEntries();
        addProteinAtlasEntries();
        saveToFile(lines, geneToExpression);
    }

    private void addBioGridEntries() {
        try (BufferedReader reader = getGzipReader(BIOGRIDFILE)) {
            reader.readLine(); // skip first line
            reader.lines()
                    .map(line -> line.split("\t"))
                    .map(BiogridParser::getEntry)
                    .forEach(this::addGeneToGeneLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void addHPRDEntries() {
        try (BufferedReader reader = getGzipReader(HPRD)) {
            reader.lines()
                    .map(line -> line.split("\t"))
                    .map(HPRDGeneParser::getEntry)
                    .forEach(this::addGeneToGeneLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void addMenthaEntries() {
        try (BufferedReader reader = getGzipReader(MENTHA)) {
            reader.lines()
                    .map(line -> line.split("\t"))
                    .map(MenthaParser::getEntry)
                    .forEach(this::addGeneToGeneLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * As Mentha database contains BioGrid entries, duplicates must be updated, but not added or replaced
     *
     * @param entry the fields with the entry info
     */
    private void addGeneToGeneLine(String[] entry) {
        try {
            final String tId = entry[0] + ":" + entry[1];
            if (index.containsKey(tId)) {
                final String[] innerEntry = index.get(tId);
                if (innerEntry[4].equals(EMPTY)) innerEntry[4] = entry[4];
                if (innerEntry[5].equals(EMPTY)) innerEntry[5] = entry[5];
                if (innerEntry[6].equals(EMPTY)) innerEntry[6] = entry[6];
            } else {
                lines.add(entry);
                index.put(tId, entry);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addOmimEntries() {
        try (BufferedReader reader = getGzipReader(OMIM)) {
            reader.lines()
                    .map(line -> line.split("\\|"))
                    .map(OmimParser::getEntries)
                    .flatMap(Collection::stream)
                    .forEach(lines::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addHPRDExpressionEntries() {
        try (BufferedReader reader = getGzipReader(HPRD_EXPRESSION)) {
            reader.lines()
                    .map(line -> line.split("\t"))
                    .map(HPRDExpressionParser::getEntry)
                    .forEach(lines::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addProteinAtlasEntries() {
        try (BufferedReader reader = getGzipReader(PROTEIN_ATLAS)) {
        final Pattern pattern = Pattern.compile("\\\"([^\\\"]*)\\\",\\\"([^\\\"]*)\\\",\\\"([^\\\"]*)\\\",\\\"([^\\\"]*)\\\",\\\"([^\\\"]*)\\\",\\\"([^\\\"]*)\\\",\\\"([^\\\"]*)\\\"");
            reader.lines()
                    .map(line -> {
                        final Matcher matcher = pattern.matcher(line);
                        if (matcher.matches()) {
                            return new String[]{matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5), matcher.group(6), matcher.group(7)};
                        } else {
                            System.err.println(line);
                            return null;
                        }
                    })
                    .map(ProteinAtlasParser::getEntry)
                    .filter(line -> line != null)
                    .forEach(lines::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedReader getGzipReader(File file) throws IOException {
        return new BufferedReader(
                new InputStreamReader(
                        new GZIPInputStream(new FileInputStream(file))));
    }

    private BufferedReader getZipReader(File file) throws IOException {
        return new BufferedReader(
                new InputStreamReader(
                        new ZipInputStream(new FileInputStream(file))));
    }

    private BufferedReader getReader(File file) throws IOException {
        return new BufferedReader(new FileReader(file));
    }

    private void saveToFile(List<String[]> lines, File file) {
        file.delete();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file))))) {
            lines.forEach(line -> {
                try {
                    writer.write(OS.asString(line));
                    writer.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
