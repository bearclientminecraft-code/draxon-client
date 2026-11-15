package client.hud;

import client.module.Module;
import client.module.ModuleRegistry;
import net.minecraft.client.gui.GuiGraphics;

/** Единственная точка отрисовки водяного знака: виден, пока включён модуль Watermark. */
public final class WatermarkHUD {
    private WatermarkHUD() {
    }

    public static void render(GuiGraphics graphics) {
        boolean enabled = ModuleRegistry.byName("Watermark")
                .map(Module::isEnabled)
                .orElse(false);
        Watermark.render(graphics, enabled);
    }
}
