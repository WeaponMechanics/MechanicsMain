package me.deecaad.weaponmechanics.weapon.shoot;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ChanceSerializer;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles breaking the weapon over a certain number of shots, so weapons do
 * not last forever.
 */
public class CustomDurability implements Serializer<CustomDurability> {

    // Cached common unbreaking values.
    private static final double[] ARMOR_LEVELS = new double[] { 1.0, 0.80, 0.73, 0.70, 0.68, 0.67, 0.66, 0.65, 0.64, 0.64, 0.64 };
    private static final double[] TOOL_LEVELS = new double[]  { 1.0, 0.50, 0.33, 0.25, 0.20, 0.17, 0.14, 0.12, 0.11, 0.10, 0.09 };

    private int maxDurability;
    private int minMaxDurability;
    private int loseMaxDurabilityPerRepair;
    private int durabilityPerShot;
    private double chance;
    private ItemStack replaceItem;
    private Mechanics durabilityMechanics;
    private Mechanics breakMechanics;

    // Repair options
    private Map<ItemStack, Integer> repairItems;
    private int repairPerExp;
    private Mechanics repairMechanics;
    private Mechanics denyRepairMechanics;
    private boolean repairOnlyBroken;

    /**
     * Default constructor for serializer
     */
    public CustomDurability() {
    }

    public CustomDurability(int maxDurability, int minMaxDurability, int loseMaxDurabilityPerRepair, int durabilityPerShot,
                            double chance, ItemStack replaceItem, Mechanics durabilityMechanics, Mechanics breakMechanics,
                            Map<ItemStack, Integer> repairItems, int repairPerExp, Mechanics repairMechanics, Mechanics denyRepairMechanics,
                            boolean repairOnlyBroken) {
        this.maxDurability = maxDurability;
        this.minMaxDurability = minMaxDurability;
        this.loseMaxDurabilityPerRepair = loseMaxDurabilityPerRepair;
        this.durabilityPerShot = durabilityPerShot;
        this.chance = chance;
        this.replaceItem = replaceItem;
        this.durabilityMechanics = durabilityMechanics;
        this.breakMechanics = breakMechanics;
        this.repairItems = repairItems;
        this.repairPerExp = repairPerExp;
        this.repairMechanics = repairMechanics;
        this.denyRepairMechanics = denyRepairMechanics;
        this.repairOnlyBroken = repairOnlyBroken;
    }

    public int getMaxDurability() {
        return maxDurability;
    }

    public void setMaxDurability(int maxDurability) {
        this.maxDurability = maxDurability;
    }

    public int getMinMaxDurability() {
        return minMaxDurability;
    }

    public void setMinMaxDurability(int minMaxDurability) {
        this.minMaxDurability = minMaxDurability;
    }

    public int getLoseMaxDurabilityPerRepair() {
        return loseMaxDurabilityPerRepair;
    }

    public void setLoseMaxDurabilityPerRepair(int loseMaxDurabilityPerRepair) {
        this.loseMaxDurabilityPerRepair = loseMaxDurabilityPerRepair;
    }

    public int getDurabilityPerShot() {
        return durabilityPerShot;
    }

