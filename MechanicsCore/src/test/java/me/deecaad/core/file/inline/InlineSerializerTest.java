package me.deecaad.core.file.inline;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InlineSerializerTest {

    private static Stream<Arguments> provide_allEqual() {
        Map<Argument, Object> expected = Map.of(FooSerializer.NUM, 1, FooSerializer.NESTED, Map.of(FooSerializer.NestedSerializer.HEY, 0));
        return Stream.of(
                Arguments.of("foo(num=1, nested=(hey=0))", expected),
                Arguments.of("foo(nested=(hey=0))", expected),
                Arguments.of("foo(nested=())", expected),
                Arguments.of("foo(())", expected),
                Arguments.of("(())", expected),
                Arguments.of("()", expected),
                Arguments.of("nested=()", expected),
                Arguments.of("num=1, nested=()", expected),
                Arguments.of("num=1,nested=()", expected),
                Arguments.of("num=1,nested=(0)", expected)
        );
    }

    @ParameterizedTest
    @MethodSource("provide_allEqual")
    void test_allEqual(String line, Map<Argument, Object> expectedData) throws InlineException {
        FooSerializer actual = new FooSerializer().inlineFormat(line);
        FooSerializer expected = new FooSerializer(expectedData);

        assertEquals(expected.getNum(), actual.getNum());
        assertEquals(expected.getNested().getHey(), actual.getNested().getHey());
    }
}
