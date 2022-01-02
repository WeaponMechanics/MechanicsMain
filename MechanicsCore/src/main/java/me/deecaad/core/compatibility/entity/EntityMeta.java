package me.deecaad.core.compatibility.entity;

import javax.annotation.Nonnull;
import java.util.Arrays;

import static me.deecaad.core.compatibility.entity.BitMutator.*;

/**
 * This class holds 8 {@link BitMutator}s, stored in a key-value format
 * ({@link EntityMetaFlag}-{@link BitMutator}, respectively). These mutators
 * can be applied to an entity's metadata to change its appearance.
 *
 * @see FakeEntity#getMeta()
 */
public class EntityMeta {

    private final BitMutator[] meta;

    public EntityMeta() {
        this.meta = new BitMutator[Byte.SIZE];
        Arrays.fill(meta, RETAIN);
    }

    /**
     * Sets the given <code>flag</code> to the given <code>bitState</code>.
     *
     * @param flag     The non-null entity meta flag (key) to modify.
     * @param bitState The non-null bit state to apply.
     * @return A non-null reference to this entity meta.
     */
    @Nonnull
    public EntityMeta setFlag(@Nonnull EntityMetaFlag flag, @Nonnull BitMutator bitState) {
        meta[flag.getIndex()] = bitState;
        return this;
    }

    /**
     * Sets the given <code>flag</code> to the given <code>bitState</code>.
     *
     * @param flag     The non-null entity meta flag (key) to modify.
     * @param bitState true for TRUE or false for FALSE.
     * @return A non-null reference to this entity meta.
     */
    @Nonnull
    public EntityMeta setFlag(@Nonnull EntityMetaFlag flag, boolean bitState) {
        meta[flag.getIndex()] = bitState ? TRUE : FALSE;
        return this;
    }

    /**
     * Returns the bit state of the given <code>flag</code>.
     *
     * @param flag The non-null meta flag to check the state of.
     * @return The non-null bit state of the given flag.
     */
    @Nonnull
    public BitMutator getFlag(@Nonnull EntityMetaFlag flag) {
        return meta[flag.getIndex()];
    }

    /**
     * Applies each {@link BitMutator} stored by this class to the given byte.
     *
     * @param data The entity's current metadata.
     * @return The modified entity metadata.
     */
    public byte apply(byte data) {

        // For each bit in the byte, set the requested data
        for (int i = 0; i < Byte.SIZE; i++) {
            BitMutator flag = meta[i];

            switch (flag) {
                case TRUE:
                    data = (byte) (data | (1 << i));
                    break;
                case FALSE:
                    data = (byte) (data & ~(1 << i));
                    break;
                case RETAIN:
                    // no changes if we want to keep the data
                    break;
            }
        }

        return data;
    }

    @Override
    public String toString() {
        return "EntityMeta{" +
                "meta=" + Arrays.toString(meta) +
                '}';
    }
}
