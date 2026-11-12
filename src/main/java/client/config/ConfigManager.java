package client.config;

import client.DraxonClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Сохранение настроек клиента в config/draxon.json. */
public final class ConfigManager {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("draxon.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Settings settings = new Settings();

    private ConfigManager() {
    }

    public static Settings settings() {
        return settings;
    }

    public static void load() {
        if (!Files.exists(FILE)) {
            return;
        }
        try {
            Settings loaded = GSON.fromJson(Files.readString(FILE), Settings.class);
            if (loaded != null) {
                settings = loaded;
            }
        } catch (IOException | JsonParseException e) {
            DraxonClient.LOGGER.error("Не удалось прочитать {}, используются значения по умолчанию", FILE, e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(settings));
        } catch (IOException e) {
            DraxonClient.LOGGER.error("Не удалось сохранить настройки в {}", FILE, e);
        }
    }

    /** Данные, которые сохраняются между перезапусками. */
    public static final class Settings {
        /** Имя темы из {@link client.gui.theme.Theme}. */
        public String theme = "midnight_blue";
        /** Код клавиши открытия ClickGUI (по умолчанию Right Shift). */
        public int clickGuiKey = GLFW.GLFW_KEY_RIGHT_SHIFT;
        /** Позиции панелей в долях экрана: id категории -> {x, y}. */
        public Map<String, double[]> panelPositions = new HashMap<>();
    }
}
