package client.gui;

import client.gui.components.Component;
import client.gui.components.TextField;
import client.gui.theme.Theme;
import client.render.ColorUtils;
import client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

/** Поле поиска модулей: иконка поиска, ввод, кнопка очистки. */
public final class SearchBar extends Component {
    private static final int RADIUS = 8;
    private static final String PLACEHOLDER = "Поиск по модулям";
    private final TextField field;

    public SearchBar(Consumer<String> onQueryChanged) {
        this.field = new TextField(PLACEHOLDER, 32, onQueryChanged);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        field.setBounds(x + 22, y + 4, width - 46, height - 8);
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
        float hoverValue = updateHover(mouseX, mouseY);
        Theme theme = Theme.current();
        boolean focused = field.isFocused();

        int border = focused
                ? theme.accent
                : ColorUtils.mix(theme.panelBorder, theme.accentSoft, hoverValue * 0.8f);

        RenderUtils.drawShadow(graphics, x, y, width, height, RADIUS, 4, theme.shadow, alpha);
        RenderUtils.fillRounded(graphics, x, y, width, height, RADIUS, ColorUtils.mulAlpha(border, alpha));
        RenderUtils.fillRounded(graphics, x + 1, y + 1, width - 2, height - 2, RADIUS - 1,
                ColorUtils.mulAlpha(theme.field, alpha));

        drawSearchIcon(graphics, x + 8, y + (height - 9) / 2, ColorUtils.mulAlpha(theme.textMuted, alpha));
        field.render(graphics, mouseX, mouseY, alpha);

        if (!field.getText().isEmpty()) {
            drawClearIcon(graphics, x + width - 14, y + height / 2 - 2,
                    ColorUtils.mulAlpha(theme.textMuted, alpha));
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

    private static void drawSearchIcon(GuiGraphics graphics, int x, int y, int color) {
        graphics.fill(x + 1, y, x + 7, y + 1, color);
        graphics.fill(x + 1, y + 6, x + 7, y + 7, color);
        graphics.fill(x, y + 1, x + 1, y + 6, color);
        graphics.fill(x + 7, y + 1, x + 8, y + 6, color);
        graphics.fill(x + 7, y + 7, x + 8, y + 8, color);
        graphics.fill(x + 8, y + 8, x + 9, y + 9, color);
    }

    private static void drawClearIcon(GuiGraphics graphics, int x, int y, int color) {
        for (int i = 0; i < 5; i++) {
            graphics.fill(x + i, y + i, x + i + 1, y + i + 1, color);
            graphics.fill(x + 4 - i, y + i, x + 5 - i, y + i + 1, color);
        }
    }
}
