package me.deecaad.core.listeners;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.events.EntityEquipmentEvent;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ItemPotionsListener implements Listener {

    @EventHandler
    public void onEquip(EntityEquipmentEvent event) {
        if (!event.isEquipping())
            return;

        LivingEntity entity = (LivingEntity) event.getEntity();
        ItemStack item = event.getEquipped();
        String str = CompatibilityAPI.getNBTCompatibility().getString(item, "MechanicsCore", "potion-effects");

        // String being empty is actually an error made by another plugin
        // modifying the tag, that should never happen. Null tag means no
        // potions.
        if (str == null || str.isEmpty())
            return;

        String[] potions = str.split(",");
        for (String pot : potions) {
            String[] split = pot.split("~");

            PotionEffectType potionEffectType = PotionEffectType.getByName(split[0]);
            int amplifier = Integer.parseInt(split[1]) - 1;
            boolean ambient = split.length > 2 ? Boolean.parseBoolean(split[2]) : false;
            boolean hide = split.length > 3 ? Boolean.parseBoolean(split[3]) : true;
            boolean icon = split.length > 4 ? Boolean.parseBoolean(split[4]) : false;

            PotionEffect effect;
            if (ReflectionUtil.getMCVersion() < 14)
                effect = new PotionEffect(potionEffectType, Integer.MAX_VALUE, amplifier, ambient, hide);
            else
                effect = new PotionEffect(potionEffectType, Integer.MAX_VALUE, amplifier, ambient, hide, icon);


            entity.addPotionEffect(effect);
        }
    }

    @EventHandler
    public void onDequip(EntityEquipmentEvent event) {
        if (!event.isDequipping())
            return;

        LivingEntity entity = (LivingEntity) event.getEntity();
        ItemStack item = event.getDequipped();
        String str = CompatibilityAPI.getNBTCompatibility().getString(item, "MechanicsCore", "potion-effects");

        // String being empty is actually an error made by another plugin
        // modifying the tag, that should never happen. Null tag means no
        // potions.
        if (str == null || str.isEmpty())
            return;

        String[] potions = str.split(",");
        for (String pot : potions) {
            String[] split = pot.split("~");

            PotionEffectType potionEffectType = PotionEffectType.getByName(split[0]);
            entity.removePotionEffect(potionEffectType);
        }
    }
}
