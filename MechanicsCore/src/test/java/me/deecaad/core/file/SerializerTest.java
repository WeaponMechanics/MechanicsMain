package me.deecaad.core.file;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;

import static  org.junit.jupiter.api.Assertions.*;

public class SerializerTest {

    private File file;
    private FileConfiguration config;

    @BeforeEach
    void setUp() {
        file = new File("test-config.yml");
        config = YamlConfiguration.loadConfiguration(file);
    }

    @AfterEach
    void tearDown() {
        file = null;
        config = null;
    }

    @Test
    public void test_parseInvalid() {
        for (int i = 0; config.contains("Squares.Invalid." + i); i++) {
            SerializeData data = new SerializeData(new Square(), file, "Squares.Invalid." + i, config);

            assertThrows(SerializerException.class, () ->  data.serializer.serialize(data));
        }
    }

    @Test
    public void test_parseValid() {
        for (int i = 0; config.contains("Squares.Invalid." + i); i++) {
            SerializeData data = new SerializeData(new Square(), file, "Squares.Invalid." + i, config);

            assertDoesNotThrow(() ->  data.serializer.serialize(data));
        }
    }
}
