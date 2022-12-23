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

public class CastData implements Cloneable {

    // Sourcing information. "Where did this come from?"
    private final LivingEntity source;
    private String itemTitle;
    private ItemStack itemStack;
    private Location sourceLocation;

    // Targeting information. This is filled in during casting.
    private LivingEntity targetEntity;
    private Location targetLocation;

    // Extra data used by some mechanics
    private Consumer<Integer> taskIdConsumer;
    private Map<String, String> tempPlaceholders;

    public CastData(LivingEntity source, String itemTitle, ItemStack itemStack) {
        this.source = source;
        this.itemTitle = itemTitle;
        this.itemStack = itemStack;
    }

    public CastData(LivingEntity source, Location sourceLocation, String itemTitle, ItemStack itemStack) {
        this.source = source;
        this.itemTitle = itemTitle;
        this.itemStack = itemStack;
        this.sourceLocation = sourceLocation;
    }

    public CastData(LivingEntity source, String itemTitle, ItemStack itemStack, Map<String, String> tempPlaceholders) {
        this.source = source;
        this.itemTitle = itemTitle;
        this.itemStack = itemStack;
        this.tempPlaceholders = tempPlaceholders;
    }

    public CastData(LivingEntity source, String itemTitle, ItemStack itemStack, Consumer<Integer> taskIdConsumer) {
        this.source = source;
        this.itemTitle = itemTitle;
        this.itemStack = itemStack;
        this.taskIdConsumer = taskIdConsumer;
    }

    @Nonnull
    public LivingEntity getSource() {
        return source;
    }

    @Nonnull
    public Location getSourceLocation() {
        return sourceLocation != null ? sourceLocation : source.getLocation();
    }


    public LivingEntity getTarget() {
        return targetEntity;
    }

    public void setTargetEntity(LivingEntity targetEntity) {
        this.targetEntity = targetEntity;
    }

    @Nonnull
    public Location getTargetLocation() {
        return targetLocation != null ? targetLocation : targetEntity.getLocation();
    }

    public void setTargetLocation(Location targetLocation) {
        this.targetLocation = targetLocation;
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

    @Override
    public CastData clone() {
        try {
            return (CastData) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}