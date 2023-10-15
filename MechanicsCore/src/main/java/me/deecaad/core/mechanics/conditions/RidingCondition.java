package me.deecaad.core.mechanics.conditions;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RidingCondition extends Condition {

    /**
     * Default constructor for serializer.
     */
    public RidingCondition() {
    }

    @Override
    protected boolean isAllowed0(CastData cast) {
        LivingEntity target = cast.getTarget();
        return target != null && target.isInsideVehicle();
    }

    @Override
    public String getKeyword() {
        return "Riding";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://github.com/WeaponMechanics/MechanicsMain/wiki/SimpleConditions#Riding-Condition";
    }

    @NotNull
    @Override
    public Condition serialize(@NotNull SerializeData data) throws SerializerException {
        return applyParentArgs(data, new RidingCondition());
    }
}
