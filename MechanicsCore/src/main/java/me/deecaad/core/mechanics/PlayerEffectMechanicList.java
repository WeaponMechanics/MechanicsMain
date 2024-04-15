package me.deecaad.core.mechanics;

import me.deecaad.core.file.JarSearcherExempt;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.conditions.Condition;
import me.deecaad.core.mechanics.defaultmechanics.Mechanic;
import me.deecaad.core.mechanics.targeters.WorldTargeter;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
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

        for (PlayerEffectMechanic mechanic : mechanics) {

            OUTER : for (Iterator<CastData> it = mechanic.targeter.getTargets(cast); it.hasNext();) {
                CastData target = it.next();
                for (Condition condition : mechanic.conditions)
                    if (!condition.isAllowed(target))
                        continue OUTER;

                // Save these variables so they don't get overridden
                LivingEntity targetEntity = target.getTarget();
                Supplier<Location> supplier = target.getTargetLocationSupplier();

                PLAYER_LOOP : for (Player player : players) {
                    target.setTargetEntity(player);
                    for (Condition condition : mechanic.getViewerConditions())
                        if (!condition.isAllowed(target))
                            continue PLAYER_LOOP;

                    cacheList.add(player);
                }

                // Rewrite our saved variations
                target.setTargetEntity(targetEntity);
                target.setTargetLocation(supplier);

                // Play the mechanic
                mechanic.playFor(target, cacheList);
                cacheList.clear();
            }
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
