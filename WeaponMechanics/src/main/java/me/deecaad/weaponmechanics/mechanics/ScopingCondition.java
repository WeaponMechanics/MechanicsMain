package me.deecaad.weaponmechanics.mechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.conditions.Condition;
import me.deecaad.weaponmechanics.WeaponMechanicsAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScopingCondition extends Condition {

    /**
     * Default constructor for serializer.
     */
    public ScopingCondition() {
    }

    @Override
    protected boolean isAllowed0(CastData cast) {
        return cast.getTarget() != null && WeaponMechanicsAPI.isScoping(cast.getTarget());
    }

    @Override
    public String getKeyword() {
        return "Scoping";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://github.com/WeaponMechanics/MechanicsMain/wiki/SimpleConditions#Scoping-Condition";
    }

    @NotNull
    @Override
    public Condition serialize(@NotNull SerializeData data) throws SerializerException {
        return applyParentArgs(data, new ScopingCondition());
    }
}
