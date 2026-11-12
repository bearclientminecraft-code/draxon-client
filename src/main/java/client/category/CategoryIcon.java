package client.category;

import client.DraxonClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Иконки категорий: PNG-текстуры из assets/draxon/textures/gui. */
public enum CategoryIcon {
    SWORDS("textures/gui/icon_swords.png"),
    ARROW("textures/gui/icon_arrow.png"),
    MONITOR("textures/gui/icon_monitor.png"),
    PERSON("textures/gui/icon_person.png"),
    GEAR("textures/gui/icon_gear.png");

    private final Identifier texture;

    CategoryIcon(String path) {
        this.texture = Identifier.fromNamespaceAndPath(DraxonClient.MOD_ID, path);
    }

    public void draw(GuiGraphics graphics, int x, int y, int size) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0f, 0f, size, size, size, size);
    }
}
