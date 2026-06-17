package me.deecaad.weaponmechanics.mechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.scope.CastScope;
import me.deecaad.core.mechanics.scope.Target;
import me.deecaad.core.mechanics.conditions.Condition;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.WeaponMechanicsAPI;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReloadingCondition extends Condition {

    /**
     * Default constructor for serializer.
     */
    public ReloadingCondition() {
    }

    @Override
    protected boolean isAllowed0(CastScope scope, Target subject) {
        // Inside a WM cast, check the shooter's firing-hand reload state (live, not a snapshot). This
        // is hand-aware: reloading the off hand does not satisfy a main-hand cast.
        WeaponCastData data = scope.getAttachment(WeaponCastData.class);
        if (data != null)
            return data.handData().isReloading();

        // Outside a WM cast, fall back to the subject entity's reload state on either hand.
        return subject != null && subject.entity() != null && WeaponMechanicsAPI.isReloading(subject.entity());
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return new NamespacedKey(WeaponMechanics.getInstance(), "reloading");
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://cjcrafter.gitbook.io/mechanics/integrations/weaponmechanics#reloading";
    }

    @NotNull @Override
    public Condition serialize(@NotNull SerializeData data) throws SerializerException {
        return applyParentArgs(data, new ReloadingCondition());
    }
}
