package me.deecaad.core.compatibility;

import me.deecaad.core.compatibility.block.BlockCompatibility;
import me.deecaad.core.compatibility.command.CommandCompatibility;
import me.deecaad.core.compatibility.entity.EntityCompatibility;
import me.deecaad.core.compatibility.nbt.NBTCompatibility;

public final class CompatibilityAPI {

    private static final double version;
    private static final ICompatibility compatibility;
    private static final boolean isPaper;

    static {
        boolean isPaper1;
        VersionSetup versionSetup = new VersionSetup();
        version = versionSetup.getVersionAsNumber(versionSetup.getVersionAsString());
        compatibility = new CompatibilitySetup().getCompatibleVersion(ICompatibility.class, "me.deecaad.core.compatibility");

        try {
            Class.forName("com.desktroystokyo.paper.VersionHistoryManager$VersionData");
            isPaper1 = true;
        } catch (ClassNotFoundException ex) {
            isPaper1 = false;
        }
        isPaper = isPaper1;
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

    public static CommandCompatibility getCommandCompatibility() {
        return compatibility.getCommandCompatibility();
    }
}