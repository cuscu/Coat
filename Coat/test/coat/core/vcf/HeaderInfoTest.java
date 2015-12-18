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

package coat.core.vcf;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class HeaderInfoTest {

    @Test
    public void test() {
        // This is basic
        final HeaderInfo headerInfo = new HeaderInfo("GENE", HeaderInfo.Type.STRING, 1, "Ensembl Gene ID");
        headerInfo.setProperty("assembly", "b37");
        Assert.assertEquals("GENE", headerInfo.getName());
        Assert.assertEquals("Ensembl Gene ID", headerInfo.getDescription());
        Assert.assertEquals(1, headerInfo.getNumber());
        Assert.assertEquals(HeaderInfo.Type.STRING, headerInfo.getType());
        Assert.assertEquals("b37", headerInfo.getProperty("assembly"));
    }
}
