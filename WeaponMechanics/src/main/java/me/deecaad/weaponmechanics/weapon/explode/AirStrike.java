package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.Projectile;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;
import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class AirStrike implements Serializer<AirStrike> {

    private Projectile projectile;

    private int min;
    private int max;
    private double height;
    private double yVariation;
    private double distanceBetweenSquared;
    private double radius;
    private int loops;
    private int delay;
    private Detonation detonation;

    /**
     * Default constructor for serializer
     */
    public AirStrike() {
    }

    /**
     * See arguments.
     *
     * @param projectile The non-null projectile to spawn for each bomb.
     * @param min        The minimum number of bombs to spawn (per layer). min < max.
     * @param max        The maximum number of bombs to spawn (per layer). max > min.
     * @param height     The vertical distance above the initial projectile to
     *                   spawn the layers.
     * @param yVariation The random variations in the <code>height</code> parameter.
     * @param distance   The minimum distance between bombs.
     * @param radius     The maximum horizontal distance away from the initial
     *                   projectile that a bomb is allowed to spawn. Larger numbers
     *                   means higher spread.
     * @param loops      The number of layers of bombs to spawn.
     * @param delay      The amount of time (in ticks) between each layer of bombs.
     */
    public AirStrike(Projectile projectile, int min, int max, double height, double yVariation,
                     double distance, double radius, int loops, int delay, Detonation detonation) {

        this.projectile = projectile;
        this.min = min;
        this.max = max;
        this.height = height;
        this.yVariation = yVariation;
        this.distanceBetweenSquared = distance * distance;
        this.radius = radius;
        this.loops = loops;
        this.delay = delay;
        this.detonation = detonation;
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

    public double getyVariation() {
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

    public Detonation getDetonation() {
        return detonation;
    }

    public void trigger(Location flareLocation, LivingEntity shooter, WeaponProjectile projectile) {
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

                    // Either use the projectile settings from the "parent" projectile,
                    // or use the projectile settings for this airstrike
                    Projectile projectileHandler = getProjectile() != null ? getProjectile() : getConfigurations().getObject(projectile.getWeaponTitle() + ".Projectile", Projectile.class);
                    if (projectileHandler != null) {
                        WeaponProjectile newProjectile = getProjectile() != null ? projectileHandler.create(shooter, location, new Vector(0, 0, 0), projectile.getWeaponStack(), projectile.getWeaponTitle())
                                : projectile.clone(location, new Vector(0, 0, 0));
                        newProjectile.setIntTag("airstrike-bomb", 1);
                        projectileHandler.shoot(newProjectile, location);
                    }
                }

                if (++count >= loops) {
                    cancel();
                }
            }
        }.runTaskTimer(WeaponMechanics.getPlugin(), 0, delay);
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

        Detonation detonation = new Detonation().serialize(file, configurationSection, path + ".Detonation");

        return new AirStrike(projectileSettings, min, max, yOffset, yNoise, separation, range, layers, interval, detonation);
    }

    static class Vector2d {

        private final double x;
        private final double z;

        Vector2d(double x, double z) {
            this.x = x;
            this.z = z;
        }

        double distanceSquared(Vector2d vector) {
            return NumberConversions.square(this.x - vector.x) + NumberConversions.square(this.z - vector.z);
        }
    }
}
