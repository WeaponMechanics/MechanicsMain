package me.deecaad.core.mechanics;

import me.deecaad.core.file.JarSearcherExempt;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.conditions.Condition;
import me.deecaad.core.mechanics.defaultmechanics.Mechanic;
import me.deecaad.core.mechanics.targeters.WorldTargeter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * This class wraps a list of {@link PlayerEffectMechanic}. The purpose of this
 * is to cache a list of targeted players so we don't waste resources instead
 * targeting every entity in the world, every mechanic we cast. This is especially
 * crucial for the {@link me.deecaad.core.mechanics.defaultmechanics.SoundMechanic},
 * which may exist 10+ times in 1 {@link Mechanics}, causing major performance issues
 * on big servers.
 */
public final class PlayerEffectMechanicList extends Mechanic implements JarSearcherExempt {

    private final List<PlayerEffectMechanic> mechanics;

    public PlayerEffectMechanicList() {
        mechanics = new LinkedList<>();  // LinkedList for smaller memory footprint
    }

    public void addMechanic(PlayerEffectMechanic mechanic) {
        if (mechanic.getViewerTargeter() instanceof WorldTargeter worldTargeter && worldTargeter.isDefaultValues())
            mechanics.add(mechanic);
        else
            throw new IllegalArgumentException("Cannot add " + mechanic + " due to modified targeter");
    }

    public boolean isEmpty() {
        return mechanics.isEmpty();
    }

    @Override
    protected void handleTargetersAndConditions(CastData cast) {
        // This Mechanic is a special Mechanic that stores a list of mechanics that
        // can have their targeters cached. This improves performance. Of course, that
        // means that this mechanic SHOULD NOT use targeters.
        use0(cast);
    }

    @Override
    protected void use0(CastData cast) {
        List<Player> players = cast.getSource().getWorld().getPlayers();

        // We re-use these variables, so we do not need to instantiate them
        // multiple times.
        List<Player> cacheList = new LinkedList<>(); // linked list for fast add and clear
        CastData target = cast.clone();
        target.setTargetLocation((Supplier<Location>) null);

        for (PlayerEffectMechanic mechanic : mechanics) {

            // TODO account for the targeter... Right now we only account for the conditions
            for (Condition condition : mechanic.getViewerConditions()) {
                for (Player player : players) {
                    target.setTargetEntity(player);
                    if (condition.isAllowed(target))
                        cacheList.add(player);
                }
            }

            mechanic.playFor(cast, cacheList);
            cacheList.clear();
        }
    }

    @NotNull
    @Override
    public Mechanic serialize(@NotNull SerializeData data) throws SerializerException {
        throw new UnsupportedOperationException("Cannot directly serialize a PlayerEffectMechanicList");
    }
}
