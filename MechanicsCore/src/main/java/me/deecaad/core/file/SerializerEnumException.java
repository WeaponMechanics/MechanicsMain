package me.deecaad.core.file;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class SerializerEnumException extends SerializerException {

    public <T extends Enum<T>> SerializerEnumException(@NotNull String name, Class<T> enumClass,
                                                       String actual, boolean allowWildcard, @NotNull String location) {

        super(name, getMessages(enumClass, actual, allowWildcard), location);
    }

    public <T extends Enum<T>> SerializerEnumException(@NotNull Serializer<?> serializer, Class<T> enumClass,
                                                       String actual, boolean allowWildcard, @NotNull String location) {

        super(serializer, getMessages(enumClass, actual, allowWildcard), location);
    }

    private static <T extends Enum<T>> String[] getMessages(Class<T> enumClass, String actual, boolean allowWildcard) {

        // With enums, there is usually the option to use '$' as a wildcard.
        // Sometimes, we are not allowed to use wildcards, so we should warn
        // the user about that.
        boolean usesWildcard = actual.startsWith("$");
        if (usesWildcard && !allowWildcard) {
            return new String[] {
                    "You tried to use a wildcard ('$') when wildcards aren't allowed to be used!",
                    forValue(actual),
                    didYouMean(actual, enumClass)
            };
        }

        String link = "https://github.com/WeaponMechanics/MechanicsMain/wiki/References";
        if (enumClass == Material.class)
            link += "#materials";
        else if (enumClass == Sound.class)
            link += "#sounds";
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
        list.add("Remember that you can " + (allowWildcard ? "" : "NOT") + " use wildcards ('$')");

        if (link != null)
            list.add("Reference: " + link);

        return list.toArray(new String[0]);
    }
}
