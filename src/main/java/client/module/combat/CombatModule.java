package client.module.combat;

import client.category.Category;
import client.gui.components.ChoiceRow;
import client.gui.components.Component;
import client.gui.components.KeybindRow;
import client.gui.components.SliderRow;
import client.gui.components.SlotBindRow;
import client.gui.components.ToggleRow;
import client.lang.LanguageManager;
import client.module.Module;
import client.notification.NotificationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Базовый боевой модуль: единая Macro Key, назначение слота, задержка 0–200 мс
 * со слайдером и рандомизацией 0–20 мс, плюс общие хелперы действий.
 *
 * <p>Настройки хранятся в {@link CombatOptions} и сохраняются в конфигурации,
 * поэтому переживают перезапуск клиента.</p>
 */
public abstract class CombatModule extends Module {
    protected final CombatOptions options;
    private long nextActionAt;

    protected CombatModule(String name, String description, Category category) {
        super(name, description, category);
        this.options = CombatOptions.of(name);
    }

    // ------------------------------------------------------------ настройки

    /** Слот 1..9 или 0 = None (единое назначение слота). */
    public int boundSlot() {
        return options.clamped("slot", 0, 0, 9);
    }

    /** Общая клавиша макроса; 0 = None (модуль работает автоматически). */
    public int macroKey() {
        return options.clamped("macroKey", 0, 0, 400);
    }

    public int delayMs() {
        return options.clamped("delay", 0, 0, 200);
    }

    public boolean randomDelay() {
        return options.getBoolean("randomDelay", false);
    }

    /** Разброс задержки в миллисекундах (0–20). */
    public int randomRange() {
        return options.clamped("randomRange", 0, 0, 20);
    }

    /** Задержка с учётом рандомизации: Delay ± Random Range. */
    public int effectiveDelayMs() {
        int base = delayMs();
        int range = randomRange();
        if (randomDelay() && range > 0) {
            base += (int) Math.round((Math.random() * 2.0 - 1.0) * range);
        }
        return Math.max(0, base);
    }

    protected boolean gateOpen() {
        return System.currentTimeMillis() >= nextActionAt;
    }

    protected void markAction() {
        nextActionAt = System.currentTimeMillis() + effectiveDelayMs();
    }

    protected void resetGate() {
        nextActionAt = 0;
    }

    /**
     * Активен ли макрос: без назначенной клавиши модуль работает автоматически,
     * с клавишей — пока она нажата или в течение короткого окна после нажатия,
     * чтобы цепочка не обрывалась на быстром тапе.
     */
    protected boolean macroHeld() {
        int key = macroKey();
        if (key <= 0) {
            return true;
        }
        if (MacroKeys.isDown(key)) {
            return true;
        }
        long last = MacroKeys.lastPress(key);
        return last > 0 && System.currentTimeMillis() - last <= MacroKeys.ENGAGE_WINDOW_MS;
    }

    /**
     * Фронт нажатия Macro Key. Нажатие забирается только один раз, поэтому
     * цепочка не запускается повторно, а модуль получает его, даже если в момент
     * нажатия не было цели или нужного состояния.
     */
    protected boolean macroJustPressed() {
        int key = macroKey();
        if (key <= 0) {
            return true;
        }
        return MacroKeys.consumePress(key, MacroKeys.PRESS_WINDOW_MS);
    }

    /** Отклик на нажатие Macro Key: подтверждает, что макрос получил клавишу. */
    public void onMacroPressed() {
        if (options.getBoolean("macroFeedback", true)) {
            NotificationManager.push(LanguageManager.moduleName(this) + " · "
                    + t("macro.triggered", "macro"), client.notification.Notification.Type.INFO);
        }
    }

    /** Принудительно завершает макрос: снимает окно активности и состояние цепочки. */
    protected void endMacro() {
        int key = macroKey();
        if (key > 0) {
            MacroKeys.consumePress(key, Long.MAX_VALUE);
        }
    }

    protected boolean holdingBoundSlot() {
        int slot = boundSlot();
        return slot == 0 || CombatApi.mainHandSlot() == slot;
    }

