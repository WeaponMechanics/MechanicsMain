package me.deecaad.core.compatibility;

import me.deecaad.core.compatibility.block.BlockCompatibility;
import me.deecaad.core.compatibility.entity.EntityCompatibility;
import me.deecaad.core.compatibility.nbt.NBTCompatibility;
import me.deecaad.core.compatibility.vault.IVaultCompatibility;
import me.deecaad.core.compatibility.worldguard.NoWorldGuard;
import me.deecaad.core.compatibility.worldguard.WorldGuardCompatibility;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;

public final class CompatibilityAPI {

    private static final double version;
    private static final ICompatibility compatibility;
    private static final WorldGuardCompatibility worldGuardCompatibility;
    private static IVaultCompatibility vaultCompatibility;
    private static final boolean isPaper;

    static {
        WorldGuardCompatibility worldGuardCompatibility1;
        boolean isPaper1;

        try {
            Class.forName("com.destroystokyo.paper.VersionHistoryManager$VersionData");
            isPaper1 = true;
        } catch (ClassNotFoundException ex) {
            isPaper1 = false;
        }
        isPaper = isPaper1;

        // Set paper ABOVE compatibility stuff
        VersionSetup versionSetup = new VersionSetup();
        version = versionSetup.getVersionAsNumber(versionSetup.getVersionAsString());
        compatibility = new CompatibilitySetup().getCompatibleVersion(ICompatibility.class, "me.deecaad.core.compatibility");

        // * ----- World Guard ----- * //
        try {
            // Check if WorldGuard is there
            Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin");

            // Kinda hacky way to use reflections on own code, but when V6 module and V7 module was both added into lib of MechanicsCompatibility
            // I couldn't compile the code because of odd BukkitAdapter thing
            if (CompatibilityAPI.getVersion() < 1.13) {
                // V6
                Constructor<?> worldGuardV6Constructor = ReflectionUtil.getConstructor(Class.forName("me.deecaad.core.compatibility.worldguard.WorldGuardV6"));
                worldGuardCompatibility1 = (WorldGuardCompatibility) ReflectionUtil.newInstance(worldGuardV6Constructor);
            } else {
                // V7
                Constructor<?> worldGuardV7Constructor = ReflectionUtil.getConstructor(Class.forName("me.deecaad.core.compatibility.worldguard.WorldGuardV7"));
                worldGuardCompatibility1 = (WorldGuardCompatibility) ReflectionUtil.newInstance(worldGuardV7Constructor);
            }
        } catch (ClassNotFoundException e) {
            worldGuardCompatibility1 = new NoWorldGuard();
        }
        worldGuardCompatibility = worldGuardCompatibility1;
    }

    /**
     *
     * Example return values:
     * <pre>{@code
     * v1_8_R2 -> 1.082
     * v1_11_R1 -> 1.111
     * v1_13_R3 -> 1.133
     * }</pre>
     *
     * @return the server version as number
     */
    public static double getVersion() {
        return version;
    }

    public static boolean isPaper() {
        return isPaper;
    }

    public static ICompatibility getCompatibility() {
        return compatibility;
    }

    public static EntityCompatibility getEntityCompatibility() {
        return compatibility.getEntityCompatibility();
    }

    public static BlockCompatibility getBlockCompatibility() {
        return compatibility.getBlockCompatibility();
    }

    public static NBTCompatibility getNBTCompatibility() {
        return compatibility.getNBTCompatibility();
    }

    public static WorldGuardCompatibility getWorldGuardCompatibility() {
        return worldGuardCompatibility;
    }

    public static IVaultCompatibility getVaultCompatibility() {
        if (vaultCompatibility == null) {
            // * ----- Vault ----- * //
            boolean hasVault = Bukkit.getPluginManager().getPlugin("Vault") != null;
            String path = "me.deecaad.core.compatibility.vault." + (hasVault ? "VaultCompatibility" : "NoVaultCompatibility");
            vaultCompatibility = ReflectionUtil.newInstance(ReflectionUtil.getClass(path));
        }
        return vaultCompatibility;
    }
}