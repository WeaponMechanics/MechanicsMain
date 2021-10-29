package me.deecaad.core.events.triggers;

import com.google.common.collect.ImmutableList;
import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.events.EquipEvent;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.List;

/**
 * This class triggers the {@link me.deecaad.core.events.EquipEvent}. This
 * event occurs when an {@link org.bukkit.inventory.EntityEquipment} is
 * changed.
 */
public class EquipListener implements Listener {

    // * ----- REFLECTIONS ----- * //
    private static final Field playerInventoryField;
    private static final Class<?> playerInventoryClass;
    private static final Field inventoryField;
    private static final Field armorField;
    private static final Field offHandField;
    private static final Field hotBarSlotField;
    private static final Field combinedField;

    static {
        Class<?> humanClass = ReflectionUtil.getNMSClass("world.entity.player", "EntityHuman");
        playerInventoryClass = ReflectionUtil.getNMSClass("world.entity.player", "PlayerInventory");
        playerInventoryField = ReflectionUtil.getField(humanClass, playerInventoryClass);

        // Used to get player inventory fields
        Class<?> nonNullListClass = ReflectionUtil.getNMSClass("core", "NonNullList");

        inventoryField = ReflectionUtil.getField(playerInventoryClass, nonNullListClass, 0);
        armorField = ReflectionUtil.getField(playerInventoryClass, nonNullListClass, 1);
        offHandField = ReflectionUtil.getField(playerInventoryClass, nonNullListClass, 2);
        combinedField = ReflectionUtil.getField(playerInventoryClass, List.class, 3); // index 3 since nonNonList is a List

        hotBarSlotField = ReflectionUtil.getField(playerInventoryClass, int.class, 0, true);
    }

    // * ----- END OF REFLECTIONS ----- * //

    private static boolean lock;

    public EquipListener() {
        if (lock)
            throw new IllegalStateException("EquipListener has already been initialized");
        lock = true;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        inject(e.getPlayer());
    }

    @EventHandler
    public void onSwap(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        Inventory inv = player.getInventory();
        if (isNotEmpty(inv.getItem(e.getNewSlot())) || isNotEmpty(inv.getItem(e.getPreviousSlot()))) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    ItemStack old = inv.getItem(e.getPreviousSlot());
                    ItemStack current = inv.getItem(e.getNewSlot());
                    Bukkit.getPluginManager().callEvent(new EquipEvent(player, EquipmentSlot.HAND, old, current));
                }
            }.runTask(MechanicsCore.getPlugin());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inv = player.getInventory();
        ItemStack item = inv.getItemInMainHand();

        if (!isNotEmpty(item)) {
            Bukkit.getPluginManager().callEvent(new EquipEvent(player, EquipmentSlot.HAND, event.getItemDrop().getItemStack(), null));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPickup(EntityPickupItemEvent event) {
    }

    @SuppressWarnings("unchecked")
    public void inject(Player player) {
        Object handle = CompatibilityAPI.getCompatibility().getEntityPlayer(player);
        Object playerInventory = ReflectionUtil.invokeField(playerInventoryField, handle);

        List<Object> inventory = CompatibilityAPI.getEntityCompatibility().generateNonNullList(36, (old, current, index) -> {
            int hotBar = (int) ReflectionUtil.invokeField(hotBarSlotField, playerInventory);

            // Not sure how important this check is, but the MC code does it.
            // I assume that means hot bar can mean something else.
            // TAKE NOTE that this code does not call an event when the hotBar
            // var is changed... We must use an event for that.
            if (hotBar >= 0 && hotBar < 9) {
                if (hotBar == index) {
                    Bukkit.getPluginManager().callEvent(new EquipEvent(player, EquipmentSlot.HAND, old, current));
                }
            }
        });
        List<Object> armor = CompatibilityAPI.getEntityCompatibility().generateNonNullList(4, (old, current, index) -> {
            EquipmentSlot slot;
            switch (index) {
                case 0:
                    slot = EquipmentSlot.FEET;
                    break;
                case 1:
                    slot = EquipmentSlot.LEGS;
                    break;
                case 2:
                    slot = EquipmentSlot.CHEST;
                    break;
                case 3:
                    slot = EquipmentSlot.HEAD;
                    break;
                default:
                    throw new IndexOutOfBoundsException("Index out of bounds: " + index + ", for list " + this);
            }

            Bukkit.getPluginManager().callEvent(new EquipEvent(player, slot, old, current));
        });
        List<Object> offhand = CompatibilityAPI.getEntityCompatibility().generateNonNullList(1, (old, current, index) -> {
            Bukkit.getPluginManager().callEvent(new EquipEvent(player, EquipmentSlot.OFF_HAND, old, current));
        });

        List<Object> oldItems = (List<Object>) ReflectionUtil.invokeField(inventoryField, playerInventory);
        for (int i = 0; i < oldItems.size(); i++) {
            inventory.set(i, oldItems.get(i));
        }
        List<Object> oldArmor = (List<Object>) ReflectionUtil.invokeField(armorField, playerInventory);
        for (int i = 0; i < oldArmor.size(); i++) {
            armor.set(i, oldArmor.get(i));
        }
        offhand.set(0, ((List<Object>) ReflectionUtil.invokeField(offHandField, playerInventory)).get(0));

        ReflectionUtil.setField(inventoryField, playerInventory, inventory);
        ReflectionUtil.setField(armorField, playerInventory, armor);
        ReflectionUtil.setField(offHandField, playerInventory, offhand);
        ReflectionUtil.setField(combinedField, playerInventory, ImmutableList.of(inventory, armor, offhand));
    }

    private static boolean isNotEmpty(ItemStack item) {
        return item != null && item.getType() != Material.AIR;
    }
}
