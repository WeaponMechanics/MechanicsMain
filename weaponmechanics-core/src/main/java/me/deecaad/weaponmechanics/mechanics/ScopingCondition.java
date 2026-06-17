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

public class ScopingCondition extends Condition {

    /**
     * Default constructor for serializer.
     */
    public ScopingCondition() {
    }

    @Override
    protected boolean isAllowed0(CastScope scope, Target subject) {
        // Inside a WM cast, check the shooter's firing-hand zoom (live, not a snapshot). This is
        // hand-aware: the off hand scoping does not satisfy a main-hand cast.
        WeaponCastData data = scope.getAttachment(WeaponCastData.class);
        if (data != null)
            return data.handData().getZoomData().isZooming();

        // Outside a WM cast, fall back to the subject entity's scope state on either hand.
        return subject != null && subject.entity() != null && WeaponMechanicsAPI.isScoping(subject.entity());
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return new NamespacedKey(WeaponMechanics.getInstance(), "scoping");
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://cjcrafter.gitbook.io/mechanics/integrations/weaponmechanics#scoping";
    }

    @NotNull @Override
    public Condition serialize(@NotNull SerializeData data) throws SerializerException {
        return applyParentArgs(data, new ScopingCondition());
    }
}
