package client.gui;

import client.gui.components.Component;
import client.gui.components.TextField;
import client.gui.theme.Theme;
import client.lang.LanguageManager;
import client.render.ColorUtils;
import client.render.RenderUtils;
import client.render.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

/** Поле поиска модулей: уменьшенная плашка со стеклянным фоном, иконкой и вводом. */
public final class SearchBar extends Component {
    private static final int RADIUS = 9;
    private static final String PLACEHOLDER = "Search for modules";
    private final TextField field;

    public SearchBar(Consumer<String> onQueryChanged) {
        this.field = new TextField(PLACEHOLDER, 32, onQueryChanged);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        field.setBounds(x + 24, y + 2, width - 44, height - 4);
    }

    public String getQuery() {
        return field.getText();
    }

    public boolean isFocused() {
        return field.isFocused();
    }

    public void unfocus() {
        field.setFocused(false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        if (alpha <= 0.01f) {
            return;
        }
        float hoverValue = updateHover(mouseX, mouseY);
        Theme theme = Theme.current();
        boolean focused = field.isFocused();

        int border = focused
                ? theme.accent
                : ColorUtils.mix(theme.panelBorder, theme.accentSoft, hoverValue * 0.6f);
        float borderAlpha = focused ? 0.85f : 0.35f + hoverValue * 0.25f;

        RenderUtils.drawShadow(graphics, x, y, width, height, RADIUS, 5, theme.shadow, alpha * 0.9f);
        RenderUtils.fillRounded(graphics, x, y, width, height, RADIUS,
                ColorUtils.mulAlpha(border, alpha * borderAlpha));
        RenderUtils.fillRounded(graphics, x + 1, y + 1, width - 2, height - 2, RADIUS - 1,
                ColorUtils.mulAlpha(theme.field, alpha));

        drawMagnifier(graphics, x + 12, y + height / 2.0, ColorUtils.mulAlpha(theme.textMuted, alpha));
        field.setPlaceholder(LanguageManager.tr("search.placeholder", PLACEHOLDER));
        field.render(graphics, mouseX, mouseY, alpha);

        if (!field.getText().isEmpty()) {
            drawClearIcon(graphics, x + width - 14, y + height / 2.0, ColorUtils.mulAlpha(theme.textMuted, alpha));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) {
            return false;
        }
        boolean onClearButton = mouseX >= x + width - 22;
        if (button == 0 && onClearButton && !field.getText().isEmpty()) {
            field.clear();
            return true;
        }
        field.mouseClicked(mouseX, mouseY, button);
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        return field.keyPressed(key, scancode, modifiers);
    }

    @Override
    public boolean charTyped(int codePoint) {
        return field.charTyped(codePoint);
    }

    private static void drawMagnifier(GuiGraphics graphics, double cx, double cy, int color) {
        RenderUtils.strokeCircle(graphics, cx, cy - 1, 4.0, 1.4, color);
        RenderUtils.fillLine(graphics, cx + 2.9, cy + 1.9, cx + 5.6, cy + 4.6, 1.4, color);
    }

    private static void drawClearIcon(GuiGraphics graphics, double cx, double cy, int color) {
        RenderUtils.fillLine(graphics, cx - 2.5, cy - 2.5, cx + 2.5, cy + 2.5, 1.2, color);
        RenderUtils.fillLine(graphics, cx + 2.5, cy - 2.5, cx - 2.5, cy + 2.5, 1.2, color);
    }
}
