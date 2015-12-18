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

package coat.view.poirot;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
class Vector {

    private double x;
    private double y;

    public Vector() {

    }

    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector(Vector source, Vector target) {
        this.x = target.x - source.x;
        this.y = target.y - source.y;
    }

    public void normalize() {
        double module = Math.sqrt(x * x + y * y);
        if (module > 0) {
            double inverse = 1.0 / module;
            x *= inverse;
            y *= inverse;
        }
    }

    public void scale(double scale) {
        x *= scale;
        y *= scale;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void moveX(double delta) {
        x += delta;
    }

    public void moveY(double delta) {
        y += delta;
    }

    public double distance(Vector vector) {
        double xx = x - vector.x;
        double yy = y - vector.y;
        return Math.sqrt(xx * xx + yy * yy);
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("[%d,%d]", (int) x, (int) y);
    }

    public void add(Vector direction) {
        x += direction.x;
        y += direction.y;

    }

    public void substract(Vector direction) {
        x -= direction.x;
        y -= direction.y;
    }
}
