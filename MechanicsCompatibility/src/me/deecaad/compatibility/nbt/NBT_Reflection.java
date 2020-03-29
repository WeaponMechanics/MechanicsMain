package me.deecaad.compatibility.nbt;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.AttributeType;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

public class NBT_Reflection implements INBTCompatibility {

    private static Method asNMSCopy;
    private static Method asBukkitCopy;

    private static Method setTag;
    private static Method getTag;

    private static Constructor<?> constructorNBTTagCompound;

    private static Method nbtTagCompoundHasKey;

    private static Method nbtTagCompoundGet;
    private static Method nbtTagCompoundGetString;

    private static Method nbtTagCompoundSet;
    private static Method nbtTagCompoundSetString;
    private static Method nbtTagCompoundSetDouble;
    private static Method nbtTagCompoundSetInt;
    private static Method nbtTagCompoundSetLong;

    private static Constructor<?> constructorNBTTagList;
    private static Method nbtTagListAdd;
    private static Field nbtTagListListField;

    private static Method nbtBaseGetTypeId;

    public NBT_Reflection() {
        Class<?> nbtTagCompoundClass = ReflectionUtil.getNMSClass("NBTTagCompound");
        Class<?> nmsItemStackClass = ReflectionUtil.getNMSClass("ItemStack");
        Class<?> nbtTagListClass = ReflectionUtil.getNMSClass("NBTTagList");
        Class<?> nbtBaseClass = ReflectionUtil.getNMSClass("NBTBase");

        asNMSCopy = ReflectionUtil.getMethod(ItemStack.class, "asNMSCopy", ItemStack.class);
        asBukkitCopy = ReflectionUtil.getMethod(ItemStack.class, "asBukkitCopy", nmsItemStackClass);

        setTag = ReflectionUtil.getMethod(nmsItemStackClass, "setTag", nbtTagCompoundClass);
        getTag = ReflectionUtil.getMethod(nmsItemStackClass, "getTag");

        constructorNBTTagCompound = ReflectionUtil.getConstructor(nbtTagCompoundClass);
        nbtTagCompoundHasKey = ReflectionUtil.getMethod(nbtTagCompoundClass, "hasKey", String.class);

        nbtTagCompoundGet = ReflectionUtil.getMethod(nbtTagCompoundClass, "get", String.class);
        nbtTagCompoundGetString = ReflectionUtil.getMethod(nbtTagCompoundClass, "getString", String.class);

        nbtTagCompoundSet = ReflectionUtil.getMethod(nbtTagCompoundClass, "set", String.class, nbtBaseClass);
        nbtTagCompoundSetString = ReflectionUtil.getMethod(nbtTagCompoundClass, "setString", String.class, String.class);
        nbtTagCompoundSetDouble = ReflectionUtil.getMethod(nbtTagCompoundClass, "setDouble", String.class, double.class);
        nbtTagCompoundSetInt = ReflectionUtil.getMethod(nbtTagCompoundClass, "setInt", String.class, int.class);
        nbtTagCompoundSetLong = ReflectionUtil.getMethod(nbtTagCompoundClass, "setLong", String.class, long.class);

        constructorNBTTagList = ReflectionUtil.getConstructor(nbtTagListClass);
        nbtTagListAdd = ReflectionUtil.getMethod(nbtTagListClass, "add", nbtBaseClass);
        nbtTagListListField = ReflectionUtil.getField(nbtTagListClass, "list");

        nbtBaseGetTypeId = ReflectionUtil.getMethod(nbtBaseClass, "getTypeId");
    }

    @Override
    public String getCustomTag(ItemStack itemStack, String tag) {
        Object nmsStack = ReflectionUtil.invokeMethod(asNMSCopy, null, itemStack);
        return getCustomTagFromNMSStack(nmsStack, tag);
    }

