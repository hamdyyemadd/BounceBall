package BounceBall;

public class rectangle {

    int lx, ly, rx, ry;

    rectangle(int x, int y, int width, int height) {
        lx = x;
        ly = y;
        rx = width + x;
        ry = y - height;
    }

    boolean intersect(rectangle r) {

        if (lx > r.rx || r.lx > rx) {
            return false;
        }
        if (ry > r.ly || r.ry > ly) {
            return false;
        }
        return true;
    }

}