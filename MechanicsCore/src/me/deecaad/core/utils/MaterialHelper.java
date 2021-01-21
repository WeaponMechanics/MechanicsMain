package me.deecaad.core.utils;

import me.deecaad.compatibility.CompatibilityAPI;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Made this into external class in case we decide to use XMaterial resource so its easier to start using it when we only have to modify this class
 */
public class MaterialHelper {

    private static Method getState;
    private static Method getBlock;
    private static Method getDurability;

    static {
        if (CompatibilityAPI.getVersion() < 1.13) {
            getState = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("block.data.CraftBlockData"), "getState");
            getBlock = ReflectionUtil.getMethod(getState.getReturnType(), "getBlock");
            getDurability = ReflectionUtil.getMethod(getBlock.getReturnType(), "getDurability");
        }
    }

    /**
     * Don't let anyone instantiate this class
     */
    private MaterialHelper() { }
    
    /**
     * Simple method to convert string to item stack.
     *
     * @param itemstackString the string containing item stack
     * @return the item stack generated from string
     */
    public static ItemStack fromStringToItemStack(String itemstackString) {
        if (CompatibilityAPI.getVersion() < 1.13) {
            String[] splitted = itemstackString.split(":");
            return new ItemStack(Material.valueOf(splitted[0].trim().toUpperCase()), 1, splitted.length > 1 ? Short.parseShort(splitted[1]) : 0);
        }
        return new ItemStack(Material.valueOf(itemstackString.trim().toUpperCase()));
    }

    /**
     * Gets any materials associated with <code>input</code>. If the
     * <code>input</code> contains a '$' character, then all materials
     * that contain <code>input</code>
     *
     * @param input The input to search for materials
     * @return The found materials
     */
    public static List<Material> parseMaterials(String input) {
        input = input.trim().toUpperCase();

        if (input.startsWith("$")) {

            List<Material> materials = new ArrayList<>();
            String base = input.substring(1);

            for (Material mat : Material.values()) {
                if (mat.name().contains(base)) {
                    materials.add(mat);
                }
            }

            return materials;

        } else {
            return Collections.singletonList(Enums.getIfPresent(Material.class, input).orElse(null));
        }
    }

    /**
     * Checks if the given <code>input</code> would match the given <code>mat</code>,
     * if it were parsed in <code>parseMaterials</code>
     *
     * @see MaterialHelper#parseMaterials(String)
     *
     * @param mat The material
     * @param input The input
     * @return True if the arguments match
     */
    public static boolean matches(Material mat, String input) {
        input = input.trim().toUpperCase();

        if (input.startsWith("$")) {

            String base = input.substring(1);
            return mat.name().contains(base);

        } else {
            return mat.name().equals(input.trim().toUpperCase());
        }
    }

    public static float getBlastResistance(Material type) {
        if (CompatibilityAPI.getVersion() < 1.13) {
            if (isFluid(type)) {
                return 100.0f;
            }

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