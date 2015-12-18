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

package coat.view.vcfreader;

/**
 * Encapsulates name, value and description of a VCF INFO field. Use it on variant INFO table. This
 * is a final class and fields will not be modified.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public final class Info {

    private final String name;
    private final String value;
    private final String description;

    /**
     * Creates a new Info.
     *
     * @param name the name of the info
     * @param value the value of the info
     * @param description the description of the info
     */
    public Info(String name, String value, String description) {
        this.name = name;
        this.value = value;
        this.description = description;
    }

    /**
     * Gets the description. Table will need this methos to display it.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the name of the info.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value of the INFO.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

}
