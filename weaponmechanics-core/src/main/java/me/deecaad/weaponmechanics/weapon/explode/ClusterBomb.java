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
import me.deecaad.core.utils.RandomUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.Projectile;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class ClusterBomb implements Serializer<ClusterBomb> {

    private Projectile projectile;
    private double speed;
    private int splits;
    private int bombs;
    private Detonation detonation;
    private Program mechanics;

    /**
     * Default constructor for serializer
     */
    public ClusterBomb() {
    }

    public ClusterBomb(Projectile projectile, double speed, int splits, int bombs, Detonation detonation, Program mechanics) {
        this.projectile = projectile;
        this.speed = speed;
        this.splits = splits;
        this.bombs = bombs;
        this.detonation = detonation;
        this.mechanics = mechanics;
    }

    public Projectile getProjectile() {
        return projectile;
    }

    public double getSpeed() {
        return speed;
    }

    public int getSplits() {
        return splits;
    }

    public int getBombs() {
        return bombs;
    }

    public Detonation getDetonation() {
        return detonation;
    }

    public void trigger(WeaponProjectile projectile, LivingEntity shooter, Location splitLocation) {

        int currentDepth = projectile.getIntTag("cluster-split-level");

        // Checking to see if we have split the proper number of times
        if (currentDepth >= splits)
            return;

        if (mechanics != null) {
            CastScope cast = new WeaponCastData(shooter, null, projectile.getWeaponTitle(), projectile.getWeaponStack())
                .scope()
                .context("SplitLocation", Context.of(Target.of(splitLocation)))
                .variable("split_level", Value.of(currentDepth))
                .variable("bomb_count", Value.of(bombs))
                .build();
            cast.setContext(CastScope.TARGET, Context.of(Target.of(projectile.getLocation().toLocation(projectile.getWorld()))));
            mechanics.run(cast);
        }

        for (int i = 0; i < bombs; i++) {
            Vector vector = RandomUtil.onUnitSphere().multiply(speed);
            vector.setY(Math.abs(vector.getY()));

            // Either use the projectile settings from the "parent" projectile,
            // or use the projectile settings for this cluster bomb
            Projectile projectileHandler = getProjectile() != null ? getProjectile() : WeaponMechanics.getInstance().getWeaponConfigurations().getObject(projectile.getWeaponTitle() + ".Projectile", Projectile.class);
            if (projectileHandler != null) {
                WeaponProjectile newProjectile = getProjectile() != null
                    ? projectileHandler.create(shooter, splitLocation, vector, projectile.getWeaponStack(), projectile.getWeaponTitle(), projectile.getHand())
                    : projectile.clone(splitLocation, vector);
                newProjectile.setIntTag("cluster-split-level", currentDepth + 1);
                projectileHandler.shoot(newProjectile, splitLocation);
            }

        }

        // Remove the parent split
        projectile.remove();
    }

    @Override
    @NotNull public ClusterBomb serialize(@NotNull SerializeData data) throws SerializerException {
        int bombs = data.of("Number_Of_Bombs").assertExists().assertRange(1, null).getInt().getAsInt();
        Projectile projectileSettings = data.of("Split_Projectile").serialize(Projectile.class).orElse(null);
        double speed = data.of("Projectile_Speed").assertRange(0.0, null).getDouble().orElse(30.0) / 20.0;
        int splits = data.of("Number_Of_Splits").assertRange(1, null).getInt().orElse(1);
        Detonation detonation = data.of("Detonation").serialize(Detonation.class).orElse(null);
        Program mechanics = data.of("Mechanics").serialize(MechanicSerializer.builder()
            .context("SplitLocation")
            .variables("split_level", "bomb_count")
            .build()).orElse(null);

        return new ClusterBomb(projectileSettings, speed, splits, bombs, detonation, mechanics);
    }
}
