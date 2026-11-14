package client.module;

import client.category.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;

/**
 * Auto Respawn: как только появляется экран смерти, игрок возрождается сам.
 * Пакет респавна отправляется ровно один раз на каждый экран смерти.
 */
public final class AutoRespawnModule extends Module {
    private DeathScreen handled;

    public AutoRespawnModule(Category category) {
        super("Auto Respawn", "Automatically respawns after death", category);
    }

    @Override
    protected void onEnable() {
        handled = null;
    }

    @Override
    public void onTick(Minecraft client) {
        if (!(client.screen instanceof DeathScreen screen)) {
            handled = null;
            return;
        }
        if (handled == screen) {
            return;
        }
        handled = screen;
        if (client.player != null) {
            client.player.respawn();
        }
    }
}
