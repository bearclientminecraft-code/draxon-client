package client.category;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Пиксельные иконки категорий (7x7). Рисуются через заливку прямоугольников,
 * поэтому не требуют текстур и не зависят от шрифтов.
 */
public enum CategoryIcon {
    SWORDS(new String[]{
            "#.....#",
            ".#...#.",
            "..#.#..",
            "...#...",
            "..#.#..",
            ".#...#.",
            "#.....#"}),
    ARROW(new String[]{
            "...#...",
            "..###..",
            ".#####.",
            "...#...",
            "...#...",
            "...#...",
            "...#..."}),
    MONITOR(new String[]{
            "#######",
            "#.....#",
            "#.....#",
            "#.....#",
            "#######",
            "...#...",
            "..###.."}),
    PERSON(new String[]{
            "..###..",
            "..###..",
            "...#...",
            ".#####.",
            "..#.#..",
            "..#.#..",
            "..#.#.."}),
    GEAR(new String[]{
            "..###..",
            ".#...#.",
            "#.#.#.#",
            "#..#..#",
            "#.#.#.#",
            ".#...#.",
            "..###.."});

    private final String[] pattern;

    CategoryIcon(String[] pattern) {
        this.pattern = pattern;
    }

    public void draw(GuiGraphics graphics, int x, int y, int pixel, int color) {
        for (int row = 0; row < pattern.length; row++) {
            String line = pattern[row];
            for (int col = 0; col < line.length(); col++) {
                if (line.charAt(col) == '#') {
                    int px = x + col * pixel;
                    int py = y + row * pixel;
                    graphics.fill(px, py, px + pixel, py + pixel, color);
                }
            }
        }
    }
}
