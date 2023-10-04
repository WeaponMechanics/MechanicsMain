package me.deecaad.core.file;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SerializerListTest {

    public static final Serializer<?> DUMMY = new Serializer<>() {
        @NotNull
        @Override
        public Object serialize(@NotNull SerializeData data) throws SerializerException {
            throw new RuntimeException();
        }
    };

    private File file;
    private FileConfiguration config;


    @BeforeEach
    void setUp() {
        file = new File("list-config.yml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/list-config.yml")));
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
    @ValueSource(strings = {"Valid"})
    public void test_valid(String key) throws Exception {
        SerializeData data = new SerializeData(DUMMY, file, "a", new BukkitConfig(config));

        List<String[]> list = data.ofList(key)
                .addArgument(String.class, true)
                .addArgument(int.class, true).assertArgumentPositive()
                .addArgument(double.class, false).assertArgumentRange(0.0, 1.0)
                .addArgument(boolean.class, false)
                .addArgument(int.class, false)
                .assertExists().assertList().get();

        for (String[] split : list) {
            String str = split[0];
            int positive = Integer.parseInt(split[1]);
            double decimal = split.length > 2 ? Double.parseDouble(split[2]) : 0.0;
            boolean bool = split.length > 3 ? Boolean.parseBoolean(split[3]) : false;
            int i = split.length > 4 ? Integer.parseInt(split[4]) : 0;
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"Invalid_0", "Invalid_1", "Invalid_2", "Invalid_3", "Invalid_4", "Invalid_5", "Invalid_6", "Invalid_7"})
    public void test_invalid(String key) {
        SerializeData data = new SerializeData(DUMMY, file, "a", new BukkitConfig(config));

        assertThrows(SerializerException.class, () -> data.ofList(key)
                .addArgument(String.class, true)
                .addArgument(int.class, true).assertArgumentPositive()
                .addArgument(double.class, false).assertArgumentRange(0.0, 1.0)
                .addArgument(boolean.class, false)
                .addArgument(int.class, false)
                .assertExists().assertList().get());
    }
}
