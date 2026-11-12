package client.gui;

import client.category.Category;
import client.gui.animation.Animation;
import client.gui.components.Component;
import client.gui.theme.Theme;
import client.render.ColorUtils;
import client.render.RenderUtils;
import client.render.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Стеклянная панель категории: заголовок, иконка и прокручиваемая область.
 * Позиция задаётся экраном; перетаскивания нет.
 */
public final class CategoryPanel extends Component {
    public static final int HEADER_HEIGHT = 30;
    public static final int ROW_HEIGHT = 30;
    private static final int RADIUS = 8;
    private static final int ICON_SIZE = 16;

    private final Category category;
    private final List<Component> components = new ArrayList<>();
    private final Animation scrollAnimation = new Animation(0f, 0.3f);
    private float scroll;
    private float scrollTarget;

    public CategoryPanel(Category category) {
        this.category = category;
    }

    public static int panelWidth(int screenWidth) {
        return Math.max(96, Math.min(150, Math.round(screenWidth * 0.13f)));
    }

    public static int panelHeight(int screenHeight) {
        return Math.max(150, Math.min(380, Math.round(screenHeight * 0.42f)));
    }

    public Category category() {
        return category;
    }

    public void addComponent(Component component) {
        components.add(component);
    }

    /** Ставит панель в указанную позицию и размер. */
    public void place(int x, int y, int width, int height) {
        setBounds(x, y, width, height);
        layoutComponents();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        scroll = scrollAnimation.update();
        layoutComponents();

        Theme theme = Theme.current();
        int edge = ColorUtils.mix(theme.panelBorder, 0xFFFFFFFF, 0.12f);

        RenderUtils.drawShadow(graphics, x, y, width, height, RADIUS, 3, theme.shadow, alpha);
        // Обводка стекла, затем сама поверхность и тонкий блик сверху.
        RenderUtils.fillRounded(graphics, x, y, width, height, RADIUS, ColorUtils.mulAlpha(edge, alpha));
        RenderUtils.fillRounded(graphics, x + 1, y + 1, width - 2, height - 2, RADIUS - 1,
                ColorUtils.mulAlpha(theme.panel, alpha));
        graphics.fill(x + RADIUS, y + 1, x + width - RADIUS, y + 2,
                ColorUtils.mulAlpha(edge, alpha));

        int titleY = y + (HEADER_HEIGHT - TextRenderer.lineHeight()) / 2;
        TextRenderer.draw(graphics, category.displayName(), x + 14, titleY,
                ColorUtils.mulAlpha(theme.text, alpha), TextRenderer.Weight.REGULAR);
        // Иконка в PNG уже нарисована нужным цветом, поэтому альфа здесь не применяется.
        category.icon().draw(graphics, x + width - ICON_SIZE - 12, y + (HEADER_HEIGHT - ICON_SIZE) / 2, ICON_SIZE);

        graphics.enableScissor(x + 1, y + HEADER_HEIGHT, x + width - 1, y + height - 1);
        for (Component component : components) {
            component.render(graphics, mouseX, mouseY, alpha);
        }
        graphics.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) {
            return false;
        }
        for (Component component : components) {
            if (component.mouseClicked(mouseX, mouseY, button)) {
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
        scrollTarget = clamp(scrollTarget - (float) amount * ROW_HEIGHT, 0f, maxScroll());
        scrollAnimation.setTarget(scrollTarget);
        return true;
    }

    private void layoutComponents() {
        int top = y + HEADER_HEIGHT - Math.round(scroll);
        for (int i = 0; i < components.size(); i++) {
            components.get(i).setBounds(x + 6, top + i * ROW_HEIGHT, width - 12, ROW_HEIGHT - 4);
        }
    }

    private float maxScroll() {
        return Math.max(0f, components.size() * ROW_HEIGHT - (height - HEADER_HEIGHT));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
