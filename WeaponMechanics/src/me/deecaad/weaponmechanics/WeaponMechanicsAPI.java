package me.deecaad.weaponmechanics;

import me.deecaad.core.commands.MainCommand;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.utils.TagHelper;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class WeaponMechanicsAPI {

    private static final String BEFORE_INIT = "Cannot use WeaponMechanicsAPI before it has been initialized";

    private static WeaponMechanics plugin;
    
    public WeaponMechanicsAPI(WeaponMechanics plugin) {
        WeaponMechanicsAPI.plugin = plugin;
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
