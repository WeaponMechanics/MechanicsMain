package me.deecaad.core.mechanics;

import me.deecaad.core.mechanics.conditions.Condition;
import me.deecaad.core.mechanics.defaultmechanics.Mechanic;
import me.deecaad.core.mechanics.targeters.Targeter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class PlayerEffectMechanic extends Mechanic {

    /**
     * Similar to {@link Mechanic#use0(CastData)} but instead of using this
     * Mechanic's viewer targeter and viewer conditions, the mechanic is used
     * for the given viewer.
     *
     * <p>This is done to address a performance issue associated with having
     * multiple per-player mechanics in the same list. See #311 for more info.
     *
     * @param castData The source of the mechanic... who is casting.
     * @param viewers  The target of the mechanic.
     */
    public abstract void playFor(CastData castData, List<Player> viewers);

    @Nullable
    public abstract Targeter getViewerTargeter();

    public abstract List<Condition> getViewerConditions();
}
