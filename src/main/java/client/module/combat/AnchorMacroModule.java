package client.module.combat;

import client.category.Category;
import client.gui.components.Component;
import client.gui.components.SlotBindRow;
import net.minecraft.client.Minecraft;

import java.util.List;

/**
 * Anchor Macro — простой макрос по Macro Key:
 * слот якоря → ПКМ → слот светокамня → ПКМ → слот тотема → ПКМ.
 *
 * <p>Каждый шаг ждёт общую задержку (Delay ± Random Range), поэтому получается
 * обычная макрос-цепочка, а не автоматический расчёт позиций. По настройке
 * цепочка повторяется, пока клавиша зажата.</p>
 */
public final class AnchorMacroModule extends CombatModule {
    private enum Step { ANCHOR, GLOWSTONE, TOTEM }

    private Step step = Step.ANCHOR;
    private boolean running;

    public AnchorMacroModule(Category category) {
        super("Anchor Macro", "Anchor, glowstone and totem macro", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = baseRows();
        rows.add(new SlotBindRow(t("combat.option.anchor_slot", "Anchor slot"),
                () -> options.clamped("anchorSlot", 0, 0, 9),
                value -> options.setInt("anchorSlot", value)));
        rows.add(new SlotBindRow(t("combat.option.glowstone_slot", "Glowstone slot"),
                () -> options.clamped("glowstoneSlot", 0, 0, 9),
                value -> options.setInt("glowstoneSlot", value)));
        rows.add(new SlotBindRow(t("combat.option.totem_slot", "Totem slot"),
                () -> options.clamped("totemSlot", 0, 0, 9),
                value -> options.setInt("totemSlot", value)));
        rows.add(toggleRow("repeatWhileHeld", true, "combat.option.repeat_while_held"));
        return rows;
    }

    @Override
    public void onTick(Minecraft client) {
        if (!CombatApi.ready()) {
            return;
        }
        if (!macroHeld()) {
            running = false;
            return;
        }
        if (!running) {
            if (!macroJustPressed()) {
                return;
            }
            running = true;
            step = Step.ANCHOR;
        }
        if (!gateOpen()) {
            return;
        }
        step();
    }

    private void step() {
        int slot = switch (step) {
            case ANCHOR -> options.clamped("anchorSlot", 0, 0, 9);
            case GLOWSTONE -> options.clamped("glowstoneSlot", 0, 0, 9);
            case TOTEM -> options.clamped("totemSlot", 0, 0, 9);
        };
        if (slot < 1) {
            // Слот не назначен — шаг пропускаем, чтобы цепочка не застревала.
            advance();
            return;
        }
        if (!CombatApi.swapToSlot(slot)) {
            return;
        }
        // Правая кнопка мыши: используем предмет в руке.
        CombatApi.useItem();
        markAction();
        advance();
    }

    private void advance() {
        switch (step) {
            case ANCHOR -> step = Step.GLOWSTONE;
            case GLOWSTONE -> step = Step.TOTEM;
            case TOTEM -> {
                step = Step.ANCHOR;
                if (options.getBoolean("repeatWhileHeld", true) && macroHeld()) {
                    markAction();
                } else {
                    running = false;
                }
            }
        }
    }

    @Override
    protected void onDisable() {
        running = false;
        step = Step.ANCHOR;
    }
}
