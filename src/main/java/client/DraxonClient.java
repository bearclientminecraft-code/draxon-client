package client;

import client.category.CategoryRegistry;
import client.config.ConfigManager;
import client.keybind.KeybindManager;
import client.module.ModuleRegistry;
import client.module.ThemesModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Точка входа клиента Draxon. */
public final class DraxonClient implements ClientModInitializer {
    public static final String MOD_ID = "draxon";
    public static final String NAME = "Draxon Client";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        CategoryRegistry.init();
        ModuleRegistry.init();
        ModuleRegistry.register(new ThemesModule());
        KeybindManager.init();

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ConfigManager.save());
        LOGGER.info("{} V1 инициализирован", NAME);
    }
}
