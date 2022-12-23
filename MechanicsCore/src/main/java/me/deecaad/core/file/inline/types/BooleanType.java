package me.deecaad.core.file.inline.types;

import me.deecaad.core.file.SerializerTypeException;
import me.deecaad.core.file.inline.ArgumentType;
import me.deecaad.core.file.inline.InlineException;

import java.util.Locale;


public class BooleanType implements ArgumentType<Boolean> {

    public BooleanType() {
    }

    @Override
    public Boolean serialize(String str) throws InlineException {
        String lower = str.toLowerCase(Locale.ROOT);

        if (lower.equals("true"))
            return true;
        if (lower.equals("false"))
            return false;

        throw new InlineException(str, new SerializerTypeException("", Integer.class, String.class, str, ""));
    }

    @Override
    public String example() {
        return "true";
    }
}
