package me.deecaad.weaponmechanics.mechanics;

import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class CastData {

    private IEntityWrapper caster;
    private Location casterLocation;
    private String weaponTitle;
    private ItemStack weaponStack;
    private Map<String, Object> data;

    public CastData(IEntityWrapper caster) {
        this.caster = caster;
    }

    public CastData(IEntityWrapper caster, @Nullable String weaponTitle, @Nullable ItemStack weaponStack) {
        this.caster = caster;
        this.weaponTitle = weaponTitle;
        this.weaponStack = weaponStack;
    }

    public CastData(Location casterLocation) {
        this.casterLocation = casterLocation;
    }

    /**
     * @return the wrapped caster of this cast, null if only location is used
     */
    @Nullable
    public IEntityWrapper getCasterWrapper() {
        return caster;
    }

    /**
     * @return the caster of this cast, null if only location is used
     */
    @Nullable
    public LivingEntity getCaster() {
        return caster == null ? null : caster.getEntity();
    }

    /**
     * @return the cast location or caster location if defined
     */
    public Location getCastLocation() {
        return casterLocation != null ? casterLocation : caster.getEntity().getLocation();
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

    public enum CommonDataTags {

        WEAPON_TITLE,
        WEAPON_STACK,
        WEAPON_INFO,
        TARGET_LOCATION

    }
}