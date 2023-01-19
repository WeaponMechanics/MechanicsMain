package me.deecaad.core.file.inline.types;

import me.deecaad.core.file.SerializerEnumException;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.inline.ArgumentType;
import me.deecaad.core.file.inline.InlineException;
import me.deecaad.core.utils.EnumUtil;
import org.bukkit.entity.EntityType;

import java.util.List;

public class EnumType<T extends Enum<T>> implements ArgumentType<T> {

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
    public T serialize(String str) throws InlineException {
        if (allowWildcard)
            throw new IllegalArgumentException("Hey developer! You made a mistake with your inline Arguments. " +
                    "When you want to use wildcard, you NEED to be using the ListType<EnumType<YourEnum>>");

        List<T> enums = EnumUtil.parseEnums(enumClass, str);
        if (enums.size() != 1)
            throw new InlineException(str, new SerializerEnumException("", enumClass, str, allowWildcard, ""));

        // Special case for EntityType, since only LivingEntities can be
        // selected. We should warn the user whenever they try to use an
        // impossible entity type, like EntityType.MINECART.
        if (enumClass == EntityType.class) {
            if (!((EntityType) enums.get(0)).isAlive())
                throw new InlineException(str, new SerializerException("", new String[]{"Tried to use '" + enums.get(0) + "' entity type, but it is not a living entity type!",
                        "Remember that Mechanics can ONLY target living entities, like ZOMBIE and PIG. Non-living entities cannot be targeted."}, ""));
        }

        return enums.get(0);
    }

    /**
     * For internal use by {@link ListType}.
     *
     * @param str The non-null string to parse.
     * @return 1 or more of your enums.
     */
    public List<T> serializeList(String str) throws InlineException {
        List<T> enums = EnumUtil.parseEnums(enumClass, str);
        if (enums.isEmpty())
            throw new InlineException(str, new SerializerEnumException("", enumClass, str, allowWildcard, ""));

        // Special case for EntityType, since only LivingEntities can be
        // selected. We should warn the user whenever they try to use an
        // impossible entity type, like EntityType.MINECART.
        if (enumClass == EntityType.class) {
            for (T element : enums) {
                if (!((EntityType) element).isAlive())
                    throw new InlineException(str, new SerializerException("", new String[]{"Tried to use '" + element + "' entity type, but it is not a living entity type!",
                            "Remember that Mechanics can ONLY target living entities, like ZOMBIE and PIG. Non-living entities cannot be targeted."}, ""));
            }
        }

        return enums;
    }

    @Override
    public String example() {
        return EnumUtil.getOptions(enumClass).stream().findAny().orElseThrow();
    }
}
