package me.deecaad.weaponmechanics.weapon.melee;

import co.aikar.timings.lib.MCTiming;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.compatibility.IWeaponCompatibility;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.projectile.HitBox;
import me.deecaad.weaponmechanics.weapon.projectile.RayTrace;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.RayTraceResult;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponMeleeMissEvent;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.HandData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collections;
import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanics.*;

public class MeleeHandler implements IValidator {

    private static final IWeaponCompatibility weaponCompatibility = WeaponCompatibilityAPI.getWeaponCompatibility();


    private WeaponHandler weaponHandler;

    /**
     * Default constructor for validator
     */
    public MeleeHandler() {
    }

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

        if (hit != null) {
            boolean result = weaponHandler.getShootHandler().shootWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, triggerType, dualWield);
            if (result) {
                hit.handleMeleeHit(shooter, direction, weaponTitle, weaponStack);
            }
            return result;
        }

        // Handle permissions
        boolean hasPermission = weaponHandler.getInfoHandler().hasPermission(shooter, weaponTitle);
        String permissionMessage = getBasicConfigurations().getString("Messages.Permissions.Use_Weapon", ChatColor.RED + "You do not have permission to use " + weaponTitle);

        if (!hasPermission) {
            if (shooter.getType() == EntityType.PLAYER) {
                shooter.sendMessage(PlaceholderAPI.applyPlaceholders(permissionMessage, (Player) shooter, weaponStack, weaponTitle, slot));
            }
            return false;
        }


        boolean consumeOnMiss = getConfigurations().getBool(weaponTitle + ".Melee.Melee_Miss.Consume_On_Miss");
        Mechanics missMechanics = getConfigurations().getObject(weaponTitle + ".Melee.Melee_Miss.Mechanics", Mechanics.class);

        WeaponMeleeMissEvent event = new WeaponMeleeMissEvent(weaponTitle, weaponStack, shooter, meleeMissDelay / 50, missMechanics, consumeOnMiss);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return false;

        // Handle miss
        if (event.isConsume()) {
            weaponHandler.getShootHandler().shootWithoutTrigger(entityWrapper, weaponTitle, weaponStack, slot, triggerType, dualWield);
        }

        if (event.getMechanics() != null) {
            event.getMechanics().use(new CastData(entityWrapper, weaponTitle, weaponStack));
        }

        if (event.getMeleeMissDelay() != 0) {
            handData.setLastMeleeMissTime(System.currentTimeMillis());
        }
        return true;
    }

    private RayTraceResult getHit(LivingEntity shooter, Location eyeLocation, Vector direction, double range, @Nullable LivingEntity knownVictim) {

        Vector eyeLocationToVector = eyeLocation.toVector();

        if (knownVictim == null) {

            if (range <= 0) {
                return null;
            }

            RayTrace rayTrace = new RayTrace().withEntityFilter(entity -> entity.getEntityId() == shooter.getEntityId());
            List<RayTraceResult> hits = rayTrace.cast(eyeLocation.getWorld(), eyeLocationToVector, direction, range);

            if (hits == null) return null;

            RayTraceResult firstHit = hits.get(0);

            // If first hit isn't entity
            if (firstHit.getLivingEntity() == null) return null;

            // If first entity hit isn't in range
            if (firstHit.getDistanceTravelled() > range) return null;

            return firstHit;
        }

        // Simply check where known victim was hit and whether it was in range
        HitBox entityBox = weaponCompatibility.getHitBox(knownVictim);
        if (entityBox == null) return null;

        RayTraceResult rayTraceResult = entityBox.rayTrace(eyeLocationToVector, direction);
        if (rayTraceResult == null || (range > 0 && rayTraceResult.getDistanceTravelled() > range)) return null; // Didn't hit in range

        return rayTraceResult;
    }

    @Override
    public String getKeyword() {
        return "Melee";
    }

    public List<String> getAllowedPaths() {
        return Collections.singletonList(".Melee");
    }

    @Override
    public void validate(Configuration configuration, SerializeData data) throws SerializerException {
        boolean enableMelee = data.of("Enable_Melee").getBool(false);
        String meleeAttachment = data.of("Melee_Attachment").get(null);
        if (!enableMelee && meleeAttachment == null) {
            throw data.exception(null, "You must use either 'Enable_Melee: true' or 'Melee_Attachment: <weapon>'",
                    "You cannot use the 'Melee' key without 1 of those 2 options");
        }

        int meleeHitDelay = data.of("Melee_Hit_Delay").assertPositive().getInt(0);
        if (meleeHitDelay != 0) {
            // Convert to millis
            configuration.set(data.key + ".Melee_Hit_Delay", meleeHitDelay * 50);
        }

        int meleeMissDelay = data.of("Melee_Miss.Melee_Miss_Delay").assertPositive().getInt(0);
        if (meleeMissDelay != 0) {
            // Convert to millis
            configuration.set(data.key + ".Melee_Miss.Melee_Miss_Delay", meleeMissDelay * 50);
        }
    }
}