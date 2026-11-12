package client.keybind;

import client.DraxonClient;
import client.config.ConfigManager;
import client.gui.ClickGuiScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/** Клавиши мода и защита от конфликтов: одна клавиша не может иметь два действия. */
public final class KeybindManager {
    private static final Map<Integer, String> RESERVED_KEYS = new HashMap<>();
    private static KeyMapping clickGuiKey;

    private KeybindManager() {
    }

    public static void init() {
        KeyMapping.Category category =
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath(DraxonClient.MOD_ID, "main"));

        int key = ConfigManager.settings().clickGuiKey;
        clickGuiKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key.draxon.clickgui", InputConstants.Type.KEYSYM, key, category));
        reserve(key, "clickgui");

        ClientTickEvents.END_CLIENT_TICK.register(KeybindManager::onClientTick);
    }

    /**
     * Резервирует клавишу за действием.
     *
     * @return false, если клавиша уже занята другим действием
     */
    public static boolean reserve(int key, String action) {
        String owner = RESERVED_KEYS.get(key);
        if (owner != null) {
            DraxonClient.LOGGER.warn("Клавиша {} уже назначена на действие '{}'", key, owner);
            return false;
        }
        RESERVED_KEYS.put(key, action);
        return true;
    }

    public static boolean isReserved(int key) {
        return RESERVED_KEYS.containsKey(key);
    }

    private static void onClientTick(Minecraft client) {
        while (clickGuiKey.consumeClick()) {
            if (client.screen == null) {
                client.setScreen(new ClickGuiScreen());
            } else if (client.screen instanceof ClickGuiScreen gui && !gui.isTyping()) {
                gui.requestClose();
            }
        }
    }
}
