package me.deecaad.weaponmechanics.weapon.scope;

import co.aikar.timings.lib.MCTiming;
import me.deecaad.core.file.*;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.compatibility.scope.IScopeCompatibility;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.trigger.Trigger;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponScopeEvent;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.HandData;
import me.deecaad.weaponmechanics.wrappers.ZoomData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.vivecraft.VSE;

import java.util.Collections;
import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanics.*;

public class ScopeHandler implements IValidator {

    private static final IScopeCompatibility scopeCompatibility = WeaponCompatibilityAPI.getScopeCompatibility();
    private WeaponHandler weaponHandler;

    /**
     * Defualt constructor for validator
     */
    public ScopeHandler() {
    }

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

        // Don't try to scope if either one of the hands is reloading or running firearm actions
        HandData main = entityWrapper.getMainHandData();
        HandData off = entityWrapper.getOffHandData();
        if (main.isReloading() || main.hasRunningFirearmAction()
                || off.isReloading() || off.hasRunningFirearmAction()) {
            return false;
        }

        if (Bukkit.getPluginManager().getPlugin("Vivecraft-Spigot-Extensions") != null
                && entityWrapper.isPlayer() && VSE.isVive((Player) entityWrapper.getEntity())) {
            // Don't try to use scope this way when player is in VR
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

        LivingEntity shooter = entityWrapper.getEntity();

        // Handle permissions
        boolean hasPermission = weaponHandler.getInfoHandler().hasPermission(shooter, weaponTitle);
        String permissionMessage = getBasicConfigurations().getString("Messages.Permissions.Use_Weapon", ChatColor.RED + "You do not have permission to use " + weaponTitle);

        // Check if entity is already zooming
        if (zoomData.isZooming()) {

            Trigger offTrigger = config.getObject(weaponTitle + ".Scope.Zoom_Off.Trigger", Trigger.class);
            // If off trigger is valid -> zoom out even if stacking hasn't reached maximum stacks
            if (offTrigger != null && offTrigger.check(triggerType, slot, entityWrapper)) {
                return zoomOut(weaponStack, weaponTitle, entityWrapper, zoomData, slot);
            }

            // If trigger is valid zoom in or out depending on situation
            if (trigger.check(triggerType, slot, entityWrapper)) {

                // Handle permissions
                if (!hasPermission) {
                    if (shooter.getType() == EntityType.PLAYER) {
                        shooter.sendMessage(PlaceholderAPI.applyPlaceholders(permissionMessage, (Player) shooter, weaponStack, weaponTitle, slot));
                    }
                    return false;
                }

                List<String> zoomStacks = config.getList(weaponTitle + ".Scope.Zoom_Stacking.Stacks", null);
                if (zoomStacks == null) { // meaning that zoom stacking is not used
                    // Should turn off
                    return zoomOut(weaponStack, weaponTitle, entityWrapper, zoomData, slot);
                }

                // E.g. when there is 2 defined values in stacks:
                // 0 < 2 // TRUE
                // 1 < 2 // TRUE
                // 2 < 2 // FALSE

                if (zoomData.getZoomStacks() < zoomStacks.size()) { // meaning that zoom stacks have NOT reached maximum stacks
                    // Should not turn off and stack instead
                    return zoomIn(weaponStack, weaponTitle, entityWrapper, zoomData, slot); // Zoom in handles stacking on its own
                }
                // Should turn off (because zoom stacks have reached maximum stacks)
                return zoomOut(weaponStack, weaponTitle, entityWrapper, zoomData, slot);
            }
        } else if (trigger.check(triggerType, slot, entityWrapper)) {

            // Handle permissions
            if (!hasPermission) {
                if (shooter.getType() == EntityType.PLAYER) {
                    shooter.sendMessage(PlaceholderAPI.applyPlaceholders(permissionMessage, (Player) shooter, weaponStack, weaponTitle, slot));
                }
                return false;
            }

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

            List<String> zoomStacks = config.getList(weaponTitle + ".Scope.Zoom_Stacking.Stacks", null);
            if (zoomStacks != null) {
                int currentStacks = zoomData.getZoomStacks();
                double zoomAmount = Double.parseDouble(zoomStacks.get(currentStacks));
                int zoomStack = currentStacks + 1;
                WeaponScopeEvent weaponScopeEvent = new WeaponScopeEvent(weaponTitle, weaponStack, entity, WeaponScopeEvent.ScopeType.STACK, zoomAmount, zoomStack);
                Bukkit.getPluginManager().callEvent(weaponScopeEvent);
                if (weaponScopeEvent.isCancelled()) {
                    return false;
                }

                updateZoom(entityWrapper, zoomData, weaponScopeEvent.getZoomAmount());
                zoomData.setZoomStacks(zoomStack);

                weaponHandler.getSkinHandler().tryUse(entityWrapper, weaponTitle, weaponStack, slot);

                Mechanics zoomStackingMechanics = config.getObject(weaponTitle + ".Scope.Zoom_Stacking.Mechanics", Mechanics.class);
                if (zoomStackingMechanics != null) zoomStackingMechanics.use(new CastData(entityWrapper, weaponTitle, weaponStack));

                return true;
            } else {
                debug.log(LogLevel.WARN, "For some reason zoom in was called on entity when it shouldn't have.",
                        "Entity was already zooming so it should have stacked zoom, but now zoom stacking wasn't used at all?",
                        "Ignoring this call, but this shouldn't even happen...",
                        "Are you sure you have defined both Zoom_Stacking.Stacks for weapon " + weaponTitle + "?");
                return false;
            }
        }

        double zoomAmount = config.getDouble(weaponTitle + ".Scope.Zoom_Amount");
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

        weaponHandler.getSkinHandler().tryUse(entityWrapper, weaponTitle, weaponStack, slot);

        if (zoomData.hasZoomNightVision()) useNightVision(entityWrapper, zoomData);

        HandData handData = slot == EquipmentSlot.HAND ? entityWrapper.getMainHandData() : entityWrapper.getOffHandData();
        handData.setLastScopeTime(System.currentTimeMillis());

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
    private void updateZoom(EntityWrapper entityWrapper, ZoomData zoomData, double newZoomAmount) {
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

    public List<String> getAllowedPaths() {
        return Collections.singletonList(".Scope");
    }

    @Override
    public void validate(Configuration configuration, SerializeData data) throws SerializerException {
        Trigger trigger = configuration.getObject(data.key + ".Trigger", Trigger.class);
        if (trigger == null)
            throw new SerializerMissingKeyException(data.serializer, data.key + ".Trigger", data.of("Trigger").getLocation());

        double zoomAmount = configuration.getDouble(data.key + ".Zoom_Amount");
        if (zoomAmount < 1 || zoomAmount > 10)
            throw new SerializerRangeException(data.serializer, 1.0, zoomAmount, 10.0, data.of("Zoom_Amount").getLocation());

        List<String> zoomStacks = configuration.getList(data.key + ".Zoom_Stacking.Stacks", null);
        if (zoomStacks != null) {
            for (int i = 0; i < zoomStacks.size(); i++) {
                String zoomStack = zoomStacks.get(i);
                try {
                    double v = Double.parseDouble(zoomStack);
                    if (v < 1 || v > 10)
                        throw new SerializerRangeException(data.serializer, 1.0, v, 10.0, data.ofList("Zoom_Stacking.Stacks").getLocation(i));
                } catch (NumberFormatException e) {
                    throw new SerializerTypeException(data.serializer, Number.class, String.class, zoomStack, data.ofList("Zoom_Stacking.Stacks").getLocation(i));
                }
            }
        }

        int shootDelayAfterScope = configuration.getInt(data.key + ".Shoot_Delay_After_Scope");
        if (shootDelayAfterScope != 0) {
            // Convert to millis
            configuration.set(data.key + ".Shoot_Delay_After_Scope", shootDelayAfterScope * 50);
        }
    }
}