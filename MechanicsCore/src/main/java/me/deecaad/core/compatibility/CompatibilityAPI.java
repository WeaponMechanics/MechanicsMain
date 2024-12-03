package me.deecaad.core.compatibility;

import com.cjcrafter.foliascheduler.util.ConstructorInvoker;
import com.cjcrafter.foliascheduler.util.MinecraftVersions;
import com.cjcrafter.foliascheduler.util.ReflectionUtil;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.compatibility.block.BlockCompatibility;
import me.deecaad.core.compatibility.entity.EntityCompatibility;
import me.deecaad.core.compatibility.nbt.NBTCompatibility;
import me.deecaad.core.compatibility.vault.IVaultCompatibility;
import me.deecaad.core.compatibility.worldguard.NoWorldGuard;
import me.deecaad.core.compatibility.worldguard.WorldGuardCompatibility;
import me.deecaad.core.utils.LogLevel;
import org.bukkit.Bukkit;

public final class CompatibilityAPI {

    private static ICompatibility compatibility;
    private static WorldGuardCompatibility worldGuardCompatibility;
    private static IVaultCompatibility vaultCompatibility;
    private static boolean isPaper;

    static {
        try {
            boolean isPaper1;
            try {
                Class.forName("com.destroystokyo.paper.VersionHistoryManager$VersionData");
                isPaper1 = true;
            } catch (ClassNotFoundException ex) {
                isPaper1 = false;
            }
            isPaper = isPaper1;

            compatibility = new CompatibilitySetup().getCompatibleVersion(ICompatibility.class, "me.deecaad.core.compatibility");

            // This happens when a server is using an unsupported version of
            // minecraft, like 1.18.1, 1.8.8, etc.
            if (compatibility == null) {
                MechanicsCore.debug.error("Unsupported server version: " + Bukkit.getVersion() + " (" + Bukkit.getBukkitVersion() + ")",
                    "Remember that MechanicsCore supports all major versions 1.12.2+, HOWEVER it doesn't support outdated versions",
                    "For example, 1.18.1 is NOT a support version, but 1.18.2 IS a supported version",
                    "If you are running a brand new version of Minecraft, ask DeeCaaD or CJCrafter to update the plugin",
                    "",
                    "!!! CRITICAL ERROR !!!");
            }

            // * ----- World Guard ----- * //
            WorldGuardCompatibility worldGuardCompatibility1;
            try {
                // Check if WorldGuard is there
                Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin");
                if (!MinecraftVersions.UPDATE_AQUATIC.isAtLeast()) {
                    // World Guard V6 for 1.12.2 support
                    ConstructorInvoker<?> worldGuardV6Constructor = ReflectionUtil.getConstructor(Class.forName("me.deecaad.core.compatibility.worldguard.WorldGuardV6"));
                    worldGuardCompatibility1 = (WorldGuardCompatibility) worldGuardV6Constructor.newInstance();
                } else {
                    // World Guard V7 for 1.13+ support
                    ConstructorInvoker<?> worldGuardV7Constructor = ReflectionUtil.getConstructor(Class.forName("me.deecaad.core.compatibility.worldguard.WorldGuardV7"));
                    worldGuardCompatibility1 = (WorldGuardCompatibility) worldGuardV7Constructor.newInstance();
                }
            } catch (Throwable e) {
                worldGuardCompatibility1 = new NoWorldGuard();
            }
            worldGuardCompatibility = worldGuardCompatibility1;
        } catch (Throwable ex) {
            MechanicsCore.debug.log(LogLevel.ERROR, "Failed to init CompatibilityAPI", ex);
        }
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
            ConstructorInvoker<?> vaultCompatibilityConstructor = ReflectionUtil.getConstructor(ReflectionUtil.getClass(path));
            vaultCompatibility = (IVaultCompatibility) vaultCompatibilityConstructor.newInstance();
        }
        return vaultCompatibility;
    }
}