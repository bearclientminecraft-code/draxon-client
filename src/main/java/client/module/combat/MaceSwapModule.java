package client.module.combat;

import client.category.Category;
import client.gui.components.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Mace Swap (HitSwap / BreachSwap): копирует атрибуты лучшего оружия на удар,
 * меняя предмет сразу после атаки и возвращая слот обратно.
 *
 * <p>Работает с ручными атаками и с ударами TriggerBot / AutoMace: для этого
 * отслеживается сброс шкалы атаки. Топоры используются для снятия щита,
 * мечи — для эффектов вроде Fire Aspect, mace — для smash-удара.</p>
 */
public final class MaceSwapModule extends CombatModule {
    private float lastScale = 1f;
    private int returnTicks;
    private int originalSlot;
    private boolean stunPending;
    private LivingEntity target;

    public MaceSwapModule(Category category) {
        super("Mace Swap", "HitSwap weapon swap", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = baseRows();
        rows.add(toggleRow("maces", true, "combat.option.maces"));
        rows.add(toggleRow("smashOnly", false, "combat.option.smash_only"));
        rows.add(toggleRow("requireBreach", false, "combat.option.require_breach"));
        rows.add(toggleRow("requireDensity", false, "combat.option.require_density"));
        rows.add(toggleRow("axes", true, "combat.option.axes"));
        rows.add(toggleRow("swords", false, "combat.option.swords"));
        rows.add(toggleRow("stunSlam", false, "combat.option.stun_slam"));
        rows.add(toggleRow("limitToItems", false, "combat.option.limit_to_items"));
        addSlotRow(rows, "combat.option.weapon_slot");
        return rows;
    }

    @Override
    public void onTick(Minecraft client) {
        if (!CombatApi.ready()) {
            return;
        }
        if (returnTicks > 0 && --returnTicks == 0) {
            if (originalSlot >= 1) {
                CombatApi.swapToSlot(originalSlot);
            }
            originalSlot = 0;
            return;
        }
        float scale = client.player.getAttackStrengthScale(0.5f);
        boolean attacked = lastScale > 0.85f && scale < 0.3f;
        lastScale = scale;
        if (!attacked || !macroHeld()) {
            return;
        }
        if (options.getBoolean("limitToItems", false) && !holdingBoundSlot()) {
            return;
        }
        if (!(client.crosshairPickEntity instanceof LivingEntity hovered) || hovered == client.player
                || !hovered.isAlive()) {
            return;
        }
        target = hovered;
        int slot = pickWeapon(hovered);
        if (slot <= 0) {
            return;
        }
        originalSlot = CombatApi.mainHandSlot();
        if (!CombatApi.swapToSlot(slot)) {
            return;
        }
        CombatApi.attack(hovered);
        if (stunPending) {
            // Второй удар после топора — mace, как только он окажется в руке.
            int mace = maceSlot(hovered);
            if (mace > 0) {
                CombatApi.swapToSlot(mace);
                CombatApi.attack(hovered);
            }
            stunPending = false;
        }
        returnTicks = Math.max(1, options.clamped("swapBackDelay", 2, 0, 10));
        markAction();
    }

    private int pickWeapon(LivingEntity target) {
        boolean shield = CombatApi.shieldUp(target);
        if (shield && options.getBoolean("axes", true)) {
            int axe = CombatApi.hotbarSlot(CombatApi::isAxe);
            if (axe > 0) {
                stunPending = options.getBoolean("stunSlam", false);
                return axe;
            }
        }
        if (options.getBoolean("maces", true)) {
            int mace = maceSlot(target);
            if (mace > 0) {
                return mace;
            }
        }
        if (options.getBoolean("swords", false)) {
            return CombatApi.hotbarSlot(CombatApi::isSword);
        }
        return -1;
    }

    private int maceSlot(LivingEntity target) {
        if (options.getBoolean("smashOnly", false) && CombatApi.player().fallDistance < 1.5f) {
            return -1;
        }
        boolean requireBreach = options.getBoolean("requireBreach", false);
        boolean requireDensity = options.getBoolean("requireDensity", false);
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = CombatApi.player().getInventory().getItem(slot);
            if (!stack.is(Items.MACE)) {
                continue;
            }
            boolean breach = CombatApi.hasEnchant(stack, net.minecraft.world.item.enchantment.Enchantments.BREACH);
            boolean density = CombatApi.hasEnchant(stack, net.minecraft.world.item.enchantment.Enchantments.DENSITY);
            if (requireBreach && !breach) {
                continue;
            }
            if (requireDensity && !density) {
                continue;
            }
            return slot + 1;
        }
        return -1;
    }

    @Override
    protected void onDisable() {
        returnTicks = 0;
        originalSlot = 0;
        stunPending = false;
    }
}
