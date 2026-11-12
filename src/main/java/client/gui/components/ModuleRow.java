package client.gui.components;

import client.gui.CategoryPanel;
import client.gui.theme.Theme;
import client.module.Module;
import client.render.ColorUtils;
import client.render.RenderUtils;
import client.render.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Строка модуля в панели. Левый клик включает модуль (если он переключаемый),
 * правый раскрывает его настройки под строкой.
 */
public final class ModuleRow extends Component {
    private final Module module;
    private final CategoryPanel panel;
    private final List<Component> children = new ArrayList<>();
    private boolean expanded;

    public ModuleRow(Module module, CategoryPanel panel) {
        this.module = module;
        this.panel = panel;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        float hoverValue = updateHover(mouseX, mouseY);
        Theme theme = Theme.current();

        if (hoverValue > 0.01f || expanded) {
            int tint = expanded ? theme.accent : theme.text;
            float strength = expanded ? 0.12f : 0.06f * hoverValue;
            RenderUtils.fillRounded(graphics, x, y, width, height, 6,
                    ColorUtils.mulAlpha(ColorUtils.mix(theme.panel, tint, strength), alpha));
        }

        int textY = y + (height - TextRenderer.lineHeight()) / 2;
        int color = module.isEnabled() ? theme.accentSoft : theme.text;
        TextRenderer.draw(graphics, module.getName(), x + 8, textY, ColorUtils.mulAlpha(color, alpha),
                TextRenderer.Weight.REGULAR);

        // Три точки справа, как в референсе. Раскрытый модуль подсвечивается акцентом.
        int dotColor = ColorUtils.mulAlpha(expanded ? theme.accent : theme.textMuted, alpha);
        int dotsX = x + width - 18;
        int dotsY = y + height / 2 - 1;
        for (int i = 0; i < 3; i++) {
            graphics.fill(dotsX + i * 4, dotsY, dotsX + i * 4 + 2, dotsY + 2, dotColor);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) {
            return false;
        }
        if (button == 0 && module.isToggleable()) {
            module.toggle();
        } else if (button == 1) {
            toggleExpanded();
        }
        return true;
    }

    private void toggleExpanded() {
        expanded = !expanded;
        if (expanded) {
            children.clear();
            children.addAll(module.createSettingRows());
            panel.insertAfter(this, children);
        } else {
            panel.removeRows(children);
            children.clear();
        }
    }
}
