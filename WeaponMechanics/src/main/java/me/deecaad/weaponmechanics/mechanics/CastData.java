package me.deecaad.weaponmechanics.mechanics;

import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class CastData {

    private EntityWrapper caster;
    private WeaponProjectile projectileCaster;
    private LivingEntity livingEntityCaster;
    private Location casterLocation;
    private String weaponTitle;
    private ItemStack weaponStack;
    private Map<String, Object> data;

    public CastData(EntityWrapper caster) {
        this.caster = caster;
    }

    public CastData(EntityWrapper caster, @Nullable String weaponTitle, @Nullable ItemStack weaponStack) {
        this.caster = caster;
        this.weaponTitle = weaponTitle;
        this.weaponStack = weaponStack;
    }

    public CastData(Location casterLocation) {
        this.casterLocation = casterLocation;
    }

    /**
     * Use when you want specific cast location for sounds etc
     */
    public CastData(EntityWrapper caster, Location casterLocation, String weaponTitle, ItemStack weaponStack) {
        this.caster = caster;
        this.casterLocation = casterLocation;
        this.weaponTitle = weaponTitle;
        this.weaponStack = weaponStack;
    }

    public CastData(WeaponProjectile caster) {
        projectileCaster = caster;
        weaponTitle = caster.getWeaponTitle();
        weaponStack = caster.getWeaponStack();
    }

    public CastData(LivingEntity caster, @Nullable String weaponTitle, @Nullable ItemStack weaponStack) {
        this.livingEntityCaster = caster;
        this.weaponTitle = weaponTitle;
        this.weaponStack = weaponStack;
    }

    /**
     * @return the wrapped caster of this cast, null if only location is used
     */
    @Nullable
    public EntityWrapper getCasterWrapper() {
        return caster;
    }

    /**
     *
     * @return the projectile caster of this cast, null if not used
     */
    @Nullable
    public WeaponProjectile getProjectileCaster() {
        return projectileCaster;
    }

    /**
     * @return the caster of this cast, null if only location is used
     */
    @Nullable
    public LivingEntity getCaster() {
        if (this.livingEntityCaster != null) {
            return this.livingEntityCaster;
        }
        return caster == null ? null : caster.getEntity();
    }

    /**
     * @return the cast location or caster location if defined
     */
    public Location getCastLocation() {
        if (casterLocation != null) return casterLocation;
        if (this.livingEntityCaster != null) return this.livingEntityCaster.getLocation();

        return projectileCaster != null ? projectileCaster.getLocation().toLocation(projectileCaster.getWorld()) : caster.getEntity().getLocation();
    }

    /**
     * @return the cast world
     */
    public World getCastWorld() {
        if (casterLocation != null) return casterLocation.getWorld();
        if (livingEntityCaster != null) return livingEntityCaster.getWorld();

        return projectileCaster != null ? projectileCaster.getWorld() : caster.getEntity().getWorld();
    }

    @Nullable
    public String getWeaponTitle() {
        return weaponTitle;
    }

    public void setWeaponTitle(String weaponTitle) {
        this.weaponTitle = weaponTitle;
    }

    @Nullable
    public ItemStack getWeaponStack() {
        return weaponStack;
    }

    public void setWeaponStack(ItemStack weaponStack) {
        this.weaponStack = weaponStack;
    }

    /**
     * Set new data for this cast.
     * For example this is used to store reload tasks to correct hand.
     *
     * @param key the key used
     * @param value the value for key
     */
    public void setData(String key, Object value) {
        if (data == null) data = new HashMap<>();
        data.put(key, value);
    }

    /**
     * @param key the key to fetch
     * @return the value of key or 0 if not used
     */
    @Nullable
    public <T> T getData(String key, Class<T> clazz) {
        if (data == null) return null;
        // clazz.cast -> returns the object after casting, or null if obj is null
        Object keyData = data.get(key);
        if (!clazz.isInstance(keyData)) {
            return null;
        } else {
            return clazz.cast(keyData);
        }
    }
}