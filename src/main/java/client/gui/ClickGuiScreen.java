package client.gui;

import client.category.Category;
import client.category.CategoryRegistry;
import client.gui.animation.Animation;
import client.gui.components.Component;
import client.gui.theme.Theme;
import client.module.Module;
import client.module.ModuleRegistry;
import client.render.ColorUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Главный экран ClickGUI: стеклянные панели категорий и поиск. Перетаскивания нет. */
public final class ClickGuiScreen extends Screen {
    private static final int PANEL_GAP = 6;
    private static final int SLIDE_DISTANCE = 14;

    private final List<CategoryPanel> panels = new ArrayList<>();
    private final SearchBar searchBar;
    private final Animation openAnimation = new Animation(0f, 0.2f);
    private List<Module> searchResults = List.of();
    private boolean closing;

    public ClickGuiScreen() {
        super(net.minecraft.network.chat.Component.literal("Draxon ClickGUI"));
        for (Category category : CategoryRegistry.all()) {
            panels.add(new CategoryPanel(category));
        }
        this.searchBar = new SearchBar(query -> searchResults = ModuleRegistry.search(query));
    }

    @Override
    protected void init() {
        int barWidth = Math.min(260, width - 40);
        searchBar.setBounds((width - barWidth) / 2, height - 46, barWidth, 22);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        openAnimation.setTarget(closing ? 0f : 1f);
        float progress = openAnimation.update();
        if (closing && progress <= 0.01f) {
            minecraft.setScreen(null);
            return;
        }

        // Размытие не вызываем: движок уже размывает фон в этом кадре.
        graphics.fill(0, 0, width, height, ColorUtils.mulAlpha(Theme.current().background, progress));

        layoutPanels(Math.round((1f - progress) * SLIDE_DISTANCE));
        for (CategoryPanel panel : panels) {
            panel.render(graphics, mouseX, mouseY, progress);
        }
        searchBar.render(graphics, mouseX, mouseY, progress);
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
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
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

    /** Все панели стоят в один ряд по центру экрана, промежуток {@value #PANEL_GAP} пикселей. */
    private void layoutPanels(int slide) {
        int panelWidth = CategoryPanel.panelWidth(width);
        int panelHeight = CategoryPanel.panelHeight(height);
        int total = panels.size() * panelWidth + (panels.size() - 1) * PANEL_GAP;
        int startX = (width - total) / 2;
        int top = Math.max(30, height / 5) + slide;

        for (int i = 0; i < panels.size(); i++) {
            panels.get(i).place(startX + i * (panelWidth + PANEL_GAP), top, panelWidth, panelHeight);
        }
    }
}
