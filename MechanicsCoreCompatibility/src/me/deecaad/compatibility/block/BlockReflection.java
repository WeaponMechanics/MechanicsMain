package me.deecaad.compatibility.block;

import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;

public class BlockReflection implements BlockCompatibility {

    private static Class<?> worldServer;

    private static Constructor<?> blockPosConstructor;
    private static Constructor<?> packetBlockBreakAnimationConstructor;

    public BlockReflection() {
        // All initialized in the constructor and not in the
        // static constructor because this class may never be
        // instantiated
        Class<?> blockPosClass = ReflectionUtil.getNMSClass("BlockPosition");
        Class<?> packetBlockBreakAnimationClass = ReflectionUtil.getNMSClass("PacketPlayOutBlockBreakAnimation");
        blockPosConstructor = ReflectionUtil.getConstructor(blockPosClass, int.class, int.class, int.class);
        packetBlockBreakAnimationConstructor = ReflectionUtil.getConstructor(packetBlockBreakAnimationClass, int.class, blockPosClass, int.class);

        worldServer = ReflectionUtil.getNMSClass("WorldServer");
    }

    @Override
    public Object getCrackPacket(Block block, int crack) {

        int id = IDS.incrementAndGet();
        if (id == Integer.MAX_VALUE) {
            IDS.set(0);
        }

        Object blockPos = ReflectionUtil.newInstance(blockPosConstructor, block.getX(), block.getY(), block.getZ());
        return ReflectionUtil.newInstance(packetBlockBreakAnimationConstructor, id, blockPos, crack);
    }

    @Override
    public Object createFallingBlock(Block block, Vector vector) {
        return null;
    }
}
