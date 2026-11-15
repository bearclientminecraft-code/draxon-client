package client.module.combat;

import client.category.Category;
import client.gui.components.Component;
import client.module.Module;
import client.module.ModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * TriggerBot: кликает сам, когда крестик висит на валидной цели.
 *
 * <p>Учитывает Mouse Over Delay, Extra Delay (в тиках, может быть отрицательным),
 * Air Crits, Require Mouse Down и Shield Check. Проверка щита обходится, если
 * включён ShieldDisabler, HitSwap с топорами или AutoMace со Stun Slam и в
 * горячей панели есть топор.</p>
 */
public final class TriggerBotModule extends CombatModule {
    private long hoveredSince;
    private LivingEntity lastHovered;
    private boolean ignoredActivationClick;

    public TriggerBotModule(Category category) {
        super("TriggerBot", "Auto attack on crosshair target", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = baseRows();
        rows.add(intSlider("extraDelay", 0, -10, 20, 1, " t", "combat.option.extra_delay"));
        rows.add(intSlider("mouseOverDelay", 0, 0, 500, 10, " ms", "combat.option.mouse_over_delay"));
        rows.add(toggleRow("requireMouseDown", false, "combat.option.require_mouse"));
        rows.add(toggleRow("ignoreActivationClick", true, "combat.option.ignore_activation_click"));
        rows.add(toggleRow("airCrits", false, "combat.option.air_crits"));
        rows.add(toggleRow("shieldCheck", true, "combat.option.shield_check"));
        rows.add(toggleRow("selectFirstHit", false, "combat.option.select_first_hit"));
        rows.add(intSlider("missChance", 0, 0, 100, 5, "%", "combat.option.target_miss_chance"));
        rows.add(intSlider("earlyChance", 0, 0, 100, 5, "%", "combat.option.early_hit_chance"));
        rows.add(toggleRow("limitToItems", false, "combat.option.limit_to_items"));
        addSlotRow(rows, "combat.option.weapon_slot");
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
        if (options.getBoolean("requireMouseDown", false) && !client.options.keyAttack.isDown()) {
            return;
        }
        if (!(client.crosshairPickEntity instanceof LivingEntity hovered) || hovered == client.player
                || !hovered.isAlive()) {
            resetHover();
            return;
        }
        if (hovered instanceof Player player && options.getBoolean("shieldCheck", true)
                && CombatApi.shieldUp(player) && !axeBypassAvailable()) {
            return;
        }
        if (options.getBoolean("selectFirstHit", false) && client.player.hurtTime <= 0
                && client.player.getLastHurtByMob() != hovered) {
            return;
        }
        if (options.getBoolean("airCrits", false) && !client.player.onGround()
                && client.player.getDeltaMovement().y >= 0.0) {
            return;
        }
        if (hovered != lastHovered) {
            lastHovered = hovered;
            hoveredSince = System.currentTimeMillis();
            ignoredActivationClick = false;
        }
        long required = options.clamped("mouseOverDelay", 0, 0, 500);
        if (System.currentTimeMillis() - hoveredSince < required) {
            return;
        }
        if (!gateOpen()) {
            return;
        }
        if (options.getBoolean("ignoreActivationClick", true) && !ignoredActivationClick
                && client.options.keyAttack.isDown()) {
            ignoredActivationClick = true;
            return;
        }
        int early = options.clamped("earlyChance", 0, 0, 100);
        boolean earlyHit = early > 0 && Math.random() * 100 < early;
        int miss = options.clamped("missChance", 0, 0, 100);
        if (miss > 0 && Math.random() * 100 < miss) {
            markAction();
            return;
        }
        int extra = options.clamped("extraDelay", 0, -10, 20);
        if (extra > 0 && System.currentTimeMillis() - hoveredSince < extra * 50L) {
            return;
        }
        boolean smash = client.player.fallDistance >= 1.5f && CombatApi.heldItem().is(net.minecraft.world.item.Items.MACE);
        if (smash || earlyHit || CombatApi.attackReady()) {
            if (CombatApi.attack(hovered)) {
                markAction();
            }
        }
    }

    private void resetHover() {
        lastHovered = null;
        hoveredSince = 0;
        ignoredActivationClick = false;
    }

    /** Есть ли топор у модулей, которые ломают щит (обход Shield Check). */
    private boolean axeBypassAvailable() {
        if (CombatApi.hotbarSlot(CombatApi::isAxe) <= 0) {
            return false;
        }
        boolean shieldBreaker = enabled("ShieldDisabler");
        boolean hitSwap = enabled("Mace Swap");
        boolean stunSlam = enabled("Auto Mace")
                && ModuleRegistry.byName("Auto Mace")
                        .map(module -> module instanceof AutoMaceModule mace && mace.options.getBoolean("stunSlam", false))
                        .orElse(false);
        return shieldBreaker || hitSwap || stunSlam;
    }

    private boolean enabled(String name) {
        return ModuleRegistry.byName(name).map(Module::isEnabled).orElse(false);
    }
}
