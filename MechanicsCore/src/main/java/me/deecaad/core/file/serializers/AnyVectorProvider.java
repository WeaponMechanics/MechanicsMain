package me.deecaad.core.file.serializers;

import me.deecaad.core.utils.ImmutableVector;
import me.deecaad.core.utils.Quaternion;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stores an arbitrary vector. If the vector is relative, it will be transformed by the local
 * transform.
 */
public class AnyVectorProvider implements VectorProvider {

    private final boolean relative;
    private final ImmutableVector vector;

    public AnyVectorProvider(boolean isRelative, @NotNull ImmutableVector vector) {
        this.relative = isRelative;
        this.vector = vector;
    }

    public boolean isRelative() {
        return relative;
    }

    /**
     * Returns the vector, as specified by the implementing class.
     *
     * @param localTransform The local transform to apply to the vector. This can be null.
     * @return The vector.
     */
    @Override
    public @NotNull Vector provide(@Nullable Quaternion localTransform) {
        if (localTransform != null && relative) {
            return localTransform.multiply(vector);
        }

        return vector;
    }
}
