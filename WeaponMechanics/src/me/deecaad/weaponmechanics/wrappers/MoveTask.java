package me.deecaad.weaponmechanics.wrappers;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.compatibility.projectile.HitBox;
import me.deecaad.compatibility.projectile.IProjectileCompatibility;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.events.PlayerJumpEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MoveTask extends BukkitRunnable {

    private IEntityWrapper entityWrapper;
    private Location from;
    private int sameMatches;
    private int jumps;
    private int groundTicks;

    public MoveTask(IEntityWrapper entityWrapper) {
        this.entityWrapper = entityWrapper;
        LivingEntity entity = entityWrapper.getEntity();
        this.from = entity.getLocation();
        if (entity instanceof Player) {
            this.jumps = ((Player) entity).getStatistic(Statistic.JUMP);
        } else {
            this.jumps = -1;
        }
    }

    @Override
    public void run() {
        LivingEntity entity = entityWrapper.getEntity();
        if (entity == null || !entity.isValid()) { // Just an extra check in case something odd happened
            cancel();
            return;
        }

        Location from = this.from;
        Location to = entity.getLocation();

        boolean disableStandOrWalkCheck = WeaponMechanics.getBasicConfigurations().getBool("Disabled_Trigger_Checks.Standing_And_Walking");

        this.from = to;
        if (isSameLocationNonRotation(from, to)) {
            ++this.sameMatches;
        } else {
            this.sameMatches = 0;
        }

        if (this.sameMatches > 3) {
            if (!disableStandOrWalkCheck) {
                entityWrapper.setStanding(true);
            }
            return;
        } else if (!disableStandOrWalkCheck) {
            entityWrapper.setWalking(true);
        }

        boolean inMidairCheck = isInMidair(entity);

        // Needed for double jump
        if (inMidairCheck) {
            groundTicks = 0;
        } else {
            ++groundTicks;
        }

        if (!WeaponMechanics.getBasicConfigurations().getBool("Disabled_Trigger_Checks.In_Midair")) {
            if (inMidairCheck) {
                if (!entityWrapper.isInMidair()) {
                    entityWrapper.setInMidair(true);
                }
            } else if (entityWrapper.isInMidair()) {
                entityWrapper.setInMidair(false);
            }
        }

        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;

        if (this.jumps != -1) {
            if (!WeaponMechanics.getBasicConfigurations().getBool("Disabled_Trigger_Checks.Jump")) {
                if (from.getY() < to.getY() && !player.getLocation().getBlock().isLiquid()) {
                    int currentJumps = player.getStatistic(Statistic.JUMP);
                    int jumpsLast = this.jumps;
                    if (currentJumps != jumpsLast) {
                        this.jumps = currentJumps;
                        double yChange = to.getY() - from.getY();
                        if ((yChange < 0.035 || yChange > 0.037) && (yChange < 0.116 || yChange > 0.118)) {
                            Bukkit.getPluginManager().callEvent(new PlayerJumpEvent(player, false));
                        }
                    }
                }
            }
        }

        if (!WeaponMechanics.getBasicConfigurations().getBool("Disabled_Trigger_Checks.Double_Jump")) {
            if (!player.getAllowFlight() && (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE)) {

                // Only give double jump ability if been on ground for at least 20 ticks
                if (this.groundTicks > 20) {
                    player.setAllowFlight(true);
                }
            }
        }
    }

    private boolean isSameLocationNonRotation(Location location1, Location location2) {
        if (Double.doubleToLongBits(location1.getX()) != Double.doubleToLongBits(location2.getX())) {
            return false;
        }
        if (Double.doubleToLongBits(location1.getY()) != Double.doubleToLongBits(location2.getY())) {
            return false;
        }
        return Double.doubleToLongBits(location1.getZ()) == Double.doubleToLongBits(location2.getZ());
    }

    /**
     * Basically checks if entity is in mid air.
     * Mid air is determined on if current block in player's position doesn't have hit box and block below that doesn't have hit box either
     */
    private boolean isInMidair(LivingEntity livingEntity) {
        IProjectileCompatibility projectileCompatibility = CompatibilityAPI.getCompatibility().getProjectileCompatibility();
        Block current = livingEntity.getLocation().getBlock();
        Block below = current.getRelative(BlockFace.DOWN);

        HitBox belowHitBox = projectileCompatibility.getHitBox(below);
        HitBox currentHitBox = projectileCompatibility.getHitBox(current);
        return belowHitBox == null && currentHitBox == null;
    }
}