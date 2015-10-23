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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PearlRelationship {

    private final Pearl source;
    private final Pearl target;
    private final Map<String, Object> properties = new HashMap<>();

    public PearlRelationship(Pearl source, Pearl target) {
        this.source = source;
        this.target = target;
    }

    public Pearl getTarget() {
        return target;
    }

    public Pearl getSource() {
        return source;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
