/*
 * Copyright (C) 2015 UICHUIMI
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
package coat.model.vcf;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author UICHUIMI
 */
public class Combinator {

    public static boolean combine(List<File> includes, List<File> excludes, File output) {
        try {
            new CombinatorVcf().combine(includes.toArray(new File[includes.size()]), excludes.toArray(new File[excludes.size()]), output);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(Combinator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
