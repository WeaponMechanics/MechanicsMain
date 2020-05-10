package me.deecaad.compatibility;

public class CompatibilityAPI {

    private static double version;
    private static ICompatibility compatibility;

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
        if (version == 0.0) {
            VersionSetup versionSetup = new VersionSetup();
            version = versionSetup.getVersionAsNumber(versionSetup.getVersionAsString());
        }
        return version;
    }

    /**
     * If compatibility isn't set up this will automatically set it up
     *
     * @return the compatible version as ICompatibility
     */
    public static ICompatibility getCompatibility() {
        if (compatibility == null) {
            compatibility = new CompatibilitySetup().getCompatibleVersion();
        }
        return compatibility;
    }

    /**
     * This should be called when reloading or shutting down server!
     */
    public static void onDisable() {
        version = 0.0;
        compatibility = null;
    }
}