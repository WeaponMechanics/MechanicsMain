package me.deecaad.weaponmechanics.wrappers;

import me.deecaad.weaponmechanics.packetlisteners.OutEntityMetadataListener;

/**
 * This enum gives location of a bit of information inside
 * the byte from <a href="https://wiki.vg/Entity_metadata#Entity">EntityMetaData</a>.
 *
 * Note that effects here are visual, and only cause animations and/or effects.
 *
 * This is most useful when modifying <code>PacketPlayOutEntityMetadata</code> packets.
 * @see OutEntityMetadataListener
 */
public enum EntityMeta {
    
    FIRE(0),      // If the entity is on fire
    SNEAKING(1),  // If the entity is sneaking
    UNUSED(2),    // If the entity is mounted
    SPRINTING(3), // If the entity is running
    SWIMMING(4),  // If the entity is swimming
    INVISIBLE(5), // If the entity is invisible
    GLOWING(6),   // If the entity is glowing
    GLIDING(7);   // If the entity is gliding on an elytra
    
    // This represents the "index" of the bit used for this
    // data on a byte.
    private final byte flag;
    
    EntityMeta(int flag) {
        this.flag = (byte) flag;
    }
    
    public byte getFlag() {
        return this.flag;
    }
    
    public byte setFlag(byte data, boolean is) {
        return (byte) (is ? data | 1 << flag : data & 1 << flag);
    }
}
