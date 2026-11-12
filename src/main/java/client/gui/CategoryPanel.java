package client.gui;

import client.category.Category;
import client.gui.animation.Animation;
import client.gui.components.Component;
import client.gui.theme.Theme;
import client.render.ColorUtils;
import client.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Панель категории: заголовок, иконка, перетаскивание за заголовок и прокручиваемая область.
 * Содержимое пока пустое: модули будут добавляться через {@link #addComponent(Component)}.
 */
public final class CategoryPanel extends Component {
    public static final int HEADER_HEIGHT = 28;
    public static final int ROW_HEIGHT = 30;
    private static final int RADIUS = 8;
    private static final int MIN_HEIGHT = 170;
    private static final int MAX_HEIGHT = 460;

    private final Category category;
    private final List<Component> components = new ArrayList<>();
    private final Animation scrollAnimation = new Animation(0f, 0.3f);

    /** Позиция левого верхнего угла в долях экрана: переживает изменение разрешения. */
    private double fractionX;
    private double fractionY;
    private boolean defaultPosition;

    private int screenWidth;
    private int screenHeight;
    private int slide;
    private float scroll;
    private float scrollTarget;

    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;

    public CategoryPanel(Category category, double[] savedPosition) {
        this.category = category;
        if (savedPosition != null && savedPosition.length == 2) {
            this.fractionX = savedPosition[0];
            this.fractionY = savedPosition[1];
        } else {
            this.defaultPosition = true;
        }
    }

    public static int panelWidth(int screenWidth) {
        return Math.max(120, Math.min(190, Math.round(screenWidth * 0.18f)));
    }

    public static int panelHeight(int screenHeight) {
        return Math.max(MIN_HEIGHT, Math.min(MAX_HEIGHT, Math.round(screenHeight * 0.5f)));
    }

    public Category category() {
        return category;
    }

    public boolean needsDefaultPosition() {
        return defaultPosition;
    }

    public void setPosition(double fractionX, double fractionY) {
        this.fractionX = fractionX;
        this.fractionY = fractionY;
        this.defaultPosition = false;
    }

    public double[] position() {
        return new double[]{fractionX, fractionY};
    }

    public void addComponent(Component component) {
        components.add(component);
    }

    /**
     * Пересчитывает размеры и положение под текущий экран.
     *
     * @param slide смещение по вертикали для анимации появления
     */
    public void updateBounds(int screenWidth, int screenHeight, int slide) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.slide = slide;
        this.width = panelWidth(screenWidth);
        this.height = panelHeight(screenHeight);
        this.x = clamp(Math.round((float) (fractionX * screenWidth)), 0, screenWidth - width);
        this.y = clamp(Math.round((float) (fractionY * screenHeight)), 0, screenHeight - height) + slide;
        layoutComponents();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        scroll = scrollAnimation.update();
        layoutComponents();

        Theme theme = Theme.current();
        RenderUtils.drawShadow(graphics, x, y, width, height, RADIUS, 6, theme.shadow, alpha);
        RenderUtils.fillRounded(graphics, x, y, width, height, RADIUS, ColorUtils.mulAlpha(theme.panelBorder, alpha));
        RenderUtils.fillRounded(graphics, x + 1, y + 1, width - 2, height - 2, RADIUS - 1,
                ColorUtils.mulAlpha(theme.panel, alpha));

        Font font = Minecraft.getInstance().font;
        int titleY = y + (HEADER_HEIGHT - font.lineHeight) / 2;
        graphics.drawString(font, category.displayName(), x + 12, titleY,
                ColorUtils.mulAlpha(theme.text, alpha), false);
        category.icon().draw(graphics, x + width - 22, y + (HEADER_HEIGHT - 14) / 2, 2,
                ColorUtils.mulAlpha(theme.accent, alpha));
        graphics.fill(x + 8, y + HEADER_HEIGHT, x + width - 8, y + HEADER_HEIGHT + 1,
                ColorUtils.mulAlpha(theme.panelBorder, alpha));

        graphics.enableScissor(x + 1, y + HEADER_HEIGHT + 1, x + width - 1, y + height - 1);
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
        if (button == 0 && mouseY < y + HEADER_HEIGHT) {
            dragging = true;
            dragOffsetX = (int) (mouseX - x);
            dragOffsetY = (int) (mouseY - y);
            return true;
        }
        for (Component component : components) {
            if (component.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return true;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!dragging) {
            return false;
        }
        int newX = clamp((int) (mouseX - dragOffsetX), 0, screenWidth - width);
        int newY = clamp((int) (mouseY - dragOffsetY), 0, screenHeight - height);
        fractionX = newX / (double) screenWidth;
        fractionY = newY / (double) screenHeight;
        x = newX;
        y = newY;
        layoutComponents();
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

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
