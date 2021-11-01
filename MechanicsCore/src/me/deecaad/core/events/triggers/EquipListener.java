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
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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

    public static final EquipListener SINGLETON = new EquipListener();

    private final Set<Player> dropCancelledPlayers;
    private final Set<Player> ignoreGiveDropPlayers;

    private EquipListener() {
        dropCancelledPlayers = new HashSet<>();
        ignoreGiveDropPlayers = new HashSet<>();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        inject(event.getPlayer());
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSwap(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        Inventory inv = player.getInventory();

        // Swapping items doesn't cause any changes in the inventory, so it is
        // not covered by the player injection system.
        if (!(isEmpty(inv.getItem(e.getNewSlot())) && isEmpty(inv.getItem(e.getPreviousSlot())))) {
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

    @EventHandler (ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent event) {

        // The command line may look like "/give CJCrafter iron_ingot"
        String commandLine = event.getMessage().toLowerCase(Locale.ROOT);
        Player player = event.getPlayer();

        // The VANILLA give command has the unfortunate side effect of causing
        // a PlayerDropItemEvent (EssentialsX overrides it and fixes it). This
        // will cause a de-equip event (seemingly randomly depending on hotbar
        // slot), then an equip event (If the item given to the player goes
        // into their hand).
        if (commandLine.startsWith("/give") || commandLine.startsWith("/minecraft:give")) {
            Listener listener = new Listener() {
                @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
                public void onDrop(PlayerDropItemEvent event) {
                    if (player.equals(event.getPlayer())) {
                        ignoreGiveDropPlayers.add(player);
                        // event.setCancelled(true);
                        Bukkit.broadcastMessage("give command occurred: added " + player.getName() + " to ignore list");
                    }
                }
            };

            // Register, then unregister in 1 tick
            Bukkit.getPluginManager().registerEvents(listener, MechanicsCore.getPlugin());
            new BukkitRunnable() {
                @Override
                public void run() {
                    HandlerList.unregisterAll(listener);
                }
            }.runTask(MechanicsCore.getPlugin());
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        // When a drop event is cancelled, the item is still removed from the
        // player's inventory, but it is reset later. This causes invalid equip
        // events. By adding cancelled events into the set, we can then filter
        // the "false" equip events.
        if (event.isCancelled()) {
            dropCancelledPlayers.add(player);
            Bukkit.broadcastMessage("Drop event is cancelled... adding " + player.getName() + " to the ignore next list");
            return;
        } else if (ignoreGiveDropPlayers.remove(player)) {
            Bukkit.broadcastMessage("drop from /give cancelled, did not add " + player.getName() + " to list.");
            return;
        }

        PlayerInventory inv = player.getInventory();
        ItemStack item = inv.getItemInMainHand();

        // Only call event if the stack was emptied by dropping the last item
        // in a stack, or dropping an un-stackable item.
        if (isEmpty(item)) {
            Bukkit.getPluginManager().callEvent(new EquipEvent(player, EquipmentSlot.HAND, event.getItemDrop().getItemStack(), null));
        }
    }

    @SuppressWarnings("unchecked")
    public void inject(Player player) {
        Object handle = CompatibilityAPI.getCompatibility().getEntityPlayer(player);
        Object playerInventory = ReflectionUtil.invokeField(playerInventoryField, handle);

        List<Object> inventory = CompatibilityAPI.getEntityCompatibility().generateNonNullList(36, (old, current, index) -> {

            // Filters out cancelled PlayerDropItemEvent
            if (dropCancelledPlayers.remove(player)) {
                System.out.println("Reset ignore next for " + player.getName());
                return;
            }

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

    private static boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }
}
