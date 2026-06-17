package me.deecaad.weaponmechanics.spike;

import me.deecaad.core.diagnostic.Diagnostic;
import me.deecaad.core.diagnostic.Severity;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SnakeYamlConfig;
import me.deecaad.core.file.verify.SchemaValidator;
import me.deecaad.weaponmechanics.weapon.WeaponSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Validates every bundled example weapon against {@link WeaponSchema#weapon()} and asserts there are
 * NO unknown/typo'd-key warnings (a false positive means the schema is missing a real key). Then
 * plants a bogus key and asserts it IS flagged (proves the detection works).
 */
public class WeaponSchemaVerifyTest {

    private static final Path RESOURCES = Path.of("src/main/resources/WeaponMechanics");
    private static final Path WEAPONS = RESOURCES.resolve("weapons");

    @BeforeEach
    void setUp() {
        // MockBukkit state is global; ensure a clean mock regardless of other tests' ordering.
        try {
            MockBukkit.unmock();
        } catch (Throwable ignored) {
        }
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        try {
            MockBukkit.unmock();
        } catch (Throwable ignored) {
        }
    }

    @Test
    void noFalsePositivesOnBundledConfigs() throws Exception {

        List<String> problems = new ArrayList<>();
        int count = 0;
        count += validateDir(WEAPONS, WeaponSchema.weapon(), problems);
        count += validateDir(RESOURCES.resolve("projectiles"), WeaponSchema.projectileObject(), problems);
        count += validateDir(RESOURCES.resolve("ammos"), WeaponSchema.ammoObject(), problems);

        System.out.println("Validated " + count + " entry(ies) across bundled weapons, projectiles, and ammos.");
        if (!problems.isEmpty()) {
            System.out.println("---- SCHEMA PROBLEMS (" + problems.size() + ") ----");
            problems.forEach(System.out::println);
            fail(problems.size() + " unexpected schema diagnostic(s) on valid configs; see output above.");
        }
    }

    /** Validates every top-level entry in every yml under {@code dir} against {@code schema}. */
    private static int validateDir(Path dir, me.deecaad.core.file.verify.ConfigSchema schema, List<String> problems) throws Exception {
        int count = 0;
        try (Stream<Path> paths = Files.walk(dir)) {
            List<Path> files = paths.filter(p -> p.toString().endsWith(".yml")).sorted().toList();
            for (Path file : files) {
                SnakeYamlConfig config = SnakeYamlConfig.ofFile(file.toFile());
                for (String title : config.getKeys(null, false)) {
                    count++;
                    SerializeData data = new SerializeData(new File(file.getFileName().toString()), title, config);
                    List<Diagnostic> diagnostics = new ArrayList<>();
                    SchemaValidator.validate(schema, data, diagnostics);
                    for (Diagnostic d : diagnostics) {
                        if (d.severity() == Severity.WARNING || d.severity() == Severity.ERROR)
                            problems.add(file.getFileName() + " [" + title + "]: " + d.severity() + " " + d.message());
                    }
                }
            }
        }
        return count;
    }

    @Test
    void plantedBadKeyIsFlagged() throws Exception {

        // The exact bug class that motivated this: a dead key under Damage.
        String yaml = """
            AK_47:
              Damage:
                Base_Damage: 10
                Victim_Mechanics:
                  - 'Sound{sound=entity.arrow.shoot} @Victim'
            """;
        SnakeYamlConfig config = SnakeYamlConfig.ofText(yaml);
        SerializeData data = new SerializeData(new File("planted.yml"), "AK_47", config);
        List<Diagnostic> diagnostics = new ArrayList<>();
        SchemaValidator.validate(WeaponSchema.weapon(), data, diagnostics);

        boolean flagged = diagnostics.stream()
            .anyMatch(d -> d.severity() == Severity.WARNING && d.message().toLowerCase().contains("victim_mechanics"));
        diagnostics.forEach(d -> System.out.println(d.severity() + " " + d.message()));
        assertTrue(flagged, "Expected an unknown-key warning for Damage.Victim_Mechanics");
    }
}
