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

import coat.core.vcfreader.MapGenerator;
import coat.core.vcfreader.VcfHeader;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Factory to load a VCF file into memory as a Dataset.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VcfLoader {

    private static List<String> headers;
    private static Dataset dataset;


    public static Dataset createDataset(File file) {
        headers = new ArrayList<>();
        headers.addAll(Arrays.asList("chrom", "pos", "id", "ref", "alt", "qual", "filter"));
        headers.addAll(readInfoHeaders(file));
        dataset = new Dataset();
        dataset.setColumnNames(headers);
        readVariants(file);
        return dataset;
    }

    private static void readVariants(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.lines().filter(line -> !line.startsWith("#")).map((VcfLoader::getFields)).forEach(dataset::addInstance);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> readInfoHeaders(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().filter(line -> line.startsWith("##INFO=<")).map(VcfLoader::extractName).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private static String extractName(String line) {
        // ##INFO=<ID=EUR_F,Number=1,Type=String,Description="Minor allele and frequency of existing variation in 1000 Genomes Phase 1 combined European population">
        return line.substring(8, line.indexOf(",")).replace("ID=", "");
    }

    private static Object[] getFields(String line) {
        final String[] variant = line.split("\t");
        final Object[] fields = new Object[headers.size()];
        // Copy chrom, pos, id, ref, alt, qual, filter
        System.arraycopy(variant, 0, fields, 0, 7);
        if (variant.length > 7) {
            final String[] infos = variant[7].split(";");
            for (String info : infos) {
                final String[] pair = info.split("=");
                final int position = dataset.indexOf(pair[0]);
                if (position > 0) fields[position] = pair.length > 1 ? pair[1] : true;
            }
        }
        return fields;
    }

    public static VcfHeader loadHeader(File reference) {
        VcfHeader header = new VcfHeader();
        try (BufferedReader reader = new BufferedReader(new FileReader(reference))) {
            reader.lines().filter(line -> line.startsWith("##")).forEach(line -> {
                if (line.startsWith("##fileformat=")) header.setFileFormat(line.substring(13));
                else if (line.startsWith("##INFO="))
                    header.getInfos().add(MapGenerator.parse(line.substring(8, line.length() - 1)));
                else if (line.startsWith("##FORMAT="))
                    header.getFormats().add(MapGenerator.parse(line.substring(10, line.length() - 1)));
                else if (line.startsWith("##FILTER="))
                    header.getFilters().add(MapGenerator.parse(line.substring(10, line.length() - 1)));
                else if (line.startsWith("##ALT="))
                    header.getAlts().add(MapGenerator.parse(line.substring(7, line.length() - 1)));
                else if (line.startsWith("##contig="))
                    header.getContigs().add(MapGenerator.parse(line.substring(10, line.length() - 1)));
                else if (line.startsWith("##SAMPLE="))
                    header.getSamples().add(MapGenerator.parse(line.substring(10, line.length() - 1)));
                else if (line.startsWith("##PEDIGREE="))
                    header.getPedigrees().add(MapGenerator.parse(line.substring(12, line.length() - 1)));
                else {
                    final int equalsPos = line.indexOf("=");
                    header.getHeaders().put(line.substring(2, equalsPos), line.substring(equalsPos + 1));

                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return header;
    }
}
