package me.deecaad.core.utils;

import me.deecaad.core.compatibility.CompatibilityAPI;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
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
     * Returns a float representing the blast resistance, or the resistance to
     * explosions, of a specific {@link Material}. In legacy minecraft
     * versions, we have to rely on version dependant code to get the blast
     * resistance.
     *
     * @param type The non-null bukkit block.
     * @return The positive resistance to explosions.
     */
    public static float getBlastResistance(@Nonnull Block type) {
        return CompatibilityAPI.getBlockCompatibility().getBlastResistance(type);
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