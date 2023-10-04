package me.deecaad.core.mechanics.conditions;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.deecaad.core.file.JarSearcherExempt;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class MythicMobsFactionCondition extends Condition implements JarSearcherExempt {

    private String faction;

    /**
     * Default constructor for serializer.
     */
    public MythicMobsFactionCondition() {
    }

    public MythicMobsFactionCondition(String faction) {
        this.faction = faction;
    }

    @Override
    protected boolean isAllowed0(CastData cast) {
        LivingEntity target = cast.getTarget();
        if (target == null) return false;

        if (target instanceof Player player) {
            AbstractEntity abstractPlayer = BukkitAdapter.adapt(player);
            Optional<String> maybeFaction = MythicBukkit.inst().getPlayerManager().getFactionProvider().getFaction(abstractPlayer.asPlayer());

            // Supports null faction
            return Objects.equals(maybeFaction.orElse(null), faction);
        }

        ActiveMob activeMob = MythicBukkit.inst().getMobManager().getMythicMobInstance(target);
        if (activeMob == null || !activeMob.hasFaction()) return false;


        return Objects.equals(activeMob.getFaction(), faction);
    }

    @Override
    public String getKeyword() {
        return "MythicMobsFaction";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://github.com/WeaponMechanics/MechanicsMain/wiki/MythicMobsFactionCondition";
    }

    @NotNull
    @Override
    public Condition serialize(@NotNull SerializeData data) throws SerializerException {
        String faction = data.of("Faction").assertType(String.class).get(null);

        return applyParentArgs(data, new MythicMobsFactionCondition(faction));
    }
}