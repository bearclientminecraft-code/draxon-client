package client.gui.components;

import client.gui.theme.Theme;
import client.render.ColorUtils;
import client.render.RenderUtils;
import client.render.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Строка-вариант в раскрытых настройках модуля: подпись и галочка у выбранного
 * значения. Используется там, где выбор — одно из нескольких значений,
 * а не переключатель (например, сторона Armor HUD).
 */
public final class OptionRow extends Component {
    private final String label;
    private final String value;
    private final Supplier<String> selected;
    private final Consumer<String> onSelect;

    public OptionRow(String label, String value, Supplier<String> selected, Consumer<String> onSelect) {
        this.label = label;
        this.value = value;
        this.selected = selected;
        this.onSelect = onSelect;
    }

    @Override
    public int indent() {
        return 10;
    }

    @Override
    public String searchKey() {
        return label;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        float hoverValue = updateHover(mouseX, mouseY);
        Theme theme = Theme.current();
        boolean active = value.equals(selected.get());

        int background = active
                ? ColorUtils.mix(theme.panel, theme.accent, 0.22f)
                : ColorUtils.mulAlpha(theme.hover, alpha * hoverValue);
        RenderUtils.fillRounded(graphics, x, y, width, height, 7, ColorUtils.mulAlpha(background, alpha));

        int textY = y + (height - TextRenderer.lineHeight()) / 2;
        int color = active ? theme.text : theme.textMuted;
        TextRenderer.draw(graphics, label, x + 8, textY, ColorUtils.mulAlpha(color, alpha),
                TextRenderer.Weight.MEDIUM);

        if (active) {
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
            onSelect.accept(value);
            return true;
        }
        return false;
    }
}
