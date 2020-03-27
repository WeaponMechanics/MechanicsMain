package me.deecaad.weaponmechanics;

import me.deecaad.core.commands.MainCommand;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class WeaponMechanicsAPI {
    
    private static WeaponMechanics plugin = (WeaponMechanics) WeaponMechanics.getPlugin();
    private static Map<String, WeaponHandler> handlers = new HashMap<>();
    
    static {
        // give handlers the weapons
    }
    
    /**
     * Gets the <code>WeaponHandler</code> associated with
     * the given weapon title. Returns null if the weapon
     * does not exist.
     *
     * @param weaponTitle Name of weapon in config
     * @return The WeaponHandler
     */
    public static WeaponHandler getWeapon(String weaponTitle) {
        return handlers.get(weaponTitle);
    }
    
    /**
     * Gets the <code>WeaponHandler</code> associated with
     * the given <code>ItemStack</code>. Returns null if the
     * item is not a weapon
     *
     * @param weapon The itemstack that is the gun
     * @return The weaponhandler for the gun
     */
    public static WeaponHandler getWeapon(ItemStack weapon) {
        return getWeapon(getWeaponTitle(weapon));
    }
    
    public static String getWeaponTitle(ItemStack item) {
        String name = item.getItemMeta().getDisplayName();
        return null;
    }
    
    public static MainCommand getCommand() {
        return plugin.getMainCommand();
    }
    
    public static WeaponMechanics getInstance() {
        return plugin;
    }
    
}
