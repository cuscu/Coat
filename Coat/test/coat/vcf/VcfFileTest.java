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
package coat.vcf;

import java.io.File;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author UICHUIMI
 */
public class VcfFileTest {

    private static final File s002 = new File("test/s002.vcf");
    private static final File test = new File("test/test.vcf");

    @Test
    public void size() {
        // Given
        VcfFile vcfFile = new VcfFile(s002);
        // Then
        Assert.assertEquals(18, vcfFile.getVariants().size());
    }

    @Test
    public void size2() {
        // Given
        VcfFile vcfFile = new VcfFile(test);
        // Then
        Assert.assertEquals(21956, vcfFile.getVariants().size());
    }

    @Test
    public void infos() {
        // Given
        VcfFile vcfFile = new VcfFile(test);
        // Then
        Assert.assertEquals(3, vcfFile.getInfos().size());
    }

    @Test
    public void infos2() {
        // Given
        VcfFile vcfFile = new VcfFile(s002);
        // Then
        Assert.assertEquals(6, vcfFile.getInfos().size());
    }

    @Test
    public void chromFilter() {
        // Given
        VcfFilter filter = new VcfFilter(VcfFilter.Field.CHROMOSOME, VcfFilter.Connector.EQUALS, "1");
        VcfFile vcfFile = new VcfFile(s002);
        // When
        vcfFile.getFilters().add(filter);
        // Then
        Assert.assertEquals(5, vcfFile.getFilteredVariants().size());
    }

    @Test
    public void refFilter() {
        // Given
        VcfFilter filter = new VcfFilter(VcfFilter.Field.REF, VcfFilter.Connector.EQUALS, "T");
        VcfFile vcfFile = new VcfFile(test);
        // When
        vcfFile.getFilters().add(filter);
        // Then
        Assert.assertEquals(4875, vcfFile.getFilteredVariants().size());
    }

    @Test
    public void infoFilter() {
        // Given
        VcfFilter filter = new VcfFilter(VcfFilter.Field.INFO, "DP", VcfFilter.Connector.GREATER, "9");
        VcfFile vcfFile = new VcfFile(s002);
        // When
        vcfFile.getFilters().add(filter);
        // Then
        Assert.assertEquals(2, vcfFile.getFilteredVariants().size());
    }

    @Test
    public void multipleFilters() {
        // Given
        VcfFile vcfFile = new VcfFile(test);
        VcfFilter[] filters = new VcfFilter[]{
            new VcfFilter(VcfFilter.Field.CHROMOSOME, VcfFilter.Connector.CONTAINS, "1"),
            new VcfFilter(VcfFilter.Field.INFO, "DP", VcfFilter.Connector.GREATER, "10"),
            new VcfFilter(VcfFilter.Field.QUALITY, VcfFilter.Connector.GREATER, "20")
        };
        // When
        vcfFile.getFilters().addAll(filters);
        // Then
        Assert.assertEquals(6444, vcfFile.getFilteredVariants().size());
    }
}
