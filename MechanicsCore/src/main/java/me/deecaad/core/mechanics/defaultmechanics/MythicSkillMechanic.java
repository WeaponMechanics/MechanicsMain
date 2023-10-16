package me.deecaad.core.mechanics.defaultmechanics;

import io.lumine.mythic.bukkit.MythicBukkit;
import me.deecaad.core.file.JarSearcherExempt;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.conditions.Condition;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class MythicSkillMechanic extends Mechanic implements JarSearcherExempt {

    private String skillName;
    private float power;

    /**
     * Default constructor for serializer.
     */
    public MythicSkillMechanic() {
    }

    public MythicSkillMechanic(String skillName, float power) {
        this.skillName = skillName;
        this.power = power;
    }

    protected void handleTargetersAndConditions(CastData cast) {
        use0(cast);
    }

    @Override
    public String getKeyword() {
        return "MythicSkill";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://cjcrafter.gitbook.io/mechanics/integrations/mythicmobs#mythic-skill-mechanic";
    }

    @Override
    protected void use0(CastData cast) {
        Collection<Entity> eTargets = new ArrayList<>();
        Collection<Location> lTargets = new ArrayList<>();

        OUTER:
        for (Iterator<CastData> it = targeter.getTargets(cast); it.hasNext(); ) {
            CastData target = it.next();
            for (Condition condition : conditions)
                if (!condition.isAllowed(target))
                    continue OUTER;

            if (target.getTarget() != null)
                eTargets.add(target.getTarget());
            else
                lTargets.add(target.getTargetLocation().clone());  // clone since may be modified by iterator
        }

        MythicBukkit.inst().getAPIHelper().castSkill(cast.getSource(), skillName, cast.getSourceLocation(), eTargets, lTargets, power);
    }

    @NotNull
    @Override
    public Mechanic serialize(@NotNull SerializeData data) throws SerializerException {
        String skill = data.of("Skill").assertExists().assertType(String.class).get();
        float power = (float) data.of("Power").getDouble(1.0);

        return applyParentArgs(data, new MythicSkillMechanic(skill, power));
    }
}