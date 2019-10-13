/* ****************************************************************************
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
 **************************************************************************** */

package coat.core;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class OmimTest {


    @Test
    public void testThree() {
        File omim = new File("omim/genemap");
        try (BufferedReader reader = new BufferedReader(new FileReader(omim))) {
            reader.lines().forEach((line) -> {
                String[] row = line.split("\\|");
                System.out.println("["+row[5] + "]\n" + row[7] + "\n" + row[11]);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
