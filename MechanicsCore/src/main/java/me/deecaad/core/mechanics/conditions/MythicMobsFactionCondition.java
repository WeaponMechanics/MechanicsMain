package me.deecaad.core.mechanics.conditions;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.conditions.Condition;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MythicMobsFactionCondition extends Condition {

    private String faction;

    public MythicMobsFactionCondition() {}

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

            if (!maybeFaction.isPresent()) return false;
            return (maybeFaction.get()).equals(faction);
        }

        ActiveMob activeMob = MythicBukkit.inst().getMobManager().getMythicMobInstance(target);
        if (activeMob == null || !activeMob.hasFaction()) return false;


        return activeMob.getFaction().equals(faction);
    }

    @Override
    public String getKeyword() {
        return "MythicMobsFaction";
    }

    @NotNull
    @Override
    public Condition serialize(SerializeData data) throws SerializerException {
        String faction = data.of("Faction").assertExists().get();

        return applyParentArgs(data, new MythicMobsFactionCondition(faction));
    }
}