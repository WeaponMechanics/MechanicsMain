package me.deecaad.core.events.triggers;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.events.HandDataUpdateEvent;
import me.deecaad.core.packetlistener.Packet;
import me.deecaad.core.packetlistener.PacketHandler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EquipListener extends PacketHandler implements Listener {

    // Following the singleton design structure to prevent multiple instances
    // of this class
    public static final EquipListener SINGLETON = new EquipListener();
    
    private final Map<Player, Map<EquipmentSlot, ItemStack>> oldItems;
    
    private EquipListener() {
        super("PacketPlayOutSetSlot");
        
        this.oldItems = new ConcurrentHashMap<>();
    }

    
    @EventHandler
    public void onDisable(PluginDisableEvent e) {
        if (e.getPlugin() == MechanicsCore.getPlugin()) {
            oldItems.clear();
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        oldItems.remove(e.getPlayer());
    }

    @EventHandler (ignoreCancelled = true)
    public void itemHeld(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItem(e.getNewSlot());
        Bukkit.getPluginManager().callEvent(new HandEquipEvent(player, item, EquipmentSlot.HAND));

        Map<EquipmentSlot, ItemStack> temp = oldItems.computeIfAbsent(e.getPlayer(), k -> new ConcurrentHashMap<>(EquipmentSlot.values().length));
        if (item == null)
            temp.remove(EquipmentSlot.HAND);
        else
            temp.put(EquipmentSlot.HAND, item);
    }

    @EventHandler (ignoreCancelled = true)
    public void itemHeld(InventoryClickEvent e) {
        handleInventory(e);
    }

    @EventHandler (ignoreCancelled = true)
    public void itemHeld(InventoryDragEvent e) {
        handleInventory(e);
    }

    public void handleInventory(InventoryInteractEvent e) {
        HumanEntity player = e.getWhoClicked();

        // Set slot handles all creative changes
        if (player.getGameMode() == GameMode.CREATIVE) return;

        // Simply wait one tick
        Bukkit.getScheduler().runTask(MechanicsCore.getPlugin(), () -> {
            Map<EquipmentSlot, ItemStack> items = oldItems.computeIfAbsent((Player) player, k -> new ConcurrentHashMap<>(EquipmentSlot.values().length));

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack item = player.getEquipment().getItem(slot);
                ItemStack oldItem = items.get(slot);

                if (hasChanges(oldItem, item)) {
                    if (slot == EquipmentSlot.HAND || (CompatibilityAPI.getVersion() >= 1.09 && slot == EquipmentSlot.OFF_HAND)) {
                        Bukkit.getPluginManager().callEvent(new HandEquipEvent(player, item, slot));
                    } else {
                        Bukkit.getPluginManager().callEvent(new ArmorEquipEvent(player, item, slot));
                    }
                }

                if (item != null)
                    items.put(slot, item);
            }
        });
    }

    @Override
    public void onPacket(Packet wrapper) {
        
        // Check that its hotbar slot edit
        int windowId = (int) wrapper.getFieldValue("a");
        if (windowId != 0) {
            return;
        }

        Player player = wrapper.getPlayer();
        int slotNum = (int) wrapper.getFieldValue("b");

        EquipmentSlot slot = null;
        if (slotNum == player.getInventory().getHeldItemSlot() + 36) {
            slot = EquipmentSlot.HAND;
        } else {
            switch (slotNum) {
                case 45:
                    slot = EquipmentSlot.OFF_HAND;
                    break;
                case 5:
                    slot = EquipmentSlot.HEAD;
                    break;
                case 6:
                    slot = EquipmentSlot.CHEST;
                    break;
                case 7:
                    slot = EquipmentSlot.LEGS;
                    break;
                case 8:
                    slot = EquipmentSlot.FEET;
                    break;
                default:
                    break;
            }
        }

        if (slot == null) return;

        Map<EquipmentSlot, ItemStack> items = oldItems.computeIfAbsent(player, k -> new ConcurrentHashMap<>(EquipmentSlot.values().length));
        ItemStack item = player.getEquipment().getItem(slot);
        ItemStack oldItem = items.get(slot);

        EquipmentSlot finalSlot = slot;
        if (hasChanges(oldItem, item)) {
            Bukkit.getScheduler().runTask(MechanicsCore.getPlugin(), () -> {
                if (finalSlot == EquipmentSlot.HAND || slotNum == 45) {
                    Bukkit.getPluginManager().callEvent(new HandEquipEvent(player, item, finalSlot));
                } else {
                    Bukkit.getPluginManager().callEvent(new ArmorEquipEvent(player, item, finalSlot));
                }
            });
        } else if ((finalSlot == EquipmentSlot.HAND || slotNum == 45) && !hasDurabilityChanges(oldItem, item)) {

            // This event is used to cancel item popping up and down on NBT data changes
            HandDataUpdateEvent dataUpdateEvent = new HandDataUpdateEvent(player, finalSlot, item, oldItem);
            Bukkit.getPluginManager().callEvent(dataUpdateEvent);
            if (dataUpdateEvent.isCancelled()) {
                wrapper.setCancelled(true);
            }
        }

        items.put(slot, item);
    }

    private boolean hasDurabilityChanges(ItemStack ogStack, ItemStack other) {
        ItemMeta ogMeta = ogStack.getItemMeta();
        ItemMeta otherMeta = other.getItemMeta();

        if (CompatibilityAPI.getVersion() >= 1.132) {
            if (((Damageable) otherMeta).getDamage() != ((Damageable) ogMeta).getDamage()) {
                ((Damageable) otherMeta).setDamage(((Damageable) ogMeta).getDamage());
                return true;
            }
        } else if (other.getDurability() != ogStack.getDurability()) {
            return true;
        }

        return false;
    }

    private boolean hasChanges(ItemStack ogStack, ItemStack other) {

        // We need to check if either of the items are null to decide if there
        // have been changes first. Concurrent hashmaps cannot use null keys or
        // null values, though, so we have to watch for that.
        int nullCounter = 0;
        if (ogStack == null || ogStack.getType() == Material.AIR) nullCounter++;
        if (other == null || other.getType() == Material.AIR) nullCounter++;

        if (nullCounter == 1)
            return true;
        else if (nullCounter == 2)
            return false;

        double version = CompatibilityAPI.getVersion();
        if (ogStack.getType() != other.getType()) {
            return true;
        }
        if (version < 1.13 && ogStack.getData().getData() != other.getData().getData()) {
            return true;
        }
        ItemMeta ogMeta = ogStack.getItemMeta();
        ItemMeta otherMeta = other.getItemMeta();

        if (version >= 1.14) {
            if (ogMeta.hasCustomModelData() != otherMeta.hasCustomModelData()
                    || (ogMeta.hasCustomModelData() && otherMeta.getCustomModelData() != ogMeta.getCustomModelData())) {
                return true;
            }
        }

        if (ogMeta.hasDisplayName() != otherMeta.hasDisplayName()
                || (ogMeta.hasDisplayName() && !ogMeta.getDisplayName().equalsIgnoreCase(otherMeta.getDisplayName()))) {
            return true;
        }

        if (ogMeta.hasLore() != otherMeta.hasLore()
                || (ogMeta.hasLore() && !ogMeta.getLore().equals(otherMeta.getLore()))) {
            return true;
        }

        if (ogMeta.hasEnchants() != otherMeta.hasEnchants()
                || (ogMeta.hasEnchants() && !equals(ogMeta.getEnchants(), otherMeta.getEnchants()))) {
            return true;
        }

        // Durability change will not be considered as change

        return false;
    }

    private static boolean equals(Map<Enchantment, Integer> ench1, Map<Enchantment, Integer> ench2) {
        if (ench1 == ench2)
            return true;
        else if (ench1.size() != ench2.size())
            return false;
        else {
            List<Map.Entry<Enchantment, Integer>> list1 = new ArrayList<>(ench1.entrySet());
            List<Map.Entry<Enchantment, Integer>> list2 = new ArrayList<>(ench2.entrySet());

            for (int i = 0; i < list1.size(); i++) {
                Map.Entry<Enchantment, Integer> entry1 = list1.get(i);
                Map.Entry<Enchantment, Integer> entry2 = list2.get(i);

                if (!entry1.getKey().equals(entry2.getKey()))
                    return false;
                else if (!entry1.getValue().equals(entry2.getValue()))
                    return false;
            }

            return true;
        }
    }
}
