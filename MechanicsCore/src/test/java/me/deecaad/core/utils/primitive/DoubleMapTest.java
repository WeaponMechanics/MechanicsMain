package me.deecaad.core.utils.primitive;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class DoubleMapTest {

    private static DoubleMap<String> provideDoubles() {
        DoubleMap<String> doubles = new DoubleMap<>();
        doubles.put("Zombie", 20.0);
        doubles.put("Player", 20.0);
        doubles.put("Wither", 200.0);
        doubles.put("Bat", 8.0);
        doubles.put("Spider", 16.0);
        doubles.put("Enderman", 30.0);

        return doubles;
    }

    @ParameterizedTest
    @CsvSource({"Zombie,20.0", "Hoglin,0.0", "Player,20.0", "Wither,200.0", "Cave Spider,0.0", "Bat,8.0", "Spider,16.0", "Enderman,30.0"})
    public void test_get(String key, double expected) {
        DoubleMap<String> doubles = provideDoubles();
        double actual = doubles.get(key);

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({"Zombie,true", "Dragon,false", "Player,true", "Bat,true", "Wither,true", "Magma,false"})
    public void test_containsKey(String key, boolean expected) {
        DoubleMap<String> doubles = provideDoubles();
        boolean actual = doubles.containsKey(key);

        assertEquals(expected, actual);
    }

    @Test
    public void test_emptyMap() {
        DoubleMap<String> empty = new DoubleMap<>();

        assertTrue(empty.isEmpty());
        assertEquals(0, empty.size());

        // Test a few keys/values
        assertFalse(empty.containsKey("String"));
        assertFalse(empty.containsKey("Zombie"));
        assertFalse(empty.containsKey("Warden"));
        assertFalse(empty.containsValue(10.5));
        assertFalse(empty.containsValue(0.0));

        // Fail if it loops at all
        empty.forEach((key, value) -> {
            fail();
        });

        // Return default number
        assertEquals(0.0, empty.get("Hi"));
        assertEquals(0.0, empty.get("Hello"));
    }
}
