package client.gui.components;

import client.gui.theme.Theme;
import client.render.ColorUtils;
import client.render.RenderUtils;
import client.render.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

/** Строка настроек с текстовым вводом: подпись слева, поле справа. */
public final class TextOptionRow extends Component {
    private static final int RADIUS = 7;
    private final String label;
    private final TextField field;

    public TextOptionRow(String label, String initial, int maxLength, Consumer<String> onChange) {
        this.label = label;
        this.field = new TextField("", maxLength, onChange);
        this.field.setText(initial);
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
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        int labelWidth = Math.min(width / 2, TextRenderer.width(label, TextRenderer.Weight.MEDIUM) + 8);
        field.setBounds(x + labelWidth + 6, y + (height - fieldHeight()) / 2, width - labelWidth - 14, fieldHeight());
    }

    private int fieldHeight() {
        return Math.max(9, TextRenderer.lineHeight());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        float hoverValue = updateHover(mouseX, mouseY);
        Theme theme = Theme.current();
        int background = hoverValue > 0.01f
                ? ColorUtils.mulAlpha(theme.hover, alpha * hoverValue)
                : ColorUtils.mulAlpha(theme.panel, alpha * 0.65f);
        RenderUtils.fillRounded(graphics, x, y, width, height, RADIUS, background);

        int textY = y + (height - TextRenderer.lineHeight()) / 2;
        TextRenderer.draw(graphics, label, x + 8, textY, ColorUtils.mulAlpha(theme.textMuted, alpha),
                TextRenderer.Weight.MEDIUM);
        field.render(graphics, mouseX, mouseY, alpha);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (field.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        // Клик мимо поля снимает фокус, иначе ввод продолжал бы уходить в Watermark.
        field.setFocused(false);
        return isHovered(mouseX, mouseY) && button == 0;
    }

    @Override
    public void blur() {
        field.setFocused(false);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        return field.keyPressed(key, scancode, modifiers);
    }

    @Override
    public boolean charTyped(int codePoint) {
        return field.charTyped(codePoint);
    }
}
