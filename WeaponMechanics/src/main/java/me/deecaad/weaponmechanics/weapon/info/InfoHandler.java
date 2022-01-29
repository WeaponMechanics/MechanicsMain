package me.deecaad.weaponmechanics.weapon.info;

import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.skin.Skin;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class InfoHandler {

    /**
     * List of all registered weapons
     */
    private final Set<String> weaponList = new HashSet<>();

    /**
     * List of all registered weapons in sorted order.
     * This is here just to make it more efficient to use list command.
     */
    private final List<String> sortedWeaponList = new ArrayList<>();

    /**
     * List of all weapons with convert option used
     */
    private final Set<String> weaponsWithConvert = new HashSet<>();

    private WeaponHandler weaponHandler;

    public InfoHandler(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    /**
     * Returns the weapon-title that is most similar to the method parameter.
     * This method will <i>always</i> return a valid weapon title, regardless
     * of how inaccurate the parameter is. Meaning, yes, <code>AK-47</code>
     * could return <code>RPG</code>.
     *
     * @param weapon The non-null "inaccurate" weapon-title.
     * @return The non-null valid weapon-title.
     */
    @Nonnull
    public String getWeaponTitle(String weapon) {

        // Before checking for similarities, do a startsWith check (since
        // players usually like to press enter before they finish typing
        // the full weapon-title)
        List<String> startsWith = new ArrayList<>();
        for (String title : weaponList) {
            if (title.toLowerCase(Locale.ROOT).startsWith(weapon.toLowerCase(Locale.ROOT)))
                startsWith.add(title);
        }

        return StringUtil.didYouMean(weapon, startsWith.isEmpty() ? weaponList : startsWith);
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
        return new ArrayList<>(sortedWeaponList);
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
        if (weaponStack == null || weaponStack.getType() == Material.AIR) return null;

        String weaponTitle = CustomTag.WEAPON_TITLE.getString(weaponStack);

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

    public ItemStack generateWeapon(String weaponTitle, int amount) {
        ItemStack weaponStack = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Item", ItemStack.class);

        // Check for weapon title typos silently
        if (weaponStack == null) {
            return null;
        }

        weaponStack = weaponStack.clone();
        weaponStack.setAmount(amount);

        ItemMeta weaponMeta = weaponStack.getItemMeta();
        weaponMeta.setDisplayName(PlaceholderAPI.applyPlaceholders(weaponMeta.getDisplayName(), null, weaponStack, weaponTitle));
        weaponMeta.setLore(PlaceholderAPI.applyPlaceholders(weaponMeta.getLore(), null, weaponStack, weaponTitle));
        weaponStack.setItemMeta(weaponMeta);

        // Apply default skin
        Map skins = getConfigurations().getObject(weaponTitle + ".Skin", Map.class);
        Skin defaultSkin = (Skin) (skins == null ? null : skins.get("Default"));
        if (defaultSkin != null) {
            defaultSkin.apply(weaponStack);
        }

        return weaponStack;
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
        ItemStack weaponStack = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Item", ItemStack.class);
        if (weaponStack == null) return;
        weaponStack = weaponStack.clone();
        weaponStack.setAmount(amount);

        ItemMeta weaponMeta = weaponStack.getItemMeta();
        weaponMeta.setDisplayName(PlaceholderAPI.applyPlaceholders(weaponMeta.getDisplayName(), player, weaponStack, weaponTitle));
        weaponMeta.setLore(PlaceholderAPI.applyPlaceholders(weaponMeta.getLore(), player, weaponStack, weaponTitle));
        weaponStack.setItemMeta(weaponMeta);

        // Apply default skin
        Map skins = getConfigurations().getObject(weaponTitle + ".Skin", Map.class);
        Skin defaultSkin = (Skin) (skins == null ? null : skins.get("Default"));
        if (defaultSkin != null) {
            defaultSkin.apply(weaponStack);
        }

        Inventory inventory = player.getInventory();

        // Check if inventory doesn't have any free slots
        if (inventory.firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation().add(0.0, 1.0, 0.0), weaponStack);
            return;
        }
        inventory.addItem(weaponStack);

        Mechanics.use(weaponTitle + ".Info.Weapon_Get_Mechanics", new CastData(WeaponMechanics.getEntityWrapper(player), weaponTitle, weaponStack));
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

        // Just simple check whether other hand is empty anyway
        if (mainWeaponTitle == null && offWeaponTitle != null || offWeaponTitle == null && mainWeaponTitle != null) return false;

        // Check that main hand weapon allows
        if (mainWeaponTitle != null) {
            DualWield mainDualWield = getConfigurations().getObject(mainWeaponTitle + ".Info.Dual_Wield", DualWield.class);
            if (mainDualWield == null) {
                // Doesn't allow since its null
                return true;
            }

            // Check if works with off hand weapon
            if (mainDualWield.denyDualWieldingWith(offWeaponTitle)) {
                mainDualWield.sendDeniedMessage(checkCause, player, mainWeaponTitle);
                return true;
            }
        }

        // Check that off hand weapon allows
        if (offWeaponTitle != null) {
            DualWield offDualWield = getConfigurations().getObject(offWeaponTitle + ".Info.Dual_Wield", DualWield.class);
            if (offDualWield == null) {
                // Doesn't allow since its null
                return true;
            }

            // Check if works with main hand weapon
            if (offDualWield.denyDualWieldingWith(mainWeaponTitle)) {
                offDualWield.sendDeniedMessage(checkCause, player, offWeaponTitle);
                return true;
            }
        }

        return false;
    }

    private static int[] mapToCharTable(String str) {
        int[] table = new int["abcdefghijklmnopqrstuvwxyz".length()];
        for (int i = 0; i < str.length(); i++) {
            try {
                table[Character.toLowerCase(str.charAt(i)) - 97]++;
            } catch (ArrayIndexOutOfBoundsException ignore) {
                // Sometimes a string will contain something like an underscore.
            }
        }
        return table;
    }
}