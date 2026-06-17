package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.scope.CastScope;
import me.deecaad.weaponmechanics.mechanics.WeaponCastData;
import me.deecaad.core.mechanics.scope.Context;
import me.deecaad.core.mechanics.scope.Target;
import me.deecaad.core.mechanics.scope.Value;
import me.deecaad.core.mechanics.program.MechanicSerializer;
import me.deecaad.core.mechanics.program.Program;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.weaponmechanics.weapon.explode.exposures.ExplosionExposure;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class Flashbang implements Serializer<Flashbang> {

    private double distance;
    private double distanceSquared;
    private Program mechanics;

    public Flashbang() {
    }

    public Flashbang(double distance, Program mechanics) {
        this.distance = distance;
        this.distanceSquared = distance * distance;
        this.mechanics = mechanics;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
        this.distanceSquared = distance * distance;
    }

    public Program getMechanics() {
        return mechanics;
    }

    public void setMechanics(Program mechanics) {
        this.mechanics = mechanics;
    }

    /**
     * Triggers this flashbang at this location, effecting all living entities in the radius
     * <code>distance</code>
     *
     * @param exposure The exposure type used
     * @param projectile The projectile used
     * @param origin The center of the flashbang
     */
    public void trigger(ExplosionExposure exposure, WeaponProjectile projectile, Location origin) {
        Collection<Entity> entities = origin.getWorld().getNearbyEntities(origin, distance, distance, distance);
        for (Entity entity : entities) {
            if (entity.getType() != EntityType.PLAYER) {
                continue;
            }
            LivingEntity livingEntity = (LivingEntity) entity;
            if (canEffect(exposure, origin, livingEntity)) {
                effect(projectile, livingEntity, origin);
            }
        }
    }

    public boolean canEffect(ExplosionExposure exposure, Location origin, LivingEntity entity) {

        // Check to make sure the entity is in the same world
        // of the flashbang (This check is needed for the distance check)
        if (origin.getWorld() != entity.getWorld()) {
            return false;
        }

        Location eye = entity.getEyeLocation();
        double distanceSquared = origin.distanceSquared(eye);

        // Check to make sure the entity is within the flashbang's radius
        if (this.distanceSquared < distanceSquared) {
            return false;
        }

        // Check if the explosion exposure can effect the entity
        return exposure.canSee(origin.add(0, 0.5, 0), entity);
    }

    public void effect(WeaponProjectile projectile, LivingEntity entity, Location origin) {
        if (mechanics != null) {
            CastScope cast = new WeaponCastData(entity, null, projectile.getWeaponTitle(), projectile.getWeaponStack())
                .scope()
                .context("Victim", Context.of(Target.of(entity)))
                .context("Origin", Context.of(Target.of(origin)))
                .variable("distance", Value.of(entity.getEyeLocation().distance(origin)))
                .build();
            cast.setContext(CastScope.TARGET, Context.of(Target.of(entity)));
            mechanics.run(cast);
        }
    }

    @Override
    @NotNull public Flashbang serialize(@NotNull SerializeData data) throws SerializerException {
        double distance = data.of("Effect_Distance").assertExists().assertRange(0.0, null).getDouble().getAsDouble();
        Program mechanics = data.of("Mechanics").assertExists().serialize(MechanicSerializer.builder()
            .contexts("Victim", "Origin")
            .variable("distance")
            .build()).orElse(null);

        return new Flashbang(distance, mechanics);
    }
}