package client;

import client.category.CategoryRegistry;
import client.command.CommandManager;
import client.config.ConfigManager;
import client.hud.ArmorHud;
import client.hud.NotificationHUD;
import client.hud.Watermark;
import client.hud.WatermarkHUD;
import client.hud.NotificationHUD;
import client.keybind.KeybindManager;
import client.module.Module;
import client.module.ModuleRegistry;
import client.notification.NotificationManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Точка входа клиента Draxon. */
public final class DraxonClient implements ClientModInitializer {
    public static final String MOD_ID = "draxon";
    public static final String NAME = "Draxon Client";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    @Override
    public void onInitializeClient() {
        CategoryRegistry.init();
        ModuleRegistry.init();
        ConfigManager.load();
        KeybindManager.init();
        CommandManager.init();

        ClientTickEvents.END_CLIENT_TICK.register(ModuleRegistry::tick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ConfigManager.save());

        // Уведомления рисуются поверх HUD; при открытом экране их рисует сам ClickGUI.
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "notifications"),
                (graphics, deltaTracker) -> {
                    if (Minecraft.getInstance().screen == null) {
                        NotificationManager.render(graphics);
                    }
                });

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "watermark"),
                (graphics, deltaTracker) -> Watermark.render(graphics));

        Module armorHud = ModuleRegistry.byName("Armor HUD").orElseThrow();
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "armor_hud"),
                (graphics, deltaTracker) -> {
                    if (armorHud.isEnabled()) {
                        ArmorHud.render(graphics);
                    }
                });

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "watermark_hud"),
                (graphics, deltaTracker) -> WatermarkHUD.render(graphics));
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "notification_hud"),
                (graphics, deltaTracker) -> NotificationHUD.render(graphics));

        LOGGER.info("{} V1 инициализирован", NAME);
    }
}
