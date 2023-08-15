package me.deecaad.core.listeners;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.mechanics.defaultmechanics.DamageMechanic;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class MechanicsCastListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!entity.hasMetadata(DamageMechanic.METADATA_KEY))
            return;

        DamageMechanic damageMechanic = (DamageMechanic) entity.getMetadata(DamageMechanic.METADATA_KEY).get(0).value();

        if (damageMechanic.isIgnoreArmor()) {
            event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0);
            event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, 0);
        }

        entity.removeMetadata(DamageMechanic.METADATA_KEY, MechanicsCore.getPlugin());
    }
}
