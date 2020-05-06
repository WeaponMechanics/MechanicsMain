package me.deecaad.weaponmechanics;

import me.deecaad.core.commands.MainCommand;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.utils.TagHelper;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class WeaponMechanicsAPI {

    private static final String BEFORE_INIT = "Cannot use WeaponMechanicsAPI before it has been initialized";

    private static WeaponMechanics plugin;
    private static Map<String, WeaponHandler> handlers;
    
    public WeaponMechanicsAPI(WeaponMechanics plugin) {
        WeaponMechanicsAPI.plugin = plugin;

        handlers = new HashMap<>(); // todo fill
    }

    /**
     * Gets the <code>WeaponHandler</code> associated with the
     * given weapon title. If no such weapon exists, this method
     * returns null.
     *
     * @param weaponTitle The weapon title
     * @return The WeaponHandler associated with the title
     *
     * @throws IllegalStateException If this method is accessed before initialization
     */
    public static WeaponHandler getWeapon(String weaponTitle) {
        if (plugin == null) {
            throw new IllegalStateException(BEFORE_INIT);
        }

        return handlers.get(weaponTitle);
    }

    /**
     * Gets the <code>WeaponHandler</code> inside the handler
     * map from the given weapon title. If the given weapon
     * <code>ItemStack</code>
     *
     * @param weapon The given weapon
     * @return The WeaponHandler associated with the title
     *
     * @throws IllegalStateException If this method is accessed before initialization
     */
    public static WeaponHandler getWeapon(@Nonnull ItemStack weapon) {
        if (plugin == null) {
            throw new IllegalStateException(BEFORE_INIT);
        } else if (weapon == null) {
            throw new IllegalArgumentException("The given weapon cannot be null!");
        }

        String title = getWeaponTitle(weapon);
        return handlers.get(title);
    }

    /**
     * Gets the weapon title associated with the given weapon,
     * if the given <code>ItemStack</code> is a weapon. Otherwise,
     * this method will return null.
     *
     * @param weapon A nonull item
     * @return The weapon title associated with it
     *
     * @throws IllegalStateException If this method is accessed before initialization
     * @throws IllegalArgumentException If the given item is null
     */
    public static String getWeaponTitle(@Nonnull ItemStack weapon) {
        if (plugin == null) {
            throw new IllegalStateException(BEFORE_INIT);
        } else if (weapon == null) {
            throw new IllegalArgumentException("Weapon cannot be null!");
        }

        return TagHelper.getCustomTag(weapon, CustomTag.WEAPON_TITLE);
    }

    /**
     * If the given projectile was fired by a WeaponMechanics
     * weapon, this will return the weapon title of the weapon
     * that fired it. Otherwise, if the projectile is not
     * a custom projectile this method returns null
     *
     * @param projectile The bukkit projectile
     * @return The weapontitle that shot the projectile
     */
    public static String getWeaponTitle(@Nonnull Projectile projectile) {
        if (plugin == null) {
            throw new IllegalStateException(BEFORE_INIT);
        } else if (projectile == null) {
            throw new IllegalArgumentException("Projectile cannot be null!");
        }

        // projectile.getMetadata("");
        return "";
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
     * @throws IllegalStateException If this method is accessed before initialization
     */
    public static MainCommand getCommand() {
        if (plugin == null) {
            throw new IllegalStateException(BEFORE_INIT);
        }

        return WeaponMechanics.getMainCommand();
    }

    /**
     * Returns the the main plugin instance, or null
     * if the api has not been initialized yet.
     *
     * @return Main weaponmechanics instance
     */
    public static WeaponMechanics getInstance() {
        return plugin;
    }
    
}
