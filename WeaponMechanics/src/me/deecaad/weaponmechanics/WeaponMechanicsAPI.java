package me.deecaad.weaponmechanics;

import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.explode.Explosion;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class outlines static utility methods to help developers find functions
 * of WeaponMechanics wrapped in one place. The following method's
 * implementations exclusively call "internal" methods to handle the function.
 */
public final class WeaponMechanicsAPI {

    /**
     * Stores the singleton instance of the WeaponMechanics plugin. Should
     */
    private static WeaponMechanics INSTANCE;

    // Don't let anyone instantiate this class
    private WeaponMechanicsAPI() {
    }

    @Nonnegative
    public int getScopeLevel(@Nonnull LivingEntity entity) {
        notNull(entity);

        IEntityWrapper wrapper = WeaponMechanics.getEntityWrapper(entity, false);
        if (wrapper == null)
            return 0;

        return Math.max(
                wrapper.getMainHandData().getZoomData().getZoomAmount(),
                wrapper.getOffHandData().getZoomData().getZoomAmount()
        );
    }

    public boolean isScoping(@Nonnull LivingEntity entity) {
        return getScopeLevel(entity) != 0;
    }

    public boolean isReloading(@Nonnull LivingEntity entity) {
        notNull(entity);

        IEntityWrapper wrapper = WeaponMechanics.getEntityWrapper(entity, false);
        if (wrapper == null)
            return false;

        return wrapper.getMainHandData().isReloading() || wrapper.getOffHandData().isReloading();
    }

    public ItemStack generateWeapon(String weaponTitle) {
        return WeaponMechanics.getWeaponHandler().getInfoHandler().generateWeapon(weaponTitle, 1);
    }

    public void giveWeapon(String weaponTitle, Player player) {
        WeaponMechanics.getWeaponHandler().getInfoHandler().giveOrDropWeapon(weaponTitle, player, 1);
    }



    @Nullable
    public String getWeaponTitle(@Nonnull ItemStack item) {
        if (!item.hasItemMeta())
            return null;
        else
            return CustomTag.WEAPON_TITLE.getString(item);
    }

    public static WeaponMechanics getInstance() {
        return INSTANCE;
    }

    // Package private
    static void setInstance(WeaponMechanics INSTANCE) {
        WeaponMechanicsAPI.INSTANCE = INSTANCE;
    }

    private void notNull(Object obj) {
        if (obj == null)
            throw new IllegalArgumentException("Expected a value, got null");
    }
}
