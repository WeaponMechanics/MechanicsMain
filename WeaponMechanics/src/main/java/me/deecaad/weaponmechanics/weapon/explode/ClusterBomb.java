package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.VectorUtil;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
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

    public void setProjectile(Projectile projectile) {
        this.projectile = projectile;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getSplits() {
        return splits;
    }

    public void setSplits(int splits) {
        this.splits = splits;
    }

    public int getBombs() {
        return bombs;
    }

    public void setBombs(int bombs) {
        this.bombs = bombs;
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

            // Either use the projectile settings from the "parent" projectile,
            // or use the projectile settings for this clusterbomb
            (this.projectile == null ? projectile.getProjectileSettings() : this.projectile)
                    .shoot(shooter, splitLocation, vector, projectile.getWeaponStack(), projectile.getWeaponTitle())
                    .setIntTag("cluster-split-level", currentDepth + 1);
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

        debug.validate(bombs > 0, "Number_Of_Bombs must be a positive number!");

        Projectile projectileSettings = new Projectile().serialize(file, configurationSection, path + ".Split_Projectile");
        double speed = configurationSection.getDouble(path + ".Projectile_Speed", 15);
        int splits = configurationSection.getInt(path + ".Number_Of_Splits", 1);

        return new ClusterBomb(projectileSettings, speed / 10.0, splits, bombs);
    }
}
