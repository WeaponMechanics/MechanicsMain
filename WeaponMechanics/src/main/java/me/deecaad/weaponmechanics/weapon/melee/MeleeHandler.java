package me.deecaad.weaponmechanics.weapon.melee;

import co.aikar.timings.lib.MCTiming;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.worldguard.WorldGuardCompatibility;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.compatibility.IWeaponCompatibility;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.info.WeaponInfoDisplay;
import me.deecaad.weaponmechanics.weapon.projectile.HitBox;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.RayTraceResult;
import me.deecaad.weaponmechanics.weapon.reload.ReloadHandler;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.HandData;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
     * @param weaponTitle the weapon title
     * @param weaponStack the weapon stack
     * @param slot the slot used on trigger
     * @param triggerType the trigger type trying to activate scope
     * @param victim the victim hit, or null
     * @return true if the melee was used
     */
    public boolean tryUse(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot,
                          TriggerType triggerType, boolean dualWield, @Nullable LivingEntity victim) {
        
        if (!getConfigurations().getBool(weaponTitle + ".Melee.Enable_Melee")) {

            // Change weapon title to match the attachment
            weaponTitle = getConfigurations().getString(weaponTitle + ".Melee.Melee_Attachment");

            // Melee isn't used for this weapon nor the melee attachment is defined
            if (weaponTitle == null) return false;
        }


        MCTiming meleeHandlerTiming = WeaponMechanics.timing("Melee Handler").startTiming();
        boolean result = meleeWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, triggerType, dualWield, victim);
        meleeHandlerTiming.stopTiming();

        return result || weaponStack.getAmount() == 0;
    }

    /**
     * @return true if was able to melee
     */
    private boolean meleeWithoutTrigger(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot,
                                        TriggerType triggerType, boolean dualWield, @Nullable LivingEntity victim) {

        // Handle worldguard flags
        WorldGuardCompatibility worldGuard = CompatibilityAPI.getWorldGuardCompatibility();
        Location loc = entityWrapper.getEntity().getLocation();
        if (!worldGuard.testFlag(loc, entityWrapper instanceof PlayerWrapper ? ((PlayerWrapper) entityWrapper).getPlayer() : null, "weapon-shoot")) {
            Object obj = worldGuard.getValue(loc, "weapon-shoot-message");
            if (obj != null && !obj.toString().isEmpty()) {
                entityWrapper.getEntity().sendMessage(StringUtil.color(obj.toString()));
            }

            return false;
        }

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
        RayTraceResult hit = null;

        if (victim == null) {
            hit = getHit(shooter, eyeLocation, direction, meleeRange, null);
            if (hit != null) victim = hit.getLivingEntity();
        } else if (meleeRange != 0) {
            hit = getHit(shooter, eyeLocation, direction, meleeRange, victim);
        }

        if (victim == null || hit == null) {
            handleMiss(entityWrapper, weaponTitle, weaponStack, dualWield, handData, meleeMissDelay);
            return false;
        }

        ReloadHandler reloadHandler = weaponHandler.getReloadHandler();

        boolean consumeOnHit = getConfigurations().getBool(weaponTitle + ".Melee.Consume_On_Hit");
        boolean consumeItemOnShoot = getConfigurations().getBool(weaponTitle + ".Shoot.Consume_Item_On_Shoot");
        if (consumeOnHit) {
            if (!consumeItemOnShoot) {
                reloadHandler.handleWeaponStackAmount(entityWrapper, weaponStack);
            }

            if (reloadHandler.getAmmoLeft(weaponStack) == 0) {
                reloadHandler.startReloadWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, dualWield);
                return false;
            }
        }


        // HitBox.handleMeleeHit() = true = cancelled
        if (hit.handleMeleeHit(shooter, direction, weaponTitle, weaponStack)) return false;

        if (meleeHitDelay != 0) handData.setLastMeleeTime(System.currentTimeMillis());

        if (consumeOnHit) {

            // Ammo has already been checked above
            reloadHandler.consumeAmmo(weaponStack, 1);

            if (consumeItemOnShoot && weaponHandler.getShootHandler().handleConsumeItemOnShoot(weaponStack)) {
                return true;
            }

            if (reloadHandler.getAmmoLeft(weaponStack) == 0) {
                reloadHandler.startReloadWithoutTrigger(entityWrapper, weaponTitle, weaponStack, EquipmentSlot.HAND, dualWield);
                return true;
            }
        }

        if (entityWrapper instanceof PlayerWrapper) {
            WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
            if (weaponInfoDisplay != null) weaponInfoDisplay.send((PlayerWrapper) entityWrapper, weaponTitle, weaponStack);
        }

        return true;
    }
    
    private void handleMiss(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, boolean dualWield, HandData handData, int meleeMissDelay) {
        if (meleeMissDelay != 0) handData.setLastMeleeMissTime(System.currentTimeMillis());

        Mechanics meleeMissMechanics = getConfigurations().getObject(weaponTitle + ".Melee.Melee_Miss.Mechanics", Mechanics.class);
        if (meleeMissMechanics != null) meleeMissMechanics.use(new CastData(entityWrapper, weaponTitle, weaponStack));

        if (getConfigurations().getBool(weaponTitle + ".Melee.Melee_Miss.Consume_On_Miss")) {

            // Here we don't really have to care whether there is ammo left since this was miss anyway
            ReloadHandler reloadHandler = weaponHandler.getReloadHandler();
            reloadHandler.consumeAmmo(weaponStack, 1);

            if (getConfigurations().getBool(weaponTitle + ".Shoot.Consume_Item_On_Shoot") && weaponHandler.getShootHandler().handleConsumeItemOnShoot(weaponStack)) {
                // Item was fully consumed
                return;
            }

            if (entityWrapper instanceof PlayerWrapper) {
                WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
                if (weaponInfoDisplay != null) weaponInfoDisplay.send((PlayerWrapper) entityWrapper, weaponTitle, weaponStack);
            }

            if (reloadHandler.getAmmoLeft(weaponStack) == 0) {
                reloadHandler.startReloadWithoutTrigger(entityWrapper, weaponTitle, weaponStack, EquipmentSlot.HAND, dualWield);
            }
        }
    }

    private RayTraceResult getHit(LivingEntity shooter, Location eyeLocation, Vector direction, double range, @Nullable LivingEntity knownVictim) {

        Vector eyeLocationToVector = eyeLocation.toVector();

        if (knownVictim == null) {

            RayTraceResult hit = null;
            BlockIterator blocks = new BlockIterator(eyeLocation.getWorld(), eyeLocationToVector, direction, 0.0, (int) range);
            while (blocks.hasNext()) {
                Block block = blocks.next();

                HitBox blockBox = weaponCompatibility.getHitBox(block);
                if (blockBox == null) continue;

                RayTraceResult rayTraceResult = blockBox.rayTrace(eyeLocationToVector, direction);
                if (rayTraceResult == null) continue; // Didn't hit

                hit = rayTraceResult;
                break;
            }

            List<LivingEntity> entities = getPossibleEntities(shooter, eyeLocation, (hit != null ? hit.getHitLocation() : eyeLocation.toVector().add(direction.clone().multiply(range))));
            hit = null;
            double distanceTravelledCheck = -1;
            if (entities != null && !entities.isEmpty()) {
                for (LivingEntity entity : entities) {
                    HitBox entityBox = weaponCompatibility.getHitBox(entity);
                    if (entityBox == null) continue;

                    RayTraceResult rayTraceResult = entityBox.rayTrace(eyeLocationToVector, direction);
                    double rayTraceResultDistance = rayTraceResult.getDistanceTravelled();
                    if (rayTraceResult == null || rayTraceResultDistance > range) continue; // Didn't hit in range

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
        if (rayTraceResult == null || rayTraceResult.getDistanceTravelled() > range) return null; // Didn't hit in range

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
                    if (!entity.getType().isAlive() || entity.isInvulnerable() && entity.getEntityId() == shooter.getEntityId()) continue;

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

        int meleeMissDelay = configuration.getInt(path + ".Melee_Miss_Delay");
        if (meleeMissDelay != 0) {
            // Convert to millis
            configuration.set(path + ".Melee_Hit_Delay", meleeMissDelay * 50);
        }
    }
}