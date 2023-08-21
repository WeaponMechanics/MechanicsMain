package me.deecaad.core.mechanics;

import me.deecaad.core.placeholder.PlaceholderData;
import me.deecaad.core.utils.LogLevel;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static me.deecaad.core.MechanicsCore.debug;

public class CastData implements Cloneable, PlaceholderData {

    // Sourcing information. "Where did this come from?"
    private final LivingEntity source;
    private final String itemTitle;
    private final ItemStack itemStack;
    private final Location sourceLocation;

    // Targeting information. This is filled in during casting.
    private LivingEntity targetEntity;
    private Supplier<Location> targetLocation;

    // Extra data used by some mechanics
    private Consumer<Integer> taskIdConsumer;
    private Map<String, String> tempPlaceholders;

    public CastData(LivingEntity source, String itemTitle, ItemStack itemStack) {
        this.source = source;
        this.sourceLocation = null;
        this.itemTitle = itemTitle;
        this.itemStack = itemStack;

    }

    public CastData(LivingEntity source, String itemTitle, ItemStack itemStack, Map<String, String> tempPlaceholders) {
        this.source = source;
        this.sourceLocation = null;
        this.itemTitle = itemTitle;
        this.itemStack = itemStack;
        this.tempPlaceholders = tempPlaceholders;
    }

    public CastData(LivingEntity source, String itemTitle, ItemStack itemStack, Consumer<Integer> taskIdConsumer) {
        this.source = source;
        this.sourceLocation = null;
        this.itemTitle = itemTitle;
        this.itemStack = itemStack;
        this.taskIdConsumer = taskIdConsumer;
    }

    @NotNull
    public LivingEntity getSource() {
        return source;
    }

    public boolean hasSourceLocation() {
        return sourceLocation != null;
    }

    @NotNull
    public Location getSourceLocation() {
        return sourceLocation != null ? sourceLocation : source.getLocation();
    }

    public LivingEntity getTarget() {
        return targetEntity;
    }

    public void setTargetEntity(LivingEntity targetEntity) {
        this.targetEntity = targetEntity;
    }

    public boolean hasTargetLocation() {
        return targetLocation != null;
    }

    @NotNull
    public Location getTargetLocation() {
        if (targetLocation == null && targetEntity == null) {
            debug.log(LogLevel.WARN, "Not targeting either entity nor location", new Throwable());
        }
        return targetLocation != null ? targetLocation.get() : targetEntity.getLocation();
    }

    public void setTargetLocation(Location targetLocation) {
        this.targetLocation = () -> targetLocation;
    }

    public void setTargetLocation(Supplier<Location> targetLocation) {
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
            throw new AssertionError(e);
        }
    }

    // Implement PlaceholderData methods

    @Override
    public @Nullable Player player() {
        if (source instanceof Player player)
            return player;
        else
            return null;
    }

    @Override
    public @Nullable ItemStack item() {
        return itemStack;
    }

    @Override
    public @Nullable String itemTitle() {
        return itemTitle;
    }

    @Override
    public @Nullable Map<String, String> tempPlaceholders() {
        return tempPlaceholders;
    }
}