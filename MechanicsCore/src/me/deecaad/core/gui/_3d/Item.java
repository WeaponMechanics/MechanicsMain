package me.deecaad.core.gui._3d;

import me.deecaad.core.utils.StringUtils;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Item {

    private EntityArmorStand armorStand;

    public Item(World world, double x, double y, double z, ItemStack display, String name) {

        //armorStand.setInvisible(true);
        setDisplay(display);
        setName(StringUtils.color(name));


        armorStand = new EntityArmorStand(((CraftWorld) world).getHandle(), x, y, z);
    }

    public void setLocation(Location loc) {
        armorStand.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public void setDisplay(ItemStack display) {
        armorStand.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(display));
    }

    public void setName(String name) {
        armorStand.setCustomName(ChatBaseComponent.ChatSerializer.a(name)); // todo this may not work
        armorStand.setCustomNameVisible(true);
    }

    public int getId() {
        return armorStand.getId();
    }

    /**
     * Displays this <code>Item</code> to the user by putting the
     * display item on the headslot of a fake armor stand, then telling
     * the player that the armorstand exists.
     *
     * @param player The player to display to
     * @param x x offset from the player
     * @param y y offset from the player
     * @param z z offset from the player
     * @return The integer id of the spawned entity
     */
    public int display(Player player, double x, double y, double z) {
        EntityArmorStand armorStand = new EntityArmorStand(((CraftWorld) player.getWorld()).getHandle(), x, y, z);

        PacketPlayOutSpawnEntity spawn = new PacketPlayOutSpawnEntity(armorStand);
        PacketPlayOutEntityMetadata meta = new PacketPlayOutEntityMetadata(armorStand.getId(), armorStand.getDataWatcher(), true);

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(spawn);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(meta);

        return armorStand.getId();
    }
}
