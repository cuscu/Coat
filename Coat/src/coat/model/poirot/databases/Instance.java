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

package coat.model.poirot.databases;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Instance {

    protected final Dataset dataset;
    private final Object[] fields;

    public Instance(Dataset dataset, Object[] fields) {
        this.dataset = dataset;
        this.fields = fields;
    }

    public Object getField(int index) {
        return fields[index];
    }

    public Object getField(String column) {
        return fields[dataset.indexOf(column)];
    }

    public Dataset getDataset() {
        return dataset;
    }
}
