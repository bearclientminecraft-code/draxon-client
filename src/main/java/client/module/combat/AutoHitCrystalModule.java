package client.module.combat;

import client.category.Category;
import client.gui.components.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;

import java.util.List;

/**
 * Auto Hit Crystal — простой макрос: держит слот кристалла, а при нажатии правой
 * кнопки мыши сразу нажимает левую (по кристаллу/сущности под прицелом).
 *
 * <p>Никакого наведения нет: действие происходит туда, куда смотрит игрок.
 * Один ПКМ — один ЛКМ, если не включён повтор при удержании.</p>
 */
public final class AutoHitCrystalModule extends CombatModule {
    private boolean useWasDown;

    public AutoHitCrystalModule(Category category) {
        super("Auto Hit Crystal", "Crystal slot and auto left click", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = baseRows();
        addSlotRow(rows, "combat.option.crystal_slot");
        // По умолчанию ЛКМ повторяется, пока держишь ПКМ: иначе после первого
        // кристалла в серии модуль выглядит «выключенным».
        rows.add(toggleRow("holdRepeat", true, "combat.option.repeat_while_held"));
        return rows;
    }

    @Override
    protected void onEnable() {
        resetTransientState();
        resetGate();
    }

    @Override
    protected void onDisable() {
        resetTransientState();
    }

    @Override
    public void resetTransientState() {
        useWasDown = false;
    }

    @Override
    public void onTick(Minecraft client) {
        if (!CombatApi.ready() || !macroHeld()) {
            resetTransientState();
            return;
        }
        // Слот кристалла держим всегда, чтобы ПКМ ставил именно кристалл.
        int slot = boundSlot();
        if (slot > 0 && CombatApi.mainHandSlot() != slot) {
            CombatApi.swapToSlot(slot);
        }

        boolean useDown = client.options.keyUse.isDown();
        boolean justPressed = useDown && !useWasDown;
        useWasDown = useDown;
        boolean holdRepeat = options.getBoolean("holdRepeat", true);
        if (!useDown || (!justPressed && !holdRepeat)) {
            return;
        }
        if (!gateOpen()) {
            return;
        }
        Entity target = crystalTarget(client);
        if (target != null && CombatApi.leftClick(target)) {
            markAction();
        }
    }

    /** Кристалл в крестике, иначе сущность под прицелом, иначе ближайший кристалл. */
    private Entity crystalTarget(Minecraft client) {
        if (client.crosshairPickEntity instanceof EndCrystal crystal && crystal.isAlive()) {
            return crystal;
        }
        if (client.crosshairPickEntity instanceof net.minecraft.world.entity.LivingEntity living
                && living != client.player && living.isAlive()) {
            return living;
        }
        EndCrystal nearest = null;
        double best = 4.5;
        for (EndCrystal crystal : client.level.getEntitiesOfClass(EndCrystal.class,
                client.player.getBoundingBox().inflate(4.5), EndCrystal::isAlive)) {
            double distance = CombatApi.distanceTo(crystal);
            if (distance < best) {
                best = distance;
                nearest = crystal;
            }
        }
        return nearest;
    }
}
