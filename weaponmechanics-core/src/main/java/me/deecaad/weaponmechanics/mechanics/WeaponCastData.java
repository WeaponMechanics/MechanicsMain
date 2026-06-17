package me.deecaad.weaponmechanics.mechanics;

import me.deecaad.core.mechanics.scope.CastAttachment;
import me.deecaad.core.mechanics.scope.CastScope;
import me.deecaad.core.mechanics.scope.ItemData;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.HandData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

/**
 * The per-cast WeaponMechanics state, attached to every {@link CastScope} WM builds at a trigger.
 *
 * <p>
 * This is the single object that carries "who fired what, from where" through a cast. It is a
 * {@link CastAttachment}, so it is read only by WM's own Java (conditions, mechanics, targeters) via
 * {@code scope.getAttachment(WeaponCastData.class)} -- it is <i>not</i> a registry vocabulary type.
 *
 * <p>
 * It holds <b>live references</b> (the shooter and, on demand, their {@link EntityWrapper}), never
 * frozen snapshots of mutable state. Delayed or repeated mechanics must observe current state
 * (reloading, ammo, zoom), so consumers read through {@link #entityWrapper()} / {@link #handData()}
 * rather than cached booleans.
 *
 * <p>
 * It <b>complements</b> the built-in {@link ItemData} attachment rather than replacing it: WM's
 * placeholder handlers read {@code item()/itemTitle()/slot()} which are backed by {@link ItemData}.
 * {@link #scope()} seeds both from this one object, so a trigger constructs a single instance and
 * every {@code item()/itemTitle()} call site collapses into one helper.
 */
public final class WeaponCastData implements CastAttachment {

    private final LivingEntity shooter;
    private final @Nullable EquipmentSlot slot;
    private final @Nullable String weaponTitle;
    private final @Nullable ItemStack weaponStack;

    // Lazily resolved; a shooter is a live entity for the duration of a cast, so a wrapper always
    // exists or is created on first read. Sites that already hold the wrapper seed it directly.
    private @Nullable EntityWrapper wrapper;

    public WeaponCastData(@NotNull EntityWrapper wrapper, @Nullable EquipmentSlot slot, @Nullable String weaponTitle, @Nullable ItemStack weaponStack) {
        this.shooter = wrapper.getEntity();
        this.wrapper = wrapper;
        this.slot = slot;
        this.weaponTitle = weaponTitle;
        this.weaponStack = weaponStack;
    }

    public WeaponCastData(@NotNull LivingEntity shooter, @Nullable EquipmentSlot slot, @Nullable String weaponTitle, @Nullable ItemStack weaponStack) {
        this.shooter = shooter;
        this.slot = slot;
        this.weaponTitle = weaponTitle;
        this.weaponStack = weaponStack;
    }

    public @NotNull LivingEntity shooter() {
        return shooter;
    }

    public @Nullable EquipmentSlot slot() {
        return slot;
    }

    public @Nullable String weaponTitle() {
        return weaponTitle;
    }

    public @Nullable ItemStack weaponStack() {
        return weaponStack;
    }

    /**
     * The shooter's wrapper, resolved lazily (and cached) so sites without one in scope pay nothing
     * until a consumer actually needs entity state.
     */
    public @NotNull EntityWrapper entityWrapper() {
        if (wrapper == null)
            wrapper = WeaponMechanics.getInstance().getEntityWrapper(shooter);
        return wrapper;
    }

    /**
     * The {@link HandData} for the hand this cast fired from. Defaults to the main hand when the slot
     * is unknown.
     */
    public @NotNull HandData handData() {
        return entityWrapper().getHandData(slot != EquipmentSlot.OFF_HAND);
    }

    /**
     * A copy of this cast bound to a different weapon stack, sharing the (already resolved) shooter,
     * wrapper, slot and title. Used by reload/firearm steps that operate on the live {@code
     * taskReference} item in hand rather than the original stack.
     */
    public @NotNull WeaponCastData withStack(@Nullable ItemStack weaponStack) {
        return wrapper != null
            ? new WeaponCastData(wrapper, slot, weaponTitle, weaponStack)
            : new WeaponCastData(shooter, slot, weaponTitle, weaponStack);
    }

    /**
     * The built-in {@link ItemData} view of this cast, backing the {@code item()/itemTitle()/slot()}
     * placeholder accessors. All three may be null on API/deny paths.
     */
    public @NotNull ItemData toItemData() {
        return new ItemData(weaponStack, weaponTitle, slot);
    }

    /**
     * A {@link CastScope.Builder} pre-seeded with the built-in {@link ItemData} and this attachment.
     * Callers add per-trigger context/variables/task-consumers as needed, then {@code build()}.
     */
    public @NotNull CastScope.Builder scope() {
        return CastScope.builder(shooter)
            .attachment(ItemData.class, toItemData())
            .attachment(WeaponCastData.class, this);
    }

    @Override
    public @NotNull Map<String, String> placeholders() {
        // Built-in weapon placeholders flow through ItemData; WM-namespaced extras (shooter_*, wm_*)
        // can be added here later without touching any cast site.
        return Collections.emptyMap();
    }
}
