package client.module;

import client.category.Category;
import client.gui.animation.Animation;
import client.hud.WatermarkHUD;
import client.config.ConfigManager;
import net.minecraft.client.Minecraft;

/** Разворачивается в Misc — здесь включается/выключается водяной знак и счётчик FPS. */
public final class WatermarkModule extends Module {
    private final Animation state = new Animation(0f, 0.25f);

    public WatermarkModule(Category category) {
        super("Watermark", "Watermark (D + FPS)", category);
    }

    @Override
    public boolean isToggleable() {
        return false;
    }

    @Override
    public void onTick(Minecraft client) {
        // Включение/выключение водяного знака и счётчика FPS управляется модулем.
        WatermarkHUD.setVisible(ConfigManager.settings().watermarkVisible);
    }
}
