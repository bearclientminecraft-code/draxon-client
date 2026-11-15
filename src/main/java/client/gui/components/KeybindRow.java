package client.gui.components;

import client.gui.theme.Theme;
import client.lang.LanguageManager;
import client.render.ColorUtils;
import client.render.RenderUtils;
import client.render.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** Общая Macro Key макроса: одна клавиша на модуль, не на каждый предмет. */
public final class KeybindRow extends Component {
    private final String label;
    private final IntSupplier getter;
    private final IntConsumer setter;
    private boolean waiting;

    public KeybindRow(String label, IntSupplier getter, IntConsumer setter) {
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
    public void blur() {
        waiting = false;
    }

    public static String keyName(int key) {
        if (key <= 0) {
            return LanguageManager.tr("bind.none", "None");
        }
        String name = GLFW.glfwGetKeyName(key, 0);
        if (name != null) {
            return name.toUpperCase(java.util.Locale.ROOT);
        }
        return switch (key) {
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LShift";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RShift";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCtrl";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCtrl";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LAlt";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RAlt";
            case GLFW.GLFW_KEY_SPACE -> "Space";
            case GLFW.GLFW_KEY_TAB -> "Tab";
            case GLFW.GLFW_KEY_ENTER -> "Enter";
            case GLFW.GLFW_KEY_BACKSPACE -> "Backspace";
            default -> "Key " + key;
        };
    }

    private String display() {
        return waiting ? LanguageManager.tr("bind.waiting_key", "Press key") : keyName(getter.getAsInt());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        float hoverValue = updateHover(mouseX, mouseY);
        Theme theme = Theme.current();
        int background = waiting
                ? ColorUtils.mix(theme.panel, theme.accent, 0.28f)
                : (hoverValue > 0.01f
                        ? ColorUtils.mulAlpha(theme.hover, alpha * hoverValue)
                        : ColorUtils.mulAlpha(theme.panel, alpha * 0.6f));
        RenderUtils.fillRounded(graphics, x, y, width, height, 7, ColorUtils.mulAlpha(background, alpha));

        String value = display();
        int valueWidth = TextRenderer.width(value, TextRenderer.Weight.MEDIUM);
        int textY = y + (height - TextRenderer.lineHeight()) / 2;
        TextRenderer.draw(graphics, TextRenderer.clip(label, width - valueWidth - 22, TextRenderer.Weight.MEDIUM),
                x + 8, textY, ColorUtils.mulAlpha(theme.textMuted, alpha), TextRenderer.Weight.MEDIUM);
        TextRenderer.drawRight(graphics, value, x + width - 8, textY,
                ColorUtils.mulAlpha(waiting ? theme.text : theme.accent, alpha), TextRenderer.Weight.MEDIUM);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) {
            return false;
        }
        if (button == 2 || button == 0) {
            waiting = true;
            return true;
        }
        if (button == 1) {
            waiting = false;
            setter.accept(0);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (!waiting) {
            return false;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            waiting = false;
            return true;
        }
        if (key >= GLFW.GLFW_KEY_F1 && key <= GLFW.GLFW_KEY_F12) {
            setter.accept(key);
            waiting = false;
            return true;
        }
        if (key == GLFW.GLFW_KEY_LEFT_SHIFT || key == GLFW.GLFW_KEY_RIGHT_SHIFT
                || key == GLFW.GLFW_KEY_LEFT_CONTROL || key == GLFW.GLFW_KEY_RIGHT_CONTROL
                || key == GLFW.GLFW_KEY_LEFT_ALT || key == GLFW.GLFW_KEY_RIGHT_ALT) {
            // Модификаторы назначаются отдельной клавишей без модификатора.
            if (modifiers == 0) {
                setter.accept(key);
                waiting = false;
            }
            return true;
        }
        if (key == ConfigModifier.clickGuiKey()) {
            return true;
        }
        setter.accept(key);
        waiting = false;
        return true;
    }

    /** Небольшая прослойка: ключ ClickGUI не назначается макросом. */
    private static final class ConfigModifier {
        private static int clickGuiKey() {
            return client.config.ConfigManager.settings().clickGuiKey;
        }
    }
}
