package me.deecaad.core.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static me.deecaad.core.utils.EnumUtilTest.TestEnum.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnumUtilTest {

    private static Stream<Arguments> provide_parseEnums() {
        return Stream.of(

                // Testing parsing 1 single material
                Arguments.of("OAK_WOOD", Collections.singletonList(OAK_WOOD)),
                Arguments.of("DirT ", Collections.singletonList(DIRT)),
                Arguments.of("   glasS  ", Collections.singletonList(GLASS)),

                // Testing invalid material names
                Arguments.of("not", Collections.emptyList()),
                Arguments.of("  mateRial ", Collections.emptyList()),
                Arguments.of("$not", Collections.emptyList()),

                // Testing '$' wildcards
                Arguments.of("$dirt", Collections.singletonList(DIRT)),
                Arguments.of("   $wood ", Arrays.asList(SPRUCE_WOOD, OAK_WOOD, BIRCH_WOOD, JUNGLE_WOOD)),
                Arguments.of("$glasS", Arrays.asList(GLASS, STAINED_GLASS, TINTED_GLASS, RED_STAINED_GLASS, GLASS_PANE, THIN_GLASS, BLUE_STAINED_GLASS_PANE))
        );
    }

    @ParameterizedTest
    @MethodSource("provide_parseEnums")
    void test_parseEnums(String input, List<TestEnum> expected) {
        List<TestEnum> actual = EnumUtil.parseEnums(TestEnum.class, input);
        assertEquals(expected, actual);

        // Assert each collection is unmodifiable
        assertThrows(Exception.class, () -> actual.add(CLAY));
    }

    @ParameterizedTest
    @CsvSource({"clay,true", "dirt,true", "gRasS,true", "empty,false", "air,false", "wood,false"})
    void test_getIfPresent(String input, boolean present) {
        assertEquals(present, EnumUtil.getIfPresent(TestEnum.class, input).isPresent());
    }

    @Test
    void test_getOptions() {
        Set<String> options = EnumUtil.getOptions(TestEnum.class);
        Set<String> actual = new HashSet<>(Arrays.asList(
                "SPRUCE_WOOD", "OAK_WOOD", "BIRCH_WOOD", "JUNGLE_WOOD",
                "DIRT", "STONE", "GRASS", "GLASS", "STAINED_GLASS",
                "TINTED_GLASS", "RED_STAINED_GLASS", "GLASS_PANE",
                "THIN_GLASS", "BLUE_STAINED_GLASS_PANE", "CLAY"
        ));

        assertEquals(actual, options);
    }

    @Test
    void test_getValues() {
        List<TestEnum> values = EnumUtil.getValues(TestEnum.class);
        List<TestEnum> actual = Arrays.asList(
                SPRUCE_WOOD, OAK_WOOD, BIRCH_WOOD, JUNGLE_WOOD,
                DIRT, STONE, GRASS, GLASS, STAINED_GLASS,
                TINTED_GLASS, RED_STAINED_GLASS, GLASS_PANE,
                THIN_GLASS, BLUE_STAINED_GLASS_PANE, CLAY
        );

        assertEquals(actual, values);
    }


    enum TestEnum {
        SPRUCE_WOOD,
        OAK_WOOD,
        BIRCH_WOOD,
        JUNGLE_WOOD,
        DIRT,
        STONE,
        GRASS,
        GLASS,
        STAINED_GLASS,
        TINTED_GLASS,
        RED_STAINED_GLASS,
        GLASS_PANE, THIN_GLASS,
        BLUE_STAINED_GLASS_PANE,
        CLAY

    }
}