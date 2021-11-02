package me.deecaad.weaponmechanics.packetlisteners;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.events.EquipEvent;
import me.deecaad.core.packetlistener.Packet;
import me.deecaad.core.packetlistener.PacketHandler;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OutSetSlotBobFix extends PacketHandler implements Listener {

    // * ----- REFLECTIONS ----- * //
    private static final Field windowField;
    private static final Field slotField;
    private static final Field itemField;

    static {
        Class<?> packetClass = ReflectionUtil.getPacketClass("PacketPlayOutSetSlot");
        windowField = ReflectionUtil.getField(packetClass, int.class, 0);
        slotField = ReflectionUtil.getField(packetClass, int.class, 1);
        itemField = ReflectionUtil.getField(packetClass, ReflectionUtil.getNMSClass("world.item", "ItemStack"));
    }

    // * ----- END OF REFLECTIONS ----- * //

    private final Map<Player, ItemStack> mainHandItem;
    private final Map<Player, ItemStack> offHandItem;

    public OutSetSlotBobFix(WeaponMechanics plugin) {
        super("PacketPlayOutSetSlot");

        mainHandItem = new HashMap<>();
        offHandItem = new HashMap<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            mainHandItem.put(player, player.getInventory().getItemInHand());

            if (CompatibilityAPI.getVersion() >= 1.09)
                offHandItem.put(player, player.getInventory().getItemInOffHand());
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEquip(EquipEvent event) {
        if (event.getEntityType() != EntityType.PLAYER)
            return;

        Player player = (Player) event.getEntity();

        if (event.getSlot() == EquipmentSlot.HAND)
            mainHandItem.put(player, event.getEquipped());
        else if (CompatibilityAPI.getVersion() >= 1.09 && event.getSlot() == EquipmentSlot.OFF_HAND)
            offHandItem.put(player, event.getEquipped());
    }

    @Override
    public void onPacket(Packet wrapper) {
        Player player = wrapper.getPlayer();
        int window = (int) ReflectionUtil.invokeField(windowField, wrapper.getPacket());
        int slotNum = (int) ReflectionUtil.invokeField(slotField, wrapper.getPacket());

        // 0 is the player's inventory.
        if (window != 0)
            return;

        // Check for main/off hand
        boolean mainHand = slotNum == 36 + player.getInventory().getHeldItemSlot();
        if (!mainHand && slotNum != 45)
            return;

        Object nmsEquipped = ReflectionUtil.invokeField(itemField, wrapper.getPacket());
        ItemStack equipped = CompatibilityAPI.getNBTCompatibility().getBukkitStack(nmsEquipped);
        ItemStack dequipped = mainHand ? mainHandItem.get(player) : offHandItem.get(player);

        player.sendMessage(wrapper.toString());

        // Only check when the item is a weapon stack
        String weaponTitle = CustomTag.WEAPON_TITLE.getString(equipped);
        if (weaponTitle == null)
            return;

        if (isDifferent(equipped, dequipped)) {
            if (mainHand)
                mainHandItem.put(player, equipped);
            else
                offHandItem.put(player, dequipped);

            player.sendMessage(ChatColor.RED + "Packet sent, probably");
        } else if (!Objects.equals(equipped, dequipped)) {
            player.sendMessage(ChatColor.GREEN + "Cancelled packet");
            wrapper.setCancelled(true);
        }
    }

    public boolean isDifferent(ItemStack a, ItemStack b) {
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

            if (CompatibilityAPI.getVersion() >= 1.14 && aMeta.getCustomModelData() != bMeta.getCustomModelData())
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

    public static boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }
}
