package me.deecaad.weaponmechanics.mechanics;

import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.Map;

public class CastData {

    private IEntityWrapper caster;
    private Location casterLocation;
    private Map<String, Integer> data;

    public CastData(IEntityWrapper caster) {
        this.caster = caster;
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

    /**
     * Set new data for this cast.
     * For example this is used to store reload tasks to correct hand.
     *
     * @param key the key used
     * @param value the value for key
     */
    public void setData(String key, int value) {
        data.put(key, value);
    }

    /**
     * @param key the key to fetch
     * @return the value of key or 0 if not used
     */
    public int getData(String key) {
        return data.getOrDefault(key, 0);
    }
}