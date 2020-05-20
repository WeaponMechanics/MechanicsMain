package me.deecaad.compatibility.block;

import me.deecaad.core.utils.NumberUtils;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.block.Block;

import java.lang.reflect.Constructor;

public class BlockReflection implements BlockCompatibility {

    private static Class<?> blockPosClass;
    private static Class<?> packetBlockBreakAnimationClass;
    private static Constructor<?> blockPosConstructor;
    private static Constructor<?> packetBlockBreakAnimationConstructor;

    public BlockReflection() {
        blockPosClass = ReflectionUtil.getNMSClass("BlockPosition");
        packetBlockBreakAnimationClass = ReflectionUtil.getNMSClass("PacketPlayOutBlockBreakAnimation");
        blockPosConstructor = ReflectionUtil.getConstructor(blockPosClass, int.class, int.class, int.class);
        packetBlockBreakAnimationConstructor = ReflectionUtil.getConstructor(packetBlockBreakAnimationClass, int.class, blockPosClass, int.class);
    }

    @Override
    public Object getCrackPacket(Block block, int crack) {

        // While it's weird to generate a random int for
        // an id, it's apparently valid
        //
        // This will also cause the block to "stop" cracking
        // randomly (If the id is duplicated)
        int id = NumberUtils.random(1000);


        Object blockPos = ReflectionUtil.newInstance(blockPosConstructor, block.getX(), block.getY(), block.getZ());
        return ReflectionUtil.newInstance(packetBlockBreakAnimationConstructor, id, blockPos, crack);
    }
}
