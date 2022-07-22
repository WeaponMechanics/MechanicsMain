package me.deecaad.weaponmechanics;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.ICompatibility;
import me.deecaad.weaponmechanics.compatibility.IWeaponCompatibility;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.damage.BlockDamageData;
import me.deecaad.weaponmechanics.weapon.projectile.AProjectile;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectilesRunnable;
import me.deecaad.weaponmechanics.weapon.reload.ammo.AmmoTypes;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class outlines static utility methods to help developers find functions
 * of WeaponMechanics wrapped in one place. The following method's
 * implementations exclusively call "internal" methods to handle the function.
 *
 * <p>WeaponMechanics should never use these methods, instead use internal
 * methods.
 */
public final class WeaponMechanicsAPI {

    private static WeaponMechanics plugin;

    // Don't let anyone instantiate this class
    private WeaponMechanicsAPI() { }

    /**
     * Returns how far the <code>entity</code> is zooming in. 0 means that the
     * entity is not scoping at all. Any other number means that the entity is
     * scoping.
     *
     * @param entity The non-null living entity to check the scope state of.
     * @return The non-negative zoom amount.
     * @see EntityWrapper
     * @see PlayerWrapper
     */
    @Nonnegative
    public static double getScopeLevel(@Nonnull LivingEntity entity) {
        checkState();
        notNull(entity);

        EntityWrapper wrapper = WeaponMechanics.getEntityWrapper(entity, true);
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
    public static boolean isScoping(@Nonnull LivingEntity entity) {
        return getScopeLevel(entity) != 0;
    }

    /**
     * Returns <code>true</code> if the given <code>entity</code> is reloading
     * their weapon.
     *
     * @param entity The non-null living entity to check the reload state of.
     * @return <code>true</code> if the entity is reloading.
     */
    public static boolean isReloading(@Nonnull LivingEntity entity) {
        checkState();
        notNull(entity);

        EntityWrapper wrapper = WeaponMechanics.getEntityWrapper(entity, true);
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
     * @see me.deecaad.weaponmechanics.weapon.info.InfoHandler
     */
    @Nonnull
    public static ItemStack generateWeapon(@Nonnull String weaponTitle) {
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
    public static void giveWeapon(String weaponTitle, Player player) {
        checkState();
        plugin.weaponHandler.getInfoHandler().giveOrDropWeapon(weaponTitle, player, 1);
    }

    /**
     * Adds the given projectile to the {@link ProjectilesRunnable}.
     * Can be run async.
     *
     * @param projectile The non-null projectile to add.
     * @see ProjectilesRunnable
     */
    public static void addProjectile(@Nonnull AProjectile projectile) {
        checkState();
        ProjectilesRunnable runnable = plugin.projectilesRunnable;
        runnable.addProjectile(projectile);
    }

    /**
     * Returns <code>true</code> if the block at the given location is broken
     * by an explosion, block damage, or otherwise.
     *
     * @param block The non-null block to check.
     * @return <code>true</code> if the block is broken.
     * @see BlockDamageData
     */
    public static boolean isBroken(@Nonnull Block block) {
        checkState();
        notNull(block);
        return BlockDamageData.isBroken(block);
    }

    /**
     * Regenerates all blocks broken by this plugin. This is a "dangerous"
     * method to call because this may cause players/entities to get stuck
     * underground, and may cause lag spikes if there are many blocks to
     * regenerate. Consider regenerating a few chunks instead of all blocks
     * {@link BlockDamageData#regenerate(Chunk)}.
     */
    public static void regenerateAllBlocks() {
        checkState();
        BlockDamageData.regenerateAll();
    }

    /**
     * Returns the weapon-title associated with the given item. If the given
     * item is not a WeaponMechanics weapon, this method will return
     * <code>null</code>.
     *
     * <p>Note that a weapon-title is the config name of a weapon, and you can
     * use a weapon-title to pull values from config easily.
     *
     * @param item The non-null item to get the weapon title from.
     * @return The item's weapon title, or null.
     */
    @Nullable
    public static String getWeaponTitle(@Nonnull ItemStack item) {
        if (!item.hasItemMeta())
            return null;
        else
            return CustomTag.WEAPON_TITLE.getString(item);
    }

    /**
     * Returns the ammo name which is currently used in weapon. If the weapon
     * doesn't use ammo, this method will return <code>null</code>.
     *
     * @param weaponTitle The non-null weapon-title of the weapon.
     * @param weaponStack The non-null weapon item stack.
     * @return The current ammo name, or null.
     */
    @Nullable
    public static String getCurrentAmmoName(@Nonnull String weaponTitle, @Nonnull ItemStack weaponStack) {
        checkState();
        notNull(weaponTitle);
        notNull(weaponStack);

        AmmoTypes ammoTypes = plugin.configurations.getObject(weaponTitle + ".Reload.Ammo.Ammo_Types", AmmoTypes.class);
        if (ammoTypes == null) return null;

        return ammoTypes.getCurrentAmmoName(weaponStack);
    }

    public static void shoot(LivingEntity shooter, String weaponTitle, Location target) {
        shoot(shooter, weaponTitle, target.toVector().subtract(shooter.getEyeLocation().toVector()));
    }

    public static void shoot(LivingEntity shooter, String weaponTitle) {
        shoot(shooter, weaponTitle, shooter.getLocation().getDirection());
    }

    public static void shoot(LivingEntity shooter, String weaponTitle, Vector direction) {
        checkState();
        notNull(shooter);
        notNull(weaponTitle);
        notNull(direction);

        if (!plugin.weaponHandler.getInfoHandler().hasWeapon(weaponTitle)) {
            throw new IllegalArgumentException("Weapon " + weaponTitle + " does not exist");
        }

        plugin.weaponHandler.getShootHandler().shoot(shooter, weaponTitle, direction.clone().normalize());
    }

    /**
     * Returns MechanicsCore's compatibility version. Useful for dealing with
     * NMS code or otherwise version dependent code.
     *
     * @return The non-null compatibility version.
     */
    public static ICompatibility getCompatibility() {
        return CompatibilityAPI.getCompatibility();
    }

    /**
     * Returns WeaponMechanics' compatibility version. Useful for dealing with
     * NMS code or otherwise version dependent code.
     *
     * @return The non-null weapon compatibility version.
     */
    public static IWeaponCompatibility getWeaponCompatibility() {
        return WeaponCompatibilityAPI.getWeaponCompatibility();
    }

    /**
     * Returns the current WeaponMechanics main plugin instance. If the plugin
     * has not been loaded, or is current reloading, this method will return
     * <code>null</code>.
     *
     * @return The nullable plugin instance.
     */
    public static WeaponMechanics getInstance() {
        return plugin;
    }

    // Package private
    static void setInstance(WeaponMechanics INSTANCE) {
        plugin = INSTANCE;
    }

    private static void notNull(Object obj) {
        if (obj == null)
            throw new IllegalArgumentException("Expected a value, got null");
    }

    private static void checkState() {
        if (plugin == null)
            throw new IllegalStateException("Tried to use WeaponMechanics API before it was setup, OR during a reload!");
    }
}
