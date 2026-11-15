package client.module.combat;

import client.category.Category;
import client.gui.components.Component;
import client.gui.components.SlotBindRow;
import client.lang.LanguageManager;
import client.notification.NotificationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * Anchor Macro — строгая последовательность по Macro Key:
 * слот якоря → ПКМ → слот светокамня → ПКМ → слот тотема → ПКМ → возврат слота.
 *
 * <p>Перед любым действием проверяются предмет в слоте и допустимость позиции:
 * якорь ставится только на верхнюю грань твёрдого блока в воздушную клетку,
 * светокамень заряжает блок под прицелом. Если предмета нет или позиция
 * недопустима — макрос останавливается и пишет причину, а не делает случайные
 * действия. Шаги выполняются строго по одному, с общей задержкой между ними.</p>
 */
public final class AnchorMacroModule extends CombatModule {
    private enum Step { ANCHOR, GLOWSTONE, TOTEM, RETURN }

    private static final long STEP_TIMEOUT_MS = 600;

    private Step step = Step.ANCHOR;
    private boolean running;
    private boolean reported;
    private int originalSlot;
    private int anchorSlot;
    private int glowstoneSlot;
    private int totemSlot;
    private long stepDeadline;

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
            stop();
            return;
        }
        if (!macroHeld()) {
            stop();
            return;
        }
        if (!running) {
            if (!macroJustPressed()) {
                return;
            }
            begin();
            return;
        }
        if (!gateOpen()) {
            return;
        }
        runStep();
    }

    // ------------------------------------------------------------- запуск

    private void begin() {
        int anchor = resolveSlot("anchorSlot", Items.RESPAWN_ANCHOR);
        if (anchor <= 0) {
            report("macro.no_item_anchor", "нужен respawn anchor в слоте");
            return;
        }
        int glowstone = resolveSlot("glowstoneSlot", Items.GLOWSTONE);
        if (glowstone <= 0) {
            report("macro.no_item_glowstone", "нужен glowstone в слоте");
            return;
        }
        if (resolveTotemSlot() < 0) {
            report("macro.no_item_totem", "нужен тотем в слоте");
            return;
        }
        int totem = resolveTotemSlot();

        anchorSlot = anchor;
        glowstoneSlot = glowstone;
        totemSlot = totem;
        originalSlot = CombatApi.mainHandSlot();
        step = Step.ANCHOR;
        running = true;
        reported = false;
        stepDeadline = System.currentTimeMillis() + STEP_TIMEOUT_MS;
    }

    /**
     * Слот предмета: назначенный вручную, иначе первый подходящий на горячей панели.
     *
     * @return слот 1..9 или -1, если предмета нет
     */
    private int resolveSlot(String key, Item item) {
        int configured = options.clamped(key, 0, 0, 9);
        if (configured > 0) {
            ItemStack stack = CombatApi.player().getInventory().getItem(configured - 1);
            return stack.is(item) ? configured : -1;
        }
        return CombatApi.hotbarSlot(item);
    }

    /** Тотем: назначенный слот, иначе горячая панель, иначе уже надетый в offhand (0 = шаг не нужен). */
    private int resolveTotemSlot() {
        if (CombatApi.hasTotemInOffhand()) {
            return 0;
        }
        int configured = options.clamped("totemSlot", 0, 0, 9);
        if (configured > 0) {
            ItemStack stack = CombatApi.player().getInventory().getItem(configured - 1);
            return stack.is(Items.TOTEM_OF_UNDYING) ? configured : -1;
        }
        return CombatApi.hotbarSlot(Items.TOTEM_OF_UNDYING);
    }

    // ------------------------------------------------------------- шаги

    private void runStep() {
        switch (step) {
            case ANCHOR -> {
                if (!validAnchorPlacement()) {
                    report("macro.bad_position", "недопустимая позиция для якоря");
                    return;
                }
                if (swapAndClick(anchorSlot)) {
                    advance(Step.GLOWSTONE);
                } else if (timedOut()) {
                    report("macro.step_failed", "якорь не установился");
                }
            }
            case GLOWSTONE -> {
                if (swapAndClick(glowstoneSlot)) {
                    advance(Step.TOTEM);
                } else if (timedOut()) {
                    report("macro.step_failed", "светокамень не применился");
                }
            }
            case TOTEM -> {
                if (totemSlot == 0 || swapAndClick(totemSlot)) {
                    advance(Step.RETURN);
                } else if (timedOut()) {
                    report("macro.step_failed", "тотем не применился");
                }
            }
            case RETURN -> {
                if (originalSlot >= 1 && originalSlot <= 9) {
                    CombatApi.swapToSlot(originalSlot);
                }
                finish();
            }
        }
    }

    private boolean timedOut() {
        return System.currentTimeMillis() > stepDeadline;
    }

    /** Переключает слот и делает ПКМ; false — действие не удалось и его надо повторить. */
    private boolean swapAndClick(int slot) {
        if (slot < 1 || slot > 9) {
            return false;
        }
        if (!CombatApi.swapToSlot(slot)) {
            return false;
        }
        return CombatApi.rightClick();
    }

    private void advance(Step nextStep) {
        step = nextStep;
        stepDeadline = System.currentTimeMillis() + STEP_TIMEOUT_MS;
        markAction();
    }

    /** Проверка позиции: ПКМ по верхней грани твёрдого блока, цель — воздух, в пределах руки. */
    private boolean validAnchorPlacement() {
        Minecraft client = Minecraft.getInstance();
        if (!(client.hitResult instanceof BlockHitResult hit)) {
            return false;
        }
        if (hit.getDirection() != Direction.UP) {
            return false;
        }
        BlockPos clicked = hit.getBlockPos();
        if (CombatApi.isAir(clicked)) {
            return false;
        }
        BlockPos target = clicked.above();
        if (!CombatApi.isAir(target)) {
            return false;
        }
        return CombatApi.distanceToVec(net.minecraft.world.phys.Vec3.atCenterOf(clicked)) <= 4.6;
    }

    // ------------------------------------------------------------- служебное

    private void report(String langKey, String fallback) {
        if (reported) {
            return;
        }
        reported = true;
        running = false;
        NotificationManager.push(LanguageManager.tr(langKey, fallback),
                client.notification.Notification.Type.ERROR);
        step = Step.ANCHOR;
    }

    private void finish() {
        step = Step.ANCHOR;
        if (options.getBoolean("repeatWhileHeld", true) && macroHeld()) {
            stepDeadline = System.currentTimeMillis() + STEP_TIMEOUT_MS;
            markAction();
            return;
        }
        running = false;
    }

    private void stop() {
        running = false;
        reported = false;
        step = Step.ANCHOR;
    }

    @Override
    protected void onDisable() {
        stop();
    }
}
