package me.deecaad.weaponmechanics.wrappers;

import com.cjcrafter.foliascheduler.TaskImplementation;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.MechanicManager;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.scope.ScopeHandler;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponScopeEvent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.vivecraft.api.VRAPI;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;

public class ZoomData {

    /**
     * The NamespacedKey used for the temporary attack speed attribute modifier applied during ADS settling.
     * Stored here so both ZoomData and the compatibility layer share the same key.
     */
    public static final NamespacedKey ADS_SPEED_MODIFIER_KEY = new NamespacedKey("weaponmechanics", "ads_speed");

    private final HandData handData;
    private double zoomAmount;
    private int zoomStacks;
    private boolean zoomNightVision;
    private ItemStack scopeWeaponStack;
    private String scopeWeaponTitle;

    /** System.currentTimeMillis() timestamp when ADS settling ends. 0 = not settling. */
    private long scopeSettleEndTime = 0;
    /** Scheduled task that removes the ADS speed attribute modifier when settling completes. */
    private TaskImplementation<Void> adsSettleTask;

    public ZoomData(HandData handData) {
        this.handData = handData;
    }

    public HandData getHandData() {
        return handData;
    }

    /**
     * @return <code>true</code> if the entity is scoped.
     */
    public boolean isZooming() {
        EntityWrapper entityWrapper = handData.getEntityWrapper();
        if (Bukkit.getPluginManager().getPlugin("Vivecraft_Spigot_Extensions") != null) {
            if (entityWrapper.getEntity() instanceof Player player) {
                VRPose pose = VRAPI.instance().getVRPose(player);
                if (pose != null) {
                    // Get the position and direction from player metadata
                    VRBodyPartData controller = handData.isMainhand() ? pose.getMainHand() : pose.getOffHand();
                    VRBodyPartData head = pose.getHead();
                    return controller.getDir().dot(head.getDir()) > 0.94;
                }
            }
        }
        return zoomAmount != 0;
    }

    /**
     * @return The magnitude of the scope amount, or 0 for not scoping.
     */
    public double getZoomAmount() {
        return zoomAmount;
    }

    /**
     * Note: This method does not set the player's FOV. This method should not be used unless you know
     * what you are doing.
     *
     * @param zoomAmount How far the player is zoomed in.
     */
    public void setZoomAmount(double zoomAmount) {
        this.zoomAmount = zoomAmount;
    }

    /**
     * @return How many times stacky-scope was used, or 0.
     */
    public int getZoomStacks() {
        return zoomStacks;
    }

    /**
     * Note: This method does not set the player's FOV. This method should not be used unless you know
     * what you are doing.
     *
     * @param zoomStacks the new zoom stack amount.
     */
    public void setZoomStacks(int zoomStacks) {
        this.zoomStacks = Math.max(0, zoomStacks);
    }

    /**
     * @return <code>true</code> if entity is scoped into a night vision scope.
     */
    public boolean hasZoomNightVision() {
        return zoomNightVision;
    }

    /**
     * Note: This method does not set the player's potion effects. This method should not be used unless
     * you know what you are doing.
     *
     * @param zoomNightVision whether zoom night vision is on
     */
    public void setZoomNightVision(boolean zoomNightVision) {
        this.zoomNightVision = zoomNightVision;
    }

    /**
     * @return {@code true} if the ADS settling timer is currently active (scope was just entered and
     *     accuracy has not yet transitioned to full ADS accuracy).
     */
    public boolean isSettling() {
        return scopeSettleEndTime != 0 && System.currentTimeMillis() < scopeSettleEndTime;
    }

    /**
     * Starts the ADS settling timer. Call this when a player enters scope.
     *
     * @param durationMillis how long (in milliseconds) until full ADS accuracy is reached
     */
    public void startSettling(long durationMillis) {
        this.scopeSettleEndTime = System.currentTimeMillis() + durationMillis;
    }

    /**
     * Stores the task scheduled to remove the ADS speed modifier at the end of settling.
     * Call {@link #stopSettling()} to cancel it early.
     */
    public void setAdsSettleTask(TaskImplementation<Void> task) {
        this.adsSettleTask = task;
    }

    /**
     * Stops ADS settling immediately: cancels the pending restore task and removes the temporary
     * attack speed modifier from the player. Safe to call when already not settling.
     */
    public void stopSettling() {
        this.scopeSettleEndTime = 0;
        if (adsSettleTask != null) {
            adsSettleTask.cancel();
            adsSettleTask = null;
        }
        // Remove the temporary attack speed modifier if it is still on the player
        EntityWrapper entityWrapper = handData.getEntityWrapper();
        if (entityWrapper.getEntity() instanceof Player player) {
            AttributeInstance attr = player.getAttribute(Attribute.ATTACK_SPEED);
            if (attr != null) {
                for (AttributeModifier mod : new java.util.ArrayList<>(attr.getModifiers())) {
                    if (ADS_SPEED_MODIFIER_KEY.equals(mod.getKey())) {
                        attr.removeModifier(mod);
                        break;
                    }
                }
            }
        }
    }

    public void ifZoomingForceZoomOut() {
        // Stop any active ADS settling (cancels task + removes attribute modifier)
        stopSettling();

        if (isZooming()) {

            // IF player is in VR this happens
            if (getZoomAmount() == 0)
                return;

            EntityWrapper entityWrapper = handData.getEntityWrapper();

            ScopeHandler scopeHandler = WeaponMechanics.getInstance().getWeaponHandler().getScopeHandler();
            scopeHandler.updateZoom(entityWrapper, this, 0);
            setZoomStacks(0);
            scopeHandler.useNightVision(entityWrapper, this, false);

            MechanicManager zoomOffMechanics = WeaponMechanics.getInstance().getWeaponConfigurations().getObject(this.scopeWeaponTitle + ".Scope.Zoom_Off.Mechanics", MechanicManager.class);

            WeaponScopeEvent weaponScopeEvent = new WeaponScopeEvent(this.scopeWeaponTitle, this.scopeWeaponStack,
                entityWrapper.getEntity(), getHandData().isMainhand() ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND,
                WeaponScopeEvent.ScopeType.OUT, 0, 0, zoomOffMechanics);
            Bukkit.getPluginManager().callEvent(weaponScopeEvent);

            // Get Mechanics from event, so we can let plugins modify them.
            if (weaponScopeEvent.getMechanics() != null)
                weaponScopeEvent.getMechanics().use(new CastData(entityWrapper.getEntity(), this.scopeWeaponTitle, this.scopeWeaponStack));
        }

        // This just ensures that these are set to null
        setScopeData(null, null);
    }

    public void setScopeData(String weaponTitle, ItemStack weaponStack) {
        this.scopeWeaponTitle = weaponTitle;
        this.scopeWeaponStack = weaponStack;
    }
}