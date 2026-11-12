package client.render;

import net.minecraft.client.gui.GuiGraphics;

/** Примитивы отрисовки: скруглённые прямоугольники и мягкая тень. */
public final class RenderUtils {
    private RenderUtils() {
    }

    /** Заливает прямоугольник со скруглёнными углами (построчно, без текстур). */
    public static void fillRounded(GuiGraphics graphics, int x, int y, int width, int height, int radius, int color) {
        if (width <= 0 || height <= 0) {
            return;
        }
        int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
        if (r == 0) {
            graphics.fill(x, y, x + width, y + height, color);
            return;
        }
        graphics.fill(x, y + r, x + width, y + height - r, color);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = r - (int) Math.round(Math.sqrt((double) r * r - dy * dy));
            graphics.fill(x + inset, y + i, x + width - inset, y + i + 1, color);
            graphics.fill(x + inset, y + height - i - 1, x + width - inset, y + height - i, color);
        }
    }

    /** Рисует мягкую тень вокруг прямоугольника несколькими слоями убывающей прозрачности. */
    public static void drawShadow(GuiGraphics graphics, int x, int y, int width, int height,
                                  int radius, int spread, int color, float alpha) {
        for (int i = spread; i >= 1; i--) {
            float falloff = 1f - (float) i / (spread + 1);
            int layerColor = ColorUtils.mulAlpha(color, alpha * falloff);
            fillRounded(graphics, x - i, y - i + 2, width + i * 2, height + i * 2, radius + i, layerColor);
        }
    }
}
