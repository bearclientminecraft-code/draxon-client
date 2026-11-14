package client.render;

import client.DraxonClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Текст интерфейса шрифтами Draxon. Отрисовка идёт через Font-рендерер Minecraft,
 * но глифы берутся из наших файлов в assets/draxon/font: Inter (400/500/600)
 * для интерфейса и Soopafre для буквы-логотипа в водяном знаке.
 *
 * <p>Строки кэшируются: интерфейс рисует одни и те же подписи каждый кадр, а сборка
 * стилизованного {@link Component} каждый раз давала бы лишний мусор для GC.</p>
 */
public final class TextRenderer {
    private static final int CACHE_LIMIT = 512;
    /** Шрифт логотипа: буква D в водяном знаке. */
    public static final Identifier BRAND = Identifier.fromNamespaceAndPath(DraxonClient.MOD_ID, "soopafre");
    private static final Map<String, FormattedCharSequence> CACHE =
            new LinkedHashMap<>(256, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, FormattedCharSequence> eldest) {
                    return size() > CACHE_LIMIT;
                }
            };

    private TextRenderer() {
    }

    /** Начертания Inter, доступные интерфейсу. */
    public enum Weight {
        REGULAR("inter_regular"),
        MEDIUM("inter_medium"),
        SEMIBOLD("inter_semibold");

        private final Identifier id;

        Weight(String id) {
            this.id = Identifier.fromNamespaceAndPath(DraxonClient.MOD_ID, id);
        }

        public Identifier id() {
            return id;
        }

        private FontDescription.Resource description() {
            return new FontDescription.Resource(id);
        }
    }

    public static int lineHeight() {
        return Minecraft.getInstance().font.lineHeight;
    }

    public static int width(String text, Weight weight) {
        return width(text, weight.id());
    }

    public static int width(String text, Identifier font) {
        return Minecraft.getInstance().font.width(sequence(text, font));
    }

    public static void draw(GuiGraphics graphics, String text, int x, int y, int color, Weight weight) {
        draw(graphics, text, x, y, color, weight.id());
    }

    public static void draw(GuiGraphics graphics, String text, int x, int y, int color, Identifier font) {
        Font renderer = Minecraft.getInstance().font;
        graphics.drawString(renderer, sequence(text, font), x, y, color, false);
    }

    public static void drawCentered(GuiGraphics graphics, String text, int centerX, int y, int color, Weight weight) {
        draw(graphics, text, centerX - width(text, weight) / 2, y, color, weight);
    }

    public static void drawRight(GuiGraphics graphics, String text, int rightX, int y, int color, Weight weight) {
        draw(graphics, text, rightX - width(text, weight), y, color, weight);
    }

    /** Обрезает строку по ширине, добавляя многоточие: длинные переводы не выходят за плашку. */
    public static String clip(String text, int maxWidth, Weight weight) {
        if (maxWidth <= 0) {
            return "";
        }
        if (width(text, weight) <= maxWidth) {
            return text;
        }
        String result = text;
        while (result.length() > 1
                && width(result + "\u2026", weight) > maxWidth) {
            result = result.substring(0, result.length() - 1);
        }
        return result + "\u2026";
    }

    private static FormattedCharSequence sequence(String text, Weight weight) {
        return sequence(text, weight.id());
    }

    private static FormattedCharSequence sequence(String text, Identifier font) {
        String key = font + "\u0000" + text;
        FormattedCharSequence cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        MutableComponent component = Component.literal(text)
                .withStyle(Style.EMPTY.withFont(new FontDescription.Resource(font)));
        FormattedCharSequence sequence = component.getVisualOrderText();
        CACHE.put(key, sequence);
        return sequence;
    }
}
