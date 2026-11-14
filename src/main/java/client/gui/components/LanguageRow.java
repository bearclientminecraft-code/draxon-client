package client.gui.components;

import client.gui.theme.Theme;
import client.lang.LanguageManager;
import client.notification.Notification;
import client.notification.NotificationManager;
import client.render.ColorUtils;
import client.render.RenderUtils;
import client.render.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;

/** Строка выбора языка внутри настроек модуля Language. */
public final class LanguageRow extends Component {
    private final LanguageManager.Language language;

    public LanguageRow(LanguageManager.Language language) {
        this.language = language;
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
        return language.displayName();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        float hoverValue = updateHover(mouseX, mouseY);
        Theme theme = Theme.current();
        boolean selected = LanguageManager.current() == language;

        int background = selected
                ? ColorUtils.mix(theme.panel, theme.accent, 0.22f)
                : ColorUtils.mulAlpha(theme.hover, alpha * hoverValue);
        RenderUtils.fillRounded(graphics, x, y, width, height, 7, ColorUtils.mulAlpha(background, alpha));

        int textY = y + (height - TextRenderer.lineHeight()) / 2;
        int color = selected ? theme.text : theme.textMuted;
        TextRenderer.draw(graphics, language.displayName(), x + 8, textY,
                ColorUtils.mulAlpha(color, alpha), TextRenderer.Weight.MEDIUM);

        if (selected) {
            int markX = x + width - 14;
            int markY = y + height / 2;
            RenderUtils.fillLine(graphics, markX, markY, markX + 4, markY + 4, 1.6,
                    ColorUtils.mulAlpha(theme.accent, alpha));
            RenderUtils.fillLine(graphics, markX + 4, markY + 4, markX + 10, markY - 5, 1.6,
                    ColorUtils.mulAlpha(theme.accent, alpha));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            LanguageManager.setLanguage(language);
            NotificationManager.push(LanguageManager.tr("notification.language", "Language") + ": "
                    + language.displayName(), Notification.Type.INFO);
            return true;
        }
        return false;
    }
}
