package client.gui.components;

import client.gui.theme.Theme;
import client.render.ColorUtils;
import client.render.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

/** Однострочное текстовое поле без фона (фон рисует контейнер). */
public final class TextField extends Component {
    private final StringBuilder text = new StringBuilder();
    private String placeholder;
    private final int maxLength;
    private final Consumer<String> onChange;
    private boolean focused;
    private int cursor;

    public TextField(String placeholder, int maxLength, Consumer<String> onChange) {
        this.placeholder = placeholder;
        this.maxLength = maxLength;
        this.onChange = onChange;
    }

    public String getText() {
        return text.toString();
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public void clear() {
        cursor = 0;
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
        if (button != 0 || !isHovered(mouseX, mouseY)) {
            focused = false;
            return false;
        }
        focused = true;
        cursor = cursorAt(mouseX);
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (!focused) {
            return false;
        }
        switch (key) {
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (cursor > 0) {
                    int from = previousIndex(cursor);
                    text.delete(from, cursor);
                    cursor = from;
                    notifyChanged();
                }
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (cursor < text.length()) {
                    text.delete(cursor, nextIndex(cursor));
                    notifyChanged();
                }
            }
            case GLFW.GLFW_KEY_LEFT -> cursor = previousIndex(cursor);
            case GLFW.GLFW_KEY_RIGHT -> cursor = nextIndex(cursor);
            case GLFW.GLFW_KEY_HOME -> cursor = 0;
            case GLFW.GLFW_KEY_END -> cursor = text.length();
            default -> {
                return false;
            }
        }
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
            text.insert(cursor, (char) codePoint);
            cursor++;
            notifyChanged();
        }
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        Theme theme = Theme.current();
        int lineHeight = TextRenderer.lineHeight();
        int textY = y + (height - lineHeight) / 2;

        if (text.isEmpty()) {
            TextRenderer.draw(graphics, placeholder, x, textY, ColorUtils.mulAlpha(theme.textMuted, alpha),
                    TextRenderer.Weight.REGULAR);
        } else {
            TextRenderer.draw(graphics, getText(), x, textY, ColorUtils.mulAlpha(theme.text, alpha),
                    TextRenderer.Weight.REGULAR);
        }

        boolean caretVisible = (System.currentTimeMillis() / 500) % 2 == 0;
        if (focused && caretVisible) {
            int caretX = x + TextRenderer.width(text.substring(0, cursor), TextRenderer.Weight.REGULAR);
            graphics.fill(caretX, textY - 1, caretX + 1, textY + lineHeight,
                    ColorUtils.mulAlpha(theme.text, alpha));
        }
    }

    private int cursorAt(double mouseX) {
        int best = 0;
        int bestDistance = Integer.MAX_VALUE;
        int target = (int) Math.round(mouseX);
        for (int i = 0; i <= text.length(); i++) {
            int position = x + TextRenderer.width(text.substring(0, i), TextRenderer.Weight.REGULAR);
            int distance = Math.abs(target - position);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = i;
            }
        }
        return best;
    }

    private int previousIndex(int index) {
        if (index <= 0) {
            return 0;
        }
        int from = index - 1;
        if (from > 0 && Character.isLowSurrogate(text.charAt(from))) {
            from--;
        }
        return from;
    }

    private int nextIndex(int index) {
        if (index >= text.length()) {
            return text.length();
        }
        int to = index + 1;
        if (to < text.length() && Character.isLowSurrogate(text.charAt(to))) {
            to++;
        }
        return to;
    }

    private void notifyChanged() {
        onChange.accept(getText());
    }
}
