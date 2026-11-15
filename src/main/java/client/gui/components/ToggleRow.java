package client.gui.components;

import client.gui.theme.Theme;
import client.render.ColorUtils;
import client.render.RenderUtils;
import client.render.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Переключатель в настройках модуля: подпись слева, ON/OFF-пилюля справа. */
public final class ToggleRow extends Component {
    private final String label;
    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;

    public ToggleRow(String label, BooleanSupplier getter, Consumer<Boolean> setter) {
        this.label = label;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public int rowHeight() {
        return 26;
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
        boolean on = getter.getAsBoolean();

        int background = hoverValue > 0.01f
                ? ColorUtils.mulAlpha(theme.hover, alpha * hoverValue)
                : ColorUtils.mulAlpha(theme.panel, alpha * 0.6f);
        RenderUtils.fillRounded(graphics, x, y, width, height, 7, background);

        int textY = y + (height - TextRenderer.lineHeight()) / 2;
        TextRenderer.draw(graphics, TextRenderer.clip(label, width - 52, TextRenderer.Weight.MEDIUM),
                x + 8, textY, ColorUtils.mulAlpha(theme.textMuted, alpha), TextRenderer.Weight.MEDIUM);

        int pillWidth = on ? 18 : 18;
        int pillX = x + width - pillWidth - 6;
        int pillY = y + (height - 10) / 2;
        RenderUtils.fillRounded(graphics, pillX, pillY, pillWidth, 10, 5,
                ColorUtils.mulAlpha(on ? theme.accent : theme.hover, alpha));
        RenderUtils.fillCircle(graphics, pillX + (on ? 13 : 5), pillY + 5, 3.4,
                ColorUtils.mulAlpha(theme.text, alpha));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            setter.accept(!getter.getAsBoolean());
            return true;
        }
        return false;
    }
}
