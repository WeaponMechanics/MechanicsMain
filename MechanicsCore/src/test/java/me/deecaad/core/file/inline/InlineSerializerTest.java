package me.deecaad.core.file.inline;

import me.deecaad.core.file.InlineSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static me.deecaad.core.file.InlineSerializer.UNIQUE_IDENTIFIER;

public class InlineSerializerTest {

    private static Stream<Arguments> provide_allEqual() {
        Map<String, Object> expected = new HashMap<>(Map.of(
                UNIQUE_IDENTIFIER, "foo",
                "num", "1",
                "nested", new HashMap<>(Map.of(UNIQUE_IDENTIFIER, "nested", "hey", "0"))));
        return Stream.of(
                Arguments.of("foo(num=1, nested=nested(hey=0))", expected),
                Arguments.of("foo(num=1,nested=nested(hey=0))", expected),
                Arguments.of("foo (num=1, nested=nested(hey=0))", expected),
                Arguments.of("foo(nested=nested(hey=0), num=1)", expected)
        );
    }

    @ParameterizedTest
    @MethodSource("provide_allEqual")
    void test_allEqual(String line, Map<String, Object> expectedData) throws InlineSerializer.FormatException {
        Map<String, Object> actual = InlineSerializer.inlineFormat(line);
        Assertions.assertEquals(expectedData, actual);
    }

    private static Stream<Arguments> provide_allError() {
        return Stream.of(
                // Just some easy syntax errors
                Arguments.of("foo((num=1, nested=nested(hey=0))"),
                Arguments.of("foo(num=1, nested=nested(hey=0)"),
                Arguments.of("foo(num, nested=nested(hey=0))"),
                Arguments.of("foo(num=1, nested)"),
                Arguments.of("foo(num==1, nested=nested(hey=0))"),

                // List related syntax errors
                Arguments.of("foo(num[0, 1, 2])"),
                Arguments.of("foo(num=[0, 1,, 2])"),
                Arguments.of("foo(num=[[0, 1])"),
                Arguments.of("foo(num=list[0, 1])"),
                Arguments.of("foo(num=list[0=hi, 1=mom])")
        );
    }

    @ParameterizedTest
    @MethodSource("provide_allError")
    void test_allError(String line) {
        assertThrows(InlineSerializer.FormatException.class, () -> {
            try {
                System.out.println(InlineSerializer.inlineFormat(line));
            } catch (InlineSerializer.FormatException ex) {
                System.err.println("Index is: " + ex.getIndex());
                throw ex;
            }
        });
    }

    @Test
    void test_list() throws InlineSerializer.FormatException {
        String line = "item(name=<yellow>AK-47, lore=[russians drink vodka, and shoot ak-47s])";
        Map<?, ?> expected = Map.of(
                UNIQUE_IDENTIFIER, "item",
                "name", "<yellow>AK-47",
                "lore", List.of("russians drink vodka", "and shoot ak-47s")
                );

        Map<?, ?> actual = InlineSerializer.inlineFormat(line);
        assertEquals(expected, actual);
    }

    @Test
    void test_nestedList() throws InlineSerializer.FormatException {
        String line = "firework(effects=[(color=red), (shape=burst, color=green), (color=blue, flicker=true)])";
        Map<?, ?> expected = Map.of(
                UNIQUE_IDENTIFIER, "firework",
                "effects", List.of(
                        Map.of("color", "red"),
                        Map.of("shape", "burst", "color", "green"),
                        Map.of("color", "blue", "flicker", "true")
                )
        );

        Map<?, ?> actual = InlineSerializer.inlineFormat(line);
        assertEquals(expected, actual);
    }
}
