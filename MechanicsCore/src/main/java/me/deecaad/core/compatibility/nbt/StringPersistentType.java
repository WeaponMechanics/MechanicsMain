/*
 * Copyright (c) 2022 Alexander Majka (mfnalex) / JEFF Media GbR
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * If you need help or have any suggestions, feel free to join my Discord and head to
 * #programming-help:
 *
 * Discord: https://discord.jeff-media.com/
 *
 * If you find this library helpful or if you're using it one of your paid plugins, please consider
 * leaving a donation to support the further development of this project :)
 *
 * Donations: https://paypal.me/mfnalex
 */

package me.deecaad.core.compatibility.nbt;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StringPersistentType implements PersistentDataType<byte[], String[]> {

    public static final StringPersistentType INSTANCE = new StringPersistentType();

    private StringPersistentType() {
    }

    @NotNull @Override
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @NotNull @Override
    public Class<String[]> getComplexType() {
        return String[].class;
    }

    @Override
    public byte @NotNull [] toPrimitive(final String[] complex, @NotNull final PersistentDataAdapterContext context) {
        final byte[][] allBytes = new byte[complex.length][];
        int total = 0;
        for (int i = 0; i < allBytes.length; i++) {
            final byte[] bytes = complex[i].getBytes(StandardCharsets.UTF_8);
            allBytes[i] = bytes;
            total += bytes.length;
        }

        final ByteBuffer buffer = ByteBuffer.allocate(total + allBytes.length * 4); // stores integers
        for (final byte[] bytes : allBytes) {
            buffer.putInt(bytes.length);
            buffer.put(bytes);
        }

        return buffer.array();
    }

    @Override
    public String @NotNull [] fromPrimitive(final byte @NotNull [] primitive, @NotNull final PersistentDataAdapterContext context) {
        final ByteBuffer buffer = ByteBuffer.wrap(primitive);
        final List<String> list = new ArrayList<>();

        while (buffer.remaining() > 0) {
            if (buffer.remaining() < 4)
                break;
            final int stringLength = buffer.getInt();
            if (buffer.remaining() < stringLength)
                break;

            final byte[] stringBytes = new byte[stringLength];
            buffer.get(stringBytes);

            list.add(new String(stringBytes, StandardCharsets.UTF_8));
        }

        return list.toArray(new String[0]);
    }
}
