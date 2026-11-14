package client.gui;

import client.category.Category;
import client.category.CategoryRegistry;
import client.config.ConfigManager;
import client.gui.animation.Animation;
import client.gui.components.ModuleRow;
import client.gui.theme.Theme;
import client.module.Module;
import client.module.ModuleRegistry;
import client.notification.NotificationManager;
import client.render.ColorUtils;
import client.render.RenderUtils;
import client.render.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Главный экран ClickGUI: пять компактных панелей категорий в один ряд и поиск под ними.
 * Вся группа центрируется по экрану; панели не двигаются, прокручивается только их содержимое.
 */
public final class ClickGuiScreen extends Screen {
    private static final int SEARCH_GAP = 16;
    private static final int SEARCH_HEIGHT = 20;
    private static final int MIN_PANEL_HEIGHT = 92;
    private static final float SLIDE_DISTANCE = 10f;

    private final List<CategoryPanel> panels = new ArrayList<>();
    private final SearchBar searchBar;
    private final Animation openAnimation = new Animation(0f, 0.22f);
    private int panelHeight;
    private int panelTop;
    private boolean closing;

    public ClickGuiScreen() {
        super(net.minecraft.network.chat.Component.literal("Draxon ClickGUI"));
        for (Category category : CategoryRegistry.all()) {
            CategoryPanel panel = new CategoryPanel(category);
            for (Module module : ModuleRegistry.byCategory(category)) {
                panel.addComponent(new ModuleRow(module, panel));
            }
            panels.add(panel);
        }
        this.searchBar = new SearchBar(query -> {
            for (CategoryPanel panel : panels) {
                panel.setQuery(query);
            }
        });
    }

