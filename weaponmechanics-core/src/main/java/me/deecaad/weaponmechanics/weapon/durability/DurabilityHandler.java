package me.deecaad.weaponmechanics.weapon.durability;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.MechanicManager;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Handles custom weapon durability and depleted behavior.
 */
public class DurabilityHandler {

    /**
     * Returns whether the given custom-durability item has no durability remaining.
     */
    public boolean isDepleted(ItemStack weaponStack) {
        if (weaponStack == null || weaponStack.getAmount() <= 0)
            return true;

        ItemMeta meta = weaponStack.getItemMeta();
        return meta instanceof Damageable damageable && damageable.hasMaxDamage() && damageable.getDamage() >= damageable.getMaxDamage();
    }

    /**
     * Returns how many uses remain before this item becomes depleted. The final use which reaches
     * maximum damage is included in the returned amount.
     */
    public int getRemainingUses(ItemStack weaponStack, int damagePerUse) {
        if (damagePerUse <= 0)
            return Integer.MAX_VALUE;

        ItemMeta meta = weaponStack.getItemMeta();
        if (!(meta instanceof Damageable damageable) || !damageable.hasMaxDamage())
            return Integer.MAX_VALUE;

        int remainingDamage = damageable.getMaxDamage() - damageable.getDamage();
        if (remainingDamage <= 0)
            return 0;

        return (remainingDamage + damagePerUse - 1) / damagePerUse;
    }

    /**
     * Applies the configured durability loss for one successful shot.
     */
    public void applyShotDurability(LivingEntity livingEntity, String weaponTitle, ItemStack weaponStack) {
        Configuration config = WeaponMechanics.getInstance().getWeaponConfigurations();
        int durabilityPerShot = config.getInt(weaponTitle + ".Shoot.Durability_Per_Shot", 0);
        applyDurability(livingEntity, weaponTitle, weaponStack, durabilityPerShot);
    }

    /**
     * Applies durability loss from any source and handles the first transition into the depleted
     * state. This is also used for vanilla item damage, such as Tool.Damage_Per_Block.
     */
    public void applyDurability(LivingEntity livingEntity, String weaponTitle, ItemStack weaponStack, int durabilityDamage) {
        if (durabilityDamage <= 0 || weaponStack == null || weaponStack.getAmount() <= 0)
            return;

        ItemMeta meta = weaponStack.getItemMeta();
        if (!(meta instanceof Damageable damageable) || !damageable.hasMaxDamage())
            return;

        int maxDamage = damageable.getMaxDamage();
        int oldDamage = damageable.getDamage();
        if (oldDamage >= maxDamage)
            return;

        int newDamage = Math.min(maxDamage, oldDamage + durabilityDamage);
        damageable.setDamage(newDamage);
        weaponStack.setItemMeta(meta);

        if (newDamage < maxDamage)
            return;

        Configuration config = WeaponMechanics.getInstance().getWeaponConfigurations();
        DepletedMode mode = config.getObject(weaponTitle + ".Info.Durability.On_Depleted", DepletedMode.BREAK, DepletedMode.class);

        String mechanicsPath = mode == DepletedMode.DISABLE ? weaponTitle + ".Info.Weapon_Depleted_Mechanics" : weaponTitle + ".Info.Weapon_Break_Mechanics";

        MechanicManager mechanics = config.getObject(mechanicsPath, MechanicManager.class);
        if (mechanics != null)
            mechanics.use(new CastData(livingEntity, weaponTitle, weaponStack));

        if (mode == DepletedMode.BREAK)
            weaponStack.setAmount(weaponStack.getAmount() - 1);
    }
}
