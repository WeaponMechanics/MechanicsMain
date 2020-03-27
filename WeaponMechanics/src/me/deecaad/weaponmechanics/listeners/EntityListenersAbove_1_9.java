package me.deecaad.weaponmechanics.listeners;

import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;

import static me.deecaad.weaponmechanics.WeaponMechanics.getBasicConfigurations;

public class EntityListenersAbove_1_9 implements Listener {

    private WeaponHandler weaponHandler;

    public EntityListenersAbove_1_9(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    @EventHandler (ignoreCancelled = true)
    public void toggleGlide(EntityToggleGlideEvent e) {
        if (getBasicConfigurations().getBool("Disabled_Trigger_Checks.Glide")) return;

        if (!e.getEntityType().isAlive()) return;
        weaponHandler.useTrigger((LivingEntity) e.getEntity(), e.isGliding() ? TriggerType.START_GLIDE : TriggerType.END_GLIDE, false);
    }
}