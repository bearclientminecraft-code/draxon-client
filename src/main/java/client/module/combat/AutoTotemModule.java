package client.module.combat;

import client.category.Category;
import client.gui.components.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Auto Inventory Totem: держит totem of undying в offhand.
 *
 * <p>Обмен идёт SWAP-кликом в собственный инвентарь (кнопка 40 = offhand), поэтому
 * в большинстве случаев экран инвентаря не нужен. Режим «Inventory Only»
 * ограничивает работу открытым инвентарём, «Combat Only» — недавней боевой
 * опасностью, «Activation Delay» задаёт, сколько эта опасность должна длиться.</p>
 */
public final class AutoTotemModule extends CombatModule {
    private long dangerSince;

    public AutoTotemModule(Category category) {
        super("Auto Inventory Totem", "Totem of undying in offhand", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = baseRows();
        rows.add(toggleRow("combatOnly", true, "combat.option.combat_only"));
        rows.add(intSlider("activationDelay", 0, 0, 1000, 50, " ms", "combat.option.activation_delay"));
        rows.add(toggleRow("inventoryOnly", false, "combat.option.inventory_only"));
        rows.add(toggleRow("openInventory", false, "combat.option.open_inventory"));
        rows.add(toggleRow("silentOpen", true, "combat.option.silent_open"));
        rows.add(toggleRow("closeInventory", true, "combat.option.close_inventory"));
        rows.add(toggleRow("refill", true, "combat.option.refill"));
        rows.add(toggleRow("randomSlot", false, "combat.option.random_slot"));
        rows.add(toggleRow("showCount", true, "combat.option.show_count"));
        return rows;
    }

    public boolean showCount() {
        return options.getBoolean("showCount", true);
    }

    @Override
    public void onTick(Minecraft client) {
        if (!CombatApi.ready()) {
            return;
        }
        if (!dangerActive()) {
            dangerSince = 0;
            return;
        }
        if (options.getBoolean("inventoryOnly", false) && !CombatApi.inventoryOpen()) {
            return;
        }
        if (!gateOpen()) {
            return;
        }
        int slot = findTotem();
        if (slot < 0) {
            if (CombatApi.hasTotemInOffhand()) {
                return;
            }
            if (options.getBoolean("openInventory", false) && !CombatApi.inventoryOpen()) {
                CombatApi.openInventory();
                markAction();
            }
            return;
        }
        if (!options.getBoolean("silentOpen", true) && !CombatApi.inventoryOpen()) {
            CombatApi.openInventory();
        }
        if (CombatApi.moveToOffhand(slot)) {
            markAction();
            if (options.getBoolean("closeInventory", true) && CombatApi.inventoryOpen()) {
                client.setScreen(null);
            }
        }
    }

    /** Индекс тотема в инвентаре (0..35), с учётом Random Slot. */
    private int findTotem() {
        if (CombatApi.player() == null) {
            return -1;
        }
        List<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = CombatApi.player().getInventory().getItem(slot);
            if (stack.is(Items.TOTEM_OF_UNDYING)) {
                slots.add(slot);
            }
        }
        if (slots.isEmpty()) {
            return -1;
        }
        if (CombatApi.hasTotemInOffhand()) {
            return -1;
        }
        if (options.getBoolean("randomSlot", false)) {
            return slots.get((int) (Math.random() * slots.size()));
        }
        return slots.get(0);
    }

    /** Свежая боевая опасность: недавний урон или противник рядом. */
    private boolean dangerActive() {
        boolean combatOnly = options.getBoolean("combatOnly", true);
        boolean hurt = CombatApi.player().hurtTime > 0
                || !CombatApi.enemies(6.0).isEmpty();
        if (!combatOnly) {
            return true;
        }
        if (!hurt) {
            return false;
        }
        if (dangerSince == 0) {
            dangerSince = System.currentTimeMillis();
        }
        long required = options.clamped("activationDelay", 0, 0, 1000);
        return System.currentTimeMillis() - dangerSince >= required;
    }
}
