package me.deecaad.core.mechanics;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.placeholder.PlaceholderData;
import me.deecaad.core.utils.LogLevel;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static me.deecaad.core.MechanicsCore.debug;

public class CastData implements Cloneable, PlaceholderData {

    // Sourcing information. "Where did this come from?"
    private final LivingEntity source;
    private final String itemTitle;
    private final ItemStack itemStack;

    // Targeting information. This is filled in during casting.
    private LivingEntity targetEntity;
    private Supplier<Location> targetLocation;

    // Extra data used by some mechanics
    private Consumer<Integer> taskIdConsumer;
    private final @NotNull Map<String, String> tempPlaceholders;

    public CastData(@NotNull LivingEntity source, @Nullable String itemTitle, @Nullable ItemStack itemStack) {
        this.source = source;
        this.itemTitle = itemTitle;
        this.itemStack = itemStack;
        this.tempPlaceholders = new HashMap<>();
        addDefaultPlaceholders();
    }

    public CastData(@NotNull LivingEntity source, @Nullable String itemTitle, @Nullable ItemStack itemStack, @NotNull Map<String, String> tempPlaceholders) {
        this.source = source;
        this.itemTitle = itemTitle;
        this.itemStack = itemStack;
        this.tempPlaceholders = tempPlaceholders;
        addDefaultPlaceholders();
    }

    public CastData(@NotNull LivingEntity source, @Nullable String itemTitle, @Nullable ItemStack itemStack, @Nullable Consumer<Integer> taskIdConsumer) {
        this.source = source;
        this.itemTitle = itemTitle;
        this.itemStack = itemStack;
        this.taskIdConsumer = taskIdConsumer;
        this.tempPlaceholders = new HashMap<>();
        addDefaultPlaceholders();
    }

    private void addDefaultPlaceholders() {
        Location location = getSourceLocation();
        tempPlaceholders.put("source_name", getName(source));
        tempPlaceholders.put("source_x", String.valueOf(location.getX()));
        tempPlaceholders.put("source_y", String.valueOf(location.getY()));
        tempPlaceholders.put("source_z", String.valueOf(location.getZ()));
    }

    private String getName(LivingEntity entity) {
        TextComponent component = LegacyComponentSerializer.legacySection().deserialize(entity.getName());
        return MechanicsCore.getPlugin().message.serialize(component);
    }

    @NotNull
    public LivingEntity getSource() {
        return source;
    }

    @NotNull
    public Location getSourceLocation() {
        return source.getLocation();
    }

    @NotNull
    public World getSourceWorld() {
        return source.getWorld();
    }

    @Nullable
    public LivingEntity getTarget() {
        return targetEntity;
    }

    @Nullable
    public World getTargetWorld() {
        if (targetEntity != null)
            return targetEntity.getWorld();
        if (targetLocation != null)
            return targetLocation.get().getWorld();

        return null;
    }

    public void setTargetEntity(@NotNull LivingEntity targetEntity) {
        Location location = targetEntity.getLocation();
        tempPlaceholders.put("target_name", getName(targetEntity));
        tempPlaceholders.put("target_x", String.valueOf(location.getX()));
        tempPlaceholders.put("target_y", String.valueOf(location.getY()));
        tempPlaceholders.put("target_z", String.valueOf(location.getZ()));

        this.targetEntity = targetEntity;
    }

    public boolean hasTargetLocation() {
        return targetLocation != null;
    }

    @Nullable
    public Location getTargetLocation() {
        if (targetLocation == null && targetEntity == null) {
            debug.log(LogLevel.WARN, "Not targeting either entity nor location", new Throwable());
        }
        return targetLocation != null ? targetLocation.get() : targetEntity.getLocation();
    }

    @NotNull
    public Supplier<Location> getTargetLocationSupplier() {
        return targetLocation;
    }

    public void setTargetLocation(@NotNull Location targetLocation) {
        tempPlaceholders.put("target_x", String.valueOf(targetLocation.getX()));
        tempPlaceholders.put("target_y", String.valueOf(targetLocation.getY()));
        tempPlaceholders.put("target_z", String.valueOf(targetLocation.getZ()));
        this.targetLocation = () -> targetLocation;
    }

    public void setTargetLocation(@Nullable Supplier<Location> targetLocation) {
        if (targetLocation != null) {
            Location location = targetLocation.get();
            tempPlaceholders.put("target_x", String.valueOf(location.getX()));
            tempPlaceholders.put("target_y", String.valueOf(location.getY()));
            tempPlaceholders.put("target_z", String.valueOf(location.getZ()));
        }
        this.targetLocation = targetLocation;
    }

    @Nullable
    public Consumer<Integer> getTaskIdConsumer() {
        return taskIdConsumer;
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
    public @NotNull Map<String, String> placeholders() {
        return tempPlaceholders;
    }
}