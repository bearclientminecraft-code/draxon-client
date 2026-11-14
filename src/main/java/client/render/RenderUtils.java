package client.render;

import net.minecraft.client.gui.GuiGraphics;

/** Примитивы отрисовки без текстур: скруглённые прямоугольники, круги, линии, тени. */
public final class RenderUtils {
    private static boolean antiAlias = true;

    private RenderUtils() {
    }

    public static void enableAntiAlias(boolean value) {
        antiAlias = value;
    }

    public static void fillRounded(GuiGraphics graphics, int x, int y, int width, int height, int radius, int color) {
        if (width <= 0 || height <= 0 || (color >>> 24) == 0) {
            return;
        }
        int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
        if (r == 0) {
            graphics.fill(x, y, x + width, y + height, color);
            return;
        }
        int right = x + width;
        int bottom = y + height;
        graphics.fill(x, y + r, right, bottom - r, color);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            double inset = r - Math.sqrt((double) r * r - dy * dy);
            span(graphics, x + inset, right - inset, y + i, color);
            span(graphics, x + inset, right - inset, bottom - i - 1, color);
        }
    }

    public static void fillRoundedSoft(GuiGraphics graphics, int x, int y, int width, int height, int radius, int color) {
        if (width <= 0 || height <= 0 || (color >>> 24) == 0) {
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
            int inset = (int) Math.ceil(r - Math.sqrt((double) r * r - dy * dy));
            graphics.fill(x + inset, y + i, x + width - inset, y + i + 1, color);
            graphics.fill(x + inset, y + height - i - 1, x + width - inset, y + height - i, color);
        }
    }

    public static void fillCircle(GuiGraphics graphics, double cx, double cy, double radius, int color) {
        if (radius <= 0 || (color >>> 24) == 0) {
            return;
        }
        int top = (int) Math.floor(cy - radius);
        int bottom = (int) Math.ceil(cy + radius);
        for (int row = top; row < bottom; row++) {
            double dy = row + 0.5 - cy;
            double square = radius * radius - dy * dy;
            if (square <= 0) {
                continue;
            }
            double half = Math.sqrt(square);
            span(graphics, cx - half, cx + half, row, color);
        }
    }

    public static void strokeCircle(GuiGraphics graphics, double cx, double cy, double radius, double thickness, int color) {
        if (radius <= 0 || (color >>> 24) == 0) {
            return;
        }
        double inner = Math.max(0, radius - Math.max(0.5, thickness));
        int top = (int) Math.floor(cy - radius);
        int bottom = (int) Math.ceil(cy + radius);
        for (int row = top; row < bottom; row++) {
            double dy = row + 0.5 - cy;
            double outside = radius * radius - dy * dy;
            if (outside <= 0) {
                continue;
            }
            double half = Math.sqrt(outside);
            double inside = inner * inner - dy * dy;
            double innerHalf = inside > 0 ? Math.sqrt(inside) : 0;
            if (innerHalf <= 0) {
                span(graphics, cx - half, cx + half, row, color);
            } else {
                span(graphics, cx - half, cx - innerHalf, row, color);
                span(graphics, cx + innerHalf, cx + half, row, color);
            }
        }
    }

    public static void fillLine(GuiGraphics graphics, double x0, double y0, double x1, double y1,
                                double thickness, int color) {
        if (thickness <= 0 || (color >>> 24) == 0) {
            return;
        }
        double dx = x1 - x0;
        double dy = y1 - y0;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length < 1e-3) {
            fillCircle(graphics, x0, y0, thickness / 2, color);
            return;
        }
        double from = Math.min(y0, y1);
        double to = Math.max(y0, y1);
        if (to - from >= 1) {
            double slope = dx / dy;
            double half = thickness * 0.5 * Math.sqrt(1 + slope * slope);
            int top = Math.max((int) Math.ceil(from), (int) Math.floor(from) - 1);
            int bottom = (int) Math.ceil(to);
            for (int row = top; row < bottom; row++) {
                double yc = row + 0.5;
                if (yc < from || yc > to) {
                    continue;
                }
                double t = (yc - y0) / dy;
                double xc = x0 + t * dx;
                span(graphics, xc - half, xc + half, row, color);
            }
        }
        double cap = thickness / 2;
        fillCircle(graphics, x0, y0, cap, color);
        fillCircle(graphics, x1, y1, cap, color);
    }

    public static void drawShadow(GuiGraphics graphics, int x, int y, int width, int height,
                                  int radius, int spread, int color, float alpha) {
        if (spread <= 0 || alpha <= 0f) {
            return;
        }
        int layers = 3;
        for (int layer = layers; layer >= 1; layer--) {
            double t = (double) layer / layers;
            int grow = (int) Math.round(spread * t);
            float strength = (float) (0.35 + 0.65 * (1.0 - t));
            int layerColor = ColorUtils.mulAlpha(color, alpha * strength * 0.5f);
            if ((layerColor >>> 24) == 0) {
                continue;
            }
            fillRoundedSoft(graphics, x - grow, y - grow + 1, width + grow * 2, height + grow * 2,
                    radius + grow, layerColor);
        }
    }

    private static void span(GuiGraphics graphics, double left, double right, int row, int color) {
        double width = right - left;
        if (width <= 0.004) {
            return;
        }
        int first = (int) Math.floor(left);
        int last = (int) Math.ceil(right);
        if (last - first <= 1) {
            if (antiAlias) {
                graphics.fill(first, row, first + 1, row + 1, ColorUtils.mulAlpha(color, (float) Math.min(1.0, width)));
            } else {
                graphics.fill(first, row, last, row + 1, color);
            }
            return;
        }
        int solidStart = first + 1;
        int solidEnd = last - 1;
        float leftCoverage = (float) Math.min(1.0, solidStart - left);
        float rightCoverage = (float) Math.min(1.0, right - solidEnd);
        if (leftCoverage > 0.01f) {
            graphics.fill(first, row, solidStart, row + 1, ColorUtils.mulAlpha(color, leftCoverage));
        }
        if (rightCoverage > 0.01f) {
            graphics.fill(solidEnd, row, last, row + 1, ColorUtils.mulAlpha(color, rightCoverage));
        }
        if (solidEnd > solidStart) {
            graphics.fill(solidStart, row, solidEnd, row + 1, color);
        }
    }
}
