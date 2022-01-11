package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.VectorUtil;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.Projectile;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.io.File;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;
import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class ClusterBomb implements Serializer<ClusterBomb> {

    private Projectile projectile;
    private double speed;
    private int splits;
    private int bombs;
    private Detonation detonation;

    public ClusterBomb() { }

    public ClusterBomb(Projectile projectile, double speed, int splits, int bombs, Detonation detonation) {
        this.projectile = projectile;
        this.speed = speed;
        this.splits = splits;
        this.bombs = bombs;
        this.detonation = detonation;
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
        if (currentDepth >= splits) {
            return;
        }

        debug.debug("Splitting cluster bomb");

        for (int i = 0; i < bombs; i++) {
            Vector vector = VectorUtil.random(speed);
            vector.setY(Math.abs(vector.getY()));

            // Either use the projectile settings from the "parent" projectile,
            // or use the projectile settings for this cluster bomb
            Projectile projectileHandler = getProjectile() != null ? getProjectile() : getConfigurations().getObject(projectile.getWeaponTitle() + ".Projectile", Projectile.class);
            if (projectileHandler != null) {
                WeaponProjectile newProjectile = getProjectile() != null ? projectileHandler.create(shooter, splitLocation, vector, projectile.getWeaponStack(), projectile.getWeaponTitle())
                        : projectile.clone(splitLocation, vector);
                newProjectile.setIntTag("cluster-split-level", currentDepth + 1);
                projectileHandler.shoot(newProjectile, splitLocation);
            }

        }

        // Remove the parent split
        projectile.remove();
    }

    @Override
    public String getKeyword() {
        return "Cluster_Bomb";
    }

    @Override
    public ClusterBomb serialize(File file, ConfigurationSection configurationSection, String path) {
        int bombs = configurationSection.getInt(path + ".Number_Of_Bombs", -1);

        if (bombs == -1) {
            return null;
        }

        debug.validate(bombs > 0, "Number_Of_Bombs must be a positive number!");

        Projectile projectileSettings = new Projectile().serialize(file, configurationSection, path + ".Split_Projectile");
        double speed = configurationSection.getDouble(path + ".Projectile_Speed", 15);
        int splits = configurationSection.getInt(path + ".Number_Of_Splits", 1);

        Detonation detonation = new Detonation().serialize(file, configurationSection, path + ".Detonation");

        return new ClusterBomb(projectileSettings, speed / 10.0, splits, bombs, detonation);
    }
}
