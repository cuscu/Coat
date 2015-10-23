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

package coat.core.poirot.dataset.mentha;

import coat.core.poirot.dataset.Dataset;
import coat.core.poirot.dataset.hgnc.HGNCDatabase;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

/**
 * (0) id, (1) source, (2) target, (3) database, (4) type, (5) method, (6) score
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class MenthaDatasetLoader extends Task<Dataset> {

    private final static String[] HEADERS = {"id", "source", "target", "database", "type", "method", "score"};

    @Override
    protected Dataset call() throws Exception {
        final Dataset dataset = new Dataset();
        dataset.setColumnNames(Arrays.asList(HEADERS));
        try (BufferedReader reader = getReader()) {
            reader.readLine();
            reader.lines().map(this::getFields).forEach(dataset::addInstance);
        }
        dataset.createIndex(1);
        dataset.createIndex(2);
        return dataset;
    }

    private BufferedReader getReader() throws IOException {
        return new BufferedReader(
                new InputStreamReader(
                        new GZIPInputStream(getClass().getResourceAsStream("mentha.tsv.gz"))));
    }

    private String[] getFields(String line) {
        /*
            00 ID(s) interactor A	uniprotkb:Q86V88
            01 ID(s) interactor B	uniprotkb:P60981

            02 Alt. ID(s) interactor A	-
            03 Alt. ID(s) interactor B	-

            04 Alias(es) interactor A	uniprotkb:MDP1(gene name)
            05 Alias(es) interactor B	uniprotkb:DSTN(gene name)

            06 Interaction detection method(s)	psi-mi:"MI:0401"(biochemical)
            07 Publication 1st author(s)	-
            08 Publication Identifier(s)	pubmed:22939629

            09 Taxid interactor A	taxid:9606(Homo sapiens)
            10 Taxid interactor B 	taxid:9606(Homo sapiens)

            11 Interaction type(s) psi-mi:"MI:0403"(colocalization)
            12 Source database(s)	psi-mi:"MI:0463"(biogrid)
            13 Interaction identifier(s)    BIOGRID:749408
            14 Confidence value(s)	mentha-score:0.081
            15 Expansion method(s)	-

            16 Biological role(s) interactor A	-
            17 Biological role(s) interactor B -

            18 Experimental role(s) interactor A	-
            19 Experimental role(s) interactor B	-

            20 Type(s) interactor A	-
            21 Type(s) interactor B	-

            22 Xref(s) interactor A	-
            23 Xref(s) interactor B	-

            24 Interaction Xref(s)	-

            25 Annotation(s) interactor A	-
            26 Annotation(s) interactor B	-

            27 Interaction annotation(s)	-
            28 Host organism(s)	-
            29 Interaction parameter(s)	-
            30 Creation date	-
            31 Update date	-

            32 Checksum(s) interactor A -
            33 Checksum(s) interactor B	-

            34 Interaction Checksum(s)	-
            35 Negative	-

            36 Feature(s) interactor A	-
            37 Feature(s) interactor B	-

            38 Stoichiometry(s) interactor A	-
            39 Stoichiometry(s) interactor B	-

            40 Identification method participant A	-
            41 Identification method participant B -
         */
        final String row[] = line.split("\t");
        final String source = extractGeneName(row[4]);
        final String target = extractGeneName(row[5]);
        final String method = extractMethod(row[6]);
        final String type = extractType(row[11]);
        final String database = extractDatabase(row[12]);
        final String id = extractId(row[13]);
        final String score = extractScore(row[14]);
        final String fields[] = new String[HEADERS.length];
        fields[0] = id;
        fields[1] = HGNCDatabase.getStandardSymbol(source);
        fields[2] = HGNCDatabase.getStandardSymbol(target);
        fields[3] = database;
        fields[4] = type;
        fields[5] = method;
        fields[6] = score;
        return fields;
    }

    private static String extractScore(String text) {
        // mentha-score:0.081
        if (text.startsWith("mentha-score:")) return text.substring(13);
        if (!text.equals("-")) System.err.println(text);
        return null;
    }

    private static String extractDatabase(String text) {
        // psi-mi:"MI:0463"(biogrid)
        return extractMethod(text);
    }

    private static String extractType(String text) {
        // psi-mi:"MI:0403"(colocalization)
        return extractMethod(text);
    }

    private static String extractMethod(String text) {
        // psi-mi:"MI:0401"(biochemical)
        if (text.startsWith("psi-mi")) {
            final int startPos = text.indexOf("(");
            final int endPos = text.indexOf(")");
            if (startPos > 0 && endPos > 0) return text.substring(startPos + 1, endPos);
        }
        System.err.println(text);
        return null;
    }

    private static String extractGeneName(String text) {
        // uniprotkb:MDP1(gene name)
        if (text.startsWith("uniprotkb:")) {
            int index = text.indexOf("(");
            if (index > 0) {
                String id = text.substring(10, index);
                if (id.contains(" ")) id = id.split(" ")[0].replace("\"", "");
                return id;
            }
        }
        System.err.println(text);
        return null;
    }

    private static String extractId(String text) {
        // BIOGRID:749408
        int index = text.indexOf(":");
        if (index > 0) return text.substring(index + 1);
        System.err.println(text);
        return null;
    }

}
