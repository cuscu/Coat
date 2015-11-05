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
package coat.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;

/**
 * Contains methods to control application properties (databases, opened projects..) and most
 * general methods, like {@code humanReadableByteCount()} or {@code asString()}.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class OS {

    private final static ResourceBundle resources = ResourceBundle.getBundle("coat.language.Texts");

    private static final ObservableList<String> standardChromosomes = FXCollections.observableArrayList();
    /**
     * The list of available locales.
     */
    private static final List<Locale> locales = new ArrayList<>();

    /**
     * Static "Constructor" of the class.
     */
    static {
        final String[] chrs = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
                "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y"};
        standardChromosomes.addAll(Arrays.asList(chrs));
        locales.add(new Locale("es", "ES"));
        locales.add(new Locale("en", "US"));
        locales.add(new Locale("en", "UK"));
    }

    /**
     * Takes a byte value and convert it to the corresponding human readable unit.
     *
     * @param bytes value in bytes
     * @param si    if true, divides by 1000; else by 1024
     * @return a human readable size
     */
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Converts an Array to String using tab as separator. Omits the last separator. [value1 value2
     * value3] to value1,value2,value3
     *
     * @param values    a list of values
     * @return the stringified list
     */
    public static String asString(String... values) {
        if (values.length == 0) {
            return "";
        }
        String s = values[0];
        int i = 1;
        while (i < values.length) {
            s += "\t" + values[i++];
        }
        return s;
    }

    /**
     * Converts an Array to String using the separator. Omits the last separator. [value1 value2
     * value3] to value1,value2,value3
     *
     * @param separator something like "\t" or ","
     * @param values    a list of values
     * @return the stringified list
     */
    public static String asString(String separator, List<String> values) {
        if (values.isEmpty()) {
            return "";
        }
        String s = "";
        int i = 0;
        while (i < values.size() - 1) {
            s += values.get(i++) + separator;
        }
        return s + values.get(i);
    }

    /**
     * Gets the temporary path of the application. (that is userdir/temp).
     *
     * @return the temp path.
     */
    public static String getTempDir() {
        File temp = new File(FileManager.getUserPath(), "temp");
        temp.mkdirs();
        return temp.getAbsolutePath();
    }

    /**
     * Gets the list of standard chromosomes (1-22, X and Y).
     *
     * @return the list of chromosomes
     */
    public static ObservableList<String> getStandardChromosomes() {
        return standardChromosomes;
    }

    public static List<Locale> getAvailableLocales() {
        return locales;
    }

    public static ResourceBundle getResources() {
        return resources;
    }

    /**
     * Formats the String key from resources (the current ResourceBundle) using a MessageFormat
     * which will have args.
     *
     * @param key  the key of the string
     * @param args the arguments of the string
     * @return the resulting string using the current locale
     */
    public static String getStringFormatted(String key, Object... args) {
        return new MessageFormat(resources.getString(key), resources.getLocale()).format(args);
    }

    public static String getString(String key) {
        return resources.getString(key);
    }

}
