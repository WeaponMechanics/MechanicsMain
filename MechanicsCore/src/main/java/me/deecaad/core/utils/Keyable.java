package me.deecaad.core.utils;

import org.jetbrains.annotations.NotNull;

/**
 * This interface is used to mark classes that have a key (or a unique identifier)
 * which can be used for {@link me.deecaad.core.mechanics.Registry}.
 */
public interface Keyable {
    @NotNull String getKey();
}
