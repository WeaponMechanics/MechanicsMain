package me.deecaad.weaponmechanics.spike;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.FileReader;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SnakeYamlConfig;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.mechanics.program.MechanicSerializer;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.io.File;
import java.util.List;

/**
 * SPIKE (throwaway): "does MockBukkit let us run the serializers without a real server?"
 * Escalating phases, each prints PASS/FAIL with the failure, so one run shows how far we get.
 */
public class SerializerSpikeTest {

    private static void banner(String s) {
        System.out.println("\n================ " + s + " ================");
    }

    @AfterEach
    void tearDown() {
        try {
            MockBukkit.unmock();
        } catch (Throwable ignored) {
        }
    }

    @Test
    void spike() {
        // -------- Phase 1: bare mock server + Bukkit registry --------
        banner("PHASE 1: MockBukkit.mock() + Bukkit registry");
        try {
            ServerMock server = MockBukkit.mock();
            System.out.println("[OK] server = " + server.getClass().getName());
            System.out.println("[OK] Material.STONE = " + Material.STONE);
            ItemStack probe = new ItemStack(Material.DIAMOND_SWORD);
            System.out.println("[OK] new ItemStack + meta = " + (probe.getItemMeta() != null));
        } catch (Throwable t) {
            System.out.println("[FAIL] phase 1");
            t.printStackTrace(System.out);
            return;
        }

        // -------- Phase 2: load MechanicsCore (metrics skipped) --------
        banner("PHASE 2: loadWith(TestMechanicsCore) [skips bStats]");
        try {
            PluginDescriptionFile desc = new PluginDescriptionFile("MechanicsCore", "26.1.0",
                TestMechanicsCore.class.getName());
            Object loaded = MockBukkit.loadWith(TestMechanicsCore.class, desc);
            System.out.println("[OK] loaded instance = " + loaded);
            System.out.println("[..] test-view MechanicsCore.class CL = " + MechanicsCore.class.getClassLoader());
            System.out.println("[..] loaded instance class       CL = " + loaded.getClass().getClassLoader());
            System.out.println("[..] loaded superclass == test MechanicsCore.class ? "
                + (loaded.getClass().getSuperclass() == MechanicsCore.class));
            System.out.println("[..] MechanicsCore.getInstance() (test view) = " + MechanicsCore.getInstance());
        } catch (Throwable t) {
            System.out.println("[FAIL] phase 2");
            t.printStackTrace(System.out);
        }

        // -------- Phase 3: load WeaponMechanics (metrics + jar-scan skipped) --------
        banner("PHASE 3: loadWith(TestWeaponMechanics) [skips bStats + jar scan]");
        try {
            PluginDescriptionFile desc = new PluginDescriptionFile("WeaponMechanics", "26.1.0",
                TestWeaponMechanics.class.getName());
            MockBukkit.loadWith(TestWeaponMechanics.class, desc);
            System.out.println("[OK] WeaponMechanics.getInstance() = " + WeaponMechanics.getInstance());
            System.out.println("[OK] getConfiguration() != null = "
                + (WeaponMechanics.getInstance().getConfiguration() != null));
        } catch (Throwable t) {
            System.out.println("[FAIL] phase 3");
            t.printStackTrace(System.out);
        }

        // -------- Phase 4: run real serializers over ofText() YAML, no files --------
        banner("PHASE 4: FileReader + ofText -> Item + Mechanics (registry path)");
        try {
            MechanicsCore core = MechanicsCore.getInstance();
            List<Serializer<?>> serializers = List.of(new ItemSerializer(), new MechanicSerializer());
            List<IValidator> validators = List.of();
            FileReader reader = new FileReader(core.getDebugger(), serializers, validators);

            String yaml = """
                Demo:
                  Item:
                    Type: DIAMOND_SWORD
                    Name: '<red>Test Blade'
                  Some_Mechanics:
                    - Sound{sound=ENTITY_GENERIC_EXPLODE}
                """;
            SnakeYamlConfig config = SnakeYamlConfig.ofText(yaml);
            System.out.println("[OK] ofText keys = " + config.getKeys("", false));

            Configuration result = reader.fillOneFile(config, new File("spike-virtual.yml"));
            Object item = result.getObject("Demo.Item");
            Object mechanics = result.getObject("Demo.Some_Mechanics");
            System.out.println("[OK] Item     -> " + describe(item));
            System.out.println("[OK] Mechanics -> " + describe(mechanics));
        } catch (Throwable t) {
            System.out.println("[FAIL] phase 4");
            t.printStackTrace(System.out);
        }

        banner("SPIKE DONE");
    }

    private static String describe(Object o) {
        return o == null ? "null" : (o + " (" + o.getClass().getName() + ")");
    }
}
