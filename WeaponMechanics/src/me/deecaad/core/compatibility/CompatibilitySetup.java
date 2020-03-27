package me.deecaad.core.compatibility;

import me.deecaad.core.compatibility.versions.ICompatibility;
import me.deecaad.core.compatibility.versions.ReflectionCompatibility;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Bukkit;

public class CompatibilitySetup {

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
     * @return the compatible version as ICompatibility
     */
    public ICompatibility getCompatibleVersion() {
        String version = getVersionAsString();
        try {
            Class<?> compatibilityClass = Class.forName("me.deecaad.core.compatibility.versions." + version);
            ICompatibility compatibility = (ICompatibility) ReflectionUtil.newInstance(ReflectionUtil.getConstructor(compatibilityClass));
            return compatibility != null ? compatibility : new ReflectionCompatibility();
        } catch (ClassNotFoundException e) {
            return new ReflectionCompatibility();
        }
    }
}