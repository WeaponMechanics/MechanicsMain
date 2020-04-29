package me.deecaad.core.inventory.entitydata;

import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

public class InventoryEntityData {

    public static Map<LivingEntity, InventoryEntityData> inventoryEntityData = new HashMap<>();
    private final LivingEntity livingEntity;
    private int currentWindow;

    public InventoryEntityData(LivingEntity livingEntity) {
        inventoryEntityData.put(livingEntity, this);
        this.livingEntity = livingEntity;
    }

    public static InventoryEntityData getInventoryEntityData(LivingEntity livingEntity) {
        return inventoryEntityData.get(livingEntity) != null ? inventoryEntityData.get(livingEntity) : new InventoryEntityData(livingEntity);
    }

    public static void removeInventoryEntityData(LivingEntity livingEntity) {
        inventoryEntityData.remove(livingEntity);
    }

    public static void shutdownInventoryEntityData() {
        inventoryEntityData.clear();
    }

    public LivingEntity getLivingEntity() {
        return this.livingEntity;
    }

    public int getCurrentWindow() {
        return this.currentWindow;
    }

    public void setCurrentWindow(int currentWindow) {
        this.currentWindow = currentWindow;
    }
}