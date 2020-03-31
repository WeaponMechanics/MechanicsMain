package me.deecaad.weaponmechanics.weapon.info;

import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.utils.TagHelper;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class InfoHandler {

    /**
     * List of all registered weapons
     */
    private static final Set<String> weaponList = new HashSet<>();

    /**
     * List of all weapons with convert option used
     */
    private static final Set<String> weaponsWithConvert = new HashSet<>();

    private WeaponHandler weaponHandler;

    public InfoHandler(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    /**
     * Adds new weapon title to weapon list
     *
     * @param weaponTitle the new weapon title
     */
    public void addWeapon(String weaponTitle) {
        weaponList.add(weaponTitle);
    }

    /**
     * Adds weapon title to weapon converter list
     *
     * @param weaponTitle the weapon title
     */
    public void addWeaponWithConvert(String weaponTitle) {
        weaponsWithConvert.add(weaponTitle);
    }

    /**
     * @param weaponStack the item stack which is supposed to be weapon
     * @param autoConvert whether or not to automatically convert weapon stack to weapon if its valid
     * @return the weapon title or null if not found
     */
    @Nullable
    public String getWeaponTitle(ItemStack weaponStack, boolean autoConvert) {
        if (weaponStack.getType() == Material.AIR) return null;
        String weaponTitle = TagHelper.getCustomTag(weaponStack, CustomTag.WEAPON_TITLE);

        // If its already weapon item stack
        if (weaponTitle != null) return weaponTitle;

        // If its not and auto convert is set to true, try to convert to weapon stack
        return autoConvert ? tryConvertingToWeapon(weaponStack) : null;
    }

    /**
     * Simple method which tries to convert the item stack to weapon stack if its configured to do so.
     *
     * @param possibleWeaponStack the possible weapon stack
     * @return the weapon title or null if not found
     */
    @Nullable
    public String tryConvertingToWeapon(ItemStack possibleWeaponStack) {
        if (weaponsWithConvert.isEmpty()) return null;

        for (String weaponWithConvert : weaponsWithConvert) {
            ItemStack weaponStack = getConfigurations().getItem(weaponWithConvert + ".Info.Weapon_Item");
            if (weaponStack == null) continue;
            WeaponConverter weaponConverter = getConfigurations().getObject(weaponWithConvert + ".Info.Weapon_Converter_Check", WeaponConverter.class);
            if (weaponConverter.isMatch(weaponStack, possibleWeaponStack)) {

                // Convert the possible weapon stack to actually match weapon stack
                possibleWeaponStack.setType(weaponStack.getType());
                possibleWeaponStack.setItemMeta(weaponStack.getItemMeta());

                return weaponWithConvert; // return the weapon title
            }
        }
        return null;
    }

    /**
     * Checks if two weapons are able to work together when holding both of them
     *
     * @param player the player for which to send denied message if not allowed to dual wield
     * @param mainWeaponTitle the main hand weapon title
     * @param offWeaponTitle the off hand weapon title
     * @return whether or not dual wielding is allowed
     */
    public boolean allowDualWielding(TriggerType checkCause, @Nullable Player player, @Nullable String mainWeaponTitle, @Nullable String offWeaponTitle) {
        DualWield mainDualWield = null;

        // Check that main hand weapon allows
        if (mainWeaponTitle != null) {
            mainDualWield = getConfigurations().getObject(mainWeaponTitle + ".Info.Dual_Wield", DualWield.class);

            // Check if works with off hand weapon
            if (mainDualWield != null && mainDualWield.denyDualWieldingWith(offWeaponTitle)) {
                mainDualWield.sendDeniedMessage(checkCause, player, mainWeaponTitle);
                return false;
            }
        }

        DualWield offDualWield = null;

        // Check that off hand weapon allows
        if (offWeaponTitle != null) {
            offDualWield = getConfigurations().getObject(offWeaponTitle + ".Info.Dual_Wield", DualWield.class);

            // Check if works with main hand weapon
            if (offDualWield != null && offDualWield.denyDualWieldingWith(mainWeaponTitle)) {
                offDualWield.sendDeniedMessage(checkCause, player, offWeaponTitle);
                return false;
            }
        }

        if (mainWeaponTitle != null && offWeaponTitle != null
                && mainDualWield == null && offDualWield == null) {
            // Dual wield option wasn't used and both hands had weapons
            // -> disable dual wielding by default
            return false;
        }

        return true;
    }
}