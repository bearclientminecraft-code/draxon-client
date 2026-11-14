package client.config;

import client.DraxonClient;
import client.keybind.KeybindManager;
import client.lang.LanguageManager;
import client.module.Module;
import client.module.ModuleRegistry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Настройки Draxon и управление конфигурациями.
 *
 * <p>Основные настройки лежат в {@code Draxon Client/settings.json}, профили —
 * в {@code Draxon Client/configs/<имя>.json}. Профили хранят состояние модулей,
 * назначенные клавиши, тему и язык. Имена профилей проверяются по шаблону, поэтому
 * удалить файл вне папки configs нельзя.</p>
 */
public final class ConfigManager {
    public static final Path ROOT = FabricLoader.getInstance().getGameDir().resolve("Draxon Client");
    public static final Path CONFIGS = ROOT.resolve("configs");
    public static final Path THEMES_DIR = ROOT.resolve("themes");
    private static final Path SETTINGS = ROOT.resolve("settings.json");
    private static final Path LEGACY = FabricLoader.getInstance().getConfigDir().resolve("draxon.json");
    private static final Pattern PROFILE_NAME = Pattern.compile("[A-Za-z0-9_-]{1,32}");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static Settings settings = new Settings();

    private ConfigManager() {
    }

    public static Settings settings() {
        return settings;
    }

    /** Результат работы с профилем. */
    public enum Result {
        OK,
        INVALID_NAME,
        MISSING,
        ERROR
    }

    public static void load() {
        createDirectories();
        Settings loaded = read(SETTINGS, Settings.class);
        if (loaded == null) {
            loaded = read(LEGACY, Settings.class);
        }
        if (loaded != null) {
            settings = loaded;
        }
        normalize();
        applyModuleStates(settings.modules);
    }

    public static void save() {
        createDirectories();
        settings.modules = captureModuleStates();
        write(SETTINGS, settings);
    }

    /** Снимок состояний всех модулей по именам. */
    public static Map<String, Boolean> captureModuleStates() {
        Map<String, Boolean> states = new HashMap<>();
        for (Module module : ModuleRegistry.all()) {
            states.put(module.getName(), module.isEnabled());
        }
        return states;
    }

    public static void applyModuleStates(Map<String, Boolean> states) {
        if (states == null) {
            return;
        }
        for (Module module : ModuleRegistry.all()) {
            Boolean state = states.get(module.getName());
            if (state != null) {
                module.setEnabledSilently(state);
            }
        }
    }

    public static boolean isValidName(String name) {
        return name != null && PROFILE_NAME.matcher(name).matches();
    }

    public static Result saveProfile(String name) {
        if (!isValidName(name)) {
            return Result.INVALID_NAME;
        }
        createDirectories();
        Profile profile = new Profile();
        profile.theme = settings.theme;
        profile.language = settings.language;
        profile.clickGuiKey = settings.clickGuiKey;
        profile.modules = captureModuleStates();
        return write(profilePath(name), profile) ? Result.OK : Result.ERROR;
    }

    public static Result loadProfile(String name) {
        if (!isValidName(name)) {
            return Result.INVALID_NAME;
        }
        Path path = profilePath(name);
        if (!Files.isRegularFile(path)) {
            return Result.MISSING;
        }
        Profile profile = read(path, Profile.class);
        if (profile == null) {
            return Result.ERROR;
        }
        if (profile.theme != null) {
            settings.theme = profile.theme;
        }
        if (profile.language != null) {
            settings.language = profile.language;
            LanguageManager.refresh();
        }
        applyModuleStates(profile.modules);
        if (profile.clickGuiKey > 0) {
            KeybindManager.applyKey(profile.clickGuiKey);
        }
        save();
        return Result.OK;
    }

