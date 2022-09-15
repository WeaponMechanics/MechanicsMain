package me.deecaad.weaponmechanics.wrappers;

import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.weapon.scope.ScopeHandler;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponScopeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.vivecraft.VSE;

import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class ZoomData {

    private HandData handData;
    private double zoomAmount;
    private int zoomStacks;
    private boolean zoomNightVision;
    private ItemStack scopeWeaponStack;
    private String scopeWeaponTitle;

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
        if (Bukkit.getPluginManager().getPlugin("Vivecraft-Spigot-Extensions") != null
                && entityWrapper.isPlayer() && VSE.isVive((Player) entityWrapper.getEntity())) {

            Player player = (Player) entityWrapper.getEntity();
            String getHandDataFrom = handData.isMainhand() ? "righthand.pos" : "lefthand.pos";
            String getHeadDataFrom = "head.pos";
            if (player.hasMetadata(getHandDataFrom) && player.hasMetadata(getHeadDataFrom)) {
                return getVSEMeta(player, getHandDataFrom).getDirection()
                        .dot(getVSEMeta(player, getHeadDataFrom).getDirection()) > 0.94;
            }

        }
        return zoomAmount != 0;
    }

    private Location getVSEMeta(Player player, String metakey) {
        List<MetadataValue> metadataValueList = player.getMetadata(metakey);
        for (MetadataValue meta : metadataValueList) {
            if (meta.getOwningPlugin() == VSE.me) {
                return (Location) meta.value();
            }
        }
        throw new IllegalArgumentException("VR metadata " + metakey + " from Vivecraft not found when player was VR player...?");
    }

    /**
     * @return The magnitude of the scope amount, or 0 for not scoping.
     */
    public double getZoomAmount() {
        return zoomAmount;
    }

    /**
     * Note: This method does not set the player's FOV. This method should not
     * be used unless you know what you are doing.
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
     * Note: This method does not set the player's FOV. This method should not
     * be used unless you know what you are doing.
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
     * Note: This method does not set the player's potion effects. This method
     * should not be used unless you know what you are doing.
     *
     * @param zoomNightVision whether zoom night vision is on
     */
    public void setZoomNightVision(boolean zoomNightVision) {
        this.zoomNightVision = zoomNightVision;
    }

    public void ifZoomingForceZoomOut() {
        if (isZooming()) {

            // IF player is in VR this happens
            if (getZoomAmount() == 0) return;

            EntityWrapper entityWrapper = handData.getEntityWrapper();

            ScopeHandler scopeHandler = WeaponMechanics.getWeaponHandler().getScopeHandler();
            scopeHandler.updateZoom(entityWrapper, this, 0);
            setZoomStacks(0);
            if (hasZoomNightVision()) scopeHandler.useNightVision(entityWrapper, this);

            Mechanics zoomOffMechanics = getConfigurations().getObject(this.scopeWeaponTitle + ".Scope.Zoom_Off.Mechanics", Mechanics.class);
            if (zoomOffMechanics != null) zoomOffMechanics.use(new CastData(entityWrapper, this.scopeWeaponTitle, this.scopeWeaponStack));

            WeaponScopeEvent weaponScopeEvent = new WeaponScopeEvent(this.scopeWeaponTitle, this.scopeWeaponStack, entityWrapper.getEntity(), WeaponScopeEvent.ScopeType.OUT, 0, 0);
            Bukkit.getPluginManager().callEvent(weaponScopeEvent);
        }

        // This just ensures that these are set to null
        setScopeData(null, null);
    }

    public void setScopeData(String weaponTitle, ItemStack weaponStack) {
        this.scopeWeaponTitle = weaponTitle;
        this.scopeWeaponStack = weaponStack;
    }
}