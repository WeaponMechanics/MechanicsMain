package me.deecaad.weaponmechanics;

import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.Location;
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

    private static WeaponMechanics plugin;

    // Don't let anyone instantiate this class
    private WeaponMechanicsAPI() {
    }

    /**
     * Returns how far the <code>entity</code> is zooming in. 0 means that the
     * entity is not scoping at all. Any other number means that the entity is
     * scoping.
     *
     * @param entity The non-null living entity to check the scope state of.
     * @return The non-negative zoom amount.
     */
    @Nonnegative
    public int getScopeLevel(@Nonnull LivingEntity entity) {
        checkState();
        notNull(entity);

        IEntityWrapper wrapper = WeaponMechanics.getEntityWrapper(entity, false);
        if (wrapper == null)
            return 0;

        return Math.max(
                wrapper.getMainHandData().getZoomData().getZoomAmount(),
                wrapper.getOffHandData().getZoomData().getZoomAmount()
        );
    }

    /**
     * Returns <code>true</code> if the given <code>entity</code> is zooming in
     * with their weapon. It doesn't matter if the player is zoom-stacking or
     * not, this method will return <code>true</code> if the entity is zoomed
     * in at all.
     *
     * @param entity The non-null living entity to check the scope state of.
     * @return <code>true</code> if the entity is scoping.
     */
    public boolean isScoping(@Nonnull LivingEntity entity) {
        return getScopeLevel(entity) != 0;
    }

    /**
     * Returns <code>true</code> if the given <code>entity</code> is reloading
     * their weapon.
     *
     * @param entity The non-null living entity to check the reload state of.
     * @return <code>true</code> if the entity is reloading.
     */
    public boolean isReloading(@Nonnull LivingEntity entity) {
        checkState();
        notNull(entity);

        IEntityWrapper wrapper = WeaponMechanics.getEntityWrapper(entity, false);
        if (wrapper == null)
            return false;

        return wrapper.getMainHandData().isReloading() || wrapper.getOffHandData().isReloading();
    }

    /**
     * Returns an item corresponding to the given <code>weaponTitle</code>. The
     * item will have a custom name, lore, enchantments, flags, nbt data, etc.
     *
     * @param weaponTitle The non-null weapon-title of the weapon to generate.
     * @return The non-null weapon item.
     */
    @Nonnull
    public ItemStack generateWeapon(@Nonnull String weaponTitle) {
        checkState();
        return plugin.weaponHandler.getInfoHandler().generateWeapon(weaponTitle, 1);
    }

    /**
     * Gives an item corresponding to the given <code>weaponTitle</code> to the
     * given <code>player</code>. If the player's inventory is full, the item
     * will be dropped via {@link org.bukkit.World#dropItemNaturally(Location, ItemStack)}.
     *
     * @param weaponTitle The non-null weapon-title of the weapon to generate.
     * @param player The non-null weapon item.
     */
    public void giveWeapon(String weaponTitle, Player player) {
        checkState();
        plugin.weaponHandler.getInfoHandler().giveOrDropWeapon(weaponTitle, player, 1);
    }

    /**
     * Adds the given projectile to the {@link me.deecaad.weaponmechanics.weapon.projectile.CustomProjectilesRunnable}.
     * Can be run async.
     *
     * @param projectile The non-null projectile to add.
     */
    public void addProjectile(@Nonnull ICustomProjectile projectile) {
        checkState();
        plugin.customProjectilesRunnable.addProjectile(projectile);
    }

    @Nullable
    public String getWeaponTitle(@Nonnull ItemStack item) {
        if (!item.hasItemMeta())
            return null;
        else
            return CustomTag.WEAPON_TITLE.getString(item);
    }

    public static WeaponMechanics getInstance() {
        return plugin;
    }

    // Package private
    static void setInstance(WeaponMechanics INSTANCE) {
        plugin = INSTANCE;
    }

    private void notNull(Object obj) {
        if (obj == null)
            throw new IllegalArgumentException("Expected a value, got null");
    }

    private void checkState() {
        if (plugin == null)
            throw new IllegalStateException("Tried to use WeaponMechanics API before it was setup, OR during a reload!");
    }
}
