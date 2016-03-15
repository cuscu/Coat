/*
 * Copyright (c) UICHUIMI 2016
 *
 * This file is part of Coat.
 *
 * Coat is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package coat.core.poirot.dataset;

import coat.utils.Web;
import org.jetbrains.annotations.Nullable;
import org.neo4j.graphdb.GraphDatabaseService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by uichuimi on 15/03/16.
 */
public class BioGridNeo {

    private static final String API_KEY = "e2d2aa59b5dd3f6ec3a45bcbafd0211d";
    private static final String BASE_URL = "http://webservice.thebiogrid.org/interactions/";
    private static final int MAX_RESULTS = 10000;

    public static void update(GraphDatabaseService graphDatabase) {
        final int total = guessTotal();
        final String url = BASE_URL + "?accesskey=" + API_KEY;
    }

    private static int guessTotal() {
        final String url = BASE_URL + "?accesskey=" + API_KEY + "?format=count";
        final String result = Web.getFromUrl(url);
        System.out.println(result);
        return 0;
    }

}
