package me.deecaad.weaponmechanics.weapon.info;

import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.utils.TagHelper;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class InfoHandler {

    /**
     * List of all registered weapons
     */
    private static final Set<String> weaponList = new HashSet<>();

    /**
     * List of all registered weapons in sorted order.
     * This is here just to make it more efficient to use list command.
     */
    private static final List<String> sortedWeaponList = new ArrayList<>();

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
        if (!sortedWeaponList.contains(weaponTitle)) {
            sortedWeaponList.add(weaponTitle);
            Collections.sort(sortedWeaponList);
        }
    }

    public boolean hasWeapon(String weaponTitle) {
        return weaponList.contains(weaponTitle);
    }

    /**
     * @return the list of all registered weapons in sorted order
     */
    public List<String> getSortedWeaponList() {
        return sortedWeaponList;
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
        String weaponTitle = TagHelper.getStringTag(weaponStack, CustomTag.WEAPON_TITLE);

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
            ItemStack weaponStack = getConfigurations().getObject(weaponWithConvert + ".Info.Weapon_Item", ItemStack.class).clone();
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
     * If weapon title is invalid, its silently ignored.
     * All general weapon get actions are used.
     *
     * @param weaponTitle the weapon to give
     * @param player the player for who to give
     * @param amount the amount of weapons to give
     */
    public void giveOrDropWeapon(String weaponTitle, Player player, int amount) {
        ItemStack weaponStack = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Item", ItemStack.class).clone();
        weaponStack.setAmount(amount);

        // Check for weapon title typos silently
        if (weaponStack == null) return;

        Inventory inventory = player.getInventory();

        // Check if inventory doesn't have any free slots
        if (inventory.firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation().add(0.0, 1.0, 0.0), weaponStack);
            return;
        }
        inventory.addItem(weaponStack);

        Mechanics.use(weaponTitle + ".Info.Weapon_Get_Actions", new CastData(WeaponMechanics.getEntityWrapper(player), weaponTitle, weaponStack));
    }

    /**
     * Checks if two weapons are able to work together when holding both of them
     *
     * @param player the player for which to send denied message if not allowed to dual wield
     * @param mainWeaponTitle the main hand weapon title
     * @param offWeaponTitle the off hand weapon title
     * @return whether or not dual wielding is allowed
     */
    public boolean denyDualWielding(TriggerType checkCause, @Nullable Player player, @Nullable String mainWeaponTitle, @Nullable String offWeaponTitle) {
        DualWield mainDualWield = null;

        // Check that main hand weapon allows
        if (mainWeaponTitle != null) {
            mainDualWield = getConfigurations().getObject(mainWeaponTitle + ".Info.Dual_Wield", DualWield.class);

            // Check if works with off hand weapon
            if (mainDualWield != null && mainDualWield.denyDualWieldingWith(offWeaponTitle)) {
                mainDualWield.sendDeniedMessage(checkCause, player, mainWeaponTitle);
                return true;
            }
        }

        DualWield offDualWield = null;

        // Check that off hand weapon allows
        if (offWeaponTitle != null) {
            offDualWield = getConfigurations().getObject(offWeaponTitle + ".Info.Dual_Wield", DualWield.class);

            // Check if works with main hand weapon
            if (offDualWield != null && offDualWield.denyDualWieldingWith(mainWeaponTitle)) {
                offDualWield.sendDeniedMessage(checkCause, player, offWeaponTitle);
                return true;
            }
        }

        // Dual wield option wasn't used and both hands had weapons
        // -> disable dual wielding by default
        // If this is false, then dual wielding is allowed
        return mainWeaponTitle != null && offWeaponTitle != null
                && mainDualWield == null && offDualWield == null;
    }
}