    public void setDurabilityPerShot(int durabilityPerShot) {
        this.durabilityPerShot = durabilityPerShot;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public ItemStack getReplaceItem() {
        return replaceItem;
    }

    public void setReplaceItem(ItemStack replaceItem) {
        this.replaceItem = replaceItem;
    }

    public Mechanics getDurabilityMechanics() {
        return durabilityMechanics;
    }

    public void setDurabilityMechanics(Mechanics durabilityMechanics) {
        this.durabilityMechanics = durabilityMechanics;
    }

    public Mechanics getBreakMechanics() {
        return breakMechanics;
    }

    public void setBreakMechanics(Mechanics breakMechanics) {
        this.breakMechanics = breakMechanics;
    }

    public Map<ItemStack, Integer> getRepairItems() {
        return repairItems;
    }

    public void setRepairItems(Map<ItemStack, Integer> repairItems) {
        this.repairItems = repairItems;
    }

    public int getRepairPerExp() {
        return repairPerExp;
    }

    public void setRepairPerExp(int repairPerExp) {
        this.repairPerExp = repairPerExp;
    }

    public Mechanics getRepairMechanics() {
        return repairMechanics;
    }

    public void setRepairMechanics(Mechanics repairMechanics) {
        this.repairMechanics = repairMechanics;
    }

    public Mechanics getDenyRepairMechanics() {
        return denyRepairMechanics;
    }

    public void setDenyRepairMechanics(Mechanics denyRepairMechanics) {
        this.denyRepairMechanics = denyRepairMechanics;
    }

    public boolean isRepairOnlyBroken() {
        return repairOnlyBroken;
    }

    public void setRepairOnlyBroken(boolean repairOnlyBroken) {
        this.repairOnlyBroken = repairOnlyBroken;
    }

    /**
     * Returns the maximum possible durability that this item can be repaired
     * up to. The more often you repair an item, the less max-durability it
     * has. This prevents users from re-using the same item forever.
     *
     * <p>It is assumed that the given item is a weapon that uses THIS instance
     * of custom durability.
     *
     * @param item The non-null weapon to check.
     * @return The maximum repair durability.
     */
    public int getMaxDurability(ItemStack item) {
        if (CustomTag.MAX_DURABILITY.hasInteger(item)) {
            return CustomTag.MAX_DURABILITY.getInteger(item);
        }

        return maxDurability;
    }

    /**
     * Shorthand to use the default 'loseMaxDurabilityPerRepair' with
     * {@link #modifyMaxDurability(ItemStack, int)}.
     *
     * @param item The item to modify.
     * @return The new max durability (might be negative).
     */
    public int modifyMaxDurability(ItemStack item) {
        return modifyMaxDurability(item, loseMaxDurabilityPerRepair);
    }

    /**
     * Subtracts from the given weapon's max durability. This method should be
     * called whenever the item is repaired. The item's current durability
     * <b>WILL</b> exceed the max durability, since this subtraction occurs
     * <b>AFTER</b> the item is repaired.
     *
     * @param item   The item to modify.
     * @param change The amount to subtract from max-durability.
     * @return The new max durability (might be negative)
     */
    public int modifyMaxDurability(ItemStack item, int change) {
        if (change < 0)
            change = loseMaxDurabilityPerRepair;

        int max = Math.max(getMaxDurability(item) - change, minMaxDurability);
        CustomTag.MAX_DURABILITY.setInteger(item, max);
        return max;
    }

    /**
     * Applies the durability effects to the given item, as if the item has
     * just been used. It is assumed that the given item is a weapon. If the
     * weapon was broken by this usage, this method will return
     * <code>true</code>.
     *
     * @param entity The non-null entity used for {@link Mechanics}.
     * @param item   The non-null weapon to lose durability.
     * @return true if the item was broken.
     */
    public boolean use(LivingEntity entity, ItemStack item, String weaponTitle) {
        ItemMeta meta = item.getItemMeta();

        // Check chance and unbreaking.
        int unbreakingLevel = meta == null ? 0 : meta.getEnchantLevel(Enchantment.DURABILITY);
        if (!NumberUtil.chance(chance) || !isLoseDurability(unbreakingLevel, false))
            return false;

        // Durability has never been applied to the weapon, so we need to
        // set the max durability - durabilityPerShot
        if (!CustomTag.DURABILITY.hasInteger(item)) {
            CustomTag.DURABILITY.setInteger(item, maxDurability - durabilityPerShot);
            CustomTag.MAX_DURABILITY.setInteger(item, maxDurability);
            return false;
        }

        int durability = CustomTag.DURABILITY.getInteger(item) - durabilityPerShot;
        if (durability > 0) {
            if (durabilityMechanics != null) durabilityMechanics.use(new CastData(entity, weaponTitle, item));
            CustomTag.DURABILITY.setInteger(item, durability);
            return false;
        }

        // When the item has broken, we have to either destroy it or replace
        // the broken item with the 'replaceItem'
        if (breakMechanics != null) breakMechanics.use(new CastData(entity, weaponTitle, item));
        if (replaceItem == null) {
            item.setAmount(0);
            return true;
        }

        ItemStack template = replaceItem.clone();
        CompatibilityAPI.getNBTCompatibility().copyTagsFromTo(item, template, "PublicBukkitValues");
        item.setType(template.getType());
        item.setItemMeta(template.getItemMeta());
        CustomTag.WEAPON_TITLE.remove(item);
        CustomTag.BROKEN_WEAPON.setString(item, CustomTag.WEAPON_TITLE.getString(template));
        CustomTag.DURABILITY.setInteger(item, 0);

        return true;
    }

    /**
     * Fully repairs the given weapon. This method assumes that the given
     * weapon uses this custom durability. You should always manually check
     * if the weapon actually does use custom durability.
     *
     * @param weapon              The non-null weapon to repair.
     * @param repairMaxDurability true to also repair max-durability.
     * @return true if changes were made to the weapon.
     * @throws IllegalArgumentException If the weapon has no meta or is null.
     */
    public boolean repair(ItemStack weapon, boolean repairMaxDurability) {
        if (weapon == null || !weapon.hasItemMeta())
            throw new IllegalArgumentException("Cannot repair " + weapon + " since it is not a weapon");

        // When weapon is fully broken, we have to set it back to the weapon item.
        String weaponTitle = CustomTag.BROKEN_WEAPON.getString(weapon);
        if (weaponTitle != null) {
            ItemStack weaponTemplate = WeaponMechanics.getWeaponHandler().getInfoHandler().generateWeapon(weaponTitle, 1);

            // Weapon no longer exists in config
            if (weaponTemplate == null) {
                WeaponMechanics.debug.warn("Tried to repair weapon '" + weaponTitle + "' when it no longer exists in config... ignoring...");
                return false;
            }

            // Turn the broken-weapon into a functional weapon
            CompatibilityAPI.getNBTCompatibility().copyTagsFromTo(weapon, weaponTemplate, "PublicBukkitValues");
            weapon.setType(weaponTemplate.getType());
            weapon.setItemMeta(weaponTemplate.getItemMeta());
            CustomTag.WEAPON_TITLE.setString(weapon, weaponTitle);
            CustomTag.BROKEN_WEAPON.remove(weapon);

            // Add durability back to the weapon
            if (repairMaxDurability) CustomTag.MAX_DURABILITY.setInteger(weapon, getMaxDurability());
            CustomTag.DURABILITY.setInteger(weapon, getMaxDurability(weapon));

            return true;
        }

        boolean changes = false;

        int maxDurability = getMaxDurability(weapon);
        if (repairMaxDurability) {
            int newMax = getMaxDurability();
            changes = maxDurability != newMax;
            CustomTag.MAX_DURABILITY.setInteger(weapon, maxDurability);
        }

        changes |= maxDurability != CustomTag.DURABILITY.getInteger(weapon);
        CustomTag.DURABILITY.setInteger(weapon, maxDurability);
        return changes;
    }

    @NotNull
    @Override
    public CustomDurability serialize(@NotNull SerializeData data) throws SerializerException {
        int maxDurability = data.of("Max_Durability").assertPositive().assertExists().getInt();
        int minMaxDurability = data.of("Min_Max_Durability").assertPositive().get(0);
        int loseMaxDurabilityPerRepair = data.of("Lose_Max_Durability_Per_Repair").assertPositive().get(0);
        int durabilityPerShot = data.of("Durability_Per_Shot").assertPositive().getInt(1);
        Double chance = data.of("Chance_To_Lose").serialize(new ChanceSerializer());
        if (chance == null)
            chance = 1.0;

        // Make sure users aren't entering tiny values.
        if (maxDurability <= durabilityPerShot) {
            throw data.exception("Max_Durability", "'Max_Durability' cannot be less than 'Durability_Per_Shot'",
                    "Found Max_Durability: " + maxDurability,
                    "Found Durability_Per_Shot: " + durabilityPerShot);
        }

        ItemStack replaceItem = data.of("Broken_Item").serialize(new ItemSerializer());
        Mechanics durabilityMechanics = data.of("Lose_Durability_Mechanics").serialize(Mechanics.class);
        Mechanics breakMechanics = data.of("Break_Mechanics").serialize(Mechanics.class);

        // Items, in a repair station or anvil, are able to repair weapons
        // for a certain amount of durability.
        Map<ItemStack, Integer> repairItems = new HashMap<>();
        if (data.has("Repair_Items")) {
            ConfigurationSection section = data.of("Repair_Items").assertType(ConfigurationSection.class).get(null);
            for (String key : section.getKeys(false)) {
                ItemStack item = data.of("Repair_Items." + key + ".Item").assertExists().serialize(new ItemSerializer());
                int healAmount = data.of("Repair_Items." + key + ".Repair_Amount").assertExists().assertPositive().getInt();

                repairItems.put(item, healAmount);
            }
        }

        int repairPerExp = data.of("Repair_Per_Exp").assertPositive().getInt(0);
        Mechanics repairMechanics = data.of("Repair_Mechanics").serialize(Mechanics.class);
        Mechanics denyRepairMechanics = data.of("Deny_Repair_Mechanics").serialize(Mechanics.class);
        boolean repairOnlyBroken = data.of("Repair_Only_Broken").getBool(false);

        return new CustomDurability(maxDurability, minMaxDurability, loseMaxDurabilityPerRepair, durabilityPerShot,
                chance, replaceItem, durabilityMechanics, breakMechanics, repairItems, repairPerExp, repairMechanics, denyRepairMechanics,
                repairOnlyBroken);
    }

    /**
     * Factory method to determine whether an item should lose durability based
     * on the given level of the {@link org.bukkit.enchantments.Enchantment#DURABILITY}.
     * For tools/weapons, the chance to lose durability is <i>LOWER</i> then for armor.
     *
     * @param level The unbreaking enchantment level.
     * @param armor true if the item is armor.
     * @return true if the item should lose durability.
     */
    public static boolean isLoseDurability(int level, boolean armor) {
        if (level <= 0)
            return true;

        if (armor && level <= 10)
            return NumberUtil.chance(ARMOR_LEVELS[level]);
        if (armor)
            return NumberUtil.chance(0.6 + 0.4 / (level + 1));

        if (level <= 10)
            return NumberUtil.chance(TOOL_LEVELS[level]);

        return NumberUtil.chance(1.0 / (level + 1));
    }
}
