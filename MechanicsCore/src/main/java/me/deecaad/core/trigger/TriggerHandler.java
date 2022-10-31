package me.deecaad.core.trigger;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TriggerHandler {

    private final List<TriggerListener> triggerListeners;

    public TriggerHandler() {
        triggerListeners = new ArrayList<>(5);
    }
}
