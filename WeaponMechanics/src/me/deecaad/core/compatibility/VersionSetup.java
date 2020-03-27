package me.deecaad.core.compatibility;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import me.deecaad.weaponmechanics.utils.NumberUtils;
import org.bukkit.Bukkit;

public class VersionSetup {

    /**
     * Example return values:
     * <pre>
     * v1_8_R2
     * v1_11_R1
     * v1_13_R3
     * </pre>
     *
     * @return the server version as string
     */
    public String getVersionAsString() {
        try {
            return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * Example return values when version string is:
     * <pre>{@code
     * v1_8_R2 -> 1.082
     * v1_11_R1 -> 1.111
     * v1_13_R3 -> 1.133
     * }</pre>
     *
     * @param version string version of server version
     * @return the server version as number
     */
    public double getVersionAsNumber(String version) {
        version = version.replaceFirst("v", "");
        version = version.replaceFirst("R", "");
        String[] splitVersion = version.split("_");
        double mainVersion = Double.parseDouble(splitVersion[0]);
        double subVersion = Double.parseDouble(splitVersion[1]) / 100;
        double subSubVersion = Double.parseDouble(splitVersion[2]) / 1000;
        return NumberUtils.getAsRounded(mainVersion + subVersion + subSubVersion, 3);
    }
}