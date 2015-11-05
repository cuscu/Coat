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

import coat.core.poirot.graph.GraphEvaluator;
import coat.core.vcf.Variant;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PhenotypeScore {

    private static int MAX_DISTANCE = 3;

    public static void score(PearlGraph database) {
        database.getPearls(Pearl.Type.DISEASE).forEach(PhenotypeScore::score);
        database.getPearls(Pearl.Type.EXPRESSION).forEach(PhenotypeScore::score);
    }

    private static void score(Pearl pearl) {
        final double max = pearl.getRelationships().entrySet().stream()
                .mapToDouble(value -> score(pearl, value, 2))
                .max().orElse(0);
        pearl.setScore(max);
    }


    private static double score(Pearl pearl, Map.Entry<Pearl, List<PearlRelationship>> entry, int distance) {
        return getGeneScore(pearl, entry, distance);
    }

    private static double getRelationshipScore(List<PearlRelationship> relationships) {
        return relationships.stream().mapToDouble(PhenotypeScore::score).max().orElse(1.0);
    }

    private static double getGeneScore(Pearl sourcePearl, Map.Entry<Pearl, List<PearlRelationship>> entry, int distance) {
        final double relationshipScore = getRelationshipScore(entry.getValue());
        final Pearl pearl = entry.getKey();
        if (distance > MAX_DISTANCE) return 0;
        if (pearl.getType() != Pearl.Type.GENE) return 0.0;
        double variantScore = getVariantScore(pearl);
        final double relationshipsScore = getRelationshipsScore(sourcePearl, distance, pearl);
        return (variantScore + relationshipsScore / (distance * distance)) * relationshipScore;
    }

    private static double getRelationshipsScore(Pearl sourcePearl, int distance, Pearl pearl) {
        return pearl.getRelationships().entrySet().stream()
                    .filter(pearlListEntry -> pearlListEntry.getKey().getType() == Pearl.Type.GENE)
                    .filter(pearlListEntry -> pearlListEntry.getKey() != sourcePearl)
                    .mapToDouble(value -> score(pearl, value, distance + 1))
                    .max().orElse(0);
    }

    private static double getVariantScore(Pearl pearl) {
        double variantScore = 0;
        if (pearl.getProperties().containsKey("variants")) {
            final List<Variant> variants = (List<Variant>) pearl.getProperties().get("variants");
            variantScore = variants.stream()
                    .mapToDouble(PhenotypeScore::score)
                    .max().orElse(1.0);
        }
        return variantScore;
    }

    private static double score(PearlRelationship pearlRelationship) {
        return GraphEvaluator.RELATIONSHIP_SCORE.getOrDefault(getRelationshipType(pearlRelationship), 1.0);
    }

    private static String getRelationshipType(PearlRelationship relationship) {
        if (relationship.getProperties().containsKey("type")) return (String) relationship.getProperties().get("type");
        if (relationship.getProperties().containsKey("method"))
            return (String) relationship.getProperties().get("method");
        if (relationship.getProperties().containsKey("confidence"))
            return (String) relationship.getProperties().get("confidence");
        return null;
    }

    private static double score(Variant variant) {
        final String cons = variant.getInfo("CONS");
        if (cons == null) return 1;
        return Arrays.stream(cons.split(",")).mapToDouble(consequence -> GraphEvaluator.CONSEQUENCE_SCORE.getOrDefault(consequence, 1.0)).max().orElse(1.0);
    }
}
