package me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.worldguard.WorldGuardCompatibility;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.damage.DamageHandler;
import me.deecaad.weaponmechanics.weapon.damage.DamagePoint;
import me.deecaad.weaponmechanics.weapon.explode.Explosion;
import me.deecaad.weaponmechanics.weapon.explode.ExplosionTrigger;
import me.deecaad.weaponmechanics.weapon.weaponevents.ProjectileHitBlockEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.ProjectileHitEntityEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponMeleeHitEvent;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.HandData;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class RayTraceResult {

    private static final DamageHandler damageHandler = WeaponMechanics.getWeaponHandler().getDamageHandler();

    private final Vector hitLocation;
    private final double distanceTravelled;
    private final BlockFace hitFace;

    // If block
    private Block block;

    // If living entity
    private LivingEntity livingEntity;
    private DamagePoint hitPoint;

    public RayTraceResult(Vector hitLocation, double distanceTravelled, BlockFace hitFace, Block block) {
        this.hitLocation = hitLocation;
        this.distanceTravelled = distanceTravelled;
        this.hitFace = hitFace;
        this.block = block;
    }

    public RayTraceResult(Vector hitLocation, double distanceTravelled, BlockFace hitFace, LivingEntity livingEntity, DamagePoint hitPoint) {
        this.hitLocation = hitLocation;
        this.distanceTravelled = distanceTravelled;
        this.hitFace = hitFace;
        this.livingEntity = livingEntity;
        this.hitPoint = hitPoint;
    }

    public Vector getHitLocation() {
        return hitLocation;
    }

    /**
     * @return the distance travelled during THIS iteration until hit
     */
    public double getDistanceTravelled() {
        return distanceTravelled;
    }

    public BlockFace getHitFace() {
        return hitFace;
    }

    public Block getBlock() {
        return block;
    }

    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    public DamagePoint getHitPoint() {
        return hitPoint;
    }

    public boolean isEntity() {
        return livingEntity != null;
    }

    public boolean isBlock() {
        return block != null;
    }

    /**
     * @param projectile the projectile which caused hit
     * @return true if hit was cancelled
     */
    public boolean handleHit(WeaponProjectile projectile) {
        return this.block != null ? handleBlockHit(projectile) : handleEntityHit(projectile);
    }

    /**
     * @return true if hit was cancelled
     */
    public boolean handleMeleeHit(LivingEntity shooter, Vector shooterDirection, String weaponTitle, ItemStack weaponStack) {
        // Handle worldguard flags
        WorldGuardCompatibility worldGuard = CompatibilityAPI.getWorldGuardCompatibility();
        Location loc = hitLocation.clone().toLocation(shooter.getWorld());

        if (!worldGuard.testFlag(loc, shooter instanceof Player ? (Player) shooter : null, "weapon-damage")) { // is cancelled check
            Object obj = worldGuard.getValue(loc, "weapon-damage-message");
            if (obj != null && !obj.toString().isEmpty() && shooter != null) {
                shooter.sendMessage(StringUtil.color(obj.toString()));
            }
            return true;
        }

        Configuration config = WeaponMechanics.getConfigurations();
        int meleeHitDelay = config.getInt(weaponTitle + ".Melee.Melee_Hit_Delay") / 50;
        boolean backstab = livingEntity.getLocation().getDirection().dot(shooterDirection) > 0.0;
        WeaponMeleeHitEvent event = new WeaponMeleeHitEvent(weaponTitle, weaponStack, shooter, livingEntity, meleeHitDelay, backstab);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return true;

        if (event.getMeleeHitDelay() != 0) {
            EntityWrapper wrapper = WeaponMechanics.getEntityWrapper(shooter);
            HandData hand = wrapper.getMainHandData(); // always mainhand for melee

            hand.setLastMeleeTime(System.currentTimeMillis());
        }

        return !damageHandler.tryUse(livingEntity, getConfigurations().getDouble(weaponTitle + ".Damage.Base_Damage"),
                hitPoint, backstab, shooter, weaponTitle, weaponStack, getDistanceTravelled());
    }

    private boolean handleBlockHit(WeaponProjectile projectile) {
        ProjectileHitBlockEvent hitBlockEvent = new ProjectileHitBlockEvent(projectile, block, hitFace, hitLocation.clone());
        Bukkit.getPluginManager().callEvent(hitBlockEvent);
        if (hitBlockEvent.isCancelled()) return true;

        Explosion explosion = getConfigurations().getObject(projectile.getWeaponTitle() + ".Explosion", Explosion.class);
        if (explosion != null) explosion.handleExplosion(projectile.getShooter(), hitLocation.clone().toLocation(projectile.getWorld()), projectile, ExplosionTrigger.BLOCK);

        return false;
    }

    private boolean handleEntityHit(WeaponProjectile projectile) {
        // Handle worldguard flags
        WorldGuardCompatibility worldGuard = CompatibilityAPI.getWorldGuardCompatibility();
        Location loc = hitLocation.clone().toLocation(projectile.getWorld());
        LivingEntity shooter = projectile.getShooter();

        if (!worldGuard.testFlag(loc, shooter instanceof Player ? (Player) shooter : null, "weapon-damage")) { // is cancelled check
            Object obj = worldGuard.getValue(loc, "weapon-damage-message");
            if (obj != null && !obj.toString().isEmpty() && shooter != null) {
                shooter.sendMessage(StringUtil.color(obj.toString()));
            }
            return true;
        }

        boolean backstab = livingEntity.getLocation().getDirection().dot(projectile.getMotion()) > 0.0;

        ProjectileHitEntityEvent hitEntityEvent = new ProjectileHitEntityEvent(projectile, livingEntity, hitLocation.clone(), hitPoint, backstab);
        Bukkit.getPluginManager().callEvent(hitEntityEvent);
        if (hitEntityEvent.isCancelled()) return true;

        hitPoint = hitEntityEvent.getPoint();
        backstab = hitEntityEvent.isBackStab();

        if (!damageHandler.tryUse(livingEntity, projectile, getConfigurations().getDouble(projectile.getWeaponTitle() + ".Damage.Base_Damage"), hitPoint, backstab)) {
            // Damage was cancelled
            return true;
        }

        Explosion explosion = getConfigurations().getObject(projectile.getWeaponTitle() + ".Explosion", Explosion.class);
        if (explosion != null) explosion.handleExplosion(projectile.getShooter(), hitLocation.clone().toLocation(projectile.getWorld()), projectile, ExplosionTrigger.ENTITY);

        return false;
    }

    public void outlineOnlyHitPosition(Entity player) {
        double x = hitLocation.getX();
        double y = hitLocation.getY();
        double z = hitLocation.getZ();
        if (CompatibilityAPI.getVersion() < 1.13) {
            player.getWorld().spawnParticle(Particle.CRIT, x, y, z, 1, 0, 0, 0, 0.0001);
        } else {
            player.getWorld().spawnParticle(Particle.REDSTONE, x, y, z, 1, 0, 0, 0, 0.0001, new Particle.DustOptions(Color.BLACK, 1.5f), true);
        }
    }
}