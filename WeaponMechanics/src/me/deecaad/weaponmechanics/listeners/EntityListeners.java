package me.deecaad.weaponmechanics.listeners;

import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.events.EntityToggleInMidairEvent;
import me.deecaad.weaponmechanics.events.EntityToggleStandEvent;
import me.deecaad.weaponmechanics.events.EntityToggleWalkEvent;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class EntityListeners implements Listener {

    private WeaponHandler weaponHandler;

    public EntityListeners(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    @EventHandler
    public void death(EntityDeathEvent e) {
        // If entity had EntityWrapper data, remove it
        WeaponMechanics.removeEntityWrapper(e.getEntity());
    }

    @EventHandler
    public void toggleWalk(EntityToggleWalkEvent e) {
        // Whether this is used its checked already in MoveTask class
        weaponHandler.useTrigger(e.getLivingEntity(), e.isWalking() ? TriggerType.START_WALK : TriggerType.END_WALK, false);
    }

    @EventHandler
    public void toggleInMidair(EntityToggleInMidairEvent e) {
        // Whether this is used its checked already in MoveTask class
        weaponHandler.useTrigger(e.getLivingEntity(), e.isInMidair() ? TriggerType.START_IN_MIDAIR : TriggerType.END_IN_MIDAIR, false);
    }

    @EventHandler
    public void toggleStand(EntityToggleStandEvent e) {
        // Whether this is used its checked already in MoveTask class
        weaponHandler.useTrigger(e.getLivingEntity(), e.isStanding() ? TriggerType.START_STAND : TriggerType.END_STAND, false);
    }
}