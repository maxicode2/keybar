package codes.maxi.keybar.util;

public class ColorUtil {
    public static int fromRGBA(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | (b);
    }
}
