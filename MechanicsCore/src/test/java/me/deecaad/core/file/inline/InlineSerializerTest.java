package me.deecaad.core.file.inline;

import me.deecaad.core.utils.StringUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InlineSerializerTest {

    private static Stream<Arguments> provide_allEqual() {
        Map<Argument, Object> expected = Map.of(FooSerializer.NUM, 1, FooSerializer.NESTED, new FooSerializer.NestedSerializer(Map.of(FooSerializer.NestedSerializer.HEY, 0)));
        return Stream.of(
                Arguments.of("FOO(NUM=1, NESTED=NESTED(HEY=0))", expected),
                Arguments.of("foo(num=1, nested=nested(hey=0))", expected),
                Arguments.of("foo(num=1, nested=(hey=0))", expected),
                Arguments.of("foo(nested=(hey=0))", expected),
                Arguments.of("foo(nested=())", expected),
                Arguments.of("foo(())", expected),
                Arguments.of("(())", expected),
                Arguments.of("nested=()", expected),
                Arguments.of("num=1, nested=()", expected),
                Arguments.of("num=1,nested=()", expected),
                Arguments.of("num=1,nested=(0)", expected)
        );
    }

    @ParameterizedTest
    @MethodSource("provide_allEqual")
    void test_allEqual(String line, Map<Argument, Object> expectedData) throws InlineException {
        try {
            FooSerializer actual = new FooSerializer().inlineFormat(line);
            FooSerializer expected = new FooSerializer(expectedData);

            assertEquals(expected.getNum(), actual.getNum());
            assertEquals(expected.getNested().getHey(), actual.getNested().getHey());
        } catch (InlineException ex) {
            printException(line, ex);
            throw ex;
        }
    }

    private static Stream<Arguments> provide_allError() {
        return Stream.of(
                Arguments.of("fool(num=1, nested=(0))"),
                Arguments.of("foo(num=hello, nested=(hey=0))"),
                Arguments.of("foo(number=1, nested=(hey=0))"),
                Arguments.of("foo(num=1,, nested=(hey=0))"),
                Arguments.of("foo(num, nested=(0))"),
                Arguments.of("foo(num=0, nested=(0))"),  // error since num=0 out of bounds
                Arguments.of("foo(num=1, nested=(-1))"),  // error since hey=-1 out of bounds
                Arguments.of("foo(num=1)"),
                Arguments.of("foo(num=1, nested=(0)"),
                Arguments.of("num=1, nested=(0))"),
                Arguments.of("foo(1)")
        );
    }

    @ParameterizedTest
    @MethodSource("provide_allError")
    void test_allError(String line) {
        assertThrows(InlineException.class, () -> {
            try {
                new FooSerializer().inlineFormat(line);
            } catch (InlineException ex) {
                printException(line, ex);
                throw ex;
            }
        });
    }

    private static void printException(String line, InlineException ex) {
        int index = ex.getIndex();
        boolean isIndexAccurate = true;
        if (index == -1) {
            if (ex.getLookAfter() != null) index = line.indexOf(ex.getLookAfter());
            index = line.indexOf(ex.getIssue(), index == -1 ? 0 : index);
            isIndexAccurate = false;
        }

        System.out.println(String.join("\n", ex.getException().getMessages()));
        System.out.println(line);
        if (index != -1) System.out.println(StringUtil.repeat(" ", index) + "^" + (isIndexAccurate ? "" : "          (Pointer location may be inaccurate)"));

    }
}
