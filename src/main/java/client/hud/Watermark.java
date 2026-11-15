package client.hud;

import client.config.ConfigManager;
import client.gui.animation.Animation;
import client.gui.theme.Theme;
import client.render.ColorUtils;
import client.render.RenderUtils;
import client.render.TextRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2fStack;

/**
 * Водяной знак Draxon как HUD-элемент: тёмная полупрозрачная подложка, плавные
 * скруглённые углы, акцентная деталь в цвете темы, типографика Inter и мягкое
 * появление. Все параметры (текст, масштаб, позиция, стиль, цвет темы) читаются
 * из настроек, поэтому изменения применяются в реальном времени.
 */
public final class Watermark {
    private static final int HEIGHT = 20;
    private static final int TOP_MARGIN = 8;
    private static final int BOSS_OFFSET = 34;
    private static final int PADDING_X = 9;
    private static final int BRAND_GAP = 5;
    private static final int SECTION_GAP = 8;
    private static final int MIN_WIDTH = 74;
    private static final int SIDE_MARGIN = 5;

    /** Плавное появление/исчезание при включении модуля. */
    private static final Animation PRESENCE = new Animation(0f, 0.22f);

    private Watermark() {
    }

    /**
     * @param enabled включён ли модуль Watermark; при false плашка плавно исчезает
     */
    public static void render(GuiGraphics graphics, boolean enabled) {
        Minecraft client = Minecraft.getInstance();
        if (client.options.hideGui) {
            return;
        }
        PRESENCE.setTarget(enabled ? 1f : 0f);
        float presence = PRESENCE.update();
        if (presence <= 0.01f) {
            return;
        }
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        if (screenWidth <= 0 || screenHeight <= 0) {
            return;
        }

        ConfigManager.Settings settings = ConfigManager.settings();
        Theme theme = Theme.current();
        float scale = Math.max(0.5f, Math.min(2.5f, settings.watermarkScale / 100f));

        boolean themeColor = settings.watermarkThemeColor;
        String style = settings.watermarkStyle == null ? "glass" : settings.watermarkStyle;

        String brand = "D";
        String name = settings.watermarkText == null || settings.watermarkText.isBlank()
                ? "Draxon" : settings.watermarkText;
        String fps = "FPS " + client.getFps();

        int brandWidth = TextRenderer.width(brand, TextRenderer.BRAND);
        int nameWidth = TextRenderer.width(name, TextRenderer.Weight.SEMIBOLD);
        int fpsWidth = TextRenderer.width(fps, TextRenderer.Weight.MEDIUM);
        boolean minimal = "minimal".equals(style);
        int backgroundColor = minimal ? 0 : (themeColor
                ? ColorUtils.mulAlpha(theme.notification, 0.88f)
                : 0xE0101216);
        int borderColor = themeColor ? ColorUtils.mulAlpha(theme.panelBorder, 0.55f) : 0x33FFFFFF;
        int accentColor = themeColor ? theme.accent : 0xFFFFFFFF;
        int textColor = themeColor ? theme.text : 0xFFFFFFFF;
        int mutedColor = themeColor ? theme.textMuted : 0xB0FFFFFF;

        int contentWidth = brandWidth + BRAND_GAP + nameWidth + SECTION_GAP * 2 + 1 + fpsWidth;
        int width = Math.max(MIN_WIDTH, contentWidth + PADDING_X * 2);
        int scaledWidth = Math.round(width * scale);
        int scaledHeight = Math.round(HEIGHT * scale);

        int x = switch (settings.watermarkPosition == null ? "center" : settings.watermarkPosition) {
            case "left" -> SIDE_MARGIN;
            case "right" -> screenWidth - scaledWidth - SIDE_MARGIN;
            default -> (screenWidth - scaledWidth) / 2;
        };
        int y = TOP_MARGIN + (BossBarProbe.visible() ? BOSS_OFFSET : 0);
        y += Math.round((1f - presence) * -4f);

        float alpha = presence;

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        if (scale != 1f) {
            pose.scale(scale, scale);
        }

        if (!minimal) {
            RenderUtils.drawShadow(graphics, 0, 0, width, HEIGHT, 7, 3, 0xFF000000, alpha * 0.55f);
            RenderUtils.fillRounded(graphics, 0, 0, width, HEIGHT, 7,
                    ColorUtils.mulAlpha(backgroundColor, alpha));
            RenderUtils.fillRounded(graphics, 0, 0, width, HEIGHT, 7,
                    ColorUtils.mulAlpha(borderColor, alpha));
            if ("accent".equals(style)) {
                RenderUtils.fillRounded(graphics, 0, 0, width, HEIGHT, 7,
                        ColorUtils.mulAlpha(accentColor, alpha * 0.16f));
            }
            // Тонкая акцентная полоска слева: узнаваемый край плашки в любой теме.
            RenderUtils.fillRounded(graphics, 1, 3, 2, HEIGHT - 6, 1,
                    ColorUtils.mulAlpha(accentColor, alpha * 0.9f));
        }

        int textY = (HEIGHT - TextRenderer.lineHeight()) / 2;
        int cursor = PADDING_X;
        TextRenderer.draw(graphics, brand, cursor, textY - 1,
                ColorUtils.mulAlpha(accentColor, alpha), TextRenderer.BRAND);
        cursor += brandWidth + BRAND_GAP;
        TextRenderer.draw(graphics, name, cursor, textY,
                ColorUtils.mulAlpha(textColor, alpha), TextRenderer.Weight.SEMIBOLD);
        cursor += nameWidth + SECTION_GAP;

        graphics.fill(cursor, 5, cursor + 1, HEIGHT - 5,
                ColorUtils.mulAlpha(borderColor, alpha * 0.9f));
        cursor += SECTION_GAP + 1;

        TextRenderer.draw(graphics, fps, cursor, textY,
                ColorUtils.mulAlpha(mutedColor, alpha), TextRenderer.Weight.MEDIUM);

        pose.popMatrix();
    }
}
