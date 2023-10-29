package me.deecaad.weaponmechanics.weapon.info;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.utils.AdventureUtil;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.shoot.CustomDurability;
import me.deecaad.weaponmechanics.weapon.skin.SkinSelector;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponGenerateEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import java.util.*;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class InfoHandler implements IValidator {

    /**
     * List of all registered weapons
     */
    private final Set<String> weaponList = new LinkedHashSet<>();

    /**
     * List of all registered weapons in sorted order.
     * This is here just to make it more efficient to use list command.
     */
    private final List<String> sortedWeaponList = new ArrayList<>();

    /**
     * List of all weapons with convert option used
     */
    private final Set<String> weaponsWithConvert = new LinkedHashSet<>();

    private WeaponHandler weaponHandler;

    /**
     * Default constructor for validator
     */
    public InfoHandler() {
    }

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
    @NotNull
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

    public boolean hasPermission(LivingEntity player, String weaponTitle) {
        return player.hasPermission("weaponmechanics.use.*")
                || player.hasPermission("weaponmechanics.use." + weaponTitle);
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

        // Applying placeholders has to be done via adventure, else we lose formatting
        AdventureUtil.updatePlaceholders(null, weaponStack);

        // Apply default skin
        SkinSelector skins = getConfigurations().getObject(weaponTitle + ".Skin", SkinSelector.class);
        if (skins != null)
            skins.getDefaultSkin().apply(weaponStack);

        return weaponStack;
    }


    public boolean giveOrDropWeapon(String weaponTitle, LivingEntity entity, int amount) {
        return giveOrDropWeapon(weaponTitle, entity, amount, Collections.emptyMap());
    }

    public boolean giveOrDropWeapon(String weaponTitle, LivingEntity entity, int amount, Map<String, Object> data) {
        if (weaponTitle == null || entity == null || amount < 1 || data == null)
            throw new IllegalArgumentException("Bad args: " + weaponTitle + ", " + entity + ", " + amount + ", " + data);

        ItemStack weaponStack = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Item", ItemStack.class);
        if (weaponStack == null)
            return false;

        weaponStack = weaponStack.clone();
        weaponStack.setAmount(amount);

        // Parse and apply all possible data
        int ammo = (int) data.getOrDefault("ammo", -1);
        int firemode = (int) data.getOrDefault("firemode", -1);
        boolean skipMainhand = ((int) data.getOrDefault("skipMainhand", 0)) == 1;
        int slot = (int) data.getOrDefault("slot", -1);
        int durability = (int) data.getOrDefault("durability", -1);
        int maxDurability = (int) data.getOrDefault("maxDurability", -1);

        if (slot != -1) skipMainhand = true;
        if (ammo != -1) CustomTag.AMMO_LEFT.setInteger(weaponStack, ammo);
        if (firemode != -1) CustomTag.SELECTIVE_FIRE.setInteger(weaponStack, firemode);

        // Custom Durability Arguments
        CustomDurability customDurability = WeaponMechanics.getConfigurations().getObject(weaponTitle + ".Shoot.Custom_Durability", CustomDurability.class);
        if (customDurability != null) {
            CustomTag.DURABILITY.setInteger(weaponStack, durability == -1 ? customDurability.getMaxDurability() : durability);
            CustomTag.MAX_DURABILITY.setInteger(weaponStack, maxDurability == -1 ? customDurability.getMaxDurability() : maxDurability);
        }

        // Let other plugins modify generated weapons (for example, to add attachments)
        Bukkit.getPluginManager().callEvent(new WeaponGenerateEvent(weaponTitle, weaponStack, entity, data));

        boolean isPlayer = entity instanceof Player;
        Player player = isPlayer ? (Player) entity : null;

        if (isPlayer) {
            AdventureUtil.updatePlaceholders(player, weaponStack);
        }

        // Apply default skin
        SkinSelector skins = getConfigurations().getObject(weaponTitle + ".Skin", SkinSelector.class);
        if (skins != null) {
            skins.getDefaultSkin().apply(weaponStack);
        }

        // First we should try to put the gun in the entity's main hand. If
        // a player's mainhand is full, we can try to put it in their
        // inventory. For any other entity, though, they won't be given
        // a weapon if their mainhand is full.
        EntityEquipment equipment = entity.getEquipment();
        if ((equipment.getItemInMainHand() == null || equipment.getItemInMainHand().getType() == Material.AIR) && (!isPlayer || !skipMainhand))
            equipment.setItemInMainHand(weaponStack);
        else if (isPlayer && slot != -1)
            player.getInventory().setItem(slot, weaponStack);
        else if (isPlayer && player.getInventory().firstEmpty() != -1)
            player.getInventory().addItem(weaponStack);
        else if (isPlayer) {
            entity.getWorld().dropItemNaturally(entity.getLocation().add(0.0, 1.0, 0.0), weaponStack);
            return false; // Return so we don't play mechanics
        } else
            return false;


        Mechanics weaponGetMechanics = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Get_Mechanics", Mechanics.class);
        if (weaponGetMechanics != null) weaponGetMechanics.use(new CastData(entity, weaponTitle, weaponStack));

        return true;
    }

    /**
     * Checks if two weapons are able to work together when holding both of them
     *
     * @param player the player for which to send denied message if not allowed to dual wield
     * @param mainWeaponTitle the main hand weapon title
     * @param offWeaponTitle the off hand weapon title
     * @return whether dual wielding is allowed
     */
    public boolean denyDualWielding(TriggerType checkCause, @Nullable Player player, @Nullable String mainWeaponTitle, @Nullable String offWeaponTitle) {

        // Just simple check whether other hand is empty anyway
        if (mainWeaponTitle == null || offWeaponTitle == null) return false;

        // Check that main hand weapon allows
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

        // Check that off hand weapon allows
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

    @Override
    public String getKeyword() {
        return "Info";
    }

    public List<String> getAllowedPaths() {
        return Collections.singletonList(".Info");
    }

    @Override
    public void validate(Configuration configuration, SerializeData data) throws SerializerException {
        int weaponEquipDelay = data.of("Weapon_Equip_Delay").assertPositive().getInt(0);
        if (weaponEquipDelay != 0) {
            // Convert to millis
            configuration.set(data.key + ".Weapon_Equip_Delay", weaponEquipDelay * 50);
        }
    }
}