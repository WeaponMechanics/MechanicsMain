package me.deecaad.weaponmechanics.weapon.reload.ammo;

import me.deecaad.core.file.BukkitConfig;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.Registry;
import me.deecaad.core.utils.FileUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class AmmoRegistry {

    public static final Registry<Ammo> AMMO_REGISTRY = new Registry<>("ammo");

    public static void init() {
        AMMO_REGISTRY.clear(); // For reloads

        File ammoFolder = new File(WeaponMechanics.getPlugin().getDataFolder(), "ammos");
        if (!ammoFolder.exists())
            return;

        try {
            FileUtil.PathReference pathReference = FileUtil.PathReference.of(ammoFolder.toURI());
            Files.walkFileTree(pathReference.path(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    InputStream stream = Files.newInputStream(file);
                    YamlConfiguration config = new YamlConfiguration();

                    try {
                        config.load(new InputStreamReader(stream));
                    } catch (InvalidConfigurationException ex) {
                        WeaponMechanics.debug.warn("Could not read file '" + file.toFile() + "'... make sure it is valid YAML",
                                ex.getMessage());
                        return FileVisitResult.CONTINUE;
                    }

                    for (String key : config.getKeys(false)) {
                        try {
                            SerializeData data = new SerializeData(new Ammo(), file.toFile(), key, new BukkitConfig(config));
                            Ammo ammo = data.of().serialize(new Ammo());
                            AmmoRegistry.AMMO_REGISTRY.add(ammo);
                        } catch (SerializerException ex) {
                            ex.log(WeaponMechanics.debug);
                        }
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Throwable ex) {
            WeaponMechanics.debug.log(LogLevel.ERROR, "Some error occurred whilst reading ammos folder", ex);
        }

        WeaponMechanics.debug.info("Loaded " + AmmoRegistry.AMMO_REGISTRY.getOptions().size() + " ammo types");
    }
}