    public static Result deleteProfile(String name) {
        if (!isValidName(name)) {
            return Result.INVALID_NAME;
        }
        Path path = profilePath(name);
        if (!Files.isRegularFile(path)) {
            return Result.MISSING;
        }
        Path parent = path.getParent();
        if (parent == null || !parent.toAbsolutePath().normalize().equals(CONFIGS.toAbsolutePath().normalize())) {
            return Result.INVALID_NAME;
        }
        try {
            Files.delete(path);
            return Result.OK;
        } catch (IOException exception) {
            DraxonClient.LOGGER.error("Не удалось удалить профиль {}", path, exception);
            return Result.ERROR;
        }
    }

    public static List<String> profiles() {
        if (!Files.isDirectory(CONFIGS)) {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        try (Stream<Path> stream = Files.list(CONFIGS)) {
            stream.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(fileName -> fileName.endsWith(".json"))
                    .map(fileName -> fileName.substring(0, fileName.length() - ".json".length()))
                    .sorted(Comparator.naturalOrder())
                    .forEach(names::add);
        } catch (IOException exception) {
            DraxonClient.LOGGER.error("Не удалось прочитать список профилей", exception);
        }
        return names;
    }

    private static Path profilePath(String name) {
        return CONFIGS.resolve(name + ".json");
    }

    private static void createDirectories() {
        try {
            Files.createDirectories(CONFIGS);
            Files.createDirectories(THEMES_DIR);
        } catch (IOException exception) {
            DraxonClient.LOGGER.error("Не удалось создать папки Draxon Client", exception);
        }
    }

    private static void normalize() {
        if (settings.modules == null) {
            settings.modules = new HashMap<>();
        }
        if (settings.panelPositions == null) {
            settings.panelPositions = new HashMap<>();
        }
        if (settings.theme == null) {
            settings.theme = "draxon";
        }
        if (settings.language == null) {
            settings.language = "en_us";
        }
        if (settings.clickGuiKey <= 0) {
            settings.clickGuiKey = GLFW.GLFW_KEY_RIGHT_SHIFT;
        }
        if (!"left".equals(settings.armorHudSide) && !"right".equals(settings.armorHudSide)) {
            settings.armorHudSide = "right";
        }
        if (settings.watermarkVisible != true) {
            settings.watermarkVisible = true;
        }
        if (settings.notificationVisible != true) {
            settings.notificationVisible = true;
        }
    }

    private static boolean write(Path path, Object value) {
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, GSON.toJson(value));
            return true;
        } catch (IOException exception) {
            DraxonClient.LOGGER.error("Не удалось записать {}", path, exception);
            return false;
        }
    }

    private static <T> T read(Path path, Class<T> type) {
        if (!Files.isRegularFile(path)) {
            return null;
        }
        try {
            return GSON.fromJson(Files.readString(path), type);
        } catch (IOException | JsonParseException exception) {
            DraxonClient.LOGGER.error("Не удалось прочитать {}", path, exception);
            return null;
        }
    }

    /** Данные, которые сохраняются между перезапусками. */
    public static final class Settings {
        /** Имя темы из {@link client.gui.theme.Theme}. */
        public String theme = "draxon";
        /** Код языка интерфейса (ru_ru, en_us, de_de, es_es). */
        public String language = "en_us";
        /** Код клавиши открытия ClickGUI (по умолчанию Right Shift). */
        public int clickGuiKey = GLFW.GLFW_KEY_RIGHT_SHIFT;
        /** Сторона Armor HUD: "left" или "right". */
        public String armorHudSide = "right";
        /** Показывать ли водяной знак (D + FPS). */
        public boolean watermarkVisible = true;
        /** Показывать ли собственные уведомления. */
        public boolean notificationVisible = true;
        /** Позиции панелей в долях экрана: id категории -> {x, y}. */
        public Map<String, double[]> panelPositions = new HashMap<>();
        /** Состояния модулей: имя модуля -> включён. */
        public Map<String, Boolean> modules = new HashMap<>();
    }

    /** Содержимое профиля конфигурации. */
    public static final class Profile {
        public String theme = "draxon";
        public String language = "en_us";
        public int clickGuiKey = GLFW.GLFW_KEY_RIGHT_SHIFT;
        public Map<String, Boolean> modules = new HashMap<>();
    }
}
