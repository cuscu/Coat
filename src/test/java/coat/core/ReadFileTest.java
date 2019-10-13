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
 ******************************************************************************/

package coat.core;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ReadFileTest {


    @Test
    public void testReadList() {
        final List<String> geneList = Arrays.asList("MUC12", "RELN", "CNTNAP2", "TNC", "CCNYL2", "RP11-139J15.7",
                "RSU1", "SAA2", "CLECL1", "MGP", "P2RX2", "CCNA1", "AL133373.1", "SYNM", "NPEPPS", "OGFOD3", "MIER2",
                "SIGLEC12", "ZNF880", "ZNF211", "GPCPD1", "XRN2", "ARFGEF2", "OSBP2", "SYNGR1", "SHROOM4");
        final List<String> readList = ReadList.read(new File("test/geneTable.list"));
        Assertions.assertEquals(geneList, readList);
    }

    @Test
    public void testReadAnotherList() {
        final List<String> geneList = Arrays.asList("SAMD11", "NOC2L", "PLEKHN1", "AGRN", "C1orf222", "PRKCZ", "PLCH2",
                "RP3-395M20.7", "FAM213B", "MEGF6", "CCDC27", "KCNAB2", "CAMTA1", "UTS2", "NMNAT1", "MASP2", "MFN2",
                "TNFRSF1B", "DHRS3", "RP11-474O21.5", "RP11-474O21.2", "PRAMEF1", "LRRC38", "KAZN", "FBXO42", "SZRD1",
                "NBPF1", "CROCCP2", "AC004824.2", "ARHGEF10L", "RP13-279N23.2", "RP5-1126H10.2", "AL137127.1", "EIF4G3",
                "CELA3B", "EPHB2", "MYOM3", "RP4-799D16.1", "RP11-70P17.1", "SEPN1", "PAFAH2", "CEP85", "UBXN11",
                "RP11-344H11.4", "RPA2", "DNAJC8", "MATN1", "RP11-439L8.2", "HCRTR1", "PEF1", "COL16A1", "FNDC5");
                final List<String> readList = ReadList.read(new File("test/genes2.list"));
        Assertions.assertEquals(geneList, readList);
    }
}
