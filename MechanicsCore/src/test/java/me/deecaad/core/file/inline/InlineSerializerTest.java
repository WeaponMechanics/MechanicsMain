package me.deecaad.core.file.inline;

import me.deecaad.core.file.InlineSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class InlineSerializerTest {

    private static Stream<Arguments> provide_allEqual() {
        Map<String, Object> expected = new HashMap<>(Map.of(
                InlineSerializer.UNIQUE_IDENTIFIER, "foo",
                "num", 1,
                "nested", new HashMap<>(Map.of(InlineSerializer.UNIQUE_IDENTIFIER, "nested", "hey", 0))));
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
        boolean equal = mapEquals(expectedData, actual);

        // Assertions are bad with maps... manual check
        if (!equal)
            Assertions.assertEquals(expectedData, actual);
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
            try {
                InlineSerializer.inlineFormat(line);
            } catch (InlineSerializer.FormatException ex) {
                System.err.println("Index is: " + ex.getIndex());
                throw ex;
            }
        });
    }

    private boolean mapEquals(Map<?, ?> a, Map<?, ?> b) {
        if (a.size() != b.size())
            return false;

        for (Map.Entry<?, ?> entry : a.entrySet()) {
            if (!b.containsKey(entry.getKey()))
                return false;

            Object value = b.get(entry.getKey());
            if (value instanceof Map<?, ?> tempA && entry.getValue() instanceof Map<?, ?> tempB) {
                if (!mapEquals(tempA, tempB))
                    return false;
            } else if (!Objects.equals(String.valueOf(entry.getValue()), String.valueOf(value))) {
                return false;
            }
        }

        return true;
    }
}
