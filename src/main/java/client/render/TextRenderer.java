package client.render;

import client.DraxonClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.resources.Identifier;

/**
 * Текст интерфейса шрифтом Inter. Отрисовка идёт через Font-рендерер Minecraft,
 * но глифы берутся из Inter, а не из стандартного шрифта. Стиль выбирается
 * через {@link FontDescription}, описания лежат в assets/draxon/font/.
 */
public final class TextRenderer {
    private TextRenderer() {
    }

    public enum Weight {
        REGULAR("inter_regular");

        private final Identifier font;

        Weight(String id) {
            this.font = Identifier.fromNamespaceAndPath(DraxonClient.MOD_ID, id);
        }

        Identifier font() {
            return font;
        }
    }

    public static int lineHeight() {
        return Minecraft.getInstance().font.lineHeight;
    }

    public static int width(String text, Weight weight) {
        return Minecraft.getInstance().font.width(sequence(text, weight));
    }

    public static void draw(GuiGraphics graphics, String text, int x, int y, int color, Weight weight) {
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font, sequence(text, weight), x, y, color, false);
    }

    private static FormattedCharSequence sequence(String text, Weight weight) {
        Component component = Component.literal(text)
                .withStyle((Style style) -> style.withFont(new FontDescription.Resource(weight.font())));
        return component.getVisualOrderText();
    }
}
