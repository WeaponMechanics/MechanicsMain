package me.deecaad.weaponmechanics.weapon.scope;

import co.aikar.timings.lib.MCTiming;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.compatibility.scope.IScopeCompatibility;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.info.WeaponInfoDisplay;
import me.deecaad.weaponmechanics.weapon.trigger.Trigger;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponScopeEvent;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.HandData;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import me.deecaad.weaponmechanics.wrappers.ZoomData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.io.File;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;
import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class ScopeHandler implements IValidator {

    private static final IScopeCompatibility scopeCompatibility = WeaponCompatibilityAPI.getScopeCompatibility();
    private WeaponHandler weaponHandler;

    public ScopeHandler() {}

    public ScopeHandler(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    /**
     * Tries to use scope
     *
     * @param entityWrapper the entity who used trigger
     * @param weaponTitle the weapon title
     * @param weaponStack the weapon stack
     * @param slot the slot used on trigger
     * @param triggerType the trigger type trying to activate scope
     * @return true if the scope used successfully to zoom in, our or stack
     */
    public boolean tryUse(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, TriggerType triggerType, boolean dualWield) {
        Configuration config = getConfigurations();

        // Don't try to scope if either one of the hands is reloading
        if (entityWrapper.getMainHandData().isReloading() || entityWrapper.getOffHandData().isReloading()) {
            return false;
        }

        ZoomData zoomData;
        // Only allow using zoom at one hand at time
        if (slot == EquipmentSlot.HAND) {
            if (entityWrapper.getOffHandData().getZoomData().isZooming()) {
                return false;
            }
            zoomData = entityWrapper.getMainHandData().getZoomData();
        } else {
            if (entityWrapper.getMainHandData().getZoomData().isZooming()) {
                return false;
            }
            zoomData = entityWrapper.getOffHandData().getZoomData();
        }

        Trigger trigger = config.getObject(weaponTitle + ".Scope.Trigger", Trigger.class);
        if (trigger == null) return false;

        // Check if entity is already zooming
        if (zoomData.isZooming()) {

            Trigger offTrigger = config.getObject(weaponTitle + ".Scope.Zoom_Off.Trigger", Trigger.class);
            // If off trigger is valid -> zoom out even if stacking hasn't reached maximum stacks
            if (offTrigger != null && offTrigger.check(triggerType, slot, entityWrapper)) {
                return zoomOut(weaponStack, weaponTitle, entityWrapper, zoomData, slot);
            }

            // If trigger is valid zoom in or out depending on situation
            if (trigger.check(triggerType, slot, entityWrapper)) {

                int maximumStacks = config.getInt(weaponTitle + ".Scope.Zoom_Stacking.Maximum_Stacks");
                if (maximumStacks <= 0) { // meaning that zoom stacking is not used
                    // Should turn off
                    return zoomOut(weaponStack, weaponTitle, entityWrapper, zoomData, slot);
                }
                if (zoomData.getZoomStacks() < maximumStacks) { // meaning that zoom stacks have NOT reached maximum stacks
                    // Should not turn off and stack instead
                    return zoomIn(weaponStack, weaponTitle, entityWrapper, zoomData, slot); // Zoom in handles stacking on its own
                }
                // Should turn off (because zoom stacks have reached maximum stacks)
                return zoomOut(weaponStack, weaponTitle, entityWrapper, zoomData, slot);
            }
        } else if (trigger.check(triggerType, slot, entityWrapper)) {
            // Try zooming in since entity is not zooming
            return zoomIn(weaponStack, weaponTitle, entityWrapper, zoomData, slot);
        }
        return false;
    }

    /**
     * @return true if successfully zoomed in or stacked
     */
    private boolean zoomIn(ItemStack weaponStack, String weaponTitle, EntityWrapper entityWrapper, ZoomData zoomData, EquipmentSlot slot) {
        MCTiming scopeHandlerTiming = WeaponMechanics.timing("Scope Handler").startTiming();
        boolean result = zoomInWithoutTiming(weaponStack, weaponTitle, entityWrapper, zoomData, slot);
        scopeHandlerTiming.stopTiming();

        return result;
    }

    /**
     * @return true if successfully zoomed in or stacked
     */
    private boolean zoomInWithoutTiming(ItemStack weaponStack, String weaponTitle, EntityWrapper entityWrapper, ZoomData zoomData, EquipmentSlot slot) {
        Configuration config = getConfigurations();
        LivingEntity entity = entityWrapper.getEntity();

        if (zoomData.isZooming()) { // zoom stack

            int increaseZoomPerStack = config.getInt(weaponTitle + ".Scope.Zoom_Stacking.Increase_Zoom_Per_Stack");
            if (increaseZoomPerStack != 0) {
                int zoomStack = zoomData.getZoomStacks() + 1;
                int zoomAmount = config.getInt(weaponTitle + ".Scope.Zoom_Amount");
                WeaponScopeEvent weaponScopeEvent = new WeaponScopeEvent(weaponTitle, weaponStack, entity, WeaponScopeEvent.ScopeType.STACK, zoomAmount + (zoomStack * increaseZoomPerStack), zoomStack);
                Bukkit.getPluginManager().callEvent(weaponScopeEvent);
                if (weaponScopeEvent.isCancelled()) {
                    return false;
                }

                updateZoom(entityWrapper, zoomData, weaponScopeEvent.getZoomAmount());
                zoomData.setZoomStacks(zoomStack);

                weaponHandler.getSkinHandler().tryUse(entityWrapper, weaponTitle, weaponStack, slot);

                Mechanics zoomStackingMechanics = config.getObject(weaponTitle + ".Scope.Zoom_Stacking.Mechanics", Mechanics.class);
                if (zoomStackingMechanics != null) zoomStackingMechanics.use(new CastData(entityWrapper, weaponTitle, weaponStack));

                WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
                if (weaponInfoDisplay != null) weaponInfoDisplay.send((PlayerWrapper) entityWrapper, weaponTitle, weaponStack);

                return true;
            } else {
                debug.log(LogLevel.WARN, "For some reason zoom in was called on entity when it shouldn't have.",
                        "Entity was already zooming so it should have stacked zoom, but not zoom stacking wasn't used at all?",
                        "Ignoring this call, but this shouldn't even happen...",
                        "Are you sure you have defined both Maximum_Stacks and Increase_Zoom_Per_Stack for weapon " + weaponTitle + "?");
                return false;
            }
        }

        int zoomAmount = config.getInt(weaponTitle + ".Scope.Zoom_Amount");
        if (zoomAmount == 0) return false;

        // zoom stack = 0, because its not used OR this is first zoom in
        WeaponScopeEvent weaponScopeEvent = new WeaponScopeEvent(weaponTitle, weaponStack, entity, WeaponScopeEvent.ScopeType.IN, zoomAmount, 0);
        Bukkit.getPluginManager().callEvent(weaponScopeEvent);
        if (weaponScopeEvent.isCancelled()) {
            return false;
        }

        updateZoom(entityWrapper, zoomData, weaponScopeEvent.getZoomAmount());

        Mechanics zoomMechanics = config.getObject(weaponTitle + ".Scope.Mechanics", Mechanics.class);
        if (zoomMechanics != null) zoomMechanics.use(new CastData(entityWrapper, weaponTitle, weaponStack));

        WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
        if (weaponInfoDisplay != null) weaponInfoDisplay.send((PlayerWrapper) entityWrapper, weaponTitle, weaponStack);

        weaponHandler.getSkinHandler().tryUse(entityWrapper, weaponTitle, weaponStack, slot);

        if (config.getBool(weaponTitle + ".Scope.Night_Vision")) useNightVision(entityWrapper, zoomData);


        return true;
    }

    /**
     * @return true if successfully zoomed out
     */
    private boolean zoomOut(ItemStack weaponStack, String weaponTitle, EntityWrapper entityWrapper, ZoomData zoomData, EquipmentSlot slot) {
        MCTiming scopeHandlerTiming = WeaponMechanics.timing("Scope Handler").startTiming();
        boolean result = zoomOutWithoutTiming(weaponStack, weaponTitle, entityWrapper, zoomData, slot);
        scopeHandlerTiming.stopTiming();

        return result;
    }

    /**
     * @return true if successfully zoomed out
     */
    private boolean zoomOutWithoutTiming(ItemStack weaponStack, String weaponTitle, EntityWrapper entityWrapper, ZoomData zoomData, EquipmentSlot slot) {
        if (!zoomData.isZooming()) return false;
        LivingEntity entity = entityWrapper.getEntity();

        // Zoom amount and stack 0 because zooming out
        WeaponScopeEvent weaponScopeEvent = new WeaponScopeEvent(weaponTitle, weaponStack, entity, WeaponScopeEvent.ScopeType.OUT, 0, 0);
        Bukkit.getPluginManager().callEvent(weaponScopeEvent);
        if (weaponScopeEvent.isCancelled()) {
            return false;
        }

        updateZoom(entityWrapper, zoomData, weaponScopeEvent.getZoomAmount());
        zoomData.setZoomStacks(0);

        Mechanics zoomOffMechanics = getConfigurations().getObject(weaponTitle + ".Scope.Zoom_Off.Mechanics", Mechanics.class);
        if (zoomOffMechanics != null) zoomOffMechanics.use(new CastData(entityWrapper, weaponTitle, weaponStack));

        WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
        if (weaponInfoDisplay != null) weaponInfoDisplay.send((PlayerWrapper) entityWrapper, weaponTitle, weaponStack);

        weaponHandler.getSkinHandler().tryUse(entityWrapper, weaponTitle, weaponStack, slot);

        if (zoomData.hasZoomNightVision()) useNightVision(entityWrapper, zoomData);

        HandData handData = slot == EquipmentSlot.HAND ? entityWrapper.getMainHandData() : entityWrapper.getOffHandData();
        handData.setLastScopeTime(System.currentTimeMillis());

        if (getConfigurations().getBool(weaponTitle + ".Info.Show_Cooldown.Shoot_Delay_After_Scope")) {
            CompatibilityAPI.getEntityCompatibility().setCooldown((Player) entity, weaponStack.getType(),
                    getConfigurations().getInt(weaponTitle + ".Scope.Shoot_Delay_After_Scope") / 50);
        }

        return true;
    }

    /**
     * Forces zooming out for entity
     *
     * @param entityWrapper the entity wrapper from whom to force zoom out
     * @param zoomData the zoom data of entity wrappers hand data
     */
    public void forceZoomOut(EntityWrapper entityWrapper, ZoomData zoomData) {
        ScopeHandler scopeHandler = WeaponMechanics.getWeaponHandler().getScopeHandler();
        scopeHandler.updateZoom(entityWrapper, zoomData, 0);
        zoomData.setZoomStacks(0);
        if (zoomData.hasZoomNightVision()) scopeHandler.useNightVision(entityWrapper, zoomData);
    }

    /**
     * Updates the zoom amount of entity.
     */
    private void updateZoom(EntityWrapper entityWrapper, ZoomData zoomData, int newZoomAmount) {
        if (entityWrapper.getEntity().getType() != EntityType.PLAYER) {
            // Not player so no need for FOV changes
            zoomData.setZoomAmount(newZoomAmount);
            return;
        }

        Player player = (Player) entityWrapper.getEntity();

        zoomData.setZoomAmount(newZoomAmount);

        // Update abilities sets the FOV change
        scopeCompatibility.updateAbilities(player);
    }

    /**
     * Toggles night vision on or off whether it was on before
     */
    public void useNightVision(EntityWrapper entityWrapper, ZoomData zoomData) {
        if (entityWrapper.getEntity().getType() != EntityType.PLAYER) {
            // Not player so no need for night vision
            return;
        }
        Player player = (Player) entityWrapper.getEntity();

        if (!zoomData.hasZoomNightVision()) { // night vision is not on
            zoomData.setZoomNightVision(true);
            scopeCompatibility.addNightVision(player);
            return;
        }
        zoomData.setZoomNightVision(false);
        scopeCompatibility.removeNightVision(player);
    }

    @Override
    public String getKeyword() {
        return "Scope";
    }

    @Override
    public void validate(Configuration configuration, File file, ConfigurationSection configurationSection, String path) {
        Trigger trigger = configuration.getObject(path + ".Trigger", Trigger.class);
        if (trigger == null) {
            debug.log(LogLevel.ERROR, "Tried to use scope without defining trigger for it.",
                    "Located at file " + file + " in " + path + ".Trigger in configurations.");
        }
        int zoomAmount = configuration.getInt(path + ".Zoom_Amount");
        if (zoomAmount < 1 || zoomAmount > 32) {
            debug.log(LogLevel.ERROR, "Tried to use scope without defining proper zoom amount for it, or it was missing.",
                    "Zoom amount has to be between 1 and 32.",
                    "Located at file " + file + " in " + path + ".Zoom_Amount in configurations.");
        }

        int maximumStacks = configuration.getInt(path + ".Zoom_Stacking.Maximum_Stacks");
        int increaseZoomPerStack = configuration.getInt(path + ".Zoom_Stacking.Increase_Zoom_Per_Stack");
        int finalValue = maximumStacks * increaseZoomPerStack + zoomAmount;
        if (finalValue > 32 || finalValue < 1) {
            debug.log(LogLevel.ERROR, "Final value of zoom stacking cannot go above 32 or below 1.",
                    "Currently potential final value is " + finalValue + ", make sure its between 1 and 32.",
                    "Located at file " + file + " in " + path + ".Zoom_Stacking in configurations.");
        }

        int shootDelayAfterScope = configuration.getInt(path + ".Shoot_Delay_After_Scope");
        if (shootDelayAfterScope != 0) {
            // Convert to millis
            configuration.set(path + ".Shoot_Delay_After_Scope", shootDelayAfterScope * 50);
        }
    }
}