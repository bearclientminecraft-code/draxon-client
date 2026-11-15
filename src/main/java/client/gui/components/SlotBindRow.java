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

/**
 * Единое назначение слота для всех макросов.
 *
 * <p>По умолчанию — «None». Нажатие колёсиком мыши переводит строку в режим ожидания,
 * цифры 1–9 назначают слот, ESC отменяет, повторное назначение заменяет старое,
 * правый клик очищает значение.</p>
 */
public final class SlotBindRow extends Component {
    private final String label;
    private final IntSupplier getter;
    private final IntConsumer setter;
    private boolean waiting;

    public SlotBindRow(String label, IntSupplier getter, IntConsumer setter) {
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

    public boolean isWaiting() {
        return waiting;
    }

    @Override
    public void blur() {
        waiting = false;
    }

    private String display() {
        int slot = getter.getAsInt();
        if (waiting) {
            return LanguageManager.tr("bind.waiting", "Press 1-9");
        }
        if (slot <= 0) {
            return LanguageManager.tr("bind.none", "None");
        }
        return LanguageManager.tr("bind.slot", "Slot") + " " + slot;
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
        switch (button) {
            case 2 -> {
                waiting = true;
                return true;
            }
            case 1 -> {
                waiting = false;
                setter.accept(0);
                return true;
            }
            case 0 -> {
                waiting = true;
                return true;
            }
            default -> {
                return false;
            }
        }
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
        if (key >= GLFW.GLFW_KEY_1 && key <= GLFW.GLFW_KEY_9) {
            setter.accept(key - GLFW.GLFW_KEY_0);
            waiting = false;
            return true;
        }
        // Любая другая клавиша не назначает слот и не закрывает GUI — просто игнорируем.
        return true;
    }
}
