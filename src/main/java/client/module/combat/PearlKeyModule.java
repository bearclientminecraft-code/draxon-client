package client.module.combat;

import client.category.Category;
import client.gui.components.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Pearl Key: бросок эндер-жемчуга по Macro Key.
 *
 * <p>Жемчуг используется только по явному нажатию игрока — никаких автоматических
 * бросков. После броска слот возвращается на назначенный «Return slot» или на
 * тот, что был в руке до нажатия. Задержка возврата общая (Delay ± Random Range).</p>
 */
public final class PearlKeyModule extends CombatModule {
    private int originalSlot;
    private boolean returning;

    public PearlKeyModule(Category category) {
        super("Pearl Key", "Pearl throw on macro key", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = baseRows();
        addSlotRow(rows, "combat.option.pearl_slot");
        rows.add(new client.gui.components.SlotBindRow(t("combat.option.return_slot", "Return slot"),
                () -> options.clamped("returnSlot", 0, 0, 9),
                value -> options.setInt("returnSlot", value)));
        rows.add(toggleRow("requireRightClick", false, "combat.option.require_right_click"));
        return rows;
    }

    @Override
    public void onTick(Minecraft client) {
        if (!CombatApi.ready()) {
            return;
        }
        if (returning && gateOpen()) {
            returning = false;
            int target = options.clamped("returnSlot", 0, 0, 9);
            int slot = target > 0 ? target : originalSlot;
            if (slot >= 1 && slot <= 9) {
                CombatApi.swapToSlot(slot);
            }
            return;
        }
        if (returning || !macroJustPressed()) {
            return;
        }
        if (options.getBoolean("requireRightClick", false) && !client.options.keyUse.isDown()) {
            return;
        }
        int pearlSlot = pearlSlot();
        if (pearlSlot <= 0) {
            return;
        }
        originalSlot = CombatApi.mainHandSlot();
        if (!CombatApi.swapToSlot(pearlSlot)) {
            return;
        }
        if (CombatApi.useItem()) {
            returning = true;
            markAction();
        } else {
            // Жемчуг не ушёл (например, нет цели) — слот сразу возвращаем.
            int target = options.clamped("returnSlot", 0, 0, 9);
            CombatApi.swapToSlot(target > 0 ? target : originalSlot);
        }
    }

    private int pearlSlot() {
        int bound = boundSlot();
        if (bound > 0) {
            ItemStack stack = CombatApi.player().getInventory().getItem(bound - 1);
            return stack.is(Items.ENDER_PEARL) ? bound : -1;
        }
        return CombatApi.hotbarSlot(Items.ENDER_PEARL);
    }

    @Override
    protected void onDisable() {
        returning = false;
    }
}
