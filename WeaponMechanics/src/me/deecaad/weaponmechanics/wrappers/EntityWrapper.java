package me.deecaad.weaponmechanics.wrappers;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.events.EntityToggleInMidairEvent;
import me.deecaad.weaponmechanics.events.EntityToggleStandEvent;
import me.deecaad.weaponmechanics.events.EntityToggleWalkEvent;
import me.deecaad.weaponmechanics.general.ColorType;
import me.deecaad.weaponmechanics.weapon.shoot.spread.Spread;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
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
    private ZoomData zoomData;
    private SpreadChange spreadChange;

    public EntityWrapper(LivingEntity livingEntity) {
        this.entity = livingEntity;

        Configuration config = WeaponMechanics.getBasicConfigurations();
        if (!config.getBool("Disabled_Trigger_Checks.In_Midair") || !config.getBool("Disabled_Trigger_Checks.Standing_And_Walking") || !config.getBool("Disabled_Trigger_Checks.Jump")) {
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
        Bukkit.getPluginManager().callEvent(new EntityToggleInMidairEvent(entity, inMidair));
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
    public boolean isReloading() {
        // todo
        return false;
    }

    @Override
    public boolean isSwimming() {
        return CompatibilityAPI.getVersion() >= 1.13 && entity.isSwimming();
    }

    @Override
    public boolean isGliding() {
        return CompatibilityAPI.getVersion() >= 1.09 && entity.isGliding();
    }

    @Nullable
    @Override
    public ColorType getThermalColor(@Nullable Player player) {
        return thermalScopeData.get(player);
    }
    
    
}