package me.deecaad.core.file.inline.types;

import me.deecaad.core.file.inline.ArgumentType;
import me.deecaad.core.file.inline.InlineException;
import me.deecaad.core.utils.StringUtil;

public class StringType implements ArgumentType<String> {

    private final boolean isColor;

    public StringType() {
        this(false);
    }

    public StringType(boolean isColor) {
        this.isColor = isColor;
    }

    public boolean isColor() {
        return isColor;
    }

    @Override
    public String serialize(String str) throws InlineException {
        return isColor ? StringUtil.colorAdventure(str) : str;
    }

    @Override
    public String example() {
        return isColor ? "<green>Hello" : "Hello";
    }
}
