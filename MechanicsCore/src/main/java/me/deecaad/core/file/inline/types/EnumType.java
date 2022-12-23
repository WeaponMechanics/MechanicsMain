package me.deecaad.core.file.inline.types;

import me.deecaad.core.file.SerializerEnumException;
import me.deecaad.core.file.inline.ArgumentType;
import me.deecaad.core.file.inline.InlineException;
import me.deecaad.core.utils.EnumUtil;

import java.util.List;

public class EnumType<T extends Enum<T>> implements ArgumentType<List<T>> {

    private final Class<T> enumClass;
    private final boolean allowWildcard;

    public EnumType(Class<T> enumClass) {
        this.enumClass = enumClass;
        this.allowWildcard = false;
    }

    public EnumType(Class<T> enumClass, boolean allowWildcard) {
        this.enumClass = enumClass;
        this.allowWildcard = allowWildcard;
    }

    public Class<T> getEnumClass() {
        return enumClass;
    }

    public boolean isAllowWildcard() {
        return allowWildcard;
    }

    @Override
    public List<T> serialize(String str) throws InlineException {
        List<T> enums = EnumUtil.parseEnums(enumClass, str);

        if (enums.isEmpty() || (enums.size() != 1 && !allowWildcard))
            throw new InlineException(str, new SerializerEnumException("", enumClass, str, allowWildcard, ""));

        return enums;
    }

    @Override
    public String example() {
        return EnumUtil.getOptions(enumClass).stream().findAny().orElseThrow();
    }
}
