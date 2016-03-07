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

import java.util.Arrays;

/**
 * Too many Strings on your application? Most of them are repeated constantly? StringStore ensures you that a String
 * will only be stored in memory once.
 * <p>
 * Usage:
 * <p>
 * <code>myString = StringStore.getString(myString)</code>
 * <p>
 * StringStore should be used only with non-mutable Strings. If you modify one String, others pointing to the same
 * object will be also being modified.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class StringStore {

    /*
        Tests using a 76MB VCF file.
                        With SS     Without SS
        Memory usage    800         1319
        After GC        438         1028
     */
    private final static int BLOCK_SIZE = 65536;

    private static String[] LIST = new String[BLOCK_SIZE];
    private static int size = 0;

    /**
     * Gets a String that is equals to value. ensuring only one copy is stored in memory. If no copy in memory,
     * <code>value</code> will be inserted and returned.
     *
     * @param value query value
     * @return an already stored String that is equals to value, or value if no equal String is found.
     */
    public static String getInstance(String value) {
        final int index = Arrays.binarySearch(LIST, 0, size, value);
        if (index >= 0) return LIST[index];
        insert(value, -index - 1);
        return value;
    }

    private static void insert(String value, int index) {
        if (size == LIST.length) resize();
        if (index != size) System.arraycopy(LIST, index, LIST, index + 1, size - index);
        LIST[index] = value;
        size++;
    }

    private static void resize() {
        String[] copy = new String[LIST.length + BLOCK_SIZE];
        System.arraycopy(LIST, 0, copy, 0, LIST.length);
        LIST = copy;
    }
}

