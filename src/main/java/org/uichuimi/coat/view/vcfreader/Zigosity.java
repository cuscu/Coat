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

package org.uichuimi.coat.view.vcfreader;

/**
 * Created by uichuimi on 14/10/16.
 */
public enum Zigosity {
    HOM {
        @Override
        public String toString() {
            return "Homozigous (1/1)";
        }
    },
    HET {
        @Override
        public String toString() {
            return "Heterozigous (0/1)";
        }
    },
    WILD {
        @Override
        public String toString() {
            return "Wild (0/0)";
        }
    },
    NO_CALL {
        @Override
        public String toString() {
            return "No call (./.)";
        }
    }
}
