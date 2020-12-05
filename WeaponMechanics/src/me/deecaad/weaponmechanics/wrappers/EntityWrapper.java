package me.deecaad.weaponmechanics.wrappers;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.events.EntityToggleInMidairEvent;
import me.deecaad.weaponmechanics.events.EntityToggleStandEvent;
import me.deecaad.weaponmechanics.events.EntityToggleSwimEvent;
import me.deecaad.weaponmechanics.events.EntityToggleWalkEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

public class EntityWrapper implements IEntityWrapper {

    private static final int MOVE_TASK_INTERVAL = 1;

    private final LivingEntity entity;

    private int moveTask;
    private boolean standing;
    private boolean walking;
    private boolean inMidair;
    private boolean swimming;
    private HandData mainHandData;
    private HandData offHandData;

    public EntityWrapper(LivingEntity livingEntity) {
        this.entity = livingEntity;

        Configuration config = WeaponMechanics.getBasicConfigurations();
        if (!config.getBool("Disabled_Trigger_Checks.In_Midair")
                || !config.getBool("Disabled_Trigger_Checks.Standing_And_Walking")
                || !config.getBool("Disabled_Trigger_Checks.Jump")
                || !config.getBool("Disabled_Trigger_Checks.Double_Jump")) {
            this.moveTask = new MoveTask(this).runTaskTimer(WeaponMechanics.getPlugin(), 0, MOVE_TASK_INTERVAL).getTaskId();
        }
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
    public boolean isRightClicking() {
        // Always false for other entities than players
        // PlayerWrapper actually checks these
        return false;
    }

    @Override
    public HandData getMainHandData() {
        return mainHandData == null ? mainHandData = new HandData(this) : mainHandData;
    }

    @Override
    public HandData getOffHandData() {
        return offHandData == null ? offHandData = new HandData(this) : offHandData;
    }
}