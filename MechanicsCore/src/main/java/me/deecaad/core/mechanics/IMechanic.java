package me.deecaad.core.mechanics;

import me.deecaad.core.file.Serializer;

public interface IMechanic<T> extends Serializer<T> {

    /**
     * Use this mechanic with given cast data
     *
     * @param castData the cast data
     */
    void use(CastData castData);

    /**
     * @return whether this mechanic should only be run if caster is player
     */
    default boolean requirePlayer() {
        return false;
    }

    /**
     * @return whether this mechanic should only be run if CastData has entity specified
     */
    default boolean requireEntity() {
        return false;
    }
}