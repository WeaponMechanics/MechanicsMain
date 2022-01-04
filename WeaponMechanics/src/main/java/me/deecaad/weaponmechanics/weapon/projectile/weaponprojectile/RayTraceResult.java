package me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile;

import me.deecaad.core.compatibility.worldguard.IWorldGuardCompatibility;
import me.deecaad.core.compatibility.worldguard.WorldGuardAPI;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.damage.DamageHandler;
import me.deecaad.weaponmechanics.weapon.damage.DamagePoint;
import me.deecaad.weaponmechanics.weapon.explode.Explosion;
import me.deecaad.weaponmechanics.weapon.weaponevents.ProjectileHitBlockEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.ProjectileHitEntityEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.ProjectilePreExplodeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Set;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;
import static me.deecaad.weaponmechanics.WeaponMechanics.getPlugin;

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

    public boolean handleHit(WeaponProjectile projectile) {
        return this.block != null ? handleBlockHit(projectile) : handleEntityHit(projectile);
    }

    private boolean handleBlockHit(WeaponProjectile projectile) {
        ProjectileHitBlockEvent hitBlockEvent = new ProjectileHitBlockEvent(projectile, block, hitFace, hitLocation.clone());
        Bukkit.getPluginManager().callEvent(hitBlockEvent);
        if (hitBlockEvent.isCancelled()) return true;

        Explosion explosion = getConfigurations().getObject(projectile.getWeaponTitle() + ".Explosion", Explosion.class);
        if (explosion != null) {
            Set<Explosion.ExplosionTrigger> triggers = explosion.getTriggers();
            boolean explosionTriggered = projectile.getIntTag("explosion-detonated") == 1;
            boolean fluid = block.isLiquid() && triggers.contains(Explosion.ExplosionTrigger.LIQUID);
            boolean solid = block.getType().isSolid() && triggers.contains(Explosion.ExplosionTrigger.BLOCK);

            if (!explosionTriggered && (fluid || solid)) {

                // Handle worldguard flags
                IWorldGuardCompatibility worldGuard = WorldGuardAPI.getWorldGuardCompatibility();
                Location loc = hitLocation.clone().toLocation(projectile.getWorld());
                LivingEntity shooter = projectile.getShooter();
                if (shooter instanceof Player
                        ? !worldGuard.testFlag(loc, (Player) shooter, "weapon-explode")
                        : !worldGuard.testFlag(loc, null, "weapon-explode")) {

                    Object obj = worldGuard.getValue(loc, "weapon-explode-message");
                    if (obj != null && !obj.toString().isEmpty()) {
                        shooter.sendMessage(StringUtil.color(obj.toString()));
                    }
                } else {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            ProjectilePreExplodeEvent event = new ProjectilePreExplodeEvent(projectile, explosion);
                            Bukkit.getPluginManager().callEvent(event);
                            if (!event.isCancelled() && event.getExplosion() != null) {
                                event.getExplosion().explode(shooter, loc, projectile);

                                if (projectile.getStickedData() != null) {
                                    projectile.remove();
                                }
                            }
                        }
                    }.runTaskLater(getPlugin(), explosion.getDelay());
                    projectile.setIntTag("explosion-detonated", 1);
                }
            }
        }

        return false;
    }

    private boolean handleEntityHit(WeaponProjectile projectile) {
        // Handle worldguard flags
        IWorldGuardCompatibility worldGuard = WorldGuardAPI.getWorldGuardCompatibility();
        Location loc = hitLocation.clone().toLocation(projectile.getWorld());
        LivingEntity shooter = projectile.getShooter();

        if (shooter instanceof Player
                ? !worldGuard.testFlag(loc, (Player) shooter, "weapon-damage")
                : !worldGuard.testFlag(loc, null, "weapon-damage")) { // is cancelled check
            Object obj = worldGuard.getValue(loc, "weapon-damage-message");
            if (obj != null && !obj.toString().isEmpty()) {
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
        if (explosion != null && projectile.getIntTag("explosion-detonated") == 0 && explosion.getTriggers().contains(Explosion.ExplosionTrigger.ENTITY)) {

            // Handle worldguard flags
            if (shooter instanceof Player
                    ? !worldGuard.testFlag(loc, (Player) shooter, "weapon-explode")
                    : !worldGuard.testFlag(loc, null, "weapon-explode")) { // is cancelled check
                Object obj = worldGuard.getValue(loc, "weapon-explode-message");
                if (obj != null && !obj.toString().isEmpty()) {
                    shooter.sendMessage(StringUtil.color(obj.toString()));
                }
            } else {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        explosion.explode(shooter, RayTraceResult.this, projectile);

                        if (projectile.getStickedData() != null) {
                            projectile.remove();
                        }
                    }
                }.runTaskLater(getPlugin(), explosion.getDelay());

                projectile.setIntTag("explosion-detonated", 1);
            }
        }

        return false;
    }
}