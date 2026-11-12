package client.gui.components;

import client.gui.theme.Theme;
import client.render.ColorUtils;
import client.render.RenderUtils;
import client.render.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;

/** Кнопка с подписью и hover-подсветкой. */
public class Button extends Component {
    private final String label;
    private final Runnable action;

    public Button(String label, Runnable action) {
        this.label = label;
        this.action = action;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        float hoverValue = updateHover(mouseX, mouseY);
        Theme theme = Theme.current();
        int background = ColorUtils.mix(theme.field, theme.accent, 0.15f + hoverValue * 0.35f);

        RenderUtils.fillRounded(graphics, x, y, width, height, 6, ColorUtils.mulAlpha(background, alpha));

        int labelX = x + (width - TextRenderer.width(label, TextRenderer.Weight.REGULAR)) / 2;
        int labelY = y + (height - TextRenderer.lineHeight()) / 2 + 1;
        TextRenderer.draw(graphics, label, labelX, labelY, ColorUtils.mulAlpha(theme.text, alpha),
                TextRenderer.Weight.REGULAR);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            action.run();
            return true;
        }
        return false;
    }
}
