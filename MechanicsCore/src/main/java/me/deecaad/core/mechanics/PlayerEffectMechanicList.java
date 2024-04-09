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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * This class wraps a list of {@link PlayerEffectMechanic}. The purpose of this is to cache a list
 * of targeted players so we don't waste resources instead targeting every entity in the world,
 * every mechanic we cast. This is especially crucial for the
 * {@link me.deecaad.core.mechanics.defaultmechanics.SoundMechanic}, which may exist 10+ times in 1
 * {@link Mechanics}, causing major performance issues on big servers.
 */
public final class PlayerEffectMechanicList extends Mechanic implements JarSearcherExempt {

    private final List<PlayerEffectMechanic> mechanics;

    public PlayerEffectMechanicList() {
        mechanics = new LinkedList<>(); // LinkedList for smaller memory footprint
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
    public void use(CastData cast) {
        List<Player> players = cast.getSource().getWorld().getPlayers();

        // We re-use these variables, so we do not need to instantiate them
        // multiple times.
        List<Player> cacheList = new LinkedList<>(); // linked list for fast add and clear
        CastData target = cast.clone();

        for (PlayerEffectMechanic mechanic : mechanics) {


            for (Iterator<CastData> iterator = mechanic.getViewerTargeter().getTargets(cast); iterator.hasNext(); ) {
                CastData targetData = iterator.next();

                for (Condition condition : mechanic.getViewerConditions()) {
                    for (Player player : players) {

                        targetData.setTargetEntity(player);
                        target.setTargetLocation((Supplier<Location>) null);


                        if (condition.isAllowed(targetData))
                            cacheList.add(player);
                    }
                }

                mechanic.playFor(cast, cacheList);
                cacheList.clear();
            }

            // TODO account for the targeter... Right now we only account for the conditions

        }
    }

    @Override
    protected void use0(CastData cast) {
        throw new UnsupportedOperationException("Cannot directly use a PlayerEffectMechanicList");
    }

    @NotNull @Override
    public Mechanic serialize(@NotNull SerializeData data) throws SerializerException {
        throw new UnsupportedOperationException("Cannot directly serialize a PlayerEffectMechanicList");
    }
}
