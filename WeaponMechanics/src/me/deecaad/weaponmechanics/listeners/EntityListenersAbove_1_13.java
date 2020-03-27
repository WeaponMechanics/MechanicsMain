package me.deecaad.weaponmechanics.listeners;

import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleSwimEvent;

import static me.deecaad.weaponmechanics.WeaponMechanics.getBasicConfigurations;

public class EntityListenersAbove_1_13 implements Listener {

    private WeaponHandler weaponHandler;

    public EntityListenersAbove_1_13(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    @EventHandler (ignoreCancelled = true)
    public void toggleSwim(EntityToggleSwimEvent e) {
        if (getBasicConfigurations().getBool("Disabled_Trigger_Checks.Swim")) return;

        if (!e.getEntityType().isAlive()) return;
        weaponHandler.useTrigger((LivingEntity) e.getEntity(), e.isSwimming() ? TriggerType.START_SWIM_MODE : TriggerType.END_SWIM_MODE, false);
    }
}