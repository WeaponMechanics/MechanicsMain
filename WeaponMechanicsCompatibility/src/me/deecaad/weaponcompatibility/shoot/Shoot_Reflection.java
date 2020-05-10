package me.deecaad.weaponcompatibility.shoot;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Shoot_Reflection implements IShootCompatibility {

    private static Method entityGetHandle;
    private static Field entityWidth;
    private static Field entityHeight;

    private static Set<?> RELATIVE_FLAGS;
    private static Set<?> ABSOLUTE_FLAGS;
    private static Constructor<?> packetPlayOutPositionConstructor;

    public Shoot_Reflection() {
        entityGetHandle = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("entity.CraftEntity"), "getHandle");
        entityWidth = ReflectionUtil.getField(ReflectionUtil.getNMSClass("Entity"), "width");
        entityHeight = ReflectionUtil.getField(ReflectionUtil.getNMSClass("Entity"), "length");

        Enum<?> x = Enum.valueOf((Class<Enum>) ReflectionUtil.getNMSClass("PacketPlayOutPosition$EnumPlayerTeleportFlags"), "X");
        Enum<?> y = Enum.valueOf((Class<Enum>) ReflectionUtil.getNMSClass("PacketPlayOutPosition$EnumPlayerTeleportFlags"), "Y");
        Enum<?> z = Enum.valueOf((Class<Enum>) ReflectionUtil.getNMSClass("PacketPlayOutPosition$EnumPlayerTeleportFlags"), "Z");
        Enum<?> xRot = Enum.valueOf((Class<Enum>) ReflectionUtil.getNMSClass("PacketPlayOutPosition$EnumPlayerTeleportFlags"), "X_ROT");
        Enum<?> yRot = Enum.valueOf((Class<Enum>) ReflectionUtil.getNMSClass("PacketPlayOutPosition$EnumPlayerTeleportFlags"), "Y_ROT");

        RELATIVE_FLAGS = new HashSet<>(Arrays.asList(x, y, z, xRot, yRot));
        ABSOLUTE_FLAGS = new HashSet<>(Arrays.asList(x, y, z));

        packetPlayOutPositionConstructor = CompatibilityAPI.getVersion() < 1.09
                ? ReflectionUtil.getConstructor(ReflectionUtil.getNMSClass("PacketPlayOutPosition"), double.class, double.class, double.class, float.class, float.class, Set.class)
                : ReflectionUtil.getConstructor(ReflectionUtil.getNMSClass("PacketPlayOutPosition"), double.class, double.class, double.class, float.class, float.class, Set.class, int.class);
    }

    @Override
    public double getWidth(Entity entity) {
        if (CompatibilityAPI.getVersion() >= 1.12) {
            return entity.getWidth();
        }
        Object nmsEntity = ReflectionUtil.invokeMethod(entityGetHandle, entity);
        return (float) ReflectionUtil.invokeField(entityWidth, nmsEntity);
    }

    @Override
    public double getHeight(Entity entity) {
        if (CompatibilityAPI.getVersion() >= 1.12) {
            return entity.getHeight();
        }
        Object nmsEntity = ReflectionUtil.invokeMethod(entityGetHandle, entity);
        return (float) ReflectionUtil.invokeField(entityHeight, nmsEntity);
    }

    @Override
    public void modifyCameraRotation(Player player, float yaw, float pitch, boolean absolute) {
        pitch *= -1;
        Object packetPlayOutPosition = CompatibilityAPI.getVersion() < 1.09
                ? ReflectionUtil.newInstance(packetPlayOutPositionConstructor, 0.0, 0.0, 0.0, yaw, pitch, absolute ? ABSOLUTE_FLAGS : RELATIVE_FLAGS)
                : ReflectionUtil.newInstance(packetPlayOutPositionConstructor, 0.0, 0.0, 0.0, yaw, pitch, absolute ? ABSOLUTE_FLAGS : RELATIVE_FLAGS, 0);
        CompatibilityAPI.getCompatibility().sendPackets(player, packetPlayOutPosition);
    }
}