    // ------------------------------------------------------------ строки UI

    protected String t(String key, String fallback) {
        return LanguageManager.tr(key, fallback);
    }

    /** Стандартный блок настроек: Macro Key, Delay, Random Delay, Random Range. */
    protected List<Component> baseRows() {
        List<Component> rows = new ArrayList<>();
        rows.add(new KeybindRow(t("combat.option.macro_key", "Macro key"), this::macroKey,
                value -> options.setInt("macroKey", value)));
        rows.add(new ToggleRow(t("combat.option.silent_aim", "Silent aim"), this::silentAim,
                value -> options.setBoolean("silentAim", value)));
        rows.add(new ToggleRow(t("combat.option.macro_feedback", "Macro feedback"),
                () -> options.getBoolean("macroFeedback", true),
                value -> options.setBoolean("macroFeedback", value)));
        rows.add(new SliderRow(t("combat.option.delay", "Delay"), 0, 200, 5, " ms",
                this::delayMs, value -> options.setInt("delay", value)));
        rows.add(new ToggleRow(t("combat.option.random_delay", "Random delay"), this::randomDelay,
                value -> options.setBoolean("randomDelay", value)));
        rows.add(new SliderRow(t("combat.option.random_range", "Random range"), 0, 20, 1, " ms",
                this::randomRange, value -> options.setInt("randomRange", value)));
        return rows;
    }

    protected void addSlotRow(List<Component> rows, String labelKey) {
        rows.add(new SlotBindRow(t(labelKey, "Slot"), this::boundSlot,
                value -> options.setInt("slot", value)));
    }

    protected ToggleRow toggleRow(String key, boolean fallback, String langKey) {
        return new ToggleRow(t(langKey, key), () -> options.getBoolean(key, fallback),
                value -> options.setBoolean(key, value));
    }

    /** Строка выбора значений; подписи берутся из lang по префиксу + значение. */
    protected ChoiceRow choiceRow(String key, String fallback, String langPrefix, String... values) {
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = t(langPrefix + values[i], values[i]);
        }
        String label = t("combat.option." + key, key);
        return new ChoiceRow(label, values, labels,
                () -> options.get(key, fallback),
                value -> options.set(key, value));
    }

    protected SliderRow intSlider(String key, int fallback, int min, int max, int step, String suffix,
                                  String langKey) {
        return new SliderRow(t(langKey, key), min, max, step, suffix,
                () -> options.clamped(key, fallback, min, max),
                value -> options.setInt(key, value));
    }

    /** Мгновенный (не дожидающийся задержки) сброс после ручного вмешательства. */
    protected void cancelPending() {
        resetGate();
    }

    // ------------------------------------------------------------ silent aim

    /** Silent Aim включён по умолчанию: удар/постановка наводятся только на сервере. */
    public boolean silentAim() {
        return options.getBoolean("silentAim", true);
    }

    private boolean silentPending;

    /** Наводится на точку: silently (без поворота камеры) или плавно вручную. */
    protected void aim(net.minecraft.world.phys.Vec3 point, String speedKey, int fallbackSpeed) {
        if (point == null) {
            return;
        }
        if (silentAim()) {
            silentPending = SilentAim.begin(point);
        } else {
            CombatApi.aimAt(point, options.clamped(speedKey, fallbackSpeed, 5, 90));
        }
    }

    protected void aim(net.minecraft.world.phys.Vec3 point) {
        aim(point, "aimSpeed", 40);
    }

    protected void aim(net.minecraft.world.entity.Entity entity, String speedKey, int fallbackSpeed) {
        if (entity != null) {
            aim(entity.position().add(0, entity.getBbHeight() * 0.5, 0), speedKey, fallbackSpeed);
        }
    }

    /** Возвращает исходную ротацию после действий тика. */
    @Override
    public void afterTick(net.minecraft.client.Minecraft client) {
        if (silentPending) {
            SilentAim.end();
            silentPending = false;
        }
    }
}
