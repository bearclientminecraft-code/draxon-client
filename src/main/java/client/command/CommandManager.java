package client.command;

import client.config.ConfigManager;
import client.lang.LanguageManager;
import client.notification.Notification;
import client.notification.NotificationManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;

import java.util.List;
import java.util.Locale;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/**
 * Команды Draxon. Работают в двух видах: через чат с точкой
 * ({@code .config save pvp}) и как обычная клиентская команда
 * ({@code /draxon config save pvp}). Оба пути ведут в один обработчик.
 */
public final class CommandManager {
    private static final String PREFIX = ".";

    private CommandManager() {
    }

    public static void init() {
        ClientSendMessageEvents.ALLOW_CHAT.register(CommandManager::onChat);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> register(dispatcher));
    }

    /** Перехват сообщения чата: строки с точкой не уходят на сервер. */
    private static boolean onChat(String message) {
        if (!message.startsWith(PREFIX) || message.length() < 2) {
            return true;
        }
        String[] args = message.substring(1).trim().split("\\s+");
        if (args.length == 0 || !args[0].equalsIgnoreCase("config")) {
            return true;
        }
        handle(args);
        return false;
    }

    private static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("draxon")
                .then(literal("config")
                        .then(literal("save")
                                .then(argument("name", StringArgumentType.word())
                                        .executes(context -> runAction("save",
                                                StringArgumentType.getString(context, "name")))))
                        .then(literal("load")
                                .then(argument("name", StringArgumentType.word())
                                        .executes(context -> runAction("load",
                                                StringArgumentType.getString(context, "name")))))
                        .then(literal("del")
                                .then(argument("name", StringArgumentType.word())
                                        .executes(context -> runAction("del",
                                                StringArgumentType.getString(context, "name")))))
                        .then(literal("list").executes(context -> runAction("list", null)))));
    }

    private static int runAction(String action, String name) {
        run(action, name);
        return 1;
    }

    private static void handle(String[] args) {
        if (args.length < 2) {
            usage();
            return;
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        String name = args.length > 2 ? args[2] : null;
        if (name == null && !action.equals("list")) {
            usage();
            return;
        }
        run(action, name);
    }

    private static void run(String action, String name) {
        switch (action.toLowerCase(Locale.ROOT)) {
            case "save" -> report(ConfigManager.saveProfile(name), "saved", name);
            case "load" -> report(ConfigManager.loadProfile(name), "loaded", name);
            case "del", "delete", "remove" -> report(ConfigManager.deleteProfile(name), "deleted", name);
            case "list" -> {
                List<String> profiles = ConfigManager.profiles();
                NotificationManager.push(LanguageManager.tr("command.config.list", "Configs") + ": "
                                + (profiles.isEmpty()
                                ? LanguageManager.tr("command.config.empty", "empty")
                                : String.join(", ", profiles)),
                        Notification.Type.INFO);
            }
            default -> usage();
        }
    }

    private static void report(ConfigManager.Result result, String okKey, String name) {
        switch (result) {
            case OK -> NotificationManager.push(
                    LanguageManager.tr("command.config." + okKey, okKey) + ": " + name, Notification.Type.INFO);
            case INVALID_NAME -> NotificationManager.push(
                    LanguageManager.tr("command.config.invalid", "Invalid config name"), Notification.Type.ERROR);
            case MISSING -> NotificationManager.push(
                    LanguageManager.tr("command.config.missing", "Config not found") + ": " + name,
                    Notification.Type.ERROR);
            case ERROR -> NotificationManager.push(
                    LanguageManager.tr("command.config.error", "Config error"), Notification.Type.ERROR);
        }
    }

    private static void usage() {
        NotificationManager.push(LanguageManager.tr("command.config.usage",
                ".config save|load|del <name>"), Notification.Type.INFO);
    }
}
