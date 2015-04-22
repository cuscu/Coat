/*
 * Copyright (C) 2014 UICHUIMI
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package coat.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * This class contains methods to deal with FileSystem. Methods to open/save files, select paths,
 * copy directories and get user/home path.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public final class FileManager {

    /**
     * Filters SAM or BAM files (.bam and .sam)
     */
    public static final FileChooser.ExtensionFilter SAM_FILTER
            = new FileChooser.ExtensionFilter("Sequence Alignment/Map Format (.bam .sam)", "*.bam",
                    "*.sam");
    /**
     * Admits all files
     */
    public static final FileChooser.ExtensionFilter ALL_FILTER
            = new FileChooser.ExtensionFilter("All files", "*");
    /**
     * Filters VCF files (.vcf)
     */
    public static final FileChooser.ExtensionFilter VCF_FILTER
            = new FileChooser.ExtensionFilter("Variant Call Format (.vcf)", "*.vcf");
    /**
     * Filters TSV files (.tsv)
     */
    public static final FileChooser.ExtensionFilter TSV_FILTER
            = new FileChooser.ExtensionFilter("Tab Separated Values (.tsv)", "*.tsv");
    /**
     * Filters MIST files (.mist)
     */
    public static final FileChooser.ExtensionFilter MIST_FILTER
            = new FileChooser.ExtensionFilter("Missing sequences tool format (.mist)", "*.mist");
    /**
     * The last successful path. Id est, the last path where the user did not canceled the file
     * selection.
     */
    static File lastPath;
    /**
     * The user home path. In Linux it use to be /home/username
     */
    private static final File USER_HOME = new File(System.getProperty("user.home"));
    /**
     * The path where ExomeSuite is running. By default is the same location than ExomeSuite.jar
     */
    private static final File USER_PATH = new File(System.getProperty("user.dir"));

    /**
     * Opens a Dialog to select a folder. It is the same than calling @code{openDirectory(title,
     * null)}
     *
     * @param title The title for the DirectoryChooser.
     * @return A File or null if user canceled.
     */
    public static File selectFolder(String title) {
        return openDirectory(title, null);
    }

    /**
     * Opens a Dialog to select a folder.
     *
     * @param title The title for the DirectoryChooser.
     * @return A File or null if user canceled.
     */
    public static File openDirectory(String title) {
        return openDirectory(title, lastPath);
    }

    /**
     * Opens a Dialog to select a folder. If initDir is a valid directory, FileChooser will open
     * there.
     *
     * @param title The title for the DirectoryChooser.
     * @param initDir the initial directory.
     * @return A File or null if user canceled.
     */
    public static File openDirectory(String title, File initDir) {
        DirectoryChooser chooser = new DirectoryChooser();
        if (title != null) {
            chooser.setTitle(title);
        }
        chooser.setInitialDirectory(initDir != null && initDir.isDirectory() && initDir.exists()
                ? initDir : getLastPath());
        File file = chooser.showDialog(null);
        return (file != null) ? (lastPath = file) : null;
    }

    /**
     * Opens a dialog window (FileChooser) and lets the user select a single File. Additionally, if
     * the selected file is not null, the textField text will be set to the file.getAbsolutePath().
     *
     * @param textField the textField to set if a file is selected
     * @param title the title of the FileChooser
     * @param filters any number of ExtensionFilter. Use OS.[FORMAT]_FILTER constants.
     * @return the selected file or null
     */
    public static File openFile(TextField textField, String title, ExtensionFilter... filters) {
        return openFile(textField, title, null, filters);
    }

    /**
     * Opens a dialog window (FileChooser) and lets the user select a single File. Additionally, if
     * the selected file is not null, the textField text will be set to the file.getAbsolutePath().
     *
     * @param textField the textField to set if a file is selected
     * @param title the title of the FileChooser
     * @param initDir the initial directory
     * @param filters any number of ExtensionFilter. Use OS.[FORMAT]_FILTER constants.
     * @return the selected file or null
     */
    public static File openFile(TextField textField, String title, File initDir,
            ExtensionFilter... filters) {
        File f = openFile(title, initDir, filters);
        if (f != null) {
            textField.setText(f.getAbsolutePath());
        }
        return f;
    }

    /**
     * Opens a dialog window (FileChooser) and lets the user select a single File.
     *
     * @param title the title of the FileChooser
     * @param filters any number of ExtensionFilter. Use OS.[FORMAT]_FILTER constants.
     * @return the selected file or null
     */
    public static File openFile(String title, ExtensionFilter... filters) {
        return openFile(title, null, filters);
    }

    /**
     * Opens a dialog window (FileChooser) and lets the user select a single File.
     *
     * @param title the title of the FileChooser
     * @param filters any number of ExtensionFilter. Use OS.[FORMAT]_FILTER constants.
     * @return the selected file or null
     */
    public static File openFile(String title, List<ExtensionFilter> filters) {
        return openFile(title, null, filters);
    }

    /**
     * Opens a dialog window (FileChooser) and lets the user select a single File.
     *
     * @param title the title of the FileChooser
     * @param initDir the initial directory
     * @param filters any number of ExtensionFilter. Use OS.[FORMAT]_FILTER constants.
     * @return the selected file or null
     */
    public static File openFile(String title, File initDir, ExtensionFilter... filters) {
        return openFile(title, initDir, Arrays.asList(filters));
    }

    /**
     * Opens a dialog window (FileChooser) and lets the user select a single File.
     *
     * @param title the title of the FileChooser
     * @param initDir the initial directory
     * @param filters any number of ExtensionFilter. Use OS.[FORMAT]_FILTER constants.
     * @return the selected file or null
     */
    public static File openFile(String title, File initDir, List<ExtensionFilter> filters) {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(initDir != null && initDir.isDirectory() && initDir.exists()
                ? initDir : getLastPath());
        chooser.getExtensionFilters().addAll(filters);
        chooser.setTitle(title);
        File f = chooser.showOpenDialog(null);
        if (f != null) {
            lastPath = f.getParentFile();
        }
        return f;
    }

    /**
     * Opens a dialog that let the user to select one or more files.
     *
     * @param title the title of the window
     * @param filters the filters to use
     * @return the list of selected files or null
     */
    public static List<File> openFiles(String title, ExtensionFilter... filters) {
        return openFiles(title, lastPath, Arrays.asList(filters));
    }

    /**
     * Opens a dialog window (FileChooser) and lets the user select multiple Files.
     *
     * @param title the title of the FileChooser
     * @param initDir the initial directory
     * @param filters any number of ExtensionFilter. Use OS.[FORMAT]_FILTER constants.
     * @return the selected files or null
     */
    public static List<File> openFiles(String title, File initDir, List<ExtensionFilter> filters) {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(initDir != null && initDir.isDirectory() && initDir.exists()
                ? initDir : getLastPath());
        chooser.getExtensionFilters().addAll(filters);
        chooser.setTitle(title);
        List<File> f = chooser.showOpenMultipleDialog(null);
        if (f != null && !f.isEmpty()) {
            lastPath = f.get(0).getParentFile();
        }
        return f;
    }

    /**
     * Opens a dialog for the user to create a file. File system file is not created in this method.
     * If the user do not write the file extension, the default will be the first of the selected
     * ExtensionFilter.
     *
     * @param title dialog title
     * @param filters any number of ExtensionFilters
     * @return the selected file or null
     */
    public static File saveFile(String title, ExtensionFilter... filters) {
        return saveFile(title, null, Arrays.asList(filters));
    }

    /**
     * Opens a dialog for the user to create a file. File system file is not created in this method.
     * If the user do not write the file extension, the default will be the first of the selected
     * ExtensionFilter.
     *
     * @param title dialog title
     * @param filters any number of ExtensionFilters
     * @return the selected file or null
     */
    public static File saveFile(String title, List<ExtensionFilter> filters) {
        return saveFile(title, null, filters);
    }

    /**
     * Opens a dialog for the user to create a file. File system file is not created in this method.
     * If the user do not write the file extension, the default will be the first of the selected
     * ExtensionFilter.
     *
     * @param title dialog title
     * @param initDir the initial directory
     * @param filters any number of ExtensionFilters
     * @return the selected file or null
     */
    public static File saveFile(String title, File initDir, List<ExtensionFilter> filters) {
        return saveFile(title, initDir, null, filters);
    }

    /**
     * Opens a dialog for the user to create a file. File system file is not created in this method.
     * If the user do not write the file extension, the default will be the first of the selected
     * ExtensionFilter.
     *
     * @param title dialog title
     * @param initDir the initial directory
     * @param initName initial name for file
     * @param filters any number of ExtensionFilters
     * @return the selected file or null
     */
    public static File saveFile(String title, File initDir, String initName, List<ExtensionFilter> filters) {
        FileChooser chooser = new FileChooser();
        if (initName != null) {
            chooser.setInitialFileName(initName);
        }
        chooser.setTitle(title);
        chooser.setInitialDirectory(initDir != null && initDir.isDirectory() && initDir.exists()
                ? initDir : getLastPath());
        chooser.getExtensionFilters().addAll(filters);
        File file = chooser.showSaveDialog(null);
        if (file != null) {
            lastPath = file.getParentFile();
            if (!filters.isEmpty()) {
                String f = chooser.getSelectedExtensionFilter().getExtensions().get(0);
                if (f != null) {
                    String ext = f.replace("*", "");
                    if (file.getName().endsWith(ext)) {
                    } else {
                        return new File(file.getAbsolutePath() + ext);
                    }
                }
            }
        }
        return file;
    }

    /**
     * Opens a dialog for the user to create a file. File system file is not created in this method.
     * If the user do not write the file extension, the default will be the first of the selected
     * ExtensionFilter.
     *
     * @param title dialog title
     * @param initDir the initial directory
     * @param initName initial name for file
     * @param filters any number of ExtensionFilters
     * @return the selected file or null
     */
    public static File saveFile(String title, File initDir, String initName, ExtensionFilter... filters) {
        return saveFile(title, initDir, initName, Arrays.asList(filters));
    }

    /**
     * Opens a dialog for the user to create a file and sets the text of the TextField to the file
     * name. If the user do not write the file extension, the default will be the first of the
     * selected ExtensionFilter.
     *
     * @param textField the textField
     * @param title dialog title
     * @param filters any number of ExtensionFilters
     * @return the selected file or null
     */
    public static File saveFile(TextField textField, String title, ExtensionFilter... filters) {
        return saveFile(textField, title, null, filters);
    }

    /**
     * Opens a dialog for the user to create a file and sets the text of the TextField to the file
     * name. If the user do not write the file extension, the default will be the first of the
     * selected ExtensionFilter.
     *
     * @param textField the textField
     * @param title dialog title
     * @param initDir the initial directory
     * @param filters any number of ExtensionFilters
     * @return the selected file or null
     */
    public static File saveFile(TextField textField, String title, File initDir,
            ExtensionFilter... filters) {
        File f = saveFile(title, initDir, Arrays.asList(filters));
        if (f != null) {
            textField.setText(f.getAbsolutePath());
        }
        return f;
    }

    /**
     * Returns the last used path when opening or saving a file or directory. Last path is only
     * modified when the open or save operation is successfully completed. First time this method is
     * called, it will be used @code{getUsrHomePath}.
     *
     * @return the last path
     */
    public static File getLastPath() {
        if (FileManager.lastPath == null) {
            FileManager.lastPath = getUserHome();
        }
        return lastPath;
    }

    /**
     * The user home directory. In linux systems it use to be /home/username.
     *
     * @return the user home directory
     */
    public static File getUserHome() {
        return USER_HOME;
    }

    /**
     * The path where ExomeSuite is running. By default is the same location than ExomeSuite.jar
     *
     * @return the execution directory.
     */
    public static File getUserPath() {
        return USER_PATH;
    }

    /**
     * Deletes this file/directory and, if recursive is true and it is a directory, everything
     * inside it.
     *
     * @param file the file or directory to remove
     * @param recursive true if you want to perform a recursive deletion
     * @return true if it could delete the file and, if specified, all of its content
     */
    public static boolean delete(File file, boolean recursive) {
        if (recursive) {
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    if (!delete(f, recursive)) {
                        return false;
                    }
                }
                return file.delete();
            } else {
                return file.delete();
            }
        } else {
            return file.delete();
        }
    }

    /**
     * Macro function that ckecks that String is not null, not empty and the file it represents
     * exists.
     *
     * @param parameter a string that represents a File
     * @return parameter not null and parameter is not empty and file exists
     */
    public static boolean tripleCheck(String parameter) {
        return parameter != null && !parameter.isEmpty() && new File(parameter).exists();
    }

    /**
     * Checks all of the files passed by argument and returns a list with the files that return
     * false in {@code tripleCheck()}. Use this function when you have to systematically check a
     * list of File parameters.
     *
     * @param parameters the list of parameters to check
     * @return the list of parameters which do not pass tripleCheck
     */
    public static List<String> tripleCheck(String... parameters) {
        return tripleCheck(Arrays.asList(parameters));
    }

    /**
     * Checks all of the files passed by argument and returns a list with the files that return
     * false in {@code tripleCheck()}. Use this function when you have to systematically check a
     * list of File parameters.
     *
     * @param parameters the list of parameters to check
     * @return the list of parameters which do not pass tripleCheck
     */
    public static List<String> tripleCheck(List<String> parameters) {
        return parameters.stream().filter(param -> !tripleCheck(param)).collect(Collectors.toList());
    }

    public static String guessEncoding(File fastFile) {
        try (BufferedReader br = openGZipBR(fastFile)) {
            int phred33 = 0;
            int phred64 = 0;
            for (int i = 0; i < 100; i++) {
                br.readLine();
                br.readLine();
                br.readLine();
                String encoding = br.readLine();
                for (int j = 0; j < encoding.length(); j++) {
                    int c = encoding.charAt(j);
                    if (c - 64 < 0) {
                        phred33++;
                    } else if (c - 33 > 41) {
                        phred64++;
                    }
                }
            }
            if (phred33 > phred64) {
                return "phred+33";
            } else if (phred64 > phred33) {
                return "phred+64";
            } else {
                System.out.println("Encoding unknown");
            }
        } catch (FileNotFoundException ex) {
//            MainViewController.printException(ex);
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     *
     * @param input
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static BufferedReader openGZipBR(File input) throws IOException {
        return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(
                input))));
    }

    //    private void changePath() {
    //        // Move files and directories
    //        final File from = new File(project.getProperties().getProperty(Project.PATH));
    //        final File to = path.getValue();
    //        clone(from, to);
    //        // Change properties by replacing path in all properties
    //        project.getProperties().forEach((Object t, Object u) -> {
    //            String key = (String) t;
    //            String value = (String) u;
    //            if (value.startsWith(from.getAbsolutePath())) {
    //                project.getProperties().setProperty(key,
    //                        value.replace(from.getAbsolutePath(), to.getAbsolutePath()));
    //            }
    //        });
    //        // Force properties file to be written
    //        project.getProperties().setProperty(Project.PATH, to.getAbsolutePath());
    //    }
    private void clone(File from, File to) {
        for (File f : from.listFiles()) {
            if (f.isFile()) {
                Path source = f.toPath();
                Path target = new File(to, f.getName()).toPath();
                try {
                    Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
                    f.delete();
                } catch (IOException ex) {
                    Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (f.isDirectory()) {
                File newPath = new File(to, f.getName());
                newPath.mkdirs();
                clone(f, newPath);
            } else {
                f.delete();
            }
        }
        from.delete();
    }

}
