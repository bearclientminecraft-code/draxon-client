package client.gui.components;

import client.gui.theme.Theme;
import client.render.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

/** Однострочное текстовое поле без фона (фон рисует контейнер). */
public final class TextField extends Component {
    private final StringBuilder text = new StringBuilder();
    private final String placeholder;
    private final int maxLength;
    private final Consumer<String> onChange;
    private boolean focused;

    public TextField(String placeholder, int maxLength, Consumer<String> onChange) {
        this.placeholder = placeholder;
        this.maxLength = maxLength;
        this.onChange = onChange;
    }

    public String getText() {
        return text.toString();
    }

    public void clear() {
        if (text.length() == 0) {
            return;
        }
        text.setLength(0);
        notifyChanged();
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        focused = button == 0 && isHovered(mouseX, mouseY);
        return focused;
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (!focused || key != GLFW.GLFW_KEY_BACKSPACE || text.length() == 0) {
            return focused;
        }
        int newLength = text.length() - 1;
        if (newLength > 0 && Character.isLowSurrogate(text.charAt(newLength))) {
            newLength--;
        }
        text.setLength(newLength);
        notifyChanged();
        return true;
    }

    @Override
    public boolean charTyped(int codePoint) {
        if (!focused) {
            return false;
        }
        if (Character.isISOControl(codePoint)) {
            return true;
        }
        if (text.length() < maxLength) {
            text.appendCodePoint(codePoint);
            notifyChanged();
        }
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        Theme theme = Theme.current();
        Font font = Minecraft.getInstance().font;
        int textY = y + (height - font.lineHeight) / 2;

        if (text.isEmpty()) {
            graphics.drawString(font, placeholder, x, textY, ColorUtils.mulAlpha(theme.textMuted, alpha), false);
        } else {
            graphics.drawString(font, getText(), x, textY, ColorUtils.mulAlpha(theme.text, alpha), false);
        }

        boolean caretVisible = (System.currentTimeMillis() / 500) % 2 == 0;
        if (focused && caretVisible) {
            int caretX = x + (text.isEmpty() ? 0 : font.width(getText())) + 1;
            graphics.fill(caretX, textY - 1, caretX + 1, textY + font.lineHeight,
                    ColorUtils.mulAlpha(theme.text, alpha));
        }
    }

    private void notifyChanged() {
        onChange.accept(getText());
    }
}
