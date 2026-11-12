package client.gui;

import client.category.Category;
import client.category.CategoryRegistry;
import client.config.ConfigManager;
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

/** Главный экран ClickGUI: панели категорий и поиск поверх затемнённого мира. */
public final class ClickGuiScreen extends Screen {
    private static final int PANEL_GAP = 12;
    private static final int SLIDE_DISTANCE = 16;
    private static final double DEFAULT_TOP = 0.22;

    private final List<CategoryPanel> panels = new ArrayList<>();
    private final SearchBar searchBar;
    private final Animation openAnimation = new Animation(0f, 0.2f);
    private List<Module> searchResults = List.of();
    private Component activeComponent;
    private boolean closing;

    public ClickGuiScreen() {
        super(net.minecraft.network.chat.Component.literal("Draxon ClickGUI"));
        for (Category category : CategoryRegistry.all()) {
            panels.add(new CategoryPanel(category, ConfigManager.settings().panelPositions.get(category.id())));
        }
        this.searchBar = new SearchBar(query -> searchResults = ModuleRegistry.search(query));
    }

    @Override
    protected void init() {
        int barWidth = Math.min(260, width - 40);
        searchBar.setBounds((width - barWidth) / 2, height - 46, barWidth, 22);
        layoutPanels();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        openAnimation.setTarget(closing ? 0f : 1f);
        float progress = openAnimation.update();
        if (closing && progress <= 0.01f) {
            minecraft.setScreen(null);
            return;
        }

        graphics.fill(0, 0, width, height, ColorUtils.mulAlpha(Theme.current().background, progress));

        int slide = Math.round((1f - progress) * SLIDE_DISTANCE);
        for (CategoryPanel panel : panels) {
            panel.updateBounds(width, height, slide);
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
            CategoryPanel panel = panels.get(i);
            if (panel.mouseClicked(mouseX, mouseY, button)) {
                searchBar.unfocus();
                activeComponent = panel;
                return true;
            }
        }
        if (searchBar.mouseClicked(mouseX, mouseY, button)) {
            activeComponent = searchBar;
            return true;
        }
        searchBar.unfocus();
        activeComponent = null;
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (activeComponent == null) {
            return false;
        }
        return activeComponent.mouseDragged(event.x(), event.y(), event.button(), deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (activeComponent == null) {
            return false;
        }
        boolean wasPanel = activeComponent instanceof CategoryPanel;
        activeComponent.mouseReleased(event.x(), event.y(), event.button());
        activeComponent = null;
        if (wasPanel) {
            savePanelPositions();
        }
        return true;
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

    private void layoutPanels() {
        int panelWidth = CategoryPanel.panelWidth(width);
        int total = panels.size() * panelWidth + (panels.size() - 1) * PANEL_GAP;
        int startX = (width - total) / 2;
        for (int i = 0; i < panels.size(); i++) {
            CategoryPanel panel = panels.get(i);
            if (panel.needsDefaultPosition()) {
                panel.setPosition((startX + i * (panelWidth + PANEL_GAP)) / (double) width, DEFAULT_TOP);
            }
        }
    }

    private void savePanelPositions() {
        for (CategoryPanel panel : panels) {
            ConfigManager.settings().panelPositions.put(panel.category().id(), panel.position());
        }
        ConfigManager.save();
    }
}
