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

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class HeaderInfo {
    private final String name;
    private final Type type;
    private final int number;
    private final String description;

    public HeaderInfo(String name, Type type, int number, String description) {
        this.name = name;
        this.type = type;
        this.number = number;
        this.description = description;
    }

    public void setProperty(String name, String value) {

    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getNumber() {
        return number;
    }

    public Type getType() {
        return type;
    }

    public String getProperty(String key) {
        return "b37";
    }

    public enum Type {STRING}
}
