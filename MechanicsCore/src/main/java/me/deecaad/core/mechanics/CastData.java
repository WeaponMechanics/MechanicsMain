package me.deecaad.core.mechanics;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static me.deecaad.core.MechanicsCore.debug;

public class CastData {

    private final LivingEntity caster;
    private String itemTitle;
    private ItemStack itemStack;
    private Location casterLocation;
    private Consumer<Integer> taskIdConsumer;
    private Map<String, String> tempPlaceholders;
    private Map<String, Object> data;

    public CastData(LivingEntity caster, String itemTitle, ItemStack itemStack) {
        this.caster = caster;
        this.itemTitle = itemTitle;
        this.itemStack = itemStack;
    }

    public CastData(LivingEntity caster, Location casterLocation, String itemTitle, ItemStack itemStack) {
        this.caster = caster;
        this.itemTitle = itemTitle;
        this.itemStack = itemStack;
        this.casterLocation = casterLocation;
    }

    public CastData(LivingEntity caster, String itemTitle, ItemStack itemStack, Map<String, String> tempPlaceholders) {
        this.caster = caster;
        this.itemTitle = itemTitle;
        this.itemStack = itemStack;
        this.tempPlaceholders = tempPlaceholders;
    }

    public CastData(LivingEntity caster, String itemTitle, ItemStack itemStack, Consumer<Integer> taskIdConsumer) {
        this.caster = caster;
        this.itemTitle = itemTitle;
        this.itemStack = itemStack;
        this.taskIdConsumer = taskIdConsumer;
    }

    /**
     * Set new data for this cast.
     *
     * @param key the key used
     * @param value the value for key
     * @return this cast data
     */
    public CastData setData(String key, Object value) {
        if (data == null) data = new HashMap<>();
        data.put(key, value);
        return this;
    }

    /**
     * @param key the key to fetch
     * @param clazz the class cast this key's object is
     * @return the value of key or null if not used or invalid data type
     */
    @Nullable
    public <T> T getData(String key, Class<T> clazz) {
        if (data == null) return null;
        // clazz.cast -> returns the object after casting, or null if obj is null
        Object keyData = data.get(key);
        if (keyData == null) return null;
        if (!clazz.isInstance(keyData)) {
            debug.error("Found invalid data type while using CastData!",
                    "Tried to fetch data using key " + key + " with class cast " + clazz + ", but found " + keyData.getClass());
            return null;
        } else {
            return clazz.cast(keyData);
        }
    }

    @Nonnull
    public LivingEntity getCaster() {
        return caster;
    }

    @Nonnull
    public Location getCastLocation() {
        return casterLocation != null ? casterLocation : caster.getLocation();
    }

    @Nullable
    public Consumer<Integer> getTaskIdConsumer() {
        return taskIdConsumer;
    }

    @Nullable
    public Map<String, String> getTempPlaceholders() {
        return tempPlaceholders;
    }

    @Nullable
    public String getItemTitle() {
        return itemTitle;
    }

    @Nullable
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Nullable
    public Location getTargetLocation() {
        return getData(DataTag.TARGET_LOCATION.name(), Location.class);
    }

    public void setTargetLocation(Location location) {
        setData(DataTag.TARGET_LOCATION.name(), location);
    }
}