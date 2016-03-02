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

import java.sql.Connection;

/**
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class BioGridRetriever {


    private final static String API_KEY = "e2d2aa59b5dd3f6ec3a45bcbafd0211d";
    private final static String BASE_URL = "http://webservice.thebiogrid.org/interactions/?";
    private Connection connection;

    public void updateData(Connection connection) {
        this.connection = connection;
    }
}
