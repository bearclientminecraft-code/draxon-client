package client.hud;

import client.config.ConfigManager;
import client.gui.theme.Theme;
import client.lang.LanguageManager;
import client.module.Module;
import client.module.ModuleRegistry;
import client.module.combat.AutoTotemModule;
import client.module.combat.CombatApi;
import client.module.combat.HoverTotemModule;
import client.render.ColorUtils;
import client.render.RenderUtils;
import client.render.TextRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Счётчик тотемов: показывается по центру под прицелом, пока включён авто-тотем. */
public final class TotemCountHud {
    private TotemCountHud() {
    }

    public static void render(GuiGraphics graphics) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) {
            return;
        }
        if (!showCount("Auto Inventory Totem", AutoTotemModule.class)
                && !showCount("Hover Totem", HoverTotemModule.class)) {
            return;
        }
        int count = CombatApi.countTotems();
        Theme theme = Theme.current();
        String text = LanguageManager.tr("hud.totem_count", "Totems") + " " + count;

        int width = TextRenderer.width(text, TextRenderer.Weight.SEMIBOLD) + 14;
        int height = 16;
        int x = client.getWindow().getGuiScaledWidth() / 2 - width / 2;
        int y = client.getWindow().getGuiScaledHeight() / 2
                + Math.max(0, ConfigManager.settings().notificationOffset) + 62;

        RenderUtils.drawShadow(graphics, x, y, width, height, 6, 3, theme.shadow, 0.8f);
        RenderUtils.fillRounded(graphics, x, y, width, height, 6,
                ColorUtils.mulAlpha(theme.notification, 0.9f));
        TextRenderer.drawCentered(graphics, text, x + width / 2, y + (height - TextRenderer.lineHeight()) / 2,
                ColorUtils.mulAlpha(theme.text, 1f), TextRenderer.Weight.SEMIBOLD);
    }

    private static boolean showCount(String moduleName, Class<?> type) {
        return ModuleRegistry.byName(moduleName)
                .filter(Module::isEnabled)
                .map(module -> type.isInstance(module) && isShown(module))
                .orElse(false);
    }

    private static boolean isShown(Module module) {
        if (module instanceof AutoTotemModule autoTotem) {
            return autoTotem.showCount();
        }
        if (module instanceof HoverTotemModule hoverTotem) {
            return hoverTotem.showCount();
        }
        return false;
    }
}
