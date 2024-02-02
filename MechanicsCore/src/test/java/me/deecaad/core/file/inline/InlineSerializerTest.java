package me.deecaad.core.file.inline;

import me.deecaad.core.file.InlineSerializer;
import me.deecaad.core.file.MapConfigLike;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static me.deecaad.core.file.InlineSerializer.UNIQUE_IDENTIFIER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InlineSerializerTest {

    private static Stream<Arguments> provide_allEqual() {
        Map<String, Object> expected = new HashMap<>(Map.of(
            UNIQUE_IDENTIFIER, "foo",
            "num", "1",
            "nested", new HashMap<>(Map.of(UNIQUE_IDENTIFIER, "nested", "hey", "0"))));
        return Stream.of(
            Arguments.of("foo{num=1, nested=nested{hey=0}}", expected),
            Arguments.of("foo{num=1,nested=nested{hey=0}}", expected),
            Arguments.of("foo {num=1, nested=nested{hey=0}}", expected),
            Arguments.of("foo{nested=nested{hey=0}, num=1}", expected));
    }

    @ParameterizedTest
    @MethodSource("provide_allEqual")
    void test_allEqual(String line, Map<String, Object> expectedData) throws InlineSerializer.FormatException {
        Map<String, Object> actual = remap(InlineSerializer.inlineFormat(line));
        Assertions.assertEquals(expectedData, actual);
    }

    private static Stream<Arguments> provide_allError() {
        return Stream.of(
            // Just some easy syntax errors
            Arguments.of("foo{{num=1, nested=nested{hey=0}}"),
            Arguments.of("foo{num=1, nested=nested{hey=0}"),
            Arguments.of("foo{num, nested=nested{hey=0}}"),
            Arguments.of("foo{num=1, nested}"),
            Arguments.of("foo{num==1, nested=nested{hey=0}}"),

            // List related syntax errors
            Arguments.of("foo{num[0, 1, 2]}"),
            Arguments.of("foo{num=[0, 1,, 2]}"),
            Arguments.of("foo{num=[[0, 1]}"),
            Arguments.of("foo{num=list[0, 1]}"),
            Arguments.of("foo{num=list[0=hi, 1=mom]}"));
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
        String line = "item{name=<yellow>AK-47, lore=[russians drink vodka, and shoot ak-47s]}";
        Map<?, ?> expected = Map.of(
            UNIQUE_IDENTIFIER, "item",
            "name", "<yellow>AK-47",
            "lore", List.of("russians drink vodka", "and shoot ak-47s"));

        Map<?, ?> actual = remap(InlineSerializer.inlineFormat(line));
        assertEquals(expected, actual);
    }

    @Test
    void test_nestedList() throws InlineSerializer.FormatException {
        String line = "firework{effects=[{color=red}, {shape=burst, color=green}, {color=blue, flicker=true}]}";
        Map<?, ?> expected = Map.of(
            UNIQUE_IDENTIFIER, "firework",
            "effects", List.of(
                Map.of("color", "red"),
                Map.of("shape", "burst", "color", "green"),
                Map.of("color", "blue", "flicker", "true")));

        Map<?, ?> actual = remap(InlineSerializer.inlineFormat(line));
        assertEquals(expected, actual);
    }

    private static Stream<Arguments> provide_escapedCharacters() {
        return Stream.of(
            Arguments.of("Key_1", Map.of(UNIQUE_IDENTIFIER, "foo", "msg", "<green>Hello, there", "response", "<red>GENERAL KENOBI")),
            Arguments.of("Key_2", Map.of(UNIQUE_IDENTIFIER, "foo", "messages", List.of("Escaped]", "escaped,", "you like jazz?"))),
            Arguments.of("Key_3", Map.of(UNIQUE_IDENTIFIER, "foo", "escaped", "\\ Slash /")));
    }

    @ParameterizedTest
    @MethodSource("provide_escapedCharacters")
    void test_escapedCharacters(String key, Map<?, ?> expected) throws InlineSerializer.FormatException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/escaped-characters.yml")));
        YamlConfiguration config = YamlConfiguration.loadConfiguration(reader);

        String line = config.getString(key);
        Map<?, ?> actual = remap(InlineSerializer.inlineFormat(line));
        assertEquals(expected, actual);
    }

    private static Map<String, Object> remap(Map<String, MapConfigLike.Holder> map) {
        Map<String, Object> temp = new HashMap<>();
        map.forEach((key, holder) -> {
            if (holder.value() instanceof Map<?, ?> nested)
                temp.put(key, remap((Map<String, MapConfigLike.Holder>) nested));
            else if (holder.value() instanceof List<?> nested)
                temp.put(key, remap((List<MapConfigLike.Holder>) nested));
            else
                temp.put(key, holder.value());
        });
        return temp;
    }

    private static List<Object> remap(List<MapConfigLike.Holder> list) {
        return list.stream().map(holder -> {
            if (holder.value() instanceof Map<?, ?> nested)
                return remap((Map<String, MapConfigLike.Holder>) nested);
            else
                return holder.value();
        }).toList();
    }
}
