package me.deecaad.core.compatibility;

import me.deecaad.core.utils.MinecraftVersions;
import me.deecaad.core.utils.ReflectionUtil;
import org.jetbrains.annotations.Nullable;

public class CompatibilitySetup {

    /**
     * Example return values:
     * 
     * <pre>
     * v1_8_R2
     * v1_11_R1
     * v1_13_R3
     * </pre>
     *
     * @return the server version as string
     */
    public String getVersionAsString() {
        return MinecraftVersions.getCURRENT().toProtocolString();
    }

    /**
     * @param interfaceClazz the compatibility interface type
     * @param directory the directory in code where compatibility should exist
     * @return the compatible version from given directory
     */
    @Nullable public <T> T getCompatibleVersion(Class<T> interfaceClazz, String directory) {
        String version = getVersionAsString();
        try {
            Class<?> compatibilityClass = Class.forName(directory + "." + version, false, interfaceClazz.getClassLoader());
            Object compatibility = ReflectionUtil.newInstance(ReflectionUtil.getConstructor(compatibilityClass));
            return compatibility != null ? interfaceClazz.cast(compatibility) : null;
        } catch (ClassNotFoundException | ClassCastException e) {
            // Do nothing
        }
        return null;
    }
}