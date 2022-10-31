package me.deecaad.core.trigger;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.trigger.events.EntityToggleInMidairEvent;
import me.deecaad.core.trigger.events.EntityToggleStandEvent;
import me.deecaad.core.trigger.events.EntityToggleSwimEvent;
import me.deecaad.core.trigger.events.EntityToggleWalkEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;

public class TriggerEntity {

    private static final int MOVE_TASK_INTERVAL = 1;

    private final LivingEntity entity;

    private int triggerTask;
    private boolean standing;
    private boolean walking;
    private boolean inMidair;
    private boolean swimming;

    public TriggerEntity(LivingEntity livingEntity) {
        this.entity = livingEntity;

        FileConfiguration config = MechanicsCore.getPlugin().getConfig();
        if (!config.getBoolean("Disabled_Trigger_Checks.In_Midair")
                || !config.getBoolean("Disabled_Trigger_Checks.Standing_And_Walking")
                || !config.getBoolean("Disabled_Trigger_Checks.Jump")
                || !config.getBoolean("Disabled_Trigger_Checks.Double_Jump")) {
            this.triggerTask = new TriggerTask(this).runTaskTimer(MechanicsCore.getPlugin(), 0, MOVE_TASK_INTERVAL).getTaskId();
        }
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    public int getTriggerTaskId() {
        return this.triggerTask;
    }

    /**
     * Returns <code>true</code> when the entity is standing still. Returns
     * <code>false</code> when the entity is moving, swimming, or mid-air.
     *
     * @return <code>true</code> if the entity is standing still.
     */
    public boolean isStanding() {
        return this.standing;
    }

    void setStanding(boolean standing) {
        if (this.standing == standing) return;
        this.standing = standing;

        if (standing) {
            // -> Can't be walking, swimming, or mid-air at same time
            setWalking(false);
            setSwimming(false);
            setInMidair(false);
        }
        Bukkit.getPluginManager().callEvent(new EntityToggleStandEvent(entity, standing));
    }

    /**
     * Returns <code>true</code> when the entity is moving. Returns
     * <code>false</code> when the entity is standing still, swimming, or
     * mid-air.
     *
     * @return <code>true</code> if the entity is moving.
     */
    public boolean isWalking() {
        return this.walking;
    }

    void setWalking(boolean walking) {
        if (this.walking == walking) return;
        this.walking = walking;

        if (walking) {
            // -> Can't be standing, swimming, in mid-air at same time
            setStanding(false);
            setSwimming(false);
            setInMidair(false);
        }
        Bukkit.getPluginManager().callEvent(new EntityToggleWalkEvent(entity, walking));
    }

    /**
     * Returns <code>true</code> when the entity is mid-air (not on the
     * ground). Returns <code>false</code> when the entity is standing still,
     * swimming, or walking.
     *
     * @return <code>true</code> if the entity is mid-air.
     */
    public boolean isInMidair() {
        return this.inMidair;
    }

    void setInMidair(boolean inMidair) {
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

    /**
     * Returns <code>true</code> when the entity is swimming (legs and head are
     * both in water, also checks 1.13+ sprint swimming). Returns
     * <code>false</code> when the entity is standing still, mid-air, or
     * walking.
     *
     * @return <code>true</code> if the entity is swimming.
     */
    public boolean isSwimming() {
        return swimming;
    }

    void setSwimming(boolean swimming) {
        if (this.swimming == swimming) return;
        this.swimming = swimming;

        if (swimming) {
            // -> Can't be walking, standing, in mid-air at same time
            setWalking(false);
            setStanding(false);
            setInMidair(false);
        }
        Bukkit.getPluginManager().callEvent(new EntityToggleSwimEvent(entity, swimming));
    }
}