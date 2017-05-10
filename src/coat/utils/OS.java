/******************************************************************************
 * Copyright (C) 2015 UICHUIMI                                                *
 * *
 * This program is free software: you can redistribute it and/or modify it    *
 * under the terms of the GNU General Public License as published by the      *
 * Free Software Foundation, either version 3 of the License, or (at your     *
 * option) any later version.                                                 *
 * *
 * This program is distributed in the hope that it will be useful, but        *
 * WITHOUT ANY WARRANTY; without even the implied warranty of                 *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 * *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 ******************************************************************************/

package coat.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Contains methods to control application properties (databases, opened projects..) and most
 * general methods, like {@code humanReadableByteCount()} or {@code asString()}.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class OS {

    private final static DateFormat df = new SimpleDateFormat("HH:mm:ss");
    private static final ResourceBundle.Control control = new UTF8Control();
    private final static ResourceBundle resources = ResourceBundle.getBundle("coat.language.Texts", control);
    private final static File configPath = new File(getJarDir(OS.class), "config");


    private static final ObservableList<String> standardChromosomes = FXCollections.observableArrayList(Arrays.asList(
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13",
            "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y"));
    /**
     * The list of available locales.
     */
    private static final List<Locale> locales = Arrays.asList(
            new Locale("es", "ES"),
            new Locale("en", "US"),
            new Locale("en", "UK")
    );

    /**
     * Compute the absolute file path to the jar file.
     * The framework is based on http://stackoverflow.com/a/12733172/1614775
     * But that gets it right for only one of the four cases.
     *
     * @param aclass A class residing in the required jar.
     * @return A File object for the directory in which the jar file resides.
     * During testing with NetBeans, the result is ./build/classes/,
     * which is the directory containing what will be in the jar.
     */
    private static File getJarDir(Class aclass) {
        final URL url1 = getClassUrl(aclass);
        final String extURL = toExternalUrl(aclass, url1);
        URL url = toUrl(extURL);
        if (url == null) url = url1;
        try {
            return new File(url.toURI());
        } catch (URISyntaxException ex) {
            return new File(url.getPath());
        }
    }

    private static URL toUrl(String extURL) {
        try {
            return new URL(extURL);
        } catch (MalformedURLException mux) {
            // leave url unchanged; probably does not happen
        }
        return null;
    }

    private static URL getClassUrl(Class aclass) {
        URL url;
        try {
            url = aclass.getProtectionDomain().getCodeSource().getLocation();
            // url is in one of two forms
            //        ./build/classes/   NetBeans test
            //        jardir/JarName.jar  from a jar
        } catch (SecurityException ex) {
            url = aclass.getResource(aclass.getSimpleName() + ".class");
            // url is in one of two forms, both ending "/com/physpics/tools/ui/PropNode.class"
            //          file:/U:/Fred/java/Tools/UI/build/classes
            //          jar:file:/U:/Fred/java/Tools/UI/dist/UI.jar!
        }
        return url;
    }

    @NotNull
    private static String toExternalUrl(Class aclass, URL url) {
        String extURL = url.toExternalForm();

        // prune for various cases
        if (extURL.endsWith(".jar"))   // from getCodeSource
            extURL = extURL.substring(0, extURL.lastIndexOf("/"));
        else {  // from getResource
            String suffix = "/" + (aclass.getName()).replace(".", "/") + ".class";
            extURL = extURL.replace(suffix, "");
            if (extURL.startsWith("jar:") && extURL.endsWith(".jar!"))
                extURL = extURL.substring(4, extURL.lastIndexOf("/"));
        }
        return extURL;
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
     * @param values a list of values
     * @return the stringified list
     */
    public static String asString(List<String> values) {
        return asString("\t", values);
    }

    /**
     * Converts an Array to String using tab as separator. Omits the last separator. [value1 value2
     * value3] to value1,value2,value3
     *
     * @param values a list of values
     * @return the stringified list
     */
    public static String asString(String... values) {
        return asString("\t", values);
    }

    public static String asString(String separator, String[] values) {
        return asString(separator, Arrays.asList(values));
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
        if (values.isEmpty()) return "";
        final StringBuilder builder = new StringBuilder(values.get(0));
        for (int i = 1; i < values.size(); i++) builder.append(separator).append(values.get(i));
        return builder.toString();
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
    public static String getFormattedString(String key, Object... args) {
        return new MessageFormat(resources.getString(key), resources.getLocale()).format(args);
    }

    /**
     * This is a shorthand for <code>OS.getResources().getString(key)</code>, and it returns empty String ("")
     * instead of null.
     *
     * @param key key to access text
     * @return the text or the empty String
     */
    public static String getString(String key) {
        return resources.containsKey(key) ? resources.getString(key) : "";
    }


    public static String getCapitalizedString(String key) {
        final String value = resources.getString(key);
        return String.valueOf(value.charAt(0)).toUpperCase() + value.substring(1);

    }

    public static File getConfigPath() {
        return configPath;
    }

    /**
     * Control to read UTF-8 property files.
     * <p>
     * Thanks to
     * http://stackoverflow.com/questions/4659929/how-to-use-utf-8-in-resource-properties-with-resourcebundle
     */
    private static class UTF8Control extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            // The below is a copy of the default implementation.
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            ResourceBundle bundle = null;
            InputStream stream = null;
            if (reload) {
                URL url = loader.getResource(resourceName);
                if (url != null) {
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName);
            }
            if (stream != null) {
                try {
                    // Only this line is changed to make it to read properties files as UTF-8.
                    bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
                } finally {
                    stream.close();
                }
            }
            return bundle;
        }
    }
}
