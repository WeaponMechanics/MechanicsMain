package me.deecaad.core.events.triggers;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.events.EquipEvent;
import me.deecaad.core.events.HandDataUpdateEvent;
import me.deecaad.core.packetlistener.Packet;
import me.deecaad.core.packetlistener.PacketHandler;
import me.deecaad.core.utils.Debugger;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class EquipListener extends PacketHandler implements Listener {

    // Save an instance of this Array now since there is a significant overhead
    // in asking for an Enum's values.
    private static final EquipmentSlot[] SLOTS = EquipmentSlot.values();
    private static final Field windowField;
    private static final Field slotField;
    private static final Field itemField;

    static {
        Class<?> packetClass = ReflectionUtil.getPacketClass("PacketPlayOutSetSlot");
        windowField = ReflectionUtil.getField(packetClass, int.class, 0);
        slotField = ReflectionUtil.getField(packetClass, int.class, 1);
        itemField = ReflectionUtil.getField(packetClass, ReflectionUtil.getNMSClass("world.item", "ItemStack"));
    }

    // Order is dictated by the order of SLOTS ^
    private final Map<Player, ItemStack[]> previous;

    public EquipListener() {
        super("PacketPlayOutSetSlot");

        previous = new ConcurrentHashMap<>();

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

        // Determine if the packet deals with an item in an EquipmentSlot
        EquipmentSlot slot = getEquipmentSlot(player, slotNum);
        if (slot == null)
            return;

        ItemStack dequipped = getPreviousItem(player, slot);
        Object nmsEquipped = ReflectionUtil.invokeField(itemField, wrapper.getPacket());
        ItemStack equipped = CompatibilityAPI.getNBTCompatibility().getBukkitStack(nmsEquipped);

        boolean isDifferent = callEvent(player, slot, false, dequipped, equipped);

        if (!isDifferent && !Objects.equals(dequipped, equipped)) {
            HandDataUpdateEvent event = new HandDataUpdateEvent(player, slot, equipped, dequipped);
            Bukkit.getPluginManager().callEvent(event);
            wrapper.setCancelled(event.isCancelled());
        }
    }

    @EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null || !(e.getWhoClicked() instanceof Player))
            return;

        Player player = (Player) e.getWhoClicked();
        Inventory inv = e.getClickedInventory();

        // Make sure the opened inventory is a player inventory, and the
        // clicked slot is in the hotbar. The offhand is included in the
        // quickbar.
        if (inv.getType() != InventoryType.PLAYER)
            return;
        else if (e.getSlotType() != InventoryType.SlotType.QUICKBAR)
            return;

        int slotNum = e.getSlot();
        EquipmentSlot slot;
        if (slotNum == player.getInventory().getHeldItemSlot()) {
            slot = EquipmentSlot.HAND;
        } else if (slotNum == 40) {
            slot = EquipmentSlot.OFF_HAND;
        } else {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                callEvent(player, slot, false, getPreviousItem(player, slot), inv.getItem(slotNum));
            }
        }.runTask(MechanicsCore.getPlugin());
    }

    @EventHandler
    public void onSwap(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        Inventory inv = player.getInventory();
        if (!isEmpty(inv.getItem(e.getNewSlot())) || !isEmpty(inv.getItem(e.getPreviousSlot()))) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    callEvent(player, EquipmentSlot.HAND, true, getPreviousItem(player, EquipmentSlot.HAND), inv.getItem(e.getNewSlot()));
                }
            }.runTask(MechanicsCore.getPlugin());
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        System.out.println("drop");
        callEvent(e.getPlayer(), EquipmentSlot.HAND, false, getPreviousItem(e.getPlayer(), EquipmentSlot.HAND), e.getPlayer().getInventory().getItem(e.getPlayer().getInventory().getHeldItemSlot()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        insert(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        remove(e.getPlayer());
    }

    private boolean callEvent(Player player, EquipmentSlot slot, boolean skipCheck, ItemStack dequipped, ItemStack equipped) {
        if (!skipCheck && !isDifferent(equipped, dequipped)) {
            return false;
        }

        callEvent0(new EquipEvent(player, slot, dequipped, equipped));
        setPreviousItem(player, slot, equipped);
        player.getEquipment()
        return true;
    }

    private void callEvent0(EquipEvent event) {
        if (!Bukkit.getServer().isPrimaryThread()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.getPluginManager().callEvent(event);
                }
            }.runTask(MechanicsCore.getPlugin());
            return;
        }

        Bukkit.getPluginManager().callEvent(event);
    }

    private boolean isDifferent(ItemStack a, ItemStack b) {
        int nullCounter = 0;
        if (isEmpty(a)) nullCounter++;
        if (isEmpty(b)) nullCounter++;

        if (nullCounter == 2)
            return false;
        else if (nullCounter == 1)
            return true;

        if (a.getType() != b.getType())
            return true;
        if (a.getAmount() != b.getAmount())
            return true;
        if (a.hasItemMeta() != b.hasItemMeta())
            return true;
        if (a.hasItemMeta()) {
            ItemMeta aMeta = a.getItemMeta();
            ItemMeta bMeta = b.getItemMeta();

            if (!Objects.equals(aMeta.getDisplayName(), bMeta.getDisplayName()))
                return true;
            if (!Objects.equals(aMeta.getLore(), bMeta.getLore()))
                return true;
            if (!Objects.equals(aMeta.getEnchants(), bMeta.getEnchants()))
                return true;

            // This durability check will cause minor gun bobbing with skins, and
            // possibly gun bobbing with block/entity interactions, but without the
            // check, tools will not appear to lose durability
            if (CompatibilityAPI.getVersion() < 1.13) {
                return a.getDurability() != b.getDurability();
            } else {
                if (aMeta instanceof Damageable != bMeta instanceof Damageable)
                    return true;
                return aMeta instanceof Damageable && ((Damageable) aMeta).getDamage() != ((Damageable) bMeta).getDamage();
            }
        }

        return false;
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

            // Cache the player's equipment so we can check for changes later.
            items[i] = item.clone();
        }
    }

    private void remove(Player player) {
        if (!previous.containsKey(player))
            throw new IllegalArgumentException("Tried to remove a player that was not yet inserted!");

        ItemStack[] items = previous.get(player);

        for (int i = 0; i < SLOTS.length; i++) {
            EquipmentSlot slot = SLOTS[i];

            ItemStack item = items[i];
            if (item == null) continue;

            // Some plugins may need to "clean up".
            EquipEvent event = new EquipEvent(player, slot, item, null);
            Bukkit.getPluginManager().callEvent(event);
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

    private static boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }
}
