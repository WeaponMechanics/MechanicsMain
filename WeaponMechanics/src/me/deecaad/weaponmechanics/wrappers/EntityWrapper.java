package me.deecaad.weaponmechanics.wrappers;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.events.EntityToggleInMidairEvent;
import me.deecaad.weaponmechanics.events.EntityToggleStandEvent;
import me.deecaad.weaponmechanics.events.EntityToggleSwimEvent;
import me.deecaad.weaponmechanics.events.EntityToggleWalkEvent;
import me.deecaad.weaponmechanics.general.ColorType;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class EntityWrapper implements IEntityWrapper {

    private static final int MOVETASKINTERVAL = 0;

    private final LivingEntity entity;
    private int moveTask;
    
    public Map<Player, ColorType> thermalScopeData;

    private boolean standing;
    private boolean walking;
    private boolean inMidair;
    private boolean swimming;

    private boolean mainUsingFullAuto;
    private boolean offUsingFullAuto;

    private ZoomData zoomData;
    private SpreadChange spreadChange;

    public EntityWrapper(LivingEntity livingEntity) {
        this.entity = livingEntity;

        Configuration config = WeaponMechanics.getBasicConfigurations();
        if (!config.getBool("Disabled_Trigger_Checks.In_Midair")
                || !config.getBool("Disabled_Trigger_Checks.Standing_And_Walking")
                || !config.getBool("Disabled_Trigger_Checks.Jump")
                || !config.getBool("Disabled_Trigger_Checks.Double_Jump")) {
            this.moveTask = new MoveTask(this).runTaskTimer(WeaponMechanics.getPlugin(), 0, MOVETASKINTERVAL).getTaskId();
        }
        thermalScopeData = new HashMap<>();
    }

    @Override
    public LivingEntity getEntity() {
        return this.entity;
    }

    @Override
    public int getMoveTask() {
        return this.moveTask;
    }

    @Override
    public boolean isStanding() {
        return this.standing;
    }

    @Override
    public void setStanding(boolean standing) {
        if (this.standing == standing) return;
        this.standing = standing;

        if (standing) {
            // -> Can't be walking, swimming, in mid air at same time
            setWalking(false);
            setSwimming(false);
            setInMidair(false);
        }
        Bukkit.getPluginManager().callEvent(new EntityToggleStandEvent(entity, standing));
    }

    @Override
    public boolean isWalking() {
        return this.walking;
    }

    @Override
    public void setWalking(boolean walking) {
        if (this.walking == walking) return;
        this.walking = walking;

        if (walking) {
            // -> Can't be standing, swimming, in mid air at same time
            setStanding(false);
            setSwimming(false);
            setInMidair(false);
        }
        Bukkit.getPluginManager().callEvent(new EntityToggleWalkEvent(entity, walking));
    }
    
    @Override
    public boolean isInMidair() {
        return this.inMidair;
    }

    @Override
    public void setInMidair(boolean inMidair) {
        if (this.inMidair == inMidair) return;
        this.inMidair = inMidair;

        if (inMidair) {
            // -> Can't be walking, swimming, standing at same time
            setWalking(false);
            setSwimming(false);
            setStanding(false);
        }
        Bukkit.getPluginManager().callEvent(new EntityToggleInMidairEvent(entity, inMidair));
    }

    @Override
    public boolean isSwimming() {
        return swimming;
    }

    @Override
    public void setSwimming(boolean swimming) {
        if (this.swimming == swimming) return;
        this.swimming = swimming;

        if (swimming) {
            // -> Can't be walking, standing, in mid air at same time
            setWalking(false);
            setStanding(false);
            setInMidair(false);
        }
        Bukkit.getPluginManager().callEvent(new EntityToggleSwimEvent(entity, swimming));
    }

    @Override
    public boolean isZooming() {
        return zoomData != null && zoomData.isZooming();
    }

    @Override
    public ZoomData getZoomData() {
        return zoomData == null ? zoomData = new ZoomData() : zoomData;
    }

    @Override
    public SpreadChange getSpreadChange() {
        return spreadChange == null ? spreadChange = new SpreadChange() : spreadChange;
    }

    @Override
    public boolean isSneaking() {
        // Always false for other entities than players
        // PlayerWrapper actually checks these
        return false;
    }

    @Override
    public boolean isSprinting() {
        // Always false for other entities than players
        // PlayerWrapper actually checks these
        return false;
    }

    @Override
    public boolean isGliding() {
        return CompatibilityAPI.getVersion() >= 1.09 && entity.isGliding();
    }

    @Override
    public boolean isReloading(EquipmentSlot slot) {
        // todo
        return false;
    }

    @Override
    public boolean isRightClicking() {
        // Always false for other entities than players
        // PlayerWrapper actually checks these
        return false;
    }

    @Override
    public void setUsingFullAuto(EquipmentSlot equipmentSlot, boolean usingFullAuto) {
        if (equipmentSlot == EquipmentSlot.HAND) {
            mainUsingFullAuto = usingFullAuto;
            return;
        }
        offUsingFullAuto = usingFullAuto;
    }

    @Override
    public boolean isUsingFullAuto(EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.HAND ? mainUsingFullAuto : offUsingFullAuto;
    }

    @Nullable
    @Override
    public ColorType getThermalColor(@Nullable Player player) {
        return thermalScopeData.get(player);
    }
    
    
}
