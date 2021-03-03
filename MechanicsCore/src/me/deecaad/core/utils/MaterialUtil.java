package me.deecaad.core.utils;

import me.deecaad.compatibility.CompatibilityAPI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * This final utility class outlines static methods that operate on or return
 * bukkit materials, blocks, or items. This class also contains methods to
 * assist in parsing materials from a {@link String} input.
 */
public final class MaterialUtil {

    private static Method getState;
    private static Method getBlock;
    private static Method getDurability;

    static {
        if (CompatibilityAPI.getVersion() < 1.13) {
            // todo
        }
    }

    // Don't let anyone instantiate this class
    private MaterialUtil() { }

    /**
     * Returns a bukkit item based on the input string.
     *
     * @param itemStackString The non-null formatted string:
     *                        <code>&lt;material&gt;:&lt;data&gt;</code>
     * @return The non-null, parsed bukkit item.
     * @throws NumberFormatException           If data after the ':' was not a
     *                                         valid short.
     * @throws StringIndexOutOfBoundsException If there was no number put after
     *                                         the ':'.
     * @throws IllegalArgumentException        If there is no {@link Material}
     *                                         with the provided name.
     */
    public static ItemStack fromStringToItemStack(String itemStackString) {
        itemStackString = itemStackString.trim().toUpperCase();

        int index = itemStackString.lastIndexOf(':');
        Optional<Material> mat;
        short data;

        if (index == -1) {
            mat = EnumUtil.getIfPresent(Material.class, itemStackString);
            data = 0;
        } else {
            mat = EnumUtil.getIfPresent(Material.class, itemStackString.substring(0, index));
            data = Short.parseShort(itemStackString.substring(index + 1));
        }

        if (!mat.isPresent()) {
            throw new IllegalArgumentException("Unknown material for input: " + (index == -1 ? itemStackString : itemStackString.substring(0, index)));
        }

        if (CompatibilityAPI.getVersion() < 1.13) {
            return new ItemStack(mat.get(), 1, data);
        } else {
            return new ItemStack(mat.get());
        }
    }

    /**
     * Returns an immutable list of bukkit materials that match the input. If
     * the <code>input</code> starts with a <code>$</code>, all materials that
     * contain the input are added to the list.
     *
     * <p>Otherwise, this method returns an immutable list of 0 or 1 materials.
     *
     * @param input The input matcher. If the input starts with a $, it matches
     *              multiple materials.
     * @return A non-null, immutable list of all parsed materials.
     * @see Collections#unmodifiableList(List)
     * @see Collections#singletonList(Object)
     * @see Collections#emptyList()
     */
    public static List<Material> parseMaterials(String input) {
        return EnumUtil.parseEnums(Material.class, input);
    }

    /**
     * Checks if the given <code>input</code> would match the given
     * <code>mat</code>, if it were parsed in {@link #parseMaterials(String)}.
     *
     * @param mat   The material to check
     * @param input The input check against
     * @return <code>true</code> if the arguments match
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

    /**
     * Returns a float representing the blast resistance, or the resistance to
     * explosions, of a specific {@link Material}. In legacy minecraft
     * versions, we have to rely on version dependant code to get the blast
     * resistance.
     *
     * <p>TODO Add a compatibility method to {@link me.deecaad.compatibility.block.BlockCompatibility}
     *
     * @param type
     * @return
     */
    public static float getBlastResistance(Material type) {
        if (CompatibilityAPI.getVersion() < 1.13) {
            if (isFluid(type)) {
                return 100.0f;
            }

            // todo
            return 0.0f;
        } else {
            return type.getBlastResistance();
        }
    }

    /**
     * Returns <code>true</code> if the given {@link Material} is empty. In
     * legacy minecraft versions, {@link Material#AIR} was the only air block.
     * In newer versions, there are multiple air blocks.
     *
     * @param type The material to check.
     * @return <code>true</code> if the material is air.
     * @see Material#CAVE_AIR
     * @see Material#VOID_AIR
     */
    public static boolean isAir(Material type) {
        if (CompatibilityAPI.getVersion() < 1.13) return type.name().equals("AIR");
        else return type.isAir();
    }

    /**
     * Returns <code>true</code> if the given {@link Material} is a fluid.
     *
     * @param type The material to check.
     * @return <code>true</code> if the material is fluid.
     */
    public static boolean isFluid(Material type) {
        String name = type.name();
        return name.equals("WATER")
                || name.equals("LAVA")
                || name.equals("STATIONARY_WATER")
                || name.equals("STATIONARY_LAVA");
    }
}