package client.hud;

import client.gui.theme.Theme;
import client.render.ColorUtils;
import client.render.RenderUtils;
import client.render.TextRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Водяной знак Draxon по центру сверху: буква-логотип Soopafre, название шрифтом Inter
 * и счётчик FPS. Если у игрока активен босс-бар, плашка опускается под него, чтобы
 * не перекрывать полосу и её название.
 */
public final class Watermark {
    private static final int HEIGHT = 20;
    private static final int TOP_MARGIN = 8;
    /** Сдвиг вниз, когда сверху висит босс-бар. */
    private static final int BOSS_OFFSET = 34;
    private static final int PADDING_X = 9;
    private static final int BRAND_GAP = 5;
    private static final int SECTION_GAP = 9;

    private Watermark() {
    }

    public static void render(GuiGraphics graphics) {
        Minecraft client = Minecraft.getInstance();
        if (client.options.hideGui) {
            return;
        }
        int screenWidth = client.getWindow().getGuiScaledWidth();
        if (screenWidth <= 0) {
            return;
        }

        Theme theme = Theme.current();
        String brand = "D";
        String name = "Draxon";
        String fps = "FPS: " + client.getFps();

        int brandWidth = TextRenderer.width(brand, TextRenderer.BRAND);
        float brandScale = 1.15f;
        int nameWidth = TextRenderer.width(name, TextRenderer.Weight.SEMIBOLD);
        int fpsWidth = TextRenderer.width(fps, TextRenderer.Weight.MEDIUM);
        int contentWidth = brandWidth + BRAND_GAP + nameWidth + SECTION_GAP * 2 + 1 + fpsWidth;
        int width = contentWidth + PADDING_X * 2;

        int x = (screenWidth - width) / 2;
        int y = TOP_MARGIN + (BossBarProbe.visible() ? BOSS_OFFSET : 0);

        RenderUtils.drawShadow(graphics, x, y, width, HEIGHT, 7, 3, theme.shadow, 0.85f);
        RenderUtils.fillRounded(graphics, x, y, width, HEIGHT, 7, ColorUtils.mulAlpha(theme.notification, 0.92f));
        RenderUtils.fillRounded(graphics, x, y, width, HEIGHT, 7, ColorUtils.mulAlpha(theme.panelBorder, 0.30f));
        // Тонкая акцентная полоска слева: узнаваемый край плашки в любой теме.
        RenderUtils.fillRounded(graphics, x + 1, y + 2, 2, HEIGHT - 6, 1, ColorUtils.mulAlpha(theme.accent, 0.85f));

        int textY = y + (HEIGHT - TextRenderer.lineHeight()) / 2 - 2;
        int cursor = x + PADDING_X;
        TextRenderer.draw(graphics, brand, cursor, textY - 1, theme.accent, TextRenderer.BRAND);
        cursor += brandWidth + BRAND_GAP;
        TextRenderer.draw(graphics, name, cursor, textY, theme.text, TextRenderer.Weight.SEMIBOLD);
        cursor += nameWidth + SECTION_GAP;

        graphics.fill(cursor, y + 5, cursor + 1, y + HEIGHT - 5, ColorUtils.mulAlpha(theme.panelBorder, 0.9f));
        cursor += SECTION_GAP + 1;

        TextRenderer.draw(graphics, fps, cursor, textY, theme.textMuted, TextRenderer.Weight.MEDIUM);
    }
}
