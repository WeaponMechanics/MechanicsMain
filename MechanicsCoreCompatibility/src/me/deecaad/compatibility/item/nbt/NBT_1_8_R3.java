package me.deecaad.compatibility.item.nbt;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.utils.AttributeType;
import net.minecraft.server.v1_8_R3.NBTBase;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class NBT_1_8_R3 implements INBTCompatibility {

    @Override
    public String getCustomTag(ItemStack itemStack, String tag) {
        return getCustomTagFromNMSStack(CraftItemStack.asNMSCopy(itemStack), tag);
    }

    @Override
    public String getCustomTagFromNMSStack(Object nmsStack, String tag) {
        net.minecraft.server.v1_8_R3.ItemStack itemStack = (net.minecraft.server.v1_8_R3.ItemStack) nmsStack;

        NBTTagCompound itemTag = itemStack.getTag();
        if (itemTag == null) return null;

        NBTTagCompound bukkitTagCompound = itemTag.getCompound("PublicBukkitValues");
        if (bukkitTagCompound == null) return null;

        String key = MechanicsCore.getPlugin().getName().toLowerCase(Locale.ROOT) + ":" + tag.toLowerCase(Locale.ROOT);

        return bukkitTagCompound.hasKey(key) ? bukkitTagCompound.getString(key) : null;
    }

    @Override
    public ItemStack setCustomTag(ItemStack itemStack, String tag, String value) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);

        NBTTagCompound itemTag = nmsStack.getTag();
        if (itemTag == null) {
            itemTag = new NBTTagCompound();
            nmsStack.setTag(itemTag);
        }

        NBTTagCompound bukkitTagCompound = itemTag.getCompound("PublicBukkitValues");
        if (bukkitTagCompound == null) {
            bukkitTagCompound = new NBTTagCompound();
            itemTag.set("PublicBukkitValues", bukkitTagCompound);
        }

        String key = MechanicsCore.getPlugin().getName().toLowerCase(Locale.ROOT) + ":" + tag.toLowerCase(Locale.ROOT);

        bukkitTagCompound.setString(key, value);

        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    @Override
    public ItemStack setAttributeValue(ItemStack itemStack, AttributeType attributeType, double amount) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);

        NBTTagCompound itemTag = nmsStack.getTag();
        if (itemTag == null) {
            itemTag = new NBTTagCompound();
            nmsStack.setTag(itemTag);
        }

        if (itemTag.hasKey("AttributeModifiers")) {
            NBTTagList nbtTagList = (NBTTagList) itemTag.get("AttributeModifiers");
            boolean main = false;
            boolean off = false;

            for (int i = 0; i < nbtTagList.size(); i++) {
                NBTBase nbtBase = nbtTagList.g(i);

                if (nbtBase.getTypeId() != 10) {
                    continue;
                }
                NBTTagCompound nbtTagCompound = (NBTTagCompound) nbtBase;
                String nbtAttributeName = nbtTagCompound.getString("AttributeName");
                if (nbtAttributeName == null || nbtAttributeName.isEmpty() || !nbtAttributeName.equals(attributeType.getMinecraftName())) {
                    continue;
                }
                String nbtName = nbtTagCompound.getString("Name");
                if (nbtName == null || nbtName.isEmpty() || (!nbtName.equals("WM_Main") && !nbtName.equals("WM_Off"))) {
                    continue;
                }

                nbtTagCompound.setDouble("Amount", amount);

                String nbtSlot = nbtTagCompound.getString("Slot");
                if (nbtSlot != null && !nbtSlot.isEmpty()) {
                    if (nbtSlot.equals("mainHand")) {
                        main = true;
                    } else {
                        off = true;
                    }
                }
            }
            if (!main) {
                nbtTagList.add(createAttributeCompound(attributeType, amount, true));
            }
            if (!off) {
                nbtTagList.add(createAttributeCompound(attributeType, amount, false));
            }
        } else {
            NBTTagList nbtTagList = new NBTTagList();
            itemTag.set("AttributeModifiers", nbtTagList);

            nbtTagList.add(createAttributeCompound(attributeType, amount, true));
            nbtTagList.add(createAttributeCompound(attributeType, amount, false));
        }

        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    private NBTTagCompound createAttributeCompound(AttributeType attributeType, double amount, boolean mainhand) {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        nbtTagCompound.setString("AttributeName", attributeType.getMinecraftName());
        nbtTagCompound.setString("Name", (mainhand ? "WM_Main" : "WM_Off"));
        nbtTagCompound.setDouble("Amount", amount);
        nbtTagCompound.setInt("Operation", 0);
        if (mainhand) {
            nbtTagCompound.setLong("UUIDLeast", attributeType.getMainhandUUID().getLeastSignificantBits());
            nbtTagCompound.setLong("UUIDMost", attributeType.getMainhandUUID().getMostSignificantBits());
            nbtTagCompound.setString("Slot", "mainhand");
        } else {
            nbtTagCompound.setLong("UUIDLeast", attributeType.getOffhandUUID().getLeastSignificantBits());
            nbtTagCompound.setLong("UUIDMost", attributeType.getOffhandUUID().getMostSignificantBits());
            nbtTagCompound.setString("Slot", "offhand");
        }
        return nbtTagCompound;
    }
}
