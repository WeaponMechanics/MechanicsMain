package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;
import me.deecaad.weaponmechanics.weapon.projectile.Projectile;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class AirStrike implements Serializer<AirStrike> {

    /**
     * The settings of the bomb that is dropped.
     */
    private Projectile projectile;

    /**
     * Minimum/Maximum number of bombs dropped
     */
    private int min;
    private int max;

    /**
     * The height to drop the bomb from, defaults to 150
     */
    private double height;

    /**
     * The randomness/noise to add to the y position of the bomb
     */
    private double yVariation;

    /**
     * The minimum horizontal distance between bombs
     */
    private double distanceBetweenSquared;

    /**
     * The maximum horizontal distance away from the origin of the explosion
     * that a bomb can be dropped.
     */
    private double radius;

    /**
     * How many times to spawn in a volley of airstrikes
     */
    private int loops;

    /**
     * Delay between volleys (Defined by <code>loops</code>)
     */
    private int delay;

    public AirStrike() { }

    public AirStrike(Projectile projectile, int min, int max, double height, double yVariation, double distanceBetween, double radius, int loops, int delay) {
        this.projectile = projectile;
        this.min = min;
        this.max = max;
        this.height = height;
        this.yVariation = yVariation;
        this.distanceBetweenSquared = distanceBetween * distanceBetween;
        this.radius = radius;
        this.loops = loops;
        this.delay = delay;
    }

    public Projectile getProjectile() {
        return projectile;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public double getHeight() {
        return height;
    }

    public double getYVariation() {
        return yVariation;
    }

    public double getDistanceBetweenSquared() {
        return distanceBetweenSquared;
    }

    public double getRadius() {
        return radius;
    }

    public int getLoops() {
        return loops;
    }

    public int getDelay() {
        return delay;
    }

    public void trigger(Location flareLocation, LivingEntity shooter, ICustomProjectile projectile) {
        new BukkitRunnable() {

            int count = 0;

            @Override
            public void run() {

                int bombs = NumberUtil.random(min, max);
                int checks = bombs * bombs;

                // Used to make sure we don't spawn bombs too close to
                // each other. Uses distanceBetweenSquared
                List<Vector2d> spawnLocations = new ArrayList<>(bombs);

                locationFinder:
                for (int i = 0; i < checks && spawnLocations.size() < bombs; i++) {

                    double x = flareLocation.getX() + NumberUtil.random(-radius, radius);
                    double z = flareLocation.getZ() + NumberUtil.random(-radius, radius);

                    Vector2d vector = new Vector2d(x, z);

                    for (Vector2d spawnLocation : spawnLocations) {
                        if (vector.distanceSquared(spawnLocation) < distanceBetweenSquared) {
                            continue locationFinder;
                        }
                    }

                    spawnLocations.add(vector);

                    double y = flareLocation.getY() + height + NumberUtil.random(-yVariation, yVariation);
                    Location location = new Location(flareLocation.getWorld(), x, y, z);

                    (getProjectile() == null ? projectile.getProjectileSettings() : getProjectile())
                            .shoot(shooter, location, new Vector(0.0, 0.0, 0.0), projectile.getWeaponStack(), projectile.getWeaponTitle())
                            .setTag("airstrike-bomb", "true");
                }

                if (++count >= loops) {
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(WeaponMechanics.getPlugin(), 0, delay);
    }

    @Override
    public String getKeyword() {
        return "Airstrike";
    }

    @Override
    public AirStrike serialize(File file, ConfigurationSection configurationSection, String path) {

        int min = configurationSection.getInt(path + ".Minimum_Bombs", -1);
        int max = configurationSection.getInt(path + ".Maximum_Bombs", -1);

        if (min == -1 || max == -1) {
            return null;
        }

        Projectile projectileSettings = new Projectile().serialize(file, configurationSection, path + ".Dropped_Projectile");

        double yOffset = configurationSection.getDouble(path + ".Height", 60);
        double yNoise = configurationSection.getDouble(path + ".Vertical_Randomness", 5);

        double separation = configurationSection.getDouble(path + ".Distance_Between_Bombs", 3);
        double range = configurationSection.getDouble(path + ".Maximum_Distance_From_Center", 25);

        int layers = configurationSection.getInt(path + ".Layers", 3);
        int interval = configurationSection.getInt(path + ".Delay_Between_Layers", 40);

        debug.validate(LogLevel.WARN, max < 100, StringUtil.foundLarge(max, file, path + ".Maximum_Bombs"));
        debug.validate(LogLevel.WARN, layers < 100, StringUtil.foundLarge(max, file, path + ".Layers"));

        return new AirStrike(projectileSettings, min, max, yOffset, yNoise, separation, range, layers, interval);
    }
}
