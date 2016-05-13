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

package coat.core.variant;

import org.junit.Assert;
import org.junit.Test;
import vcf.StringStore;

/**
 * Created by uichuimi on 7/03/16.
 */
public class StringStoreTest {


    @Test
    public void letsTest() {
        Assert.assertEquals("hello", StringStore.getInstance("hello"));
        Assert.assertEquals("goodbye", StringStore.getInstance("goodbye"));
        Assert.assertEquals("hi", StringStore.getInstance("hi"));
        Assert.assertEquals("patri", StringStore.getInstance("patri"));
        Assert.assertEquals("hello", StringStore.getInstance("hello"));
        for (int i = 0; i < 5000; i++) {
            Assert.assertEquals(String.valueOf(i), StringStore.getInstance(String.valueOf(i)));
        }
    }

}