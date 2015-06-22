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
