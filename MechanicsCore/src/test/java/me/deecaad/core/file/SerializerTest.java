package me.deecaad.core.file;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import static  org.junit.jupiter.api.Assertions.*;

public class SerializerTest {

    private File file;
    private FileConfiguration config;

    @BeforeEach
    void setUp() {
        file = new File("test-config.yml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/test-config.yml")));
        config = YamlConfiguration.loadConfiguration(reader);

        System.out.println(file);
        System.out.println(config.getKeys(false));
        System.out.println();
    }

    @AfterEach
    void tearDown() {
        file = null;
        config = null;
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5})
    public void test_parseInvalid(int i) {
        SerializeData data = new SerializeData(new Square(), file, "Squares.Invalid." + i, config);
        assertThrows(SerializerException.class, () ->  data.of().serialize(Square.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    public void test_parseValid(int i) {
        SerializeData data = new SerializeData(new Square(), file, "Squares.Invalid." + i, config);
        assertThrows(SerializerException.class, () ->  data.of().serialize(Square.class));
    }
}
