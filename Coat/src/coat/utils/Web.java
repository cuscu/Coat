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

package coat.utils;

import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;

/**
 * Created by uichuimi on 15/03/16.
 */
public class Web {


    public static String getFromUrl(String textUrl) {
        String result = null;
        try {
            final URL url = new URL(textUrl);
            url.openConnection();
            final BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream) url.getContent()));
            final StringBuilder builder = new StringBuilder();
            reader.lines().forEach((line) -> builder.append(line).append(System.lineSeparator()));
            result = builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
