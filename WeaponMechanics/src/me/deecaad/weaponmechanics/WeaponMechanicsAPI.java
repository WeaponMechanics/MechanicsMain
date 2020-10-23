package me.deecaad.weaponmechanics;

import me.deecaad.core.commands.MainCommand;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.utils.TagHelper;
import me.deecaad.weaponmechanics.weapon.damage.BlockDamageData;
import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class WeaponMechanicsAPI implements Listener {

    private static final String BEFORE_INIT = "Cannot use WeaponMechanicsAPI before it has been initialized";

    private static WeaponMechanics plugin;

    /**
     * Only let the WeaponMechanics plugin instantiate this class
     */
    WeaponMechanicsAPI(WeaponMechanics plugin) {
        if (WeaponMechanicsAPI.plugin != null)
            throw new UnsupportedOperationException("Cannot redefine singleton plugin");

        WeaponMechanicsAPI.plugin = plugin;
    }

    /**
     * Gets the weapon title associated with the given weapon,
     * if the given <code>ItemStack</code> is a weapon. Otherwise,
     * this method will return null.
     *
     * @param weapon A nonnull item
     * @return The weapon title associated with it, or null
     *
     * @throws IllegalStateException If this method is invoked before initialization
     * @throws IllegalArgumentException If the given item is null
     */
    public static String getWeaponTitle(@Nonnull ItemStack weapon) {
        if (plugin == null)
            throw new IllegalStateException(BEFORE_INIT);
        else if (weapon == null)
            throw new IllegalArgumentException("Weapon cannot be null!");

        return TagHelper.getStringTag(weapon, CustomTag.WEAPON_TITLE);
    }

    /**
     * Gets the plugin's <code>MainCommand</code>. Useful if you want
     * to add your plugins <code>SubCommand</code>s to this plugin
     *
     * For instructions on sub command use, see the <a href="TODO">wiki</a>
     *
     * @see me.deecaad.core.commands.MainCommand
     * @see me.deecaad.core.commands.SubCommand
     * @return The main command
     *
     * @throws IllegalStateException If this method is invoked before initialization
     */
    public static MainCommand getCommand() {
        if (plugin == null)
            throw new IllegalStateException(BEFORE_INIT);

        return WeaponMechanics.getMainCommand();
    }

    /**
     * Gets a <code>Set</code> of active projectiles associated with
     * the given <code>shooter</code>
     *
     * @param shooter The shooter of the projectiles
     * @return A set of all of the active projectiles, or null if the shooter has not shot a projectile
     */
    @Nullable
    public static Set<ICustomProjectile> getActiveProjectilesFor(LivingEntity shooter) {
        IEntityWrapper wrapper = getEntityWrapper(shooter);

        // return wrapper.getActiveProjectiles();
        return null;
    }

    /**
     * Gets a copy of the weapon <code>ItemStack</code> for the weapon
     * with the given <code>weaponTitle</code>.
     *
     * @param weaponTitle The weaponTitle of the weapon
     * @return The nonnull weapon item
     *
     * @throws IllegalStateException If this method is invoked before initialization
     * @throws IllegalArgumentException If the given weaponTitle is invalid
     */
    @Nonnull
    public static ItemStack generateWeapon(String weaponTitle) {
        if (plugin == null)
            throw new IllegalStateException(BEFORE_INIT);

        ItemStack item;
        try {
            item = WeaponMechanics.getConfigurations().getObject(weaponTitle, ItemStack.class);
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException("Invalid weaponTitle, path was not an item", ex);
        }

        if (item == null)
            throw new IllegalArgumentException("Invalid weaponTitle, path had no item");

        return item.clone();
    }

    /**
     * Returns <code>true</code> if the given bukkit <code>Block</code>
     * was broken by WeaponMechanics from an <code>Explosion</code> or
     * an external plugin that calls the damage method.
     *
     * @see BlockDamageData#damageBlock(Block, int, int, boolean, int)
     *
     * @param block Which block to check
     * @return true if the block is broken, otherwise false
     */
    public static boolean isBroken(@Nonnull Block block) {
        if (plugin == null)
            throw new IllegalStateException(BEFORE_INIT);
        else if (block == null)
            throw new IllegalArgumentException("block cannot be null");

        return BlockDamageData.isBroken(block);
    }

    /**
     * Gets the <code>IEntityWrapper</code> associated with the given
     * <code>entity</code>, or instantiates one if there is none.
     *
     * @param entity The living entity
     * @return The wrapper
     */
    @Nonnull
    public static IEntityWrapper getEntityWrapper(LivingEntity entity) {
        return WeaponMechanics.getEntityWrapper(entity);
    }

    /**
     * Gets the <code>IPlayerWrapper</code> associated with the given
     * <code>player</code>, or instantiates one if there is none.
     *
     * @param player The player
     * @return The wrapper
     */
    @Nonnull
    public static IPlayerWrapper getPlayerWrapper(Player player) {
        return WeaponMechanics.getPlayerWrapper(player);
    }
}