package me.deecaad.core.utils;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import org.bukkit.map.MapFont;
import org.bukkit.map.MinecraftFont;
import org.jetbrains.annotations.Contract;

import java.util.function.IntFunction;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;

public class TableBuilder {

    public static final TableConstraints DEFAULT_CONSTRAINTS = new TableConstraints(8, 2, 320);
    public static final MapFont DEFAULT_FONT = MinecraftFont.Font;

    private TableConstraints constraints;
    private MapFont font;
    private String header;
    private TextComponent left;
    private TextComponent right;
    
    // The fill char is used in the header and footer. The header is centered
    // between the fillChars.
    // The elementChar is used before each element.
    private char fillChar;
    private char elementChar;

    private boolean attemptSinglePixelFix;

    private Style headerStyle;
    private Style fillCharStyle;
    private Style elementCharStyle;
    private Style elementStyle;


    private IntFunction<TextComponent> supplier;

    public TableBuilder() {
        constraints = DEFAULT_CONSTRAINTS;
        font = DEFAULT_FONT;
        elementChar = 0; // use a space as default
    }

    public TableBuilder withConstraints(TableConstraints constraints) {
        this.constraints = constraints;
        return this;
    }

    public TableBuilder withFont(MapFont font) {
        this.font = font;
        return this;
    }

    public TableBuilder withHeader(String header) {
        this.header = header;
        return this;
    }

    public TableBuilder withLeft(TextComponent left) {
        this.left = left;
        return this;
    }

    public TableBuilder withRight(TextComponent right) {
        this.right = right;
        return this;
    }

    public TableBuilder withFillChar(char fillChar) {
        this.fillChar = fillChar;
        return this;
    }

    public TableBuilder withElementChar(char elementChar) {
        this.elementChar = elementChar;
        return this;
    }

    public TableBuilder withHeaderStyle(Style style) {
        this.headerStyle = style;
        return this;
    }

    public TableBuilder withElementStyle(Style style) {
        this.elementStyle = style;
        return this;
    }

    public TableBuilder withElementCharStyle(Style style) {
        this.elementCharStyle = style;
        return this;
    }

    public TableBuilder withFillCharStyle(Style style) {
        this.fillCharStyle = style;
        return this;
    }

    public TableBuilder withSupplier(IntFunction<TextComponent> supplier) {
        this.supplier = supplier;
        return this;
    }

    /**
     * When you use this method, all supplied content needs to be uppercase.
     * Otherwise, the content will still be off by 1 pixel.
     *
     * @return A non-null reference to this (builder pattern).
     */
    public TableBuilder withAttemptSinglePixelFix() {
        this.attemptSinglePixelFix = true;
        return this;
    }

    public TextComponent build() {
        TextComponent.Builder builder = text();

        if (header != null && !header.isEmpty()) {
            builder.append(buildHeader());
            builder.append(newline());
        }

        String prefix = (elementChar == 0) ? "" : elementChar + " ";
        int cellSize = constraints.pixels / constraints.columns - font.getWidth(prefix);
        for (int i = 0; i < constraints.rows * constraints.columns; i++) {

            TextComponent text = supplier.apply(i);

            // When the supplier returns null, that means we should end the
            // table. Sometimes we need to add a '\n'.
            if (text == null) {
                if (i % constraints.columns != 0) {
                    builder.append(newline());
                }
                break;
            }

            // In order to create a properly spaced table, we need each cell
            // to have the same number of pixels. To accomplish this, we need
            // to add spaces until the cell is full, then take away any extra
            // characters. This will "abbreviate" long strings, and "fill"
            // short strings.
            StringBuilder cell = new StringBuilder(text.content());

            int count = 0;
            if (i % constraints.columns != constraints.columns - 1) {
                while (MinecraftFont.Font.getWidth(prefix + cell) < cellSize)
                    cell.append(' ');
                while (MinecraftFont.Font.getWidth(prefix + cell) > cellSize)
                    cell.setLength(cell.length() - 1);
                while (attemptSinglePixelFix && MinecraftFont.Font.getWidth(prefix + cell + StringUtil.repeat("|", count)) < cellSize)
                    count++;
            }

            // Although we reset style here, (and maybe we shouldn't reset
            // style), it is still important to allow TextComponents for
            // click and hover events.
            text = text.content(cell.toString()).style(text.style().merge(elementStyle));
            builder.append(text().content(prefix).style(elementCharStyle));
            builder.append(text);
            builder.append(text().content(StringUtil.repeat("|", count)).style(elementCharStyle));

            if (i % constraints.columns == constraints.columns - 1) {
                builder.append(newline());
            }
        }

        if (left != null && right != null) {
            builder.append(buildFooter());
        }

        return builder.build();
    }

    public TextComponent buildHeader() {
        StringBuilder a = new StringBuilder();

        while (font.getWidth(a + " " + this.header + " " + a) < constraints.pixels)
            a.append(fillChar);

        a.setLength(a.length() - 1);

        TextComponent.Builder builder = text();
        builder.append(text().content(a.toString()).style(fillCharStyle));
        builder.append(text().content(" " + this.header + " ").style(headerStyle));
        builder.append(text().content(a.toString()).style(fillCharStyle));
        return builder.build();
    }

    public TextComponent buildFooter() {
        StringBuilder a = new StringBuilder();
        String footer = " " + left.content() + StringUtil.repeat(" ", header.length()) + right.content() + " ";

        while (font.getWidth(a + " " + footer + " " + a) < constraints.pixels)
            a.append(fillChar);

        a.setLength(a.length() - 1);

        TextComponent.Builder builder = text();
        builder.append(text().content(a.toString()).style(fillCharStyle));
        builder.append(text(" "));
        builder.append(left);
        builder.append(text(StringUtil.repeat(" ", header.length())));
        builder.append(right);
        builder.append(text(" "));
        builder.append(text().content(a.toString()).style(fillCharStyle));

        return builder.build();
    }


    /**
     * Immutable class that defines the <i>limits</i> of a table (the maximum
     * number of rows that can fit in chat, the requested number of columns,
     * and the maximum number of pixels that can fit in 1 row).
     */
    public static class TableConstraints {

        private final int rows;
        private final int columns;
        private final int pixels;

        public TableConstraints(int rows, int columns, int pixels) {
            this.rows = rows;
            this.columns = columns;
            this.pixels = pixels;
        }

        public int getRows() {
            return rows;
        }

        @Contract(pure = true)
        public TableConstraints setRows(int rows) {
            return new TableConstraints(rows, columns, pixels);
        }

        public int getColumns() {
            return columns;
        }

        @Contract(pure = true)
        public TableConstraints setColumns(int columns) {
            return new TableConstraints(rows, columns, pixels);
        }

        public int getPixels() {
            return pixels;
        }

        @Contract(pure = true)
        public TableConstraints setPixels(int pixels) {
            return new TableConstraints(rows, columns, pixels);
        }
    }
}