    @Override
    protected void init() {
        int content = 0;
        for (CategoryPanel panel : panels) {
            content = Math.max(content, panel.contentHeight());
        }
        int natural = Math.max(MIN_PANEL_HEIGHT, content + CategoryPanel.HEADER_HEIGHT + 8);
        panelHeight = Math.min(natural, Math.max(MIN_PANEL_HEIGHT, height - 120));

        // Группа «панели + поиск» стоит по центру экрана.
        int groupHeight = panelHeight + SEARCH_GAP + SEARCH_HEIGHT;
        panelTop = Math.max(14, (height - groupHeight) / 2);

        int barWidth = Math.max(140, Math.min(Math.round(CategoryPanel.panelWidth(width) * 0.88f),
                Math.max(120, width - 60)));
        int barY = Math.min(panelTop + panelHeight + SEARCH_GAP, height - SEARCH_HEIGHT - 10);
        searchBar.setBounds((width - barWidth) / 2, barY, barWidth, SEARCH_HEIGHT);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Размытие фона движок применяет сам при открытом экране: повторный вызов
        // renderBlurredBackground() в этом же кадре падает с «Can only blur once per frame».
        openAnimation.setTarget(closing ? 0f : 1f);
        float progress = openAnimation.update();
        if (closing && progress <= 0.01f) {
            minecraft.setScreen(null);
            return;
        }

        Theme theme = Theme.current();
        graphics.fill(0, 0, width, height, ColorUtils.mulAlpha(theme.background, progress * 0.6f));

        layoutPanels(progress);
        for (int i = 0; i < panels.size(); i++) {
            panels.get(i).render(graphics, mouseX, mouseY, panelProgress(progress, i));
        }
        searchBar.render(graphics, mouseX, mouseY, clamp01(progress * 1.6f - 0.45f));

        renderThemePreview(graphics, mouseX, mouseY);
        NotificationManager.render(graphics);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (closing) {
            return true;
        }
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

        for (int i = panels.size() - 1; i >= 0; i--) {
            if (panels.get(i).mouseClicked(mouseX, mouseY, button)) {
                searchBar.unfocus();
                return true;
            }
        }
        if (searchBar.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        searchBar.unfocus();
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (CategoryPanel panel : panels) {
            if (panel.mouseScrolled(mouseX, mouseY, scrollY)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE
                || event.key() == ConfigManager.settings().clickGuiKey) {
            onClose();
            return true;
        }
        // Стрелки прокручивают содержимое панелей; сами панели остаются на месте.
        if (!searchBar.isFocused() && (event.key() == GLFW.GLFW_KEY_UP || event.key() == GLFW.GLFW_KEY_DOWN)) {
            float step = event.key() == GLFW.GLFW_KEY_UP ? CategoryPanel.ROW_HEIGHT : -CategoryPanel.ROW_HEIGHT;
            for (CategoryPanel panel : panels) {
                panel.scrollBy(step);
            }
            return true;
        }
        return searchBar.keyPressed(event.key(), event.scancode(), event.modifiers());
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return searchBar.charTyped(event.codepoint());
    }

    @Override
    public void onClose() {
        requestClose();
    }

    /** Запускает анимацию закрытия; экран закроется, когда она завершится. */
    public void requestClose() {
        closing = true;
    }

    public boolean isTyping() {
        return searchBar.isFocused();
    }

    /** Предпросмотр темы при наведении на плашку в настройках модуля Themes. */
    private void renderThemePreview(GuiGraphics graphics, int mouseX, int mouseY) {
        Theme preview = null;
        for (CategoryPanel panel : panels) {
            Theme hovered = panel.hoveredTheme(mouseX, mouseY);
            if (hovered != null) {
                preview = hovered;
                break;
            }
        }
        if (preview == null) {
            return;
        }
        int boxWidth = 136;
        int boxHeight = 80;
        int x = Math.min(mouseX + 12, width - boxWidth - 8);
        int y = Math.max(8, Math.min(mouseY - 10, height - boxHeight - 8));

        RenderUtils.drawShadow(graphics, x, y, boxWidth, boxHeight, 9, 4, preview.shadow, 1f);
        RenderUtils.fillRounded(graphics, x, y, boxWidth, boxHeight, 9,
                ColorUtils.mulAlpha(preview.notification, 0.98f));
        RenderUtils.fillRounded(graphics, x, y, boxWidth, boxHeight, 9,
                ColorUtils.mulAlpha(preview.panelBorder, 0.45f));
        RenderUtils.fillRounded(graphics, x + 1, y + 1, boxWidth - 2, boxHeight - 2, 8,
                ColorUtils.mulAlpha(preview.panel, 0.98f));

        TextRenderer.draw(graphics, preview.displayName, x + 9, y + 8,
                preview.text, TextRenderer.Weight.SEMIBOLD);
        for (int i = 0; i < 3; i++) {
            RenderUtils.fillRounded(graphics, x + 8, y + 23 + i * 13, boxWidth - 16, 10, 4,
                    ColorUtils.mulAlpha(preview.module, 0.85f));
        }
        int[] palette = preview.palette();
        int gap = 3;
        int chipWidth = (boxWidth - 16 - (palette.length - 1) * gap) / palette.length;
        int chipY = y + boxHeight - 14;
        for (int i = 0; i < palette.length; i++) {
            int chipX = x + 8 + i * (chipWidth + gap);
            graphics.fill(chipX - 1, chipY - 1, chipX + chipWidth + 1, chipY + 7,
                    ColorUtils.mulAlpha(preview.textMuted, 0.45f));
            graphics.fill(chipX, chipY, chipX + chipWidth, chipY + 6, palette[i]);
        }
    }

    /** Сдвиг и прозрачность панели: панели всплывают каскадом слева направо. */
    private float panelProgress(float progress, int index) {
        return clamp01(progress * (1f + index * 0.07f) - index * 0.07f);
    }

    /** Все панели стоят в один ряд по центру экрана. */
    private void layoutPanels(float progress) {
        int panelWidth = CategoryPanel.panelWidth(width);
        int gap = Math.max(5, Math.round(panelWidth * 0.05f));
        int total = panels.size() * panelWidth + (panels.size() - 1) * gap;
        int startX = (width - total) / 2;

        for (int i = 0; i < panels.size(); i++) {
            float panelProgress = panelProgress(progress, i);
            int slide = Math.round((1f - panelProgress) * SLIDE_DISTANCE);
            panels.get(i).place(startX + i * (panelWidth + gap), panelTop + slide, panelWidth, panelHeight);
        }
    }

    private static float clamp01(float value) {
        return value < 0f ? 0f : Math.min(value, 1f);
    }
}
