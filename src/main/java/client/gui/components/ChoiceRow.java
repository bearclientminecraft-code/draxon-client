package client.gui.components;

import client.gui.theme.Theme;
import client.render.ColorUtils;
import client.render.RenderUtils;
import client.render.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Компактная строка выбора: подпись слева, текущее значение справа.
 * Левый клик перебирает варианты по кругу — так даже 5 вариантов занимают одну строку.
 */
public final class ChoiceRow extends Component {
    private final String label;
    private final String[] values;
    private final String[] labels;
    private final java.util.function.Supplier<String> getter;
    private final java.util.function.Consumer<String> setter;

    public ChoiceRow(String label, String[] values, java.util.function.Supplier<String> getter,
                     java.util.function.Consumer<String> setter) {
        this(label, values, values, getter, setter);
    }

    public ChoiceRow(String label, String[] values, String[] labels,
                     java.util.function.Supplier<String> getter,
                     java.util.function.Consumer<String> setter) {
        this.label = label;
        this.values = values;
        this.labels = labels;
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

    private int currentIndex() {
        String current = getter.get();
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(current)) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        float hoverValue = updateHover(mouseX, mouseY);
        Theme theme = Theme.current();
        int background = hoverValue > 0.01f
                ? ColorUtils.mulAlpha(theme.hover, alpha * hoverValue)
                : ColorUtils.mulAlpha(theme.panel, alpha * 0.6f);
        RenderUtils.fillRounded(graphics, x, y, width, height, 7, background);

        int index = currentIndex();
        String value = labels.length > index ? labels[index] : values[index];
        int valueWidth = TextRenderer.width(value, TextRenderer.Weight.MEDIUM);
        int textY = y + (height - TextRenderer.lineHeight()) / 2;
        int labelSpace = width - valueWidth - 22;

        TextRenderer.draw(graphics, TextRenderer.clip(label, labelSpace, TextRenderer.Weight.MEDIUM),
                x + 8, textY, ColorUtils.mulAlpha(theme.textMuted, alpha), TextRenderer.Weight.MEDIUM);
        TextRenderer.drawRight(graphics, value, x + width - 8, textY,
                ColorUtils.mulAlpha(theme.accent, alpha), TextRenderer.Weight.MEDIUM);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) {
            return false;
        }
        if (button == 0) {
            int next = (currentIndex() + 1) % values.length;
            setter.accept(values[next]);
            return true;
        }
        if (button == 1) {
            int previous = (currentIndex() - 1 + values.length) % values.length;
            setter.accept(values[previous]);
            return true;
        }
        return false;
    }
}
