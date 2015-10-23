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

package coat.core.vcfreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VcfHeader {
    private String fileFormat;
    private List<Map<String, String>> infos = new ArrayList<>();
    private List<Map<String, String>> formats = new ArrayList<>();
    private List<Map<String, String>> filters = new ArrayList<>();
    private List<Map<String, String>> alts = new ArrayList<>();
    private List<Map<String, String>> contigs = new ArrayList<>();
    private List<Map<String, String>> samples = new ArrayList<>();
    private List<Map<String, String>> pedigrees = new ArrayList<>();
    private Map<String, String> headers = new HashMap<>();

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public List<Map<String, String>> getInfos() {
        return infos;
    }

    public List<Map<String, String>> getFormats() {
        return formats;
    }

    public List<Map<String, String>> getFilters() {
        return filters;
    }

    public List<Map<String, String>> getAlts() {
        return alts;
    }

    public List<Map<String, String>> getContigs() {
        return contigs;
    }

    public List<Map<String, String>> getSamples() {
        return samples;
    }

    public List<Map<String, String>> getPedigrees() {
        return pedigrees;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getFileFormat() {
        return fileFormat;
    }
}
