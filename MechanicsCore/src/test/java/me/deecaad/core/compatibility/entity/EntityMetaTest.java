package me.deecaad.core.compatibility.entity;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static me.deecaad.core.compatibility.entity.EntityMetaFlag.*;
import static me.deecaad.core.compatibility.entity.BitMutator.*;

class EntityMetaTest {

    public static final Supplier<EntityMeta> META_A = () -> new EntityMeta()
            .setFlag(FIRE, false) // index 0
            .setFlag(SNEAKING, true) // index 1
            .setFlag(GLOWING, true); // index 6

    public static final Supplier<EntityMeta> META_B = EntityMeta::new;


    private static Stream<Arguments> provide_getFlag() {
        return Stream.of(
                Arguments.of(META_A.get(), FIRE, FALSE),
                Arguments.of(META_A.get(), SNEAKING, TRUE),
                Arguments.of(META_A.get(), GLOWING, TRUE),
                Arguments.of(META_A.get(), GLIDING, RETAIN),
                Arguments.of(META_A.get(), INVISIBLE, RETAIN),

                Arguments.of(META_B.get(), FIRE, RETAIN),
                Arguments.of(META_B.get(), INVISIBLE, RETAIN),
                Arguments.of(META_B.get(), GLOWING, RETAIN)
        );
    }

    @ParameterizedTest
    @MethodSource("provide_getFlag")
    void test_getFlag(EntityMeta meta, EntityMetaFlag flag, BitMutator expected) {
        BitMutator actual = meta.getFlag(flag);
        assertEquals(expected, actual);
    }

    private static Stream<Arguments> provide_apply() {
        return Stream.of(
                Arguments.of(META_A.get(), fromBinary(0, 1, 0, 0, 1, 1, 1, 0), fromBinary(0, 1, 0, 0, 1, 1, 1, 0)),
                Arguments.of(META_A.get(), fromBinary(1, 1, 1, 1, 1, 1, 1, 1), fromBinary(0, 1, 1, 1, 1, 1, 1, 1)),
                Arguments.of(META_A.get(), fromBinary(1, 0, 0, 0, 0, 0, 0, 1), fromBinary(0, 1, 0, 0, 0, 0, 1, 1)),
                Arguments.of(META_A.get(), (byte) 0,                                 fromBinary(0, 1, 0, 0, 0, 0, 1, 0)),

                Arguments.of(META_B.get(), fromBinary(1, 0, 1, 0, 1, 0, 1, 0), fromBinary(1, 0, 1, 0, 1, 0, 1, 0)),
                Arguments.of(META_B.get(), (byte) 0,                                 (byte) 0),
                Arguments.of(META_B.get(), fromBinary(1, 1, 1, 1, 1, 1, 1, 1), fromBinary(1, 1, 1, 1, 1, 1, 1, 1))
        );
    }

    @ParameterizedTest
    @MethodSource("provide_apply")
    void test_apply(EntityMeta meta, byte input, byte expected) {
        byte actual = meta.apply(input);
        assertEquals(expected, actual, "meta=" + meta
                + " input=" + Integer.toUnsignedString(input) + "(" + Integer.toUnsignedString(input, 2)
                + ") expected=" + Integer.toUnsignedString(expected) + "(" + Integer.toUnsignedString(expected, 2) + ")");
    }

    private static byte fromBinary(int... arr) {
        int binary = 0;

        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != 0 && arr[i] != 1)
                throw new IllegalArgumentException("num was not a 0 or 1");

            binary |= arr[i] << i;
        }

        return (byte) binary;
    }
}