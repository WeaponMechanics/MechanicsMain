package me.deecaad.compatibility;

import me.deecaad.compatibility.block.BlockCompatibility;
import me.deecaad.compatibility.entity.EntityCompatibility;
import me.deecaad.compatibility.item.nbt.INBTCompatibility;

public class CompatibilityAPI {

    private static final double version;
    private static final ICompatibility compatibility;

    static {
        VersionSetup versionSetup = new VersionSetup();
        version = versionSetup.getVersionAsNumber(versionSetup.getVersionAsString());
        compatibility = new CompatibilitySetup().getCompatibleVersion(ICompatibility.class, "me.deecaad.compatibility");
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

    public static ICompatibility getCompatibility() {
        return compatibility;
    }

    public static EntityCompatibility getEntityCompatibility() {
        return compatibility.getEntityCompatibility();
    }

    public static BlockCompatibility getBlockCompatibility() {
        return compatibility.getBlockCompatibility();
    }

    public static INBTCompatibility getNBTCompatibility() {
        return compatibility.getNBTCompatibility();
    }
}