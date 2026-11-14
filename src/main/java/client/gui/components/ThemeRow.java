package client.gui.components;

import client.config.ConfigManager;
import client.gui.theme.Theme;
import client.lang.LanguageManager;
import client.notification.Notification;
import client.notification.NotificationManager;
import client.render.ColorUtils;
import client.render.RenderUtils;
import client.render.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;

/** Плашка темы: название и четыре базовых цвета. Клик выбирает тему. */
public final class ThemeRow extends Component {
    private static final int SWATCH = 6;
    private static final int SWATCH_GAP = 3;

    private final Theme theme;

    public ThemeRow(Theme theme) {
        this.theme = theme;
    }

    public Theme theme() {
        return theme;
    }

    @Override
    public int rowHeight() {
        return 34;
    }

    @Override
    public int indent() {
        return 10;
    }

    @Override
    public String searchKey() {
        return theme.displayName;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        float hoverValue = updateHover(mouseX, mouseY);
        Theme current = Theme.current();
        boolean selected = current == theme;

        int background = selected
                ? ColorUtils.mix(current.panel, current.accent, 0.22f)
                : ColorUtils.mulAlpha(current.hover, alpha * hoverValue);
        RenderUtils.fillRounded(graphics, x, y, width, height, 7, ColorUtils.mulAlpha(background, alpha));

        int textY = y + (height - TextRenderer.lineHeight()) / 2;
        int textColor = selected ? current.text : current.textMuted;
        TextRenderer.draw(graphics, theme.displayName, x + 8, textY, ColorUtils.mulAlpha(textColor, alpha),
                TextRenderer.Weight.MEDIUM);

        // Четыре цвета темы справа: фон, поверхность, акцент, текст.
        int swatchX = x + width - 8 - (SWATCH * 4 + SWATCH_GAP * 3);
        int swatchY = y + (height - SWATCH) / 2;
        for (int i = 0; i < theme.swatches.length; i++) {
            int sx = swatchX + i * (SWATCH + SWATCH_GAP);
            graphics.fill(sx, swatchY, sx + SWATCH, swatchY + SWATCH, ColorUtils.mulAlpha(theme.swatches[i], alpha));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            ConfigManager.settings().theme = theme.id;
            ConfigManager.save();
            NotificationManager.push(LanguageManager.tr("notification.theme", "Theme") + ": "
                    + theme.displayName, Notification.Type.INFO);
            return true;
        }
        return false;
    }
}
