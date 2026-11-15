package client.gui.components;

import client.gui.CategoryPanel;
import client.gui.animation.Animation;
import client.gui.theme.Theme;
import client.lang.LanguageManager;
import client.module.Module;
import client.render.ColorUtils;
import client.render.RenderUtils;
import client.render.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Компактная карточка модуля: название слева, индикатор раскрытия и тумблер справа.
 *
 * <p>Левый клик: переключаемый модуль включается/выключается, модуль-настройка
 * (Themes, Language, Watermark, Notification) раскрывает свои строки. Правый клик
 * раскрывает и сворачивает настройки у любого модуля — раньше у не-переключаемых
 * модулей настройки не открывались вовсе.</p>
 */
public final class ModuleRow extends Component {
    private static final int SWITCH_WIDTH = 18;
    private static final int SWITCH_HEIGHT = 10;
    private static final int SWITCH_INSET = 7;
    private static final int RADIUS = 5;

    private final Module module;
    private final CategoryPanel panel;
    private final List<Component> children = new ArrayList<>();
    private final Animation state = new Animation(0f, 0.3f);
    private final Animation presence = new Animation(0f, 0.35f);
    private boolean expanded;

    public ModuleRow(Module module, CategoryPanel panel) {
        this.module = module;
        this.panel = panel;
        this.state.snap(module.isEnabled() ? 1f : 0f);
        this.presence.snap(module.isEnabled() ? 1f : 0f);
    }

    @Override
    public String searchKey() {
        return module.getName();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        float hoverValue = updateHover(mouseX, mouseY);
        state.setTarget(module.isEnabled() ? 1f : 0f);
        float switchState = state.update();
        presence.setTarget(module.isEnabled() ? 1f : 0f);
        float switchPresence = presence.update();
        Theme theme = Theme.current();

        // Плашка модуля: цвет темы Module Color, поверх него hover и раскрытое состояние.
        RenderUtils.fillRounded(graphics, x, y, width, height, RADIUS,
                ColorUtils.mulAlpha(theme.module, alpha));
        if (expanded) {
            RenderUtils.fillRounded(graphics, x, y, width, height, RADIUS,
                    ColorUtils.mulAlpha(ColorUtils.mix(theme.panel, theme.accent, 0.12f), alpha));
            // Акцентная полоска слева у раскрытой карточки.
            RenderUtils.fillRounded(graphics, x + 1, y + 3, 2, height - 6, 1,
                    ColorUtils.mulAlpha(theme.accent, alpha * 0.9f));
        } else if (hoverValue > 0.01f) {
            RenderUtils.fillRounded(graphics, x, y, width, height, RADIUS,
                    ColorUtils.mulAlpha(theme.hover, alpha * hoverValue));
        }

        int textY = y + (height - TextRenderer.lineHeight()) / 2;
        int switchSpace = module.isToggleable() ? SWITCH_WIDTH + 6 : 14;
        int textSpace = width - 18 - switchSpace;
        String label = TextRenderer.clip(LanguageManager.moduleName(module), textSpace,
                TextRenderer.Weight.REGULAR);
        TextRenderer.draw(graphics, label, x + 9, textY,
                ColorUtils.mulAlpha(module.isToggleable() ? theme.text : theme.text, alpha),
                TextRenderer.Weight.REGULAR);

        if (module.isToggleable()) {
            // Три точки у правого края: у включённого модуля они уезжают под тумблер.
            int dotsRight = x + width - 7 - Math.round(switchPresence * (SWITCH_WIDTH + 2));
            int dotColor = ColorUtils.mulAlpha(expanded ? theme.accent : theme.textMuted, alpha * 0.65f);
            int dotsY = y + height / 2 - 1;
            for (int i = 0; i < 3; i++) {
                graphics.fill(dotsRight - 7 + i * 3, dotsY, dotsRight - 5 + i * 3, dotsY + 2, dotColor);
            }
            if (switchPresence > 0.01f) {
                drawSwitch(graphics, theme, switchState, alpha * switchPresence);
            }
        } else {
            drawChevron(graphics, theme, alpha * (expanded ? 1f : 0.75f));
        }
    }

    private void drawSwitch(GuiGraphics graphics, Theme theme, float switchState, float alpha) {
        int trackX = x + width - SWITCH_INSET - SWITCH_WIDTH;
        int trackY = y + (height - SWITCH_HEIGHT) / 2;
        int trackRadius = SWITCH_HEIGHT / 2;

        int idleTrack = ColorUtils.mix(theme.panel, theme.text, 0.16f);
        RenderUtils.fillRounded(graphics, trackX, trackY, SWITCH_WIDTH, SWITCH_HEIGHT, trackRadius,
                ColorUtils.mulAlpha(idleTrack, alpha));
        if (switchState > 0.01f) {
            RenderUtils.fillRounded(graphics, trackX, trackY, SWITCH_WIDTH, SWITCH_HEIGHT, trackRadius,
                    ColorUtils.mulAlpha(theme.accent, alpha * switchState));
        }

        double travel = SWITCH_WIDTH - SWITCH_HEIGHT;
        double knobX = trackX + trackRadius + switchState * travel;
        double knobY = trackY + trackRadius;
        int knobColor = ColorUtils.mix(ColorUtils.mix(theme.text, theme.panel, 0.25f), theme.text, switchState);
        RenderUtils.fillCircle(graphics, knobX, knobY, 3.4, ColorUtils.mulAlpha(knobColor, alpha));
    }

    /** Уголок раскрытия для модулей-настроек: вниз — свёрнуто, вверх — раскрыто. */
    private void drawChevron(GuiGraphics graphics, Theme theme, float alpha) {
        double cx = x + width - 11;
        double cy = y + height / 2.0;
        int color = ColorUtils.mulAlpha(expanded ? theme.accent : theme.textMuted, alpha);
        if (expanded) {
            RenderUtils.fillLine(graphics, cx - 3.5, cy + 1.5, cx, cy - 2.0, 1.3, color);
            RenderUtils.fillLine(graphics, cx, cy - 2.0, cx + 3.5, cy + 1.5, 1.3, color);
        } else {
            RenderUtils.fillLine(graphics, cx - 3.5, cy - 1.5, cx, cy + 2.0, 1.3, color);
            RenderUtils.fillLine(graphics, cx, cy + 2.0, cx + 3.5, cy - 1.5, 1.3, color);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) {
            return false;
        }
        if (button == 0) {
            if (module.isToggleable()) {
                module.toggle();
            } else {
                // Модули-настройки (Themes, Language, Watermark, Notification) раскрываются
                // левым кликом: именно этого не хватало, поэтому Theme Manager «не открывался».
                toggleExpanded();
            }
            return true;
        }
        if (button == 1) {
            toggleExpanded();
            return true;
        }
        return true;
    }

    private void toggleExpanded() {
        expanded = !expanded;
        if (expanded) {
            children.clear();
            children.addAll(module.createSettingRows());
            if (children.isEmpty()) {
                expanded = false;
                return;
            }
            panel.insertAfter(this, children);
        } else {
            panel.removeRows(children);
            children.clear();
        }
    }
}