    @Override
    public String getCustomTagFromNMSStack(Object nmsStack, String tag) {
        Object itemTag = ReflectionUtil.invokeMethod(getTag, nmsStack);
        if (itemTag == null) return null; // Tag is missing

        Object bukkitTagCompound = ReflectionUtil.invokeMethod(nbtTagCompoundGet, itemTag, "PublicBukkitValues");
        if (bukkitTagCompound == null) return null; // Compound is missing

        // Make the key match NamespacedKey type
        String key = WeaponMechanics.getPlugin().getName().toLowerCase(Locale.ROOT) + ":" + tag.toLowerCase(Locale.ROOT);

        // Check that key exists and return its value or null otherwise
        return ((boolean) ReflectionUtil.invokeMethod(nbtTagCompoundHasKey, bukkitTagCompound, key)) ? ((String) ReflectionUtil.invokeMethod(nbtTagCompoundGetString, bukkitTagCompound, key)) : null;
    }

    @Override
    public ItemStack setCustomTag(ItemStack itemStack, String tag, String value) {

        Object nmsStack = ReflectionUtil.invokeMethod(asNMSCopy, null, itemStack);
        Object itemTag = ReflectionUtil.invokeMethod(getTag, nmsStack);
        if (itemTag == null) {

            // Compound not found, create new and add it
            itemTag = ReflectionUtil.newInstance(constructorNBTTagCompound);
            ReflectionUtil.invokeMethod(setTag, nmsStack, itemTag);
        }

        // Using "PublicBukkitValues" in case server updates to 1.13 R2 or newer server version
        // -> Server is able to automatically convert data
        Object bukkitTagCompound = ReflectionUtil.invokeMethod(nbtTagCompoundGet, itemTag, "PublicBukkitValues");
        if (bukkitTagCompound == null) {

            // Compound not found, create new and add it
            bukkitTagCompound = ReflectionUtil.newInstance(constructorNBTTagCompound);
            ReflectionUtil.invokeMethod(nbtTagCompoundSet, itemTag, "PublicBukkitValues", bukkitTagCompound);
        }

        // Make the key match NamespacedKey type
        String key = WeaponMechanics.getPlugin().getName().toLowerCase(Locale.ROOT) + ":" + tag.toLowerCase(Locale.ROOT);

        // Set the value for compound
        ReflectionUtil.invokeMethod(nbtTagCompoundSetString, bukkitTagCompound, key, value);

        // Convert back to bukkit itemstack
        return (ItemStack) ReflectionUtil.invokeMethod(asBukkitCopy, null, nmsStack);
    }

