package me.deecaad.core.file.serializers;

import me.deecaad.core.utils.Quaternion;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Returns a vector, with behaviors set by the implementing class.
 */
public interface VectorProvider {

    /**
     * Returns the vector, as specified by the implementing class.
     *
     * @param localTransform The local transform to apply to the vector. This can be null.
     * @return The vector.
     */
    @NotNull Vector provide(@Nullable Quaternion localTransform);
}
