package me.deecaad.core.utils;

import me.deecaad.compatibility.entity.EntityCompatibility;

/**
 * Defines basic bit operations, useful for byte data
 * for packets
 *
 * @see EntityCompatibility#getMetadataPacket(Object, BitOperation, boolean, EntityCompatibility.EntityMeta...)
 */
public enum BitOperation {

    SET {
        @Override
        public byte invoke(byte a, byte b) {
            return b;
        }
    }, AND {
        @Override
        public byte invoke(byte a, byte b) {
            return (byte) (a & b);
        }
    }, OR {
        @Override
        public byte invoke(byte a, byte b) {
            return (byte) (a | b);
        }
    }, XOR {
        @Override
        public byte invoke(byte a, byte b) {
            return (byte) (a ^ b);
        }
    };

    public abstract byte invoke(byte a, byte mask);
}
