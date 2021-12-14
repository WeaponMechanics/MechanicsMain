package me.deecaad.weaponmechanics.compatibility.projectile;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.VectorUtil;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.damage.DamagePoint;
import me.deecaad.weaponmechanics.weapon.projectile.CollisionData;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.function.Predicate;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class HitBox implements IValidator {

    /**
     * Simple final modifier for front hit adjusting.
     * Basically -0.2 means that 20% of hit box's front
     */
    private static final double FRONT_HIT = -0.2;
    private static final IProjectileCompatibility projectileCompatibility = WeaponCompatibilityAPI.getProjectileCompatibility();

    public Vector min;
    public Vector max;
    public Vector direction;

    public HitBox() { }

    public HitBox(Vector min, Vector max) {
        this.min = min;
        this.max = max;
    }

    public HitBox(Vector center, float width, float length) {
        min = new Vector();
        max = new Vector();
        update(center, width, length);
    }

    /**
     * Gets the Height of this <code>HitBox</code>,
     * or the change in y between the lower and upper
     * point.
     *
     * @return The (positive) height of the box
     */
    public double getHeight() {
        return max.getY() - min.getY();
    }

    /**
     * Gets the Width of this <code>HitBox</code>,
     * or the change in x between the lower and upper
     * point.
     *
     * @return The (positive) width of the box
     */
    public double getWidth() {
        return max.getX() - min.getX();
    }

    /**
     * Gets the Depth of this <code>HitBox</code>,
     * or the change in z between the lower and upper
     * point.
     *
     * @return The (positive) depth of the box
     */
    public double getDepth() {
        return max.getZ() - min.getZ();
    }

    /**
     * Updates this hit box minimum and maximum values to match new location
     *
     * @param center the center of hit box
     * @param width the width of hit box (XZ)
     * @param length the length of hit box (Y)
     */
    public void update(Vector center, float width, float length) {
        float widthDivided = width / 2;
        float lengthDivided = length / 2;

        min.setX(center.getX() - widthDivided);
        min.setY(center.getY() - lengthDivided);
        min.setZ(center.getZ() - widthDivided);

        max.setX(center.getX() + widthDivided);
        max.setY(center.getY() + lengthDivided);
        max.setZ(center.getZ() + widthDivided);
    }

    /**
     * This is mostly used with custom projectiles to shift the hit box based on motion.
     * Basically this just moves hit box with specific amount.
     *
     * @param motion the motion used to shift hit box
     */
    public void shift(Vector motion) {
        min.add(motion);
        max.add(motion);
    }

    /**
     * @param other the other hit box to compare
     * @return true only if these two hit boxes collide with each other at least in one corner
     */
    public boolean collides(HitBox other) {
        return min.getX() < other.max.getX()
                && max.getX() > other.min.getX()
                && min.getY() < other.max.getY()
                && max.getY() > other.min.getY()
                && min.getZ() < other.max.getZ()
                && max.getZ() > other.min.getZ();
    }

    public Vector getCenter() {
        return VectorUtil.lerp(min, max, 0.5);
    }

    /**
     * This is separated to new method for easier usage from collidesFront method.
     * That's why this is private.
     */
    private boolean collides(Vector point, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return point.getX() >= minX
                && point.getX() <= maxX
                && point.getY() >= minY
                && point.getY() <= maxY
                && point.getZ() >= minZ
                && point.getZ() <= maxZ;
    }

    /**
     * This is used to get the center of hit box collision with other hit box.
     *
     * @param other the other hit box to compare
     * @return the collision point's center as vector OR null if hit boxes don't collide
     */
    public Vector collisionPoint(HitBox other) {
        if (!collides(other)) return null;

        Vector point = direction != null ? getCenter().add(direction.clone().multiply(-Math.max(getWidth(), getHeight()))) : getCenter();

        return new Vector(NumberUtil.minMax(other.min.getX(), point.getX(), other.max.getX()),
                NumberUtil.minMax(other.min.getY(), point.getY(), other.max.getY()),
                NumberUtil.minMax(other.min.getZ(), point.getZ(), other.max.getZ()));
    }

    /**
     * Returns <code>true</code> if the given location <code>loc</code> is inside
     * this hitbox.
     *
     * @param loc The point to test
     * @return If the point is in this hitbox
     * @throws IllegalArgumentException If the given point is null
     */
    public boolean contains(@Nonnull Vector loc) {
        if (loc == null) throw new IllegalArgumentException("loc cannot be null");

        return loc.getX() > min.getX() && loc.getX() < max.getX() &&
                loc.getY() > min.getY() && loc.getY() < max.getY() &&
                loc.getZ() > min.getZ() && loc.getZ() < max.getZ();
    }

    /**
     * Returns <code>true</code> if the given point <code>loc</code> is inside
     * this hitbox.
     *
     * @param loc The point to test
     * @return If the point is in this hitbox
     * @throws IllegalArgumentException If the given point is null
     */
    public boolean contains(@Nonnull Location loc) {
        if (loc == null) throw new IllegalArgumentException("loc cannot be null");

        return loc.getX() > min.getX() && loc.getX() < max.getX() &&
                loc.getY() > min.getY() && loc.getY() < max.getY() &&
                loc.getZ() > min.getZ() && loc.getZ() < max.getZ();
    }

    /**
     * @param collisionData the collision data (must be living entity)
     * @param normalizedDirection the direction of projectile
     * @return the damage point of projectile
     */
    public DamagePoint getDamagePoint(CollisionData collisionData, Vector normalizedDirection) {
        LivingEntity livingEntity = collisionData.getLivingEntity();
        EntityType type = livingEntity.getType();
        Configuration basicConfiguration = WeaponMechanics.getBasicConfigurations();

        HitBox collisionHitBox = collisionData.getHitBox();
        double entityMaxY = collisionHitBox.max.getY();
        double entityHeight = entityMaxY - collisionHitBox.min.getY();

        double hitY = collisionData.getHitLocation().getY();

        // Check HEAD
        double head = basicConfiguration.getDouble("Entity_Hitboxes." + type.name() + "." + DamagePoint.HEAD.name());
        if (head > 0.0 && entityMaxY - (entityHeight * head) < hitY) {
            return DamagePoint.HEAD;
        }

        // Check BODY
        double body = basicConfiguration.getDouble("Entity_Hitboxes." + type.name() + "." + DamagePoint.BODY.name());
        if (body >= 1.0 || body > 0.0 && entityMaxY - (entityHeight * (head + body)) < hitY) {
            boolean horizontalEntity = basicConfiguration.getBool("Entity_Hitboxes." + type.name() + ".Horizontal_Entity", false);
            boolean arms = basicConfiguration.getBool("Entity_Hitboxes." + type.name() + "." + DamagePoint.ARMS.name(), false);

            Vector normalizedEntityDirection = livingEntity.getLocation().getDirection();
            if (horizontalEntity && collidesFront(normalizedEntityDirection, collisionData.getHitLocation())) {
                return DamagePoint.HEAD;
            }

            double dot = normalizedDirection.clone().setY(0).dot(normalizedEntityDirection.setY(0));
            if (arms && dot < 0.5 && dot > -0.5) {
                return DamagePoint.ARMS;
            }

            return DamagePoint.BODY;
        }

        // Check LEGS
        double legs = basicConfiguration.getDouble("Entity_Hitboxes." + type.name() + "." + DamagePoint.LEGS.name());
        if (legs > 0.0 && entityMaxY - (entityHeight * (head + body + legs)) < hitY) {
            return DamagePoint.LEGS;
        }

        // Check FEET
        double feet = basicConfiguration.getDouble("Entity_Hitboxes." + type.name() + "." + DamagePoint.FEET.name());
        if (feet > 0.0) { // No need for actual check since it can't be HEAD, BODY or LEGS anymore so only option left is FEET
            return DamagePoint.FEET;
        }

        debug.log(LogLevel.WARN, "Something unexpected happened and HEAD, BODY, LEGS or FEET wasn't valid",
                "This should never happen. Using BODY as default value...",
                "This happened with entity type " + type + ".");
        return DamagePoint.BODY;
    }

    /**
     * Checks whether or not point is in front of this hit box
     *
     * @param direction the direction of hit box's holder
     * @param point the point to check
     * @return true only if point is inside front of the hit box
     */
    public boolean collidesFront(Vector direction, Vector point) {
        double dirX = direction.getX(), dirY = direction.getY(), dirZ = direction.getZ();

        double negativeX = dirX < 0.0D ? -dirX * FRONT_HIT : 0.0D;
        double negativeY = dirY < 0.0D ? -dirY * FRONT_HIT : 0.0D;
        double negativeZ = dirZ < 0.0D ? -dirZ * FRONT_HIT : 0.0D;
        double positiveX = dirX > 0.0D ? dirX * FRONT_HIT : 0.0D;
        double positiveY = dirY > 0.0D ? dirY * FRONT_HIT : 0.0D;
        double positiveZ = dirZ > 0.0D ? dirZ * FRONT_HIT : 0.0D;

        double newMinX = min.getX() - negativeX;
        double newMinY = min.getY() - negativeY;
        double newMinZ = min.getZ() - negativeZ;
        double newMaxX = max.getX() + positiveX;
        double newMaxY = max.getY() + positiveY;
        double newMaxZ = max.getZ() + positiveZ;
        double centerZ;
        if (newMinX > newMaxX) {
            centerZ = min.getX() + (max.getX() - min.getX()) * 0.5;
            if (newMaxX >= centerZ) {
                newMinX = newMaxX;
            } else if (newMinX <= centerZ) {
                newMaxX = newMinX;
            } else {
                newMinX = centerZ;
                newMaxX = centerZ;
            }
        }

        if (newMinY > newMaxY) {
            centerZ = min.getY() + (max.getY() - min.getY()) * 0.5;
            if (newMaxY >= centerZ) {
                newMinY = newMaxY;
            } else if (newMinY <= centerZ) {
                newMaxY = newMinY;
            } else {
                newMinY = centerZ;
                newMaxY = centerZ;
            }
        }

        if (newMinZ > newMaxZ) {
            centerZ = min.getZ() + (max.getZ() - min.getZ()) * 0.5;
            if (newMaxZ >= centerZ) {
                newMinZ = newMaxZ;
            } else if (newMinZ <= centerZ) {
                newMaxZ = newMinZ;
            } else {
                newMinZ = centerZ;
                newMaxZ = centerZ;
            }
        }
        return !collides(point, newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
    }

    /**
     * @return the floored minimum vector
     */
    public Vector getFlooredMin() {
        return new Vector(floor(min.getX() + 0.001), floor(min.getY() + 0.001), floor(min.getZ() + 0.001));
    }

    /**
     * @return the floored maximum vector
     */
    public Vector getFlooredMax() {
        return new Vector(floor(max.getX() - 0.001), floor(max.getY() - 0.001), floor(max.getZ() - 0.001));
    }

    /**
     * Simple flooring method used in NMS
     *
     * @param toFloor value to be floored
     * @return the floored value
     */
    private int floor(double toFloor) {
        int flooredValue = (int) toFloor;
        return toFloor < (double) flooredValue ? flooredValue - 1 : flooredValue;
    }

    /**
     * @param world the world where to check
     * @param filter if filter matches entity, its not valid
     * @return one entity in box if found
     */
    @Nullable
    public CollisionData getEntityInBox(World world, @Nullable Predicate<Entity> filter) {
        int minX = floor((min.getX() - 2.0D) / 16.0D);
        int maxX = floor((max.getX() + 2.0D) / 16.0D);
        int minZ = floor((min.getZ() - 2.0D) / 16.0D);
        int maxZ = floor((max.getZ() + 2.0D) / 16.0D);
        for(int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                Chunk chunk = world.getChunkAt(x, z);
                for (final Entity entity : chunk.getEntities()) {

                    HitBox entityBox = projectileCompatibility.getHitBox(entity);
                    if (entityBox == null) continue; // entity is invulnerable or non alive

                    Vector hitLocation = collisionPoint(entityBox);
                    if (hitLocation == null) continue; // Null means that projectile hit box and entity hit box didn't collide

                    if (filter != null && filter.test(entity)) continue;

                    return new CollisionData(entityBox, hitLocation, (LivingEntity) entity);
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "HitBox{" +
                "min=" + min +
                ", max=" + max +
                '}';
    }

    @Override
    public String getKeyword() {
        return "Entity_Hitboxes";
    }

    @Override
    public void validate(Configuration configuration, File file, ConfigurationSection configurationSection, String path) {
        for (EntityType entityType : EntityType.values()) {
            if (!entityType.isAlive()) continue;

            double head = configuration.getDouble("Entity_Hitboxes." + entityType.name() + "." + DamagePoint.HEAD.name(), -1.0);
            double body = configuration.getDouble("Entity_Hitboxes." + entityType.name() + "." + DamagePoint.BODY.name(), -1.0);
            double legs = configuration.getDouble("Entity_Hitboxes." + entityType.name() + "." + DamagePoint.LEGS.name(), -1.0);
            double feet = configuration.getDouble("Entity_Hitboxes." + entityType.name() + "." + DamagePoint.FEET.name(), -1.0);

            if (head < 0 || body < 0 || legs < 0 || feet < 0) {
                debug.log(LogLevel.WARN, "Entity type " + entityType.name() + " is missing some of its damage point values, please add it",
                        "Located at file /WeaponMechanics/config.yml in Entity_Hitboxes." + entityType.name() + " in configurations",
                        "Its missing one of these: HEAD, BODY, LEGS or FEET");

                putDefaults(configuration, entityType);
                continue;
            }

            boolean horizontalEntity = configuration.getBool("Entity_Hitboxes." + entityType.name() + ".Horizontal_Entity", false);
            if (horizontalEntity && head > 0.0) {
                debug.log(LogLevel.WARN, "Entity type " + entityType.name() + " hit box had horizontal entity true and HEAD was not 0.0",
                        "Located at file /WeaponMechanics/config.yml in Entity_Hitboxes." + entityType.name() + " in configurations",
                        "When using horizontal entity true HEAD should be set to 0.0!");

                // Set default value to BODY
                putDefaults(configuration, entityType);
                continue;
            }

            double sumOf = head + body + legs + feet;
            if (Math.abs(sumOf - 1.0) > 1e-5) { // If the numbers are not super close together (floating point issues)
                debug.log(LogLevel.WARN, "Entity type " + entityType.name() + " hit box values sum doesn't match 1.0",
                        "Located at file /WeaponMechanics/config.yml in Entity_Hitboxes." + entityType.name() + " in configurations",
                        "Now the total sum was " + sumOf + ", please make it 1.0.");

                putDefaults(configuration, entityType);
            }
        }
    }

    /**
     * Simply resets hit boxes to default is they're missing or are invalid
     *
     * @param basicConfiguration the config.yml configuration instance
     * @param entityType the entity type
     */
    private void putDefaults(Configuration basicConfiguration, EntityType entityType) {
        basicConfiguration.set("Entity_Hitboxes." + entityType.name() + "." + DamagePoint.HEAD.name(), 0.0);

        // Set default value to BODY 100%
        basicConfiguration.set("Entity_Hitboxes." + entityType.name() + "." + DamagePoint.BODY.name(), 1.0);

        basicConfiguration.set("Entity_Hitboxes." + entityType.name() + "." + DamagePoint.LEGS.name(), 0.0);
        basicConfiguration.set("Entity_Hitboxes." + entityType.name() + "." + DamagePoint.FEET.name(), 0.0);

        basicConfiguration.set("Entity_Hitboxes." + entityType.name() + "." + DamagePoint.ARMS.name(), false);
        basicConfiguration.set("Entity_Hitboxes." + entityType.name() + ".Horizontal_Entity", false);
    }
}
