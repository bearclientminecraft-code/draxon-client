package client.module.combat;

import client.category.Category;
import client.gui.components.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Hover Totem: «hover-only» вариант авто-тотема — экипирует тотем только когда
 * крестик реально наведён на противника (а не постоянно), чтобы убрать лишние
 * действия при записи CPvP-контента.
 *
 * <p>Наведение курсора на слот тотема в открытом инвентаре недоступно через
 * публичный API клиента (координаты слотов не отдаются), поэтому здесь
 * используется наведение крестика на цель; сам тотем берётся по назначенному слоту.</p>
 */
public final class HoverTotemModule extends CombatModule {
    public HoverTotemModule(Category category) {
        super("Hover Totem", "Equip totem while hovering a target", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = baseRows();
        rows.add(toggleRow("requireWeapon", false, "combat.option.require_weapon"));
        rows.add(intSlider("range", 40, 10, 60, 5, "", "combat.option.range"));
        rows.add(toggleRow("showCount", true, "combat.option.show_count"));
        addSlotRow(rows, "combat.option.totem_slot");
        return rows;
    }

    public boolean showCount() {
        return options.getBoolean("showCount", true);
    }

    @Override
    public void onTick(Minecraft client) {
        if (!CombatApi.ready() || !gateOpen()) {
            return;
        }
        if (!hoveringTarget()) {
            return;
        }
        if (CombatApi.hasTotemInOffhand()) {
            return;
        }
        int slot = options.clamped("slot", 0, 0, 9);
        int totemSlot = slot > 0 ? slot : CombatApi.hotbarSlot(Items.TOTEM_OF_UNDYING);
        if (totemSlot <= 0) {
            return;
        }
        ItemStack stack = client.player.getInventory().getItem(totemSlot - 1);
        if (!stack.is(Items.TOTEM_OF_UNDYING)) {
            return;
        }
        // Тотем с горячей панели меняем в offhand через swap-клик.
        int inventoryIndex = totemSlot - 1;
        if (CombatApi.moveToOffhand(inventoryIndex)) {
            markAction();
        }
    }

    private boolean hoveringTarget() {
        if (!(CombatApi.mc().crosshairPickEntity instanceof net.minecraft.world.entity.player.Player hovered)) {
            return false;
        }
        if (hovered == CombatApi.player() || !hovered.isAlive()) {
            return false;
        }
        if (CombatApi.distanceTo(hovered) > options.clamped("range", 40, 10, 60) / 10.0) {
            return false;
        }
        if (options.getBoolean("requireWeapon", false)) {
            ItemStack held = CombatApi.heldItem();
            return CombatApi.isSword(held) || CombatApi.isAxe(held) || CombatApi.isMace(held);
        }
        return true;
    }
}
