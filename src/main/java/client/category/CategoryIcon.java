package client.category;

import client.DraxonClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Иконки категорий: белые PNG-текстуры из assets/draxon/textures/gui, тонируются цветом. */
public enum CategoryIcon {
    SWORDS("textures/gui/icon_swords.png"),
    RUNNER("textures/gui/icon_runner.png"),
    PALETTE("textures/gui/icon_palette.png"),
    PERSON("textures/gui/icon_person.png"),
    GEAR("textures/gui/icon_gear.png");

    /** Сторона исходной текстуры: SVG-подобные глифы рендерятся в 32 px и уменьшаются при выводе. */
    private static final int ATLAS_SIZE = 32;

    private final Identifier texture;

    CategoryIcon(String path) {
        this.texture = Identifier.fromNamespaceAndPath(DraxonClient.MOD_ID, path);
    }

    public void draw(GuiGraphics graphics, int x, int y, int size, int color) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0f, 0f, size, size, ATLAS_SIZE, ATLAS_SIZE, color);
    }

    public void draw(GuiGraphics graphics, int x, int y, int size) {
        draw(graphics, x, y, size, 0xFFFFFFFF);
    }
}
