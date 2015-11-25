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

import coat.core.poirot.dataset.hgnc.HGNC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class OmimParser {

    private final static Pattern OMIM_PATTERN = Pattern.compile("\\s*(.*), (\\d{6})\\s?\\((\\d)\\)");
    private final static Pattern OMIM_SUB_PATTERN = Pattern.compile("\\s*(.*) \\((\\d)\\)");
    private final static Pattern OMIM_SUB_SUB_PATTERN = Pattern.compile("\\s*(.*), (\\d{6})");

    private final static Map<String, String> STATUS = new HashMap<>();
    static {
        STATUS.put("C", "Confirmed");
        STATUS.put("P", "Provisional");
        STATUS.put("I", "Inconsistent");
        STATUS.put("L", "Limbo");
    }

    public static List<String[]> getEntries(String[] line) {
        List<String[]> list = new ArrayList<>();
         /*
            1  - Numbering system, in the format  Chromosome.Map_Entry_Number
            2  - Month entered
            3  - Day     "
            4  - Year    "
            5  - Cytogenetic location
            6  - Gene Symbol(s)
            7  - Gene Status (see below for codes)
            8  - Title
            9 - MIM Number
            10 - Method (see below for codes)
            11 - Comments
            12 - Disorders (each disorder is followed by its MIM number, if
                different from that of the locus, and phenotype mapping method (see
                below).  Allelic disorders are separated by a semi-colon.
            13 - Mouse correlate
            14 - Reference
            */
        final String id = line[0];
        final String gene = HGNC.getStandardSymbol(line[5].split(",")[0]);
        final String status = STATUS.get(line[6]);
        final String mimNumbuer = line[8];
        final String[] phenotypes = line[11].split(";");
        for (String pheno : phenotypes) {
            if (!pheno.equals("-")) {
                Disease disease = getDisease(pheno);
                if (disease == null) disease = getSubDisease(pheno, mimNumbuer);
                if (disease == null) disease = getSubSubDisease(pheno);
                if (disease != null) list.add(new String[]{"OMIM", id, gene, status, disease.name, disease.id, disease.status});
            }
        }
        return list;

    }


    private static Disease getDisease(String pheno) {
        final Matcher matcher = OMIM_PATTERN.matcher(pheno); //  disease, 134532 (2)
        if (matcher.matches()) return new Disease(matcher.group(1), matcher.group(2), matcher.group(3));
        return null;
    }


    private static Disease getSubDisease(String pheno, String mimNumber) {
        final Matcher matcher = OMIM_SUB_PATTERN.matcher(pheno); // disease (2)
        if (matcher.matches()) return new Disease(matcher.group(1), mimNumber, matcher.group(2));
        return null;
    }

    private static Disease getSubSubDisease(String pheno) {
        final Matcher subSubMatcher = OMIM_SUB_SUB_PATTERN.matcher(pheno); // disease, 123432
        if (subSubMatcher.matches()) return new Disease(subSubMatcher.group(1), subSubMatcher.group(2), "-");
        return null;
    }


    private static class Disease {
        final String name, id, status;


        private Disease(String name, String id, String status) {
            this.name = name;
            this.id = id;
            this.status = status;
        }
    }
}
