package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DamageMechanic extends Mechanic {

    public static final String METADATA_KEY = "mechanicscore-damagemechanic";

    private double damage;
    private boolean resetHitCooldown;

    private boolean requiresEvent;
    private boolean ignoreArmor;

    /**
     * Default constructor for serializer
     */
    public DamageMechanic() {
    }

    public DamageMechanic(double damage, boolean ignoreArmor, boolean resetHitCooldown) {
        this.damage = damage;
        this.resetHitCooldown = resetHitCooldown;

        this.requiresEvent = ignoreArmor; // add more conditions in the future
        this.ignoreArmor = ignoreArmor;
    }

    public double getDamage() {
        return damage;
    }

    public boolean isRequireEvent() {
        return requiresEvent;
    }

    public boolean isIgnoreArmor() {
        return ignoreArmor;
    }

    @Override
    public String getKeyword() {
        return "Damage";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://cjcrafter.gitbook.io/mechanics/mechanics/damage";
    }

    @Override
    protected void use0(CastData cast) {

        // We must have an entity to ignite
        if (cast.getTarget() == null)
            return;

        LivingEntity target = cast.getTarget();
        if (requiresEvent)
            target.setMetadata(METADATA_KEY, new FixedMetadataValue(MechanicsCore.getPlugin(), this));

        // The MechanicsCastListener will modify this damage for armor
        cast.getTarget().damage(damage);

        if (resetHitCooldown)
            cast.getTarget().setNoDamageTicks(0);
    }

    @NotNull
    @Override
    public Mechanic serialize(@NotNull SerializeData data) throws SerializerException {
        double damage = data.of("Damage").getDouble(1.0);
        boolean ignoreArmor = data.of("Ignore_Armor").getBool(false);
        boolean resetHitCooldown = data.of("Reset_Cooldown").getBool(false);

        return applyParentArgs(data, new DamageMechanic(damage, ignoreArmor, resetHitCooldown));
    }
}
