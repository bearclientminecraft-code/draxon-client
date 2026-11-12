package client.render;

/** Утилиты для работы с цветами в формате ARGB (0xAARRGGBB). */
public final class ColorUtils {
    private ColorUtils() {
    }

    /** Умножает альфа-канал цвета на factor (0..1). */
    public static int mulAlpha(int argb, float factor) {
        int alpha = Math.round(((argb >>> 24) & 0xFF) * clamp01(factor));
        return (alpha << 24) | (argb & 0xFFFFFF);
    }

    /** Линейно смешивает два цвета; t = 0 даёт from, t = 1 даёт to. */
    public static int mix(int from, int to, float t) {
        float k = clamp01(t);
        int a = lerp((from >>> 24) & 0xFF, (to >>> 24) & 0xFF, k);
        int r = lerp((from >>> 16) & 0xFF, (to >>> 16) & 0xFF, k);
        int g = lerp((from >>> 8) & 0xFF, (to >>> 8) & 0xFF, k);
        int b = lerp(from & 0xFF, to & 0xFF, k);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int lerp(int a, int b, float t) {
        return Math.round(a + (b - a) * t);
    }

    private static float clamp01(float value) {
        return value < 0f ? 0f : Math.min(value, 1f);
    }
}
