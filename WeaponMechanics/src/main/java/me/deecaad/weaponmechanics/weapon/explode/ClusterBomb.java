package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.VectorUtil;
import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;
import me.deecaad.weaponmechanics.weapon.projectile.Projectile;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.io.File;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class ClusterBomb implements Serializer<ClusterBomb> {

    private Projectile projectile;
    private double speed;
    private int splits;
    private int bombs;

    public ClusterBomb() { }

    public ClusterBomb(Projectile projectile, double speed, int splits, int bombs) {
        this.projectile = projectile;
        this.speed = speed;
        this.splits = splits;
        this.bombs = bombs;
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

    public void trigger(ICustomProjectile projectile, LivingEntity shooter, Location splitLocation) {

        int currentDepth = 0;

        if (projectile.getTag("cluster-split-level") != null) {
            currentDepth = Integer.parseInt(projectile.getTag("cluster-split-level"));
        }

        // Checking to see if we have split the proper number of times
        if (currentDepth >= splits) {
            return;
        }

        debug.debug("Splitting cluster bomb");

        for (int i = 0; i < bombs; i++) {
            Vector vector = VectorUtil.random(speed);

            // Either use the projectile settings from the "parent" projectile,
            // or use the projectile settings for this clusterbomb
            (this.projectile == null ? projectile.getProjectileSettings() : this.projectile)
                    .shoot(shooter, splitLocation, vector, projectile.getWeaponStack(), projectile.getWeaponTitle())
                    .setTag("cluster-split-level", String.valueOf(currentDepth + 1));
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

        Projectile projectileSettings = new Projectile().serialize(file, configurationSection, path + ".Split_Projectile");
        double speed = configurationSection.getDouble(path + ".Projectile_Speed", 15);
        int splits = configurationSection.getInt(path + ".Number_Of_Splits", 1);

        return new ClusterBomb(projectileSettings, speed / 10.0, splits, bombs);
    }
}
