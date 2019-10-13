/*
 * Copyright (c) UICHUIMI 2017
 *
 * This file is part of Coat.
 *
 * Coat is free software:
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Coat.
 *
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.uichuimi.coat.view.graphic;

import javafx.scene.image.ImageView;

/**
 * You can specify a custom size for the graphic. Only the width is resized and the ratio is
 * preserved.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class SizableImageView extends ImageView {

    /**
     * 16x16.
     */
    public final static double SMALL_SIZE = 16;
    /**
     * 32x32.
     */
    public final static double MEDIUM_SIZE = 32;
    /**
     * 48x48.
     */
    public final static double LARGE_SIZE = 48;

    /**
     * Creates a new SizableImageView using the url as icon and resizing to size width and preserving
     * aspect ratio.
     *
     * @param url the icon url
     * @param size the size of the icon
     */
    public SizableImageView(String url, double size) {
        super(url);
        setFitWidth(size);
        setPreserveRatio(true);
        setSmooth(true);
    }

}
