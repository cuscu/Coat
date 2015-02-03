package coat.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Contains methods to control application properties (databases, opened projects..) and most
 * general methods, like {@code humanReadableByteCount()} or {@code asString()}. If you want to
 * perform a general GUI operation, such as print a message, use {@link MainViewController}.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class OS {

    private final static ResourceBundle resources = ResourceBundle.getBundle("coat.language.Texts");

    private static final ObservableList<String> standardChromosomes = FXCollections.observableArrayList();
    /**
     * The list of available locales.
     */
    private static final List<Locale> locales = new ArrayList();

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
     * @param si if true, divides by 1000; else by 1024
     * @return a human readable size
     */
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Converts an Array to String using the separator. Omits the last separator. [value1 value2
     * value3] to value1,value2,value3
     *
     * @param separator something like "\t" or ","
     * @param values a list of values
     * @return the stringified list
     */
    public static String asString(String separator, String... values) {
        if (values.length == 0) {
            return "";
        }
        String s = values[0];
        int i = 1;
        while (i < values.length) {
            s += separator + values[i++];
        }
        return s;
    }

    /**
     * Converts an Array to String using the separator. Omits the last separator. [value1 value2
     * value3] to value1,value2,value3
     *
     * @param separator something like "\t" or ","
     * @param values a list of values
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

}
