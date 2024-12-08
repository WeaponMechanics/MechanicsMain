package me.deecaad.core.file;

import me.deecaad.core.utils.EnumUtil;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SerializerEnumException extends SerializerException {

    private final Set<String> options;
    private final String actual;
    private final boolean allowWildcard;

    public <T extends Enum<T>> SerializerEnumException(@NotNull String name, Class<T> enumClass,
        String actual, boolean allowWildcard, @NotNull String location) {

        super(name, getMessages(enumClass, actual, allowWildcard), location);

        this.options = EnumUtil.getOptions(enumClass);
        this.actual = actual;
        this.allowWildcard = allowWildcard;
    }

    public <T extends Enum<T>> SerializerEnumException(@NotNull Serializer<?> serializer, Class<T> enumClass,
        String actual, boolean allowWildcard, @NotNull String location) {

        super(serializer, getMessages(enumClass, actual, allowWildcard), location);

        this.options = EnumUtil.getOptions(enumClass);
        this.actual = actual;
        this.allowWildcard = allowWildcard;

        // 1.13+ remove the legacy materials, so they don't clutter the console.
        if (enumClass == Material.class) {
            this.options.removeIf(name -> name.startsWith("LEGACY_"));
        }
    }

    public Set<String> getOptions() {
        return new HashSet<>(options);
    }

    public String getActual() {
        return actual;
    }

    public boolean isAllowWildcard() {
        return allowWildcard;
    }

    private static <T extends Enum<T>> String[] getMessages(Class<T> enumClass, String actual, boolean allowWildcard) {

        // With enums, there is usually the option to use '$' as a wildcard.
        // Sometimes, we are not allowed to use wildcards, so we should warn
        // the user about that.
        boolean usesWildcard = actual.startsWith("$");
        if (usesWildcard && !allowWildcard) {
            return new String[]{
                    "You tried to use a wildcard ('$') when wildcards aren't allowed to be used!",
                    forValue(actual),
                    didYouMean(actual, enumClass)
            };
        }

        String link = "https://cjcrafter.gitbook.io/core/references";
        if (enumClass == Material.class)
            link += "#materials";
        else if (enumClass == Particle.class)
            link += "#particles";
        else if (enumClass == EntityType.class)
            link += "#entities";
        else
            link = null;

        List<String> list = new LinkedList<>();
        list.add("Could not match config to any " + enumClass.getSimpleName());
        list.add(forValue(actual));
        list.add(didYouMean(actual, enumClass));

        if (link != null)
            list.add(enumClass.getSimpleName() + " Reference: " + link);

        return list.toArray(new String[0]);
    }
}
