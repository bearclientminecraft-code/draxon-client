package client.gui;

import client.category.Category;
import client.gui.animation.Animation;
import client.gui.components.Component;
import client.gui.components.ThemeRow;
import client.gui.theme.Theme;
import client.lang.LanguageManager;
import client.render.ColorUtils;
import client.render.RenderUtils;
import client.render.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Стеклянная панель категории: заголовок, иконка и прокручиваемая область.
 * Позиция и размер задаются экраном; перетаскивания нет.
 */
public final class CategoryPanel extends Component {
    public static final int HEADER_HEIGHT = 34;
    public static final int ROW_HEIGHT = 26;
    private static final int RADIUS = 12;
    private static final int ICON_SIZE = 12;
    private static final int PADDING_X = 6;
    private static final int PADDING_BOTTOM = 6;

    private final Category category;
    private final List<Component> components = new ArrayList<>();
    private final Animation scrollAnimation = new Animation(0f, 0.3f);
    private float scroll;
    private float scrollTarget;
    private String query = "";
    private boolean layoutDirty = true;
    private int lastLayoutScroll = Integer.MIN_VALUE;

    public CategoryPanel(Category category) {
        this.category = category;
    }

    /** Компактная ширина панели: 168 px при экране 1440 px. */
    public static int panelWidth(int screenWidth) {
        return Math.max(160, Math.min(200, Math.round(screenWidth * 0.142f)));
    }

    public Category category() {
        return category;
    }

    public void addComponent(Component component) {
        components.add(component);
        layoutDirty = true;
    }

    /** Вставляет строки сразу после anchor (для раскрытых настроек модуля). */
    public void insertAfter(Component anchor, List<Component> rows) {
        int index = components.indexOf(anchor);
        if (index < 0) {
            return;
        }
        components.addAll(index + 1, rows);
        scrollTarget = clamp(scrollTarget, 0f, maxScroll());
        layoutDirty = true;
        layoutComponents();
    }

    /** Убирает строки, которые были раскрыты под модулем. */
    public void removeRows(List<Component> rows) {
        components.removeAll(rows);
        scrollTarget = clamp(scrollTarget, 0f, maxScroll());
        layoutDirty = true;
        layoutComponents();
    }

    /** Суммарная высота строк с учётом текущего поиска. */
    public int contentHeight() {
        int total = 0;
        for (Component component : components) {
            if (matches(component)) {
                total += component.rowHeight();
            }
        }
        return total;
    }

    /** Фильтр поиска: пока запрос пуст, видны все строки. */
    public void setQuery(String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals(this.query)) {
            return;
        }
        this.query = normalized;
        scrollTarget = clamp(scrollTarget, 0f, maxScroll());
        scrollAnimation.setTarget(scrollTarget);
        layoutDirty = true;
        layoutComponents();
    }

    /** Ставит панель в указанную позицию и размер. */
    public void place(int x, int y, int width, int height) {
        setBounds(x, y, width, height);
        layoutDirty = true;
        layoutComponents();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        if (alpha <= 0.01f) {
            return;
        }
        scroll = scrollAnimation.update();
        layoutComponents();

        Theme theme = Theme.current();
        int edge = ColorUtils.mix(theme.panelBorder, 0xFFFFFFFF, 0.16f);

        RenderUtils.drawShadow(graphics, x, y, width, height, RADIUS, 5, theme.shadow, alpha);
        RenderUtils.fillRounded(graphics, x, y, width, height, RADIUS, ColorUtils.mulAlpha(edge, alpha * 0.5f));
        RenderUtils.fillRounded(graphics, x + 1, y + 1, width - 2, height - 2, RADIUS - 1,
                ColorUtils.mulAlpha(theme.panel, alpha));

        int titleY = y + (HEADER_HEIGHT - TextRenderer.lineHeight()) / 2;
        TextRenderer.draw(graphics, LanguageManager.category(category.id(), category.displayName()), x + 10, titleY,
                ColorUtils.mulAlpha(theme.text, alpha), TextRenderer.Weight.SEMIBOLD);
        category.icon().draw(graphics, x + width - ICON_SIZE - 10, y + (HEADER_HEIGHT - ICON_SIZE) / 2,
                ICON_SIZE, ColorUtils.mulAlpha(theme.text, alpha * 0.92f));

        if (contentHeight() == 0) {
            TextRenderer.drawCentered(graphics, LanguageManager.tr("panel.empty", "No modules"), x + width / 2,
                    y + HEADER_HEIGHT + 8, ColorUtils.mulAlpha(theme.textMuted, alpha * 0.85f),
                    TextRenderer.Weight.REGULAR);
            return;
        }

        graphics.enableScissor(x + 1, y + HEADER_HEIGHT, x + width - 1, y + height - PADDING_BOTTOM);
        for (Component component : components) {
            if (matches(component)) {
                component.render(graphics, mouseX, mouseY, alpha);
            }
        }
        graphics.disableScissor();
    }

    /** Тема под курсором (для предпросмотра в настройках модуля Themes). */
    public Theme hoveredTheme(int mouseX, int mouseY) {
        for (Component component : components) {
            if (component instanceof ThemeRow row && matches(component) && row.isHovered(mouseX, mouseY)) {
                return row.theme();
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) {
            return false;
        }
        // Копия списка: клик может раскрыть строки и изменить components.
        for (Component component : List.copyOf(components)) {
            if (matches(component) && component.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!isHovered(mouseX, mouseY)) {
            return false;
        }
        scrollBy((float) amount * ROW_HEIGHT);
        return true;
    }

    /** Прокручивает содержимое на delta пикселей (положительное — вверх). */
    public void scrollBy(float delta) {
        scrollTarget = clamp(scrollTarget - delta, 0f, maxScroll());
        scrollAnimation.setTarget(scrollTarget);
    }

    private boolean matches(Component component) {
        if (query.isEmpty()) {
            return true;
        }
        String key = component.searchKey();
        return key != null && key.toLowerCase(Locale.ROOT).contains(query);
    }

    /** Раскладка пересчитывается только когда действительно менялись границы, скролл или фильтр. */
    private void layoutComponents() {
        int applied = Math.round(scroll);
        if (!layoutDirty && applied == lastLayoutScroll) {
            return;
        }
        layoutDirty = false;
        lastLayoutScroll = applied;

        int top = y + HEADER_HEIGHT - applied;
        for (Component component : components) {
            if (!matches(component)) {
                continue;
            }
            int rowHeight = component.rowHeight();
            int indent = component.indent();
            component.setBounds(x + PADDING_X + indent, top, width - PADDING_X * 2 - indent, rowHeight - 4);
            top += rowHeight;
        }
    }

    private float maxScroll() {
        return Math.max(0f, contentHeight() - (height - HEADER_HEIGHT - PADDING_BOTTOM));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
