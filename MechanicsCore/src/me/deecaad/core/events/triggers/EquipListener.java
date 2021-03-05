package me.deecaad.core.events.triggers;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.events.ArmorEquipEvent;
import me.deecaad.core.events.HandEquipEvent;
import me.deecaad.core.packetlistener.Packet;
import me.deecaad.core.packetlistener.PacketHandler;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipListener extends PacketHandler {

    public EquipListener() {
        super("PacketPlayOutSetSlot");
    }

    public static Map<EquipmentSlot, ItemStack> test = new HashMap<>();

    @Override
    public void onPacket(Packet wrapper) {

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

        ItemStack item = player.getEquipment().getItem(slot);
        ItemStack oldItem = test.getOrDefault(slot, item);

        if (hasChanges(oldItem, item)) {
            EquipmentSlot finalSlot = slot;
            Bukkit.getScheduler().runTask(MechanicsCore.getPlugin(), () -> {
                if (finalSlot == EquipmentSlot.HAND || slotNum == 45) {
                    Bukkit.getPluginManager().callEvent(new HandEquipEvent(player, item, finalSlot));
                } else {
                    Bukkit.getPluginManager().callEvent(new ArmorEquipEvent(player, item, finalSlot));
                }
            });
        }

        test.put(slot, item);
    }

    private boolean hasChanges(ItemStack ogStack, ItemStack other) {
        if (ogStack == null || ogStack.getType() == Material.AIR || other == null || other.getType() == Material.AIR) {
            return true;
        }

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
