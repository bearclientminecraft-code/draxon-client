package client.module;

import client.category.Category;
import client.gui.animation.Animation;
import client.render.RenderUtils;
import net.minecraft.client.Minecraft;

/** Простая оптимизация интерфейса: сглаживание фона включено только на кадры с активными модулями. */
public final class OptimizationModule extends Module {
    private final Animation state = new Animation(0f, 0.25f);

    public OptimizationModule(Category category) {
        super("Optimization", "Simple interface optimization", category);
    }

    @Override
    public boolean isToggleable() {
        return false;
    }

    @Override
    public void onTick(Minecraft client) {
        float enabled = state.update();
        if (enabled > 0.01f) {
            RenderUtils.enableAntiAlias(true);
        } else {
            RenderUtils.enableAntiAlias(false);
        }
    }
}
