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
import org.junit.Before;
import org.junit.Test;
import vcf.Info;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class InfoTest {

    final Info info = new Info();
    private final Map<String, Object> someInfo = new HashMap<>();


    public InfoTest() {
        someInfo.put("this", "hello");
        someInfo.put("that", "hi");
        someInfo.put("those", 17);
        someInfo.put("number", -14.67);
        someInfo.put("true", true);
        someInfo.put("false", false);
    }

    @Before
    public void init() {
        someInfo.forEach(info::setInfo);
    }

    @Test
    public void testGetInfo() {
        someInfo.forEach((s, o) -> Assert.assertEquals(o, info.getInfo(s)));
    }

    @Test
    public void testGetString() {
        Assert.assertEquals("hello", info.getString("this"));
        Assert.assertEquals("hi", info.getString("that"));
    }

    @Test
    public void testGetNumber() {
        Assert.assertEquals(17, info.getNumber("those"));
        Assert.assertEquals(-14.67, (double) info.getNumber("number"), 0.001);
    }

    @Test
    public void testGetBoolean() {
        Assert.assertEquals(true, info.getBoolean("true"));
        Assert.assertEquals(false, info.getBoolean("false"));

    }

}