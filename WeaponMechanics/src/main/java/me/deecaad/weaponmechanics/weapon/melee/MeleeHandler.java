package me.deecaad.weaponmechanics.weapon.melee;

import co.aikar.timings.lib.MCTiming;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.compatibility.IWeaponCompatibility;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.projectile.HitBox;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.RayTraceResult;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.HandData;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;
import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class MeleeHandler implements IValidator {

    private static final IWeaponCompatibility weaponCompatibility = WeaponCompatibilityAPI.getWeaponCompatibility();

    private WeaponHandler weaponHandler;

    public MeleeHandler() {}

    public MeleeHandler(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    /**
     * Tries to use melee
     *
     * @param entityWrapper the entity who used trigger
     * @param weaponTitle   the weapon title
     * @param weaponStack   the weapon stack
     * @param slot          the slot used on trigger
     * @param triggerType   the trigger type trying to activate melee
     * @param dualWield     whether this was dual wield
     * @return true if was able to melee
     */
    public boolean tryUse(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, TriggerType triggerType, boolean dualWield, @Nullable LivingEntity knownVictim) {
        if (triggerType != TriggerType.MELEE) return false;
        Configuration config = getConfigurations();
        if (!config.getBool(weaponTitle + ".Melee.Enable_Melee")) {

            // Change weapon title to match the attachment
            weaponTitle = config.getString(weaponTitle + ".Melee.Melee_Attachment");

            // Melee isn't used for this weapon nor the melee attachment is defined
            if (weaponTitle == null) return false;
        }

        MCTiming meleeHandlerTiming = WeaponMechanics.timing("Melee Handler").startTiming();
        boolean result = meleeWithoutTimings(entityWrapper, weaponTitle, weaponStack, slot, triggerType, dualWield, knownVictim);
        meleeHandlerTiming.stopTiming();

        return result;
    }

    private boolean meleeWithoutTimings(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, TriggerType triggerType, boolean dualWield, @Nullable LivingEntity knownVictim) {
        Configuration config = getConfigurations();
        HandData handData = entityWrapper.getMainHandData();

        int meleeHitDelay = config.getInt(weaponTitle + ".Melee.Melee_Hit_Delay");
        if (meleeHitDelay != 0 && !NumberUtil.hasMillisPassed(handData.getLastMeleeTime(), meleeHitDelay)) return false;

        int meleeMissDelay = config.getInt(weaponTitle + ".Melee.Melee_Miss.Melee_Miss_Delay");
        if (meleeMissDelay != 0 && !NumberUtil.hasMillisPassed(handData.getLastMeleeMissTime(), meleeMissDelay)) return false;

        double meleeRange = config.getDouble(weaponTitle + ".Melee.Melee_Range");
        LivingEntity shooter = entityWrapper.getEntity();
        Location eyeLocation = shooter.getEyeLocation();
        Vector direction = eyeLocation.getDirection();
        RayTraceResult hit = getHit(shooter, eyeLocation, direction, meleeRange, knownVictim);

        boolean result = false;
        if (hit != null) {
            result = weaponHandler.getShootHandler().shootWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, triggerType, dualWield);
            if (result) {
                if (meleeHitDelay != 0) {
                    handData.setLastMeleeTime(System.currentTimeMillis());
                    if (getConfigurations().getBool(weaponTitle + ".Info.Show_Cooldown.Melee_Hit_Delay") && shooter.getType() == EntityType.PLAYER) {
                        CompatibilityAPI.getEntityCompatibility().setCooldown((Player) shooter, weaponStack.getType(), meleeHitDelay / 50);
                    }
                }
                hit.handleMeleeHit(shooter, direction, weaponTitle, weaponStack);
            }
        } else {
            // Handle miss
            Mechanics meleeMissMechanics = getConfigurations().getObject(weaponTitle + ".Melee.Melee_Miss.Mechanics", Mechanics.class);
            if (meleeMissMechanics != null) meleeMissMechanics.use(new CastData(entityWrapper, weaponTitle, weaponStack));

            if (getConfigurations().getBool(weaponTitle + ".Melee.Melee_Miss.Consume_On_Miss")) {
                weaponHandler.getShootHandler().shootWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, triggerType, dualWield);
                result = true;
            }

            if (meleeMissDelay != 0) {
                handData.setLastMeleeMissTime(System.currentTimeMillis());

                if (getConfigurations().getBool(weaponTitle + ".Info.Show_Cooldown.Melee_Miss_Delay") && shooter.getType() == EntityType.PLAYER) {
                    CompatibilityAPI.getEntityCompatibility().setCooldown((Player) shooter, weaponStack.getType(), meleeMissDelay / 50);
                }
            }
        }
        return result;
    }

    private RayTraceResult getHit(LivingEntity shooter, Location eyeLocation, Vector direction, double range, @Nullable LivingEntity knownVictim) {

        Vector eyeLocationToVector = eyeLocation.toVector();

        if (knownVictim == null) {

            if (range <= 0) {
                return null;
            }

            double blockIteratorDistance = Math.ceil(range);

            RayTraceResult hit = null;
            BlockIterator blocks = new BlockIterator(eyeLocation.getWorld(), eyeLocationToVector, direction, 0.0, (int) (blockIteratorDistance < 1 ? 1 : blockIteratorDistance));
            while (blocks.hasNext()) {
                Block block = blocks.next();

                HitBox blockBox = weaponCompatibility.getHitBox(block);
                if (blockBox == null) continue;

                RayTraceResult rayTraceResult = blockBox.rayTrace(eyeLocationToVector, direction);
                if (rayTraceResult == null) continue; // Didn't hit

                hit = rayTraceResult;
                break;
            }

            double distanceTravelledCheck = hit != null ? hit.getDistanceTravelled() : -1;
            List<LivingEntity> entities = getPossibleEntities(shooter, eyeLocation, (hit != null ? hit.getHitLocation() : eyeLocation.toVector().add(direction.clone().multiply(range))));
            hit = null;
            if (entities != null && !entities.isEmpty()) {
                for (LivingEntity entity : entities) {
                    HitBox entityBox = weaponCompatibility.getHitBox(entity);
                    if (entityBox == null) continue;

                    RayTraceResult rayTraceResult = entityBox.rayTrace(eyeLocationToVector, direction);
                    if (rayTraceResult == null) continue;// Didn't hit

                    double rayTraceResultDistance = rayTraceResult.getDistanceTravelled();
                    if (rayTraceResultDistance > range) continue; // Didn't hit in range

                    if (distanceTravelledCheck == -1 || rayTraceResultDistance < distanceTravelledCheck) {
                        // Only change if closer than last hit result
                        hit = rayTraceResult;
                        distanceTravelledCheck = rayTraceResultDistance;
                    }
                }
            }

            return hit;
        }

        // Simply check where known victim was hit and whether it was in range
        HitBox entityBox = weaponCompatibility.getHitBox(knownVictim);
        if (entityBox == null) return null;

        RayTraceResult rayTraceResult = entityBox.rayTrace(eyeLocationToVector, direction);
        if (rayTraceResult == null || (range > 0 && rayTraceResult.getDistanceTravelled() > range)) return null; // Didn't hit in range

        return rayTraceResult;
    }

    private List<LivingEntity> getPossibleEntities(LivingEntity shooter, Location start, Vector end) {

        // Get the box of current location to end of this iteration
        HitBox hitBox = new HitBox(start.toVector(), end);

        int minX = floor((hitBox.getMinX() - 2.0D) / 16.0D);
        int maxX = floor((hitBox.getMaxX() + 2.0D) / 16.0D);
        int minZ = floor((hitBox.getMinZ() - 2.0D) / 16.0D);
        int maxZ = floor((hitBox.getMaxZ() + 2.0D) / 16.0D);

        List<LivingEntity> entities = new ArrayList<>(8);

        World world = start.getWorld();

        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                Chunk chunk = world.getChunkAt(x, z);
                for (final Entity entity : chunk.getEntities()) {
                    if (!entity.getType().isAlive() || entity.isInvulnerable() || entity.getEntityId() == shooter.getEntityId()) continue;

                    entities.add((LivingEntity) entity);
                }
            }
        }

        return entities.isEmpty() ? null : entities;
    }

    private int floor(double toFloor) {
        int flooredValue = (int) toFloor;
        return toFloor < flooredValue ? flooredValue - 1 : flooredValue;
    }

    @Override
    public String getKeyword() {
        return "Melee";
    }

    @Override
    public void validate(Configuration configuration, File file, ConfigurationSection configurationSection, String path) {
        boolean enableMelee = configurationSection.getBoolean(path + ".Enable_Melee");
        String meleeAttachment = configurationSection.getString(path + ".Melee_Attachment");
        if (!enableMelee && meleeAttachment == null) {
            debug.log(LogLevel.ERROR, "Tried to use melee without enable melee or melee attachment.",
                    "Located at file " + file + " in " + path + " in configurations.");
        }

        int meleeHitDelay = configuration.getInt(path + ".Melee_Hit_Delay");
        if (meleeHitDelay != 0) {
            // Convert to millis
            configuration.set(path + ".Melee_Hit_Delay", meleeHitDelay * 50);
        }

        int meleeMissDelay = configuration.getInt(path + ".Melee_Miss.Melee_Miss_Delay");
        if (meleeMissDelay != 0) {
            // Convert to millis
            configuration.set(path + ".Melee_Miss.Melee_Miss_Delay", meleeMissDelay * 50);
        }
    }
}