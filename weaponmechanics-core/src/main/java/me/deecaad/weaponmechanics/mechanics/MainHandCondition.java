package me.deecaad.weaponmechanics.mechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.conditions.Condition;
import me.deecaad.core.mechanics.scope.CastScope;
import me.deecaad.core.mechanics.scope.Target;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Passes when the weapon that triggered this cast was fired from the main hand.
 */
public class MainHandCondition extends Condition {

    /**
     * Default constructor for serializer.
     */
    public MainHandCondition() {
    }

    @Override
    protected boolean isAllowed0(@NotNull CastScope scope, @Nullable Target subject) {
        WeaponCastData data = scope.getAttachment(WeaponCastData.class);
        return data != null && data.slot() == EquipmentSlot.HAND;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return new NamespacedKey(WeaponMechanics.getInstance(), "main_hand");
    }

    @NotNull @Override
    public Condition serialize(@NotNull SerializeData data) throws SerializerException {
        return applyParentArgs(data, new MainHandCondition());
    }
}
