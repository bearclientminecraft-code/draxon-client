package client.module.combat;

import client.category.Category;
import client.gui.components.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Auto Mace: держит mace в руке и бьёт им, пока игрок падает (mace smash).
 *
 * <p>В воздухе модуль ищет цель под собой, при Auto Unequip Elytra меняет элитры
 * на нагрудник из горячей панели, чтобы началось падение, и возвращает элитры
 * после отскока. Выбор mace — Manual (Density/Breach) или Auto (по броне цели
 * и высоте падения).</p>
 */
public final class AutoMaceModule extends CombatModule {
    private long unequippedAt;
    private boolean elytraRemoved;

    public AutoMaceModule(Category category) {
        super("Auto Mace", "Mace smash automation", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = baseRows();
        rows.add(intSlider("aimRange", 40, 10, 60, 5, "", "combat.option.aim_range"));
        rows.add(intSlider("aimSpeed", 30, 5, 90, 5, "", "combat.option.aim_speed"));
        rows.add(intSlider("extraDelay", 0, -10, 20, 1, " t", "combat.option.extra_delay"));
        rows.add(choiceRow("maceSelection", "auto", "mace.select.", "manual", "auto"));
        rows.add(choiceRow("maceType", "density", "mace.type.", "density", "breach"));
        rows.add(toggleRow("smashOnly", true, "combat.option.smash_only"));
        rows.add(toggleRow("autoUnequipElytra", false, "combat.option.auto_unequip_elytra"));
        rows.add(toggleRow("reEquipElytra", true, "combat.option.re_equip_elytra"));
        rows.add(toggleRow("stunSlam", false, "combat.option.stun_slam"));
        rows.add(intSlider("stunChance", 50, 0, 100, 5, "%", "combat.option.stun_chance"));
        rows.add(toggleRow("limitToItems", false, "combat.option.limit_to_items"));
        addSlotRow(rows, "combat.option.mace_slot");
        return rows;
    }

    @Override
    public void onTick(Minecraft client) {
        if (!CombatApi.ready() || !macroHeld()) {
            return;
        }
        if (options.getBoolean("limitToItems", false) && !holdingBoundSlot()) {
            return;
        }
        Player target = CombatApi.nearestEnemy(options.clamped("aimRange", 40, 10, 60) / 10.0);
        if (target == null) {
            reEquipElytra();
            return;
        }
        handleElytra(target);
        if (!falling()) {
            return;
        }
        if (options.getBoolean("smashOnly", true) && client.player.fallDistance < 1.5f) {
            return;
        }
        if (!gateOpen()) {
            return;
        }
        int slot = maceSlot(target);
        if (slot < 0) {
            return;
        }
        if (!CombatApi.swapToSlot(slot)) {
            return;
        }
        aim(target, "aimSpeed", 30);
        int extra = options.clamped("extraDelay", 0, -10, 20);
        boolean smash = client.player.fallDistance >= 1.5f;
        long wait = extra > 0 ? extra * 50L : 0;
        if (wait > 0 && System.currentTimeMillis() - unequippedAt < wait) {
            return;
        }
        if (smash || CombatApi.attackReady()) {
            if (CombatApi.attack(target)) {
                markAction();
            }
        }
    }

    private boolean falling() {
        return CombatApi.player() != null && !CombatApi.player().onGround()
                && CombatApi.player().getDeltaMovement().y <= 0.0;
    }

    private void handleElytra(Player target) {
        Minecraft client = CombatApi.mc();
        if (!options.getBoolean("autoUnequipElytra", false) || !client.player.isFallFlying()) {
            return;
        }
        // Меняем элитры на нагрудник, только если цель ниже и падение даст smash.
        if (target.getY() > client.player.getY() - 1.0) {
            return;
        }
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = client.player.getInventory().getItem(slot);
            if (CombatApi.isChestplate(stack)) {
                if (CombatApi.equipChestFromHotbar(slot)) {
                    elytraRemoved = true;
                    unequippedAt = System.currentTimeMillis();
                }
                return;
            }
        }
    }

    private void reEquipElytra() {
        if (!elytraRemoved || !options.getBoolean("reEquipElytra", true)) {
            return;
        }
        Minecraft client = CombatApi.mc();
        if (client.player == null || client.player.getDeltaMovement().y <= 0.0) {
            return;
        }
        for (int slot = 0; slot < 9; slot++) {
            if (client.player.getInventory().getItem(slot).is(Items.ELYTRA)) {
                if (CombatApi.equipChestFromHotbar(slot)) {
                    elytraRemoved = false;
                }
                return;
            }
        }
        elytraRemoved = false;
    }

    /** Слот mace на горячей панели с учётом Manual/Auto выбора. */
    private int maceSlot(Player target) {
        boolean manual = "manual".equals(options.get("maceSelection", "auto"));
        boolean breach = "breach".equals(options.get("maceType", "density"));
        boolean wantBreach = manual ? breach : target.getArmorValue() >= 12;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = CombatApi.player().getInventory().getItem(slot);
            if (!CombatApi.isMace(stack)) {
                continue;
            }
            boolean hasDensity = CombatApi.hasEnchant(stack, net.minecraft.world.item.enchantment.Enchantments.DENSITY);
            boolean hasBreach = CombatApi.hasEnchant(stack, net.minecraft.world.item.enchantment.Enchantments.BREACH);
            if (hasDensity || hasBreach) {
                if (wantBreach == hasBreach) {
                    return slot + 1;
                }
            }
        }
        int fallback = CombatApi.hotbarSlot(Items.MACE);
        if (fallback < 0) {
            return -1;
        }
        int bound = boundSlot();
        return bound > 0 && CombatApi.player().getInventory().getItem(bound - 1).is(Items.MACE) ? bound : fallback;
    }
}
