package me.deecaad.core.events.triggers;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.events.EquipEvent;
import me.deecaad.core.packetlistener.Packet;
import me.deecaad.core.packetlistener.PacketHandler;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class EquipListener0 extends PacketHandler {

    // Save an instance of this Array now since there is a significant overhead
    // in asking for an Enum's values.
    private static final EquipmentSlot[] SLOTS = EquipmentSlot.values();
    private static final Field windowField;
    private static final Field slotField;

    static {
        Class<?> packetClass = ReflectionUtil.getNMSClass("PacketPlayOutSetSlot");
        windowField = ReflectionUtil.getField(packetClass, "a");
        slotField = ReflectionUtil.getField(packetClass, "b");
    }

    // Order is dictated by the order of SLOTS ^
    private final Map<Player, ItemStack[]> previous;

    /**
     * See {@link net.minecraft.server.v1_16_R3.PacketPlayOutSetSlot}
     */
    public EquipListener0() {
        super("PacketPlayOutSetSlot");

        // No need to use a concurrent map since we jump to the main thread to
        // handle packets.
        previous = new HashMap<>();

        // Handle players that are already connected to the server. While this
        // class should always be instantiated before players join, better safe
        // then sorry.
        for (Player player : Bukkit.getOnlinePlayers())
            insert(player);
    }

    @Override
    public void onPacket(Packet wrapper) {
        Player player = wrapper.getPlayer();
        int window = (int) ReflectionUtil.invokeField(windowField, wrapper.getPacket());
        int slotNum = (int) ReflectionUtil.invokeField(slotField, wrapper.getPacket());

        // 0 is the player's inventory.
        if (window != 0)
            return;

        EquipmentSlot slot = getEquipmentSlot(player, slotNum);
        if (slot == null)
            return;

        new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack dequipped = getPreviousItem(player, slot);
                ItemStack equipped = player.getInventory().getItem(slot);

                EquipEvent event = new EquipEvent(player, slot, equipped, dequipped);
                Bukkit.getPluginManager().callEvent(event);
                equipped = event.getEquipped();
                player.getInventory().setItem(slot, equipped);

                setPreviousItem(player, slot, equipped);
            }
        }.runTask(MechanicsCore.getPlugin());

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        insert(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        remove(e.getPlayer());
    }

    private void insert(Player player) {
        if (previous.containsKey(player))
            throw new IllegalArgumentException("Tried to insert a player that has already been inserted!");

        ItemStack[] items = previous.computeIfAbsent(player, k -> new ItemStack[SLOTS.length]);

        for (int i = 0; i < SLOTS.length; i++) {
            EquipmentSlot slot = SLOTS[i];

            ItemStack item = player.getInventory().getItem(slot);
            if (item == null) continue;

            EquipEvent event = new EquipEvent(player, slot, null, item);
            Bukkit.getPluginManager().callEvent(event);
            item = event.getEquipped();
            player.getInventory().setItem(slot, item);

            // Cache the player's equipment so we can check for changes later.
            items[i] = item.clone();
        }
    }

    private void remove(Player player) {
        if (!previous.containsKey(player))
            throw new IllegalArgumentException();

        ItemStack[] items = previous.get(player);

        for (int i = 0; i < SLOTS.length; i++) {
            EquipmentSlot slot = SLOTS[i];

            ItemStack item = items[i];
            if (item == null) continue;

            // Some plugins may need to "clean up".
            EquipEvent event = new EquipEvent(player, slot, item, null);
            Bukkit.getPluginManager().callEvent(event);
            item = event.getEquipped();
            player.getInventory().setItem(slot, item);
        }
    }

    private EquipmentSlot getEquipmentSlot(Player player, int slot) {
        if (slot == 36 + player.getInventory().getHeldItemSlot())
            return EquipmentSlot.HAND;
        else {
            switch (slot) {
                case 5:
                    return EquipmentSlot.HEAD;
                case 6:
                    return EquipmentSlot.CHEST;
                case 7:
                    return EquipmentSlot.LEGS;
                case 8:
                    return EquipmentSlot.FEET;
                case 45:
                    return EquipmentSlot.OFF_HAND;
                default:
                    return null;
            }
        }
    }

    private ItemStack getPreviousItem(Player player, EquipmentSlot slot) {
        ItemStack[] items = previous.get(player);

        // While an array is technically O(n) search while a HashMap is O(1),
        // the RAM usage and overhead from slots being in the same "bucket" is
        // probably not worth it.
        for (int i = 0; i < SLOTS.length; i++) {
            if (SLOTS[i] == slot)
                return items[i];
        }

        throw new IllegalArgumentException("Unexpected Slot: " + slot);
    }

    private void setPreviousItem(Player player, EquipmentSlot slot, ItemStack item) {
        ItemStack[] items = previous.get(player);

        // While an array is technically O(n) search while a HashMap is O(1),
        // the RAM usage and overhead from slots being in the same "bucket" is
        // probably not worth it.
        for (int i = 0; i < SLOTS.length; i++) {
            if (SLOTS[i] == slot) {
                items[i] = item;
                return;
            }
        }

        throw new IllegalArgumentException("Unexpected Slot: " + slot);
    }
}
