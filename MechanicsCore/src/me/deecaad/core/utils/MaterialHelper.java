package me.deecaad.core.utils;

import me.deecaad.compatibility.CompatibilityAPI;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

/**
 * Made this into external class in case we decide to use XMaterial resource so its easier to start using it when we only have to modify this class
 */
public class MaterialHelper {

    private static Method getState;
    private static Method getBlock;
    private static Method getDurability;

    /**
     * Don't let anyone instantiate this class
     */
    private MaterialHelper() { }

    private static void reflectionSetup() {
        if (CompatibilityAPI.getVersion() < 1.13) {
            getState = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("block.data.CraftBlockData"), "getState");
            getBlock = ReflectionUtil.getMethod(getState.getReturnType(), "getBlock");
            getDurability = ReflectionUtil.getMethod(getBlock.getReturnType(), "getDurability");
        }
    }
    
    /**
     * Simple method to convert string to item stack.
     *
     * @param itemstackString the string containing item stack
     * @return the item stack generated from string
     */
    public static ItemStack fromStringToItemStack(String itemstackString) {
        if (CompatibilityAPI.getVersion() < 1.13) {
            String[] splitted = itemstackString.split(":");
            return new ItemStack(Material.valueOf(splitted[0].toUpperCase()), 1, splitted.length > 1 ? Short.parseShort(splitted[1]) : 0);
        }
        return new ItemStack(Material.valueOf(itemstackString.toUpperCase()));
    }

    public static float getBlastResistance(Material type) {
        if (CompatibilityAPI.getVersion() < 1.13) {
            if (isFluid(type)) {
                return 100.0f;
            }

            reflectionSetup();

            BlockData data = type.createBlockData();

            Object nmsData = ReflectionUtil.invokeMethod(getState, data);
            Object nmsBlock = ReflectionUtil.invokeMethod(getBlock, nmsData);

            return (float) ReflectionUtil.invokeMethod(getDurability, nmsBlock);
        } else {
            return type.getBlastResistance();
        }
    }

    public static boolean isAir(Material type) {
        if (CompatibilityAPI.getVersion() < 1.13) return type.name().equals("AIR");
        else return type.isAir();
    }

    public static boolean isFluid(Material type) {
        String name = type.name();
        return name.equals("WATER")
                || name.equals("LAVA")
                || name.equals("STATIONARY_WATER")
                || name.equals("STATIONARY_LAVA");
    }
}