package client.hud;

import client.config.ConfigManager;
import client.gui.theme.Theme;
import client.render.ColorUtils;
import client.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Вертикальный HUD брони: шлем, нагрудник, штаны и ботинки столбиком у выбранного края
 * экрана. Пустые слоты пропускаются, поэтому плашка не растягивается зря.
 * Сторона (лево/право) выбирается в настройках модуля Armor HUD.
 */
public final class ArmorHud {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final int SLOT_SIZE = 18;
    private static final int GAP = 2;
    private static final int MARGIN = 4;

    private ArmorHud() {
    }

    public static void render(GuiGraphics graphics) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null || client.options.hideGui) {
            return;
        }

        ItemStack[] worn = new ItemStack[SLOTS.length];
        int count = 0;
        for (int i = 0; i < SLOTS.length; i++) {
            ItemStack stack = player.getItemBySlot(SLOTS[i]);
            if (!stack.isEmpty()) {
                worn[count++] = stack;
            }
        }
        if (count == 0) {
            return;
        }

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        if (screenWidth <= 0 || screenHeight <= 0) {
            return;
        }
        boolean left = "left".equals(ConfigManager.settings().armorHudSide);

        // Упрощённое положение: сдвиг от босс-бара и от тотема в начале кадра.
        boolean bossBar = BossBarProbe.visible();
        int totemOffset = ConfigManager.settings().armorHudSide.equals("left") ? -14 : 0;
        int xOffset = (bossBar ? 28 : 0) + totemOffset;

        int total = count * SLOT_SIZE + (count - 1) * GAP;
        int x = left ? MARGIN + xOffset : screenWidth - SLOT_SIZE - MARGIN + xOffset;
        int y = (screenHeight - total) / 2;

        Theme theme = Theme.current();
        for (int i = 0; i < count; i++) {
            RenderUtils.fillRounded(graphics, x, y, SLOT_SIZE, SLOT_SIZE, 5,
                    ColorUtils.mulAlpha(theme.module, 0.75f));
            graphics.renderItem(worn[i], x + 1, y + 1);
            graphics.renderItemDecorations(client.font, worn[i], x + 1, y + 1);
            y += SLOT_SIZE + GAP;
        }
    }
}
