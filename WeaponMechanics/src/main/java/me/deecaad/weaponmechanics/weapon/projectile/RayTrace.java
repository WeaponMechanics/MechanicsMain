package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.HitBox;
import me.deecaad.core.compatibility.RayTraceResult;
import me.deecaad.core.compatibility.worldguard.WorldGuardCompatibility;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.damage.DamageHandler;
import me.deecaad.weaponmechanics.weapon.damage.DamagePoint;
import me.deecaad.weaponmechanics.weapon.explode.Explosion;
import me.deecaad.weaponmechanics.weapon.explode.ExplosionTrigger;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import me.deecaad.weaponmechanics.weapon.weaponevents.ProjectileHitBlockEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.ProjectileHitEntityEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponMeleeHitEvent;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.HandData;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;
import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class RayTrace {

    /**
     * Simple final modifier for front hit adjusting.
     * Basically -0.2 means that 20% of hit box's front
     */
    private static final double FRONT_HIT = -0.2;
    private static final DamageHandler damageHandler = WeaponMechanics.getWeaponHandler().getDamageHandler();
    private boolean disableEntityChecks;
    private boolean disableBlockChecks;
    private Predicate<LivingEntity> entityFilter;
    private Predicate<Block> blockFilter;
    private Entity entity;
    private boolean outlineHitPosition;
    private boolean outlineHitBox;
    private boolean allowLiquid;
    private double raySize = 0.1;

    public RayTrace() { }

    public RayTrace disableEntityChecks() {
        this.disableEntityChecks = true;
        return this;
    }

    public RayTrace disableBlockChecks() {
        this.disableBlockChecks = true;
        return this;
    }

    public RayTrace withEntityFilter(Predicate<LivingEntity> entityFilter) {
        this.entityFilter = entityFilter;
        return this;
    }

    public RayTrace withBlockFilter(Predicate<Block> blockFilter) {
        this.blockFilter = blockFilter;
        return this;
    }

    public RayTrace withOutlineHitPosition(Entity entity) {
        this.outlineHitPosition = true;
        this.entity = entity;
        return this;
    }

    public RayTrace withOutlineHitBox(Entity entity) {
        this.outlineHitBox = true;
        this.entity = entity;
        return this;
    }

    public RayTrace enableLiquidChecks() {
        this.allowLiquid = true;
        return this;
    }

    public RayTrace withRaySize(double size) {
        this.raySize = size;
        return this;
    }

    public List<RayTraceResult> cast(World world, Vector start, Vector direction, double range) {
        return cast(world, start, start.clone().add(direction.clone().multiply(range)), direction);
    }

    public List<RayTraceResult> cast(World world, Vector start, Vector end) {
        return cast(world, start, end, end.clone().subtract(start).normalize());
    }

    public List<RayTraceResult> cast(World world, Vector start, Vector end, Vector direction) {
        return cast(world, start, end, direction, 0);
    }

    public List<RayTraceResult> cast(World world, Vector start, Vector end, Vector direction, int maximumBlockThrough) {
        List<RayTraceResult> hits = new ArrayList<>(5);
        getBlockHits(hits, world, start, end, direction, maximumBlockThrough);
        getEntityHits(hits, world, start, end, direction);

        if (!hits.isEmpty()) {

            // If more than 1 hit, sort based on distance travelled (lowest to highest)
            if (hits.size() > 1) hits.sort(Comparator.comparingDouble(RayTraceResult::getDistanceTravelled));

            if (this.outlineHitPosition) hits.get(0).outlineOnlyHitPosition(entity);
            if (this.outlineHitBox) {
                RayTraceResult firstHit = hits.get(0);
                if (firstHit.isBlock()) {
                    CompatibilityAPI.getBlockCompatibility().getHitBox(firstHit.getBlock()).outlineAllBoxes(entity);
                } else {
                    HitBox entityBox = CompatibilityAPI.getEntityCompatibility().getHitBox(firstHit.getLivingEntity());
                    entityBox.grow(raySize);
                    entityBox.outlineAllBoxes(entity);
                }
            }
            return hits;
        }

        return null;
    }

    private void getBlockHits(List<RayTraceResult> hits, World world, Vector start, Vector end, Vector direction, int maximumBlockThrough) {
        if (this.disableBlockChecks) return;

        // Method based on NMS block traversing

        double startX = NumberUtil.lerp(start.getX(), end.getX(), -1.0E-7);
        double startY = NumberUtil.lerp(start.getY(), end.getY(), -1.0E-7);
        double startZ = NumberUtil.lerp(start.getZ(), end.getZ(), -1.0E-7);

        int currentX = NumberUtil.intFloor(startX);
        int currentY = NumberUtil.intFloor(startY);
        int currentZ = NumberUtil.intFloor(startZ);

        Block startBlock = world.getBlockAt(currentX, currentY, currentZ);
        RayTraceResult rayStartBlock = rayBlock(startBlock, start, direction);
        if (rayStartBlock != null) {
            hits.add(rayStartBlock);

            // Don't count liquid as actual hits along the path
            if (!allowLiquid || !startBlock.isLiquid()) {
                if (maximumBlockThrough != -1 && --maximumBlockThrough < 0) return;
            }
        }

        double endX = NumberUtil.lerp(end.getX(), start.getX(), -1.0E-7);
        double endY = NumberUtil.lerp(end.getY(), start.getY(), -1.0E-7);
        double endZ = NumberUtil.lerp(end.getZ(), start.getZ(), -1.0E-7);

        double directionX = endX - startX;
        double directionY = endY - startY;
        double directionZ = endZ - startZ;
        int blockX = NumberUtil.sign(directionX);
        int blockY = NumberUtil.sign(directionY);
        int blockZ = NumberUtil.sign(directionZ);
        double addX = blockX == 0 ? Double.MAX_VALUE : (double) blockX / directionX;
        double addY = blockY == 0 ? Double.MAX_VALUE : (double) blockY / directionY;
        double addZ = blockZ == 0 ? Double.MAX_VALUE : (double) blockZ / directionZ;
        double maxX = addX * (blockX > 0 ? 1.0 - NumberUtil.frac(startX) : NumberUtil.frac(startX));
        double maxY = addY * (blockY > 0 ? 1.0 - NumberUtil.frac(startY) : NumberUtil.frac(startY));
        double maxZ = addZ * (blockZ > 0 ? 1.0 - NumberUtil.frac(startZ) : NumberUtil.frac(startZ));

        while (maximumBlockThrough > -1) {
            if (maxX > 1.0 && maxY > 1.0 && maxZ > 1.0) {
                break;
            }

            if (maxX < maxY) {
                if (maxX < maxZ) {
                    currentX += blockX;
                    maxX += addX;
                } else {
                    currentZ += blockZ;
                    maxZ += addZ;
                }
            } else if (maxY < maxZ) {
                currentY += blockY;
                maxY += addY;
            } else {
                currentZ += blockZ;
                maxZ += addZ;
            }

            Block newBlock = world.getBlockAt(currentX, currentY, currentZ);
            RayTraceResult rayNewBlock = rayBlock(newBlock, start, direction);
            if (rayNewBlock != null) {
                hits.add(rayNewBlock);

                // Don't count liquid as actual hits along the path
                if (!allowLiquid || !newBlock.isLiquid()) {
                    if (maximumBlockThrough != -1 && --maximumBlockThrough < 0) break;
                }

            }
        }
    }

    private RayTraceResult rayBlock(Block block, Vector start, Vector direction) {
        if (blockFilter != null && blockFilter.test(block)) return null;

        HitBox blockBox = CompatibilityAPI.getBlockCompatibility().getHitBox(block, allowLiquid);
        if (blockBox == null) return null;

        return blockBox.rayTrace(start, direction);
    }

    private void getEntityHits(List<RayTraceResult> hits, World world, Vector start, Vector end, Vector direction) {
        if (this.disableEntityChecks) return;
        HitBox hitBox = new HitBox(start, end);

        int minX = NumberUtil.intFloor((hitBox.getMinX() - 2.0) / 16.0);
        int maxX = NumberUtil.intFloor((hitBox.getMaxX() + 2.0) / 16.0);
        int minZ = NumberUtil.intFloor((hitBox.getMinZ() - 2.0) / 16.0);
        int maxZ = NumberUtil.intFloor((hitBox.getMaxZ() + 2.0) / 16.0);

        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                Chunk chunk = world.getChunkAt(x, z);
                for (final Entity entity : chunk.getEntities()) {
                    RayTraceResult rayNewEntity = rayEntity(hitBox, entity, start, direction);
                    if (rayNewEntity != null) {
                        hits.add(rayNewEntity);
                    }
                }
            }
        }
    }

    private RayTraceResult rayEntity(HitBox hitBox, Entity entity, Vector start, Vector direction) {
        if (!entity.getType().isAlive()) return null;
        if (entityFilter != null && entityFilter.test((LivingEntity) entity)) return null;

        HitBox entityBox = CompatibilityAPI.getEntityCompatibility().getHitBox(entity);
        if (entityBox == null) return null;

        entityBox.grow(raySize);
        if (!hitBox.overlaps(entityBox)) return null;

        return entityBox.rayTrace(start, direction);
    }

    /**
     * @param result the ray trace result used
     * @param projectile the projectile which caused hit
     * @return true if hit was cancelled
     */
    public boolean handleHit(RayTraceResult result, WeaponProjectile projectile) {
        return result.isBlock() ? handleBlockHit(result, projectile) : handleEntityHit(result, projectile);
    }

    /**
     * @return true if hit was cancelled
     */
    public boolean handleMeleeHit(RayTraceResult result, LivingEntity shooter, Vector shooterDirection, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot) {
        // Handle worldguard flags
        WorldGuardCompatibility worldGuard = CompatibilityAPI.getWorldGuardCompatibility();
        Location loc = result.getHitLocation().clone().toLocation(shooter.getWorld());

        if (!worldGuard.testFlag(loc, shooter instanceof Player ? (Player) shooter : null, "weapon-damage")) { // is cancelled check
            Object obj = worldGuard.getValue(loc, "weapon-damage-message");
            if (obj != null && !obj.toString().isEmpty() && shooter != null) {
                shooter.sendMessage(StringUtil.color(obj.toString()));
            }
            return true;
        }

        Configuration config = WeaponMechanics.getConfigurations();
        LivingEntity livingEntity = result.getLivingEntity();
        int meleeHitDelay = config.getInt(weaponTitle + ".Melee.Melee_Hit_Delay") / 50;
        boolean backstab = livingEntity.getLocation().getDirection().dot(shooterDirection) > 0.0;
        WeaponMeleeHitEvent event = new WeaponMeleeHitEvent(weaponTitle, weaponStack, shooter, slot, livingEntity, meleeHitDelay, backstab);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return true;

        if (event.getMeleeHitDelay() != 0) {
            EntityWrapper wrapper = WeaponMechanics.getEntityWrapper(shooter);
            HandData hand = wrapper.getMainHandData(); // always mainhand for melee

            hand.setLastMeleeTime(System.currentTimeMillis());
        }

        return !damageHandler.tryUse(livingEntity, getConfigurations().getDouble(weaponTitle + ".Damage.Base_Damage"),
                getDamagePoint(result, shooterDirection), backstab, shooter, weaponTitle, weaponStack, slot, result.getDistanceTravelled());
    }

    private boolean handleBlockHit(RayTraceResult result, WeaponProjectile projectile) {
        ProjectileHitBlockEvent hitBlockEvent = new ProjectileHitBlockEvent(projectile, result.getBlock(), result.getHitFace(), result.getHitLocation().clone());
        Bukkit.getPluginManager().callEvent(hitBlockEvent);
        if (hitBlockEvent.isCancelled()) return true;

        Explosion explosion = getConfigurations().getObject(projectile.getWeaponTitle() + ".Explosion", Explosion.class);
        if (explosion != null) explosion.handleExplosion(projectile.getShooter(), result.getHitLocation().clone().toLocation(projectile.getWorld()), projectile, ExplosionTrigger.BLOCK);

        return false;
    }

    private boolean handleEntityHit(RayTraceResult result, WeaponProjectile projectile) {
        // Handle worldguard flags
        WorldGuardCompatibility worldGuard = CompatibilityAPI.getWorldGuardCompatibility();
        Location loc = result.getHitLocation().clone().toLocation(projectile.getWorld());
        LivingEntity shooter = projectile.getShooter();

        if (!worldGuard.testFlag(loc, shooter instanceof Player ? (Player) shooter : null, "weapon-damage")) { // is cancelled check
            Object obj = worldGuard.getValue(loc, "weapon-damage-message");
            if (obj != null && !obj.toString().isEmpty() && shooter != null) {
                shooter.sendMessage(StringUtil.color(obj.toString()));
            }
            return true;
        }

        LivingEntity livingEntity = result.getLivingEntity();
        boolean backstab = livingEntity.getLocation().getDirection().dot(projectile.getMotion()) > 0.0;

        DamagePoint hitPoint = getDamagePoint(result, shooter.getLocation().getDirection());

        ProjectileHitEntityEvent hitEntityEvent = new ProjectileHitEntityEvent(projectile, livingEntity, result.getHitLocation().clone(), hitPoint, backstab);
        Bukkit.getPluginManager().callEvent(hitEntityEvent);
        if (hitEntityEvent.isCancelled()) return true;

        hitPoint = hitEntityEvent.getPoint();
        backstab = hitEntityEvent.isBackStab();

        if (!damageHandler.tryUse(livingEntity, projectile, getConfigurations().getDouble(projectile.getWeaponTitle() + ".Damage.Base_Damage"), hitPoint, backstab)) {
            // Damage was cancelled
            return true;
        }

        Explosion explosion = getConfigurations().getObject(projectile.getWeaponTitle() + ".Explosion", Explosion.class);
        if (explosion != null) explosion.handleExplosion(projectile.getShooter(), result.getHitLocation().clone().toLocation(projectile.getWorld()), projectile, ExplosionTrigger.ENTITY);

        return false;
    }

    /**
     * @param result the hit result
     * @param normalizedMotion the normalized direction
     * @return the damage point or null if tried to cast when living entity was not defined
     */
    private DamagePoint getDamagePoint(RayTraceResult result, Vector normalizedMotion) {
        LivingEntity livingEntity = result.getLivingEntity();
        if (livingEntity == null) return null;
        Configuration basicConfiguration = WeaponMechanics.getBasicConfigurations();

        EntityType type = livingEntity.getType();
        double entityHeight = CompatibilityAPI.getEntityCompatibility().getHeight(livingEntity);

        double hitY = result.getHitLocation().getY();

        HitBox hitBox = result.getHitBox();
        double maxY = hitBox.getMaxY();

        // Check HEAD
        double head = basicConfiguration.getDouble("Entity_Hitboxes." + type.name() + "." + DamagePoint.HEAD.name());
        if (head > 0.0 && maxY - (entityHeight * head) < hitY) {
            return DamagePoint.HEAD;
        }

        // Check BODY
        double body = basicConfiguration.getDouble("Entity_Hitboxes." + type.name() + "." + DamagePoint.BODY.name());
        if (body >= 1.0 || body > 0.0 && maxY - (entityHeight * (head + body)) < hitY) {

            boolean horizontalEntity = basicConfiguration.getBool("Entity_Hitboxes." + type.name() + ".Horizontal_Entity", false);
            boolean arms = basicConfiguration.getBool("Entity_Hitboxes." + type.name() + "." + DamagePoint.ARMS.name(), false);
            if (horizontalEntity || arms) {
                Vector normalizedEntityDirection = livingEntity.getLocation().getDirection();

                if (horizontalEntity && !hitBox.cloneDimensions().expand(normalizedEntityDirection, FRONT_HIT).collides(result.getHitLocation())) {
                    // Basically removes directionally 0.2 from this entity hitbox and check if the hit location is still in the hitbox
                    return DamagePoint.HEAD;
                }

                if (arms && Math.abs(normalizedMotion.clone().setY(0).dot(normalizedEntityDirection.setY(0))) < 0.5) {
                    return DamagePoint.ARMS;
                }
            }

            return DamagePoint.BODY;
        }

        // Check LEGS
        double legs = basicConfiguration.getDouble("Entity_Hitboxes." + type.name() + "." + DamagePoint.LEGS.name());
        if (legs > 0.0 && maxY - (entityHeight * (head + body + legs)) < hitY) {
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
}