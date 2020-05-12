package me.deecaad.weaponmechanics.weapon.scope;

import me.deecaad.weaponcompatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponcompatibility.scope.IScopeCompatibility;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.utils.UsageHelper;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.trigger.Trigger;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponScopeEvent;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import me.deecaad.weaponmechanics.wrappers.ZoomData;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class ScopeHandler {

    private static final IScopeCompatibility scopeCompatibility = WeaponCompatibilityAPI.getScopeCompatibility();
    private WeaponHandler weaponHandler;

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
    public boolean tryUse(IEntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, TriggerType triggerType, boolean dualWield) {
        Configuration config = getConfigurations();

        Trigger trigger = config.getObject(weaponTitle + ".Scope.Trigger", Trigger.class);
        if (trigger == null) return false;

        ZoomData zoomData = entityWrapper.getZoomData();

        // Check if entity is already zooming
        if (zoomData.isZooming()) {

            Trigger offTrigger = config.getObject(weaponTitle + ".Scope.Zoom_Off.Trigger", Trigger.class);
            // If off trigger is valid -> zoom out even if stacking has't reached maximum stacks
            if (offTrigger != null && offTrigger.check(triggerType, slot, entityWrapper)) {
                return zoomOut(weaponStack, weaponTitle, entityWrapper);
            }

            // If trigger is valid zoom in or out depending on situation
            if (trigger.check(triggerType, slot, entityWrapper)) {

                int maximumStacks = config.getInt(weaponTitle + ".Scope.Zoom_Stacking.Maximum_Stacks");
                if (maximumStacks <= 0) { // meaning that zoom stacking is not used
                    // Should turn off
                    return zoomOut(weaponStack, weaponTitle, entityWrapper);
                }
                if (zoomData.getZoomStacks() < maximumStacks) { // meaning that zoom stacks have NOT reached maximum stacks
                    // Should not turn off and stack instead
                    return zoomIn(weaponStack, weaponTitle, entityWrapper); // Zoom in handles stacking on its own
                }
                // Should turn off (because zoom stacks have reached maximum stacks)
                return zoomOut(weaponStack, weaponTitle, entityWrapper);
            }
        } else if (trigger.check(triggerType, slot, entityWrapper)) {
            // Try zooming in since entity is not zooming
            return zoomIn(weaponStack, weaponTitle, entityWrapper);
        }
        return false;
    }

    /**
     * @return true if successfully zoomed in or stacked
     */
    private boolean zoomIn(ItemStack weaponStack, String weaponTitle, IEntityWrapper entityWrapper) {
        Configuration config = getConfigurations();
        ZoomData zoomData = entityWrapper.getZoomData();
        LivingEntity entity = entityWrapper.getEntity();

        if (zoomData.isZooming()) { // zoom stack

            int increaseZoomPerStack = config.getInt(weaponTitle + ".Scope.Zoom_Stacking.Increase_Zoom_Per_Stack");
            if (increaseZoomPerStack != 0) {
                int zoomStack = zoomData.getZoomStacks() + 1;
                WeaponScopeEvent weaponScopeEvent = new WeaponScopeEvent(weaponTitle, entity, WeaponScopeEvent.ScopeType.STACK, zoomStack * increaseZoomPerStack, zoomStack);
                Bukkit.getPluginManager().callEvent(weaponScopeEvent);
                if (weaponScopeEvent.isCancelled()) {
                    return false;
                }

                updateZoom(entityWrapper, zoomData, weaponScopeEvent.getZoomAmount());
                zoomData.setZoomStacks(zoomStack);
                UsageHelper.useGeneral(weaponTitle + ".Scope.Zoom_Stacking", entity, weaponStack, weaponTitle);

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
        WeaponScopeEvent weaponScopeEvent = new WeaponScopeEvent(weaponTitle, entity, WeaponScopeEvent.ScopeType.IN, zoomAmount, 0);
        Bukkit.getPluginManager().callEvent(weaponScopeEvent);
        if (weaponScopeEvent.isCancelled()) {
            return false;
        }

        updateZoom(entityWrapper, zoomData, weaponScopeEvent.getZoomAmount());
        UsageHelper.useGeneral(weaponTitle + ".Scope", entity, weaponStack, weaponTitle);
        if (config.getBool(weaponTitle + ".Scope.Night_Vision")) useNightVision(entityWrapper, zoomData);

        return true;
    }

    /**
     * @return true if successfully zoomed out
     */
    public boolean zoomOut(ItemStack weaponStack, String weaponTitle, IEntityWrapper entityWrapper) {
        ZoomData zoomData = entityWrapper.getZoomData();
        if (!zoomData.isZooming()) return false;
        LivingEntity entity = entityWrapper.getEntity();

        // Zoom amount and stack 0 because zooming out
        WeaponScopeEvent weaponScopeEvent = new WeaponScopeEvent(weaponTitle, entity, WeaponScopeEvent.ScopeType.OUT, 0, 0);
        Bukkit.getPluginManager().callEvent(weaponScopeEvent);
        if (weaponScopeEvent.isCancelled()) {
            return false;
        }

        updateZoom(entityWrapper, zoomData, weaponScopeEvent.getZoomAmount());
        zoomData.setZoomStacks(0);
        UsageHelper.useGeneral(weaponTitle + ".Scope.Zoom_Off", entity, weaponStack, weaponTitle);
        if (zoomData.hasZoomNightVision()) useNightVision(entityWrapper, zoomData);

        return true;
    }

    /**
     * Updates the zoom amount of entity
     */
    private void updateZoom(IEntityWrapper entityWrapper, ZoomData zoomData, int newZoomAmount) {
        if (entityWrapper.getEntity().getType() != EntityType.PLAYER) {
            // Not player so no need for FOV changes
            zoomData.setZoomAmount(newZoomAmount);
            return;
        }

        Player player = (Player) entityWrapper.getEntity();

        // First 12 levels:
        // -> PacketPlayOutUpdateAttributes (generic.movementSpeed)
        // Rest 20 levels:
        // -> PacketPlayOutAbilities (walk speed)

        int lastZoomAmount = zoomData.getZoomAmount();
        zoomData.setZoomAmount(newZoomAmount);
        if (lastZoomAmount < 13 && newZoomAmount > 12 || lastZoomAmount > 12 && newZoomAmount < 13) {
            // If last zoom was with attributes AND new one should be with abilities
            // -> Update both
            // OR
            // If last zoom was with abilities AND new one should be with attributes
            // This might happen in rare cases.
            // E.g. negative zoom increases when stacking
            // -> Update both

            // Update attributes
            scopeCompatibility.updateAttributesFor(player);
            // Update abilities
            scopeCompatibility.updateAbilities(player);
        } else if (newZoomAmount < 13) {
            // Update attributes
            scopeCompatibility.updateAttributesFor(player);
        } else {
            // Update abilities
            scopeCompatibility.updateAbilities(player);
        }
    }

    /**
     * Toggles night vision on or off whether it was on before
     */
    private void useNightVision(IEntityWrapper entityWrapper, ZoomData zoomData) {
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
}