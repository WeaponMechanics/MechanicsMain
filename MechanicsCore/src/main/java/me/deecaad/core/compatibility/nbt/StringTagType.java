package me.deecaad.core.compatibility.nbt;

import org.bukkit.inventory.meta.tags.ItemTagAdapterContext;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StringTagType implements ItemTagType<byte[], String[]> {

    public static final StringTagType INSTANCE = new StringTagType();

    private StringTagType() {
    }

    @NotNull
    @Override
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @NotNull
    @Override
    public Class<String[]> getComplexType() {
        return String[].class;
    }


    @Override
    public byte @NotNull [] toPrimitive(String[] complex, @NotNull ItemTagAdapterContext context) {
        final byte[][] allBytes = new byte[complex.length][];
        int total = 0;
        for (int i = 0; i < allBytes.length; i++) {
            final byte[] bytes = complex[i].getBytes(StandardCharsets.UTF_8);
            allBytes[i] = bytes;
            total += bytes.length;
        }

        final ByteBuffer buffer = ByteBuffer.allocate(total + allBytes.length * 4); //stores integers
        for (final byte[] bytes : allBytes) {
            buffer.putInt(bytes.length);
            buffer.put(bytes);
        }

        return buffer.array();
    }

    @Override
    public String @NotNull [] fromPrimitive(byte @NotNull [] primitive, @NotNull ItemTagAdapterContext context) {
        final ByteBuffer buffer = ByteBuffer.wrap(primitive);
        final List<String> list = new ArrayList<>();

        while (buffer.remaining() > 0) {
            if (buffer.remaining() < 4) break;
            final int stringLength = buffer.getInt();
            if (buffer.remaining() < stringLength) break;

            final byte[] stringBytes = new byte[stringLength];
            buffer.get(stringBytes);

            list.add(new String(stringBytes, StandardCharsets.UTF_8));
        }

        return list.toArray(new String[0]);
    }
}