    @Override
    public ItemStack setAttributeValue(ItemStack itemStack, AttributeType attributeType, double amount) {
        Object nmsStack = ReflectionUtil.invokeMethod(asNMSCopy, null, itemStack);
        Object itemTag = ReflectionUtil.invokeMethod(getTag, nmsStack);
        if (itemTag == null) {

            // Compound not found, create new and add it
            itemTag = ReflectionUtil.newInstance(constructorNBTTagCompound);
            ReflectionUtil.invokeMethod(setTag, nmsStack, itemTag);
        }
        // Check if there is already AttributeModifiers NBTTagList
        Object nbtTagList = ReflectionUtil.invokeMethod(nbtTagCompoundGet, itemTag, "AttributeModifiers");
        if (nbtTagList != null) { // If there is, find if it contains WM attribute compounds
            List<?> listInsideNBTTagList = (List<?>) ReflectionUtil.invokeField(nbtTagListListField, nbtTagList);
            boolean main = false;
            boolean off = false;
            for (Object nbtBase : listInsideNBTTagList) {
                if (((byte) ReflectionUtil.invokeMethod(nbtBaseGetTypeId, nbtBase)) != 10) { // 10 means NBTTagCompound
                    // For some reason AttributeModifiers had NBTBase object which wasn't NBTTagCompound?
                    continue;
                }
                String nbtAttributeName = (String) ReflectionUtil.invokeMethod(nbtTagCompoundGetString, nbtBase, "AttributeName");
                if (nbtAttributeName == null || nbtAttributeName.isEmpty() || !nbtAttributeName.equals(attributeType.getMinecraftName())) {
                    continue;
                }
                String nbtName = (String) ReflectionUtil.invokeMethod(nbtTagCompoundGetString, nbtBase, "Name");
                if (nbtName == null || nbtName.isEmpty() || (!nbtName.equals("WM_Main") && !nbtName.equals("WM_Off"))) {
                    continue;
                }

                // Found WM attribute compound -> modify it
                ReflectionUtil.invokeMethod(nbtTagCompoundSetDouble, nbtBase, "Amount", amount);

                if (CompatibilityAPI.getVersion() >= 1.09) {
                    // Check for which slot that compound was
                    String nbtSlot = (String) ReflectionUtil.invokeMethod(nbtTagCompoundGetString, nbtBase, "Slot");
                    if (nbtSlot != null && !nbtSlot.isEmpty()) {
                        if (nbtSlot.equals("mainHand")) {
                            main = true;
                        } else {
                            off = true;
                        }
                    }
                } else {
                    // 1.8 didn't have Slot in attributes
                    main = true;
                }
            }
            if (!main) { // If attribute for main hand slot was not found -> add it
                ReflectionUtil.invokeMethod(nbtTagListAdd, nbtTagList, createAttributeCompound(attributeType, amount, true));
            }
            if (!off && CompatibilityAPI.getVersion() >= 1.09) { // If not found for off hand -> add it
                ReflectionUtil.invokeMethod(nbtTagListAdd, nbtTagList, createAttributeCompound(attributeType, amount, false));
            }
        } else {

            // List not found, create new and add it
            nbtTagList = ReflectionUtil.newInstance(constructorNBTTagList);
            ReflectionUtil.invokeMethod(nbtTagCompoundSet, itemTag, "AttributeModifiers", nbtTagList);

            ReflectionUtil.invokeMethod(nbtTagListAdd, nbtTagList, createAttributeCompound(attributeType, amount, true));
            if (CompatibilityAPI.getVersion() >= 1.09) {
                ReflectionUtil.invokeMethod(nbtTagListAdd, nbtTagList, createAttributeCompound(attributeType, amount, false));
            }
        }
        return (ItemStack) ReflectionUtil.invokeMethod(asBukkitCopy, null, nmsStack);
    }

    private static Object createAttributeCompound(AttributeType attributeType, double amount, boolean mainhand) {
        Object nbtTagCompound = ReflectionUtil.newInstance(constructorNBTTagCompound);
        ReflectionUtil.invokeMethod(nbtTagCompoundSetString, nbtTagCompound, "AttributeName", attributeType.getMinecraftName());
        ReflectionUtil.invokeMethod(nbtTagCompoundSetString, nbtTagCompound, "Name", (mainhand ? "WM_Main" : "WM_Off"));
        ReflectionUtil.invokeMethod(nbtTagCompoundSetDouble, nbtTagCompound, "Amount", amount);
        ReflectionUtil.invokeMethod(nbtTagCompoundSetInt, nbtTagCompound, "Operation", 0);
        if (mainhand) {
            ReflectionUtil.invokeMethod(nbtTagCompoundSetLong, nbtTagCompound, "UUIDLeast", attributeType.getMainhandUUID().getLeastSignificantBits());
            ReflectionUtil.invokeMethod(nbtTagCompoundSetLong, nbtTagCompound, "UUIDMost", attributeType.getMainhandUUID().getMostSignificantBits());
            if (CompatibilityAPI.getVersion() >= 1.09) {
                ReflectionUtil.invokeMethod(nbtTagCompoundSetString, nbtTagCompound, "Slot", "mainhand");
            }
        } else {
            ReflectionUtil.invokeMethod(nbtTagCompoundSetLong, nbtTagCompound, "UUIDLeast", attributeType.getOffhandUUID().getLeastSignificantBits());
            ReflectionUtil.invokeMethod(nbtTagCompoundSetLong, nbtTagCompound, "UUIDMost", attributeType.getOffhandUUID().getMostSignificantBits());
            ReflectionUtil.invokeMethod(nbtTagCompoundSetString, nbtTagCompound, "Slot", "offhand");
        }
        return nbtTagCompound;
    }
}