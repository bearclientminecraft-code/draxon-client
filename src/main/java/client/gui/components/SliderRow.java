package client.gui.components;

import client.gui.theme.Theme;
import client.render.ColorUtils;
import client.render.RenderUtils;
import client.render.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** Плавный слайдер: значение меняется перетаскиванием и сразу отображается справа. */
public final class SliderRow extends Component {
    private final String label;
    private final int min;
    private final int max;
    private final int step;
    private final String suffix;
    private final IntSupplier getter;
    private final IntConsumer setter;
    private boolean dragging;

    public SliderRow(String label, int min, int max, int step, String suffix,
                     IntSupplier getter, IntConsumer setter) {
        this.label = label;
        this.min = min;
        this.max = max;
        this.step = Math.max(1, step);
        this.suffix = suffix;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public int rowHeight() {
        return 28;
    }

    @Override
    public int indent() {
        return 10;
    }

    @Override
    public String searchKey() {
        return label;
    }

    private int trackLeft() {
        return x + 8;
    }

    private int trackWidth() {
        return Math.max(12, width - 16);
    }

    private int trackY() {
        return y + height - 7;
    }

    private float ratio() {
        int value = Math.max(min, Math.min(max, getter.getAsInt()));
        return (value - min) / (float) (max - min);
    }

    private void applyFromMouse(double mouseX) {
        float ratio = (float) ((mouseX - trackLeft()) / trackWidth());
        ratio = Math.max(0f, Math.min(1f, ratio));
        int value = Math.round(min + ratio * (max - min));
        value = Math.round(value / (float) step) * step;
        setter.accept(Math.max(min, Math.min(max, value)));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        if (dragging) {
            applyFromMouse(mouseX);
        }
        float hoverValue = updateHover(mouseX, mouseY);
        Theme theme = Theme.current();
        int background = hoverValue > 0.01f || dragging
                ? ColorUtils.mulAlpha(theme.hover, alpha * Math.max(hoverValue, 0.7f))
                : ColorUtils.mulAlpha(theme.panel, alpha * 0.6f);
        RenderUtils.fillRounded(graphics, x, y, width, height, 7, background);

        int value = getter.getAsInt();
        String valueText = value + suffix;
        TextRenderer.draw(graphics, TextRenderer.clip(label, width - 60, TextRenderer.Weight.MEDIUM),
                x + 8, y + 5, ColorUtils.mulAlpha(theme.textMuted, alpha), TextRenderer.Weight.MEDIUM);
        TextRenderer.drawRight(graphics, valueText, x + width - 8, y + 5,
                ColorUtils.mulAlpha(theme.accent, alpha), TextRenderer.Weight.MEDIUM);

        int left = trackLeft();
        int track = trackWidth();
        int ty = trackY();
        RenderUtils.fillRounded(graphics, left, ty, track, 3, 1,
                ColorUtils.mulAlpha(theme.hover, alpha * 0.9f));
        int filled = Math.round(track * ratio());
        RenderUtils.fillRounded(graphics, left, ty, Math.max(2, filled), 3, 1,
                ColorUtils.mulAlpha(theme.accent, alpha * 0.95f));
        RenderUtils.fillCircle(graphics, left + filled, ty + 1.5, 3.2,
                ColorUtils.mulAlpha(theme.text, alpha));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            dragging = true;
            applyFromMouse(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && button == 0) {
            applyFromMouse(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
    }
}
