package me.deecaad.core.file;

import me.deecaad.core.file.simple.BooleanSerializer;
import me.deecaad.core.file.simple.DoubleSerializer;
import me.deecaad.core.file.simple.IntSerializer;
import me.deecaad.core.file.simple.StringSerializer;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SerializerListTest {

    public static final Serializer<?> DUMMY = new Serializer<>() {
        @NotNull @Override
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

        try {
            List<List<Optional<Object>>> list = data.ofList("Valid")
                .addArgument(new StringSerializer())
                .addArgument(new IntSerializer(0, null))
                .requireAllPreviousArgs()
                .addArgument(new DoubleSerializer(0.0, 1.0))
                .addArgument(new BooleanSerializer())
                .addArgument(new IntSerializer())
                .assertList();

            for (List<Optional<Object>> split : list) {
                String str = (String) split.get(0).get();
                int positive = (Integer) split.get(1).get();
                double decimal = (Double) split.get(2).orElse(0.0);
                boolean bool = (Boolean) split.get(3).orElse(false);
                int i = (Integer) split.get(4).orElse(0);
            }
        } catch (SerializerException e) {
            e.getMessages().forEach(System.err::println);
            System.err.println(e.getLocation());
            throw e;
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"Invalid_0", "Invalid_1", "Invalid_2", "Invalid_3", "Invalid_4", "Invalid_5", "Invalid_6", "Invalid_7"})
    public void test_invalid(String key) {
        SerializeData data = new SerializeData(DUMMY, file, "a", new BukkitConfig(config));

        assertThrows(SerializerException.class, () -> data.ofList()
            .addArgument(new StringSerializer())
            .addArgument(new IntSerializer(0, null))
            .requireAllPreviousArgs()
            .addArgument(new DoubleSerializer(0.0, 1.0))
            .addArgument(new BooleanSerializer())
            .addArgument(new IntSerializer())
            .assertList());
    }
}
