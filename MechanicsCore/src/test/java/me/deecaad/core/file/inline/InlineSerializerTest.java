package me.deecaad.core.file.inline;

import me.deecaad.core.file.InlineSerializer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InlineSerializerTest {

    private static Stream<Arguments> provide_allEqual() {
        Map<String, Object> expected = Map.of(
                InlineSerializer.UNIQUE_IDENTIFIER, "foo",
                "num", 1,
                "nested", Map.of(InlineSerializer.UNIQUE_IDENTIFIER, "nested", "hey", 0));
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
        assertEquals(expectedData, actual);
    }

    private static Stream<Arguments> provide_allError() {
        return Stream.of(
                Arguments.of("foo((num=1, nested=nested(hey=0))"),
                Arguments.of("foo(num=1, nested=nested(hey=0)"),
                Arguments.of("foo(num, nested=nested(hey=0))"),
                Arguments.of("foo(num=1, nested)"),
                Arguments.of("foo(num==1, nested=nested(hey=0))")
        );
    }

    @ParameterizedTest
    @MethodSource("provide_allError")
    void test_allError(String line) {
        assertThrows(InlineSerializer.FormatException.class, () -> {
            InlineSerializer.inlineFormat(line);
        });
    }
}
