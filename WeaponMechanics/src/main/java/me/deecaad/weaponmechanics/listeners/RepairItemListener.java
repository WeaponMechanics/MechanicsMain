package me.deecaad.weaponmechanics.listeners;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.*;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.utils.FileUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.shoot.CustomDurability;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RepairItemListener implements Listener {

    // Singleton pattern (kindof)
    private static RepairItemListener INSTANCE = null;

    public static RepairItemListener getInstance() {
        if (INSTANCE == null)
            INSTANCE = new RepairItemListener();
        return INSTANCE;
    }
    // end of singleton pattern


    public final Map<String, RepairKit> repairKits;

    private RepairItemListener() {
        File repairKitFolder = new File(WeaponMechanics.getPlugin().getDataFolder(), "repair_kits");
        repairKits = new HashMap<>();

        try {

            // Ensure the folder exists
            if (!repairKitFolder.exists())
                FileUtil.copyResourcesTo(getClass().getClassLoader().getResource("WeaponMechanics/repair_kits"), repairKitFolder.toPath());

            // Read in all files within the folder
            FileUtil.PathReference pathReference = FileUtil.PathReference.of(repairKitFolder.toURI());
            Files.walkFileTree(pathReference.path(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                    InputStream stream = Files.newInputStream(file);
                    YamlConfiguration config;

                    // Let DEVS at a readme file without breaking this.
                    if (file.endsWith("readme.txt")) {
                        WeaponMechanics.debug.debug("Found readme.txt at '" + file + "', skipping.");
                        return FileVisitResult.CONTINUE;
                    }

                    try {
                        config = new YamlConfiguration();
                        config.load(new InputStreamReader(stream));
                    } catch (InvalidConfigurationException ex) {
                        WeaponMechanics.debug.log(LogLevel.WARN, "Could not read file '" + file.toFile() + "'... make sure it is a valid YAML file");
                        return FileVisitResult.CONTINUE;
                    }

                    // For each key in the file, treat it as a new repair kit.
                    for (String key : config.getKeys(false)) {
                        try {
                            SerializeData data = new SerializeData(new RepairKit(), file.toFile(), key, new BukkitConfig(config));
                            RepairKit repairKit = data.of().serialize(RepairKit.class);

                            if (repairKits.containsKey(key))
                                throw data.exception(null, "Found duplicate Repair Kit name '" + key + "'");

                            repairKits.put(key, repairKit);
                        } catch (SerializerException ex) {
                            ex.log(WeaponMechanics.debug);
                        }
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Throwable e) {
            WeaponMechanics.debug.log(LogLevel.ERROR, "Some error occurred whilst reading repair_kits folder", e);
        }

        WeaponMechanics.debug.info("Registered " + repairKits.size() + " RepairKits");
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {

        // Don't let creative players use repair items, since this event loves
        // to cause item duplication.
        if (event instanceof InventoryCreativeEvent) {
            WeaponMechanics.debug.debug("Cannot use InventoryCreativeEvent for repair item");
            return;
        }

        // Avoid "special cases" by forcing the player to use their own
        // inventory (no chests or crafting inventories)
        if (event.getClickedInventory() instanceof PlayerInventory inventory) {
            if (!event.getWhoClicked().equals(inventory.getHolder()))
                return;

            ItemStack weapon = inventory.getItem(event.getSlot());
            String weaponTitle = weapon == null ? null : CustomTag.WEAPON_TITLE.getString(weapon);

            if (weapon == null)
                return;

            // When an item is completely broken, the config can be set, so it
            // is replaced with a broken "dummy" item. This prevents people from
            // losing their hard-earned weapons. This repair system is a bit more
            // complicated, so we handle it separately.
            if (CustomTag.BROKEN_WEAPON.hasString(weapon)) {
                repairBrokenItem(event);

            } else {

                // Only attempt to repair guns with proper repair items
                if (weaponTitle == null || event.getCursor() == null)
                    return;

                Configuration config = WeaponMechanics.getConfigurations();
                CustomDurability customDurability = config.getObject(weaponTitle + ".Shoot.Custom_Durability", CustomDurability.class);
                if (customDurability != null && customDurability.isRepairOnlyBroken()) {
                    return;
                }

                CastData cast = new CastData(event.getWhoClicked(), weaponTitle, weapon);
                repair(weapon, weaponTitle, event.getCursor(), cast);
            }
        }
    }

    /**
     * Handles repairing a completely broken item. Remember that when items are
     * completely broken, their type and meta have been completely changed to
     * a separate item.
     *
     * @param event The non-null click event involved.
     */
    public void repairBrokenItem(InventoryClickEvent event) {
        ItemStack weapon = event.getClickedInventory().getItem(event.getSlot());
        String weaponTitle = CustomTag.BROKEN_WEAPON.getString(weapon);
        CastData cast = new CastData(event.getWhoClicked(), weaponTitle, weapon);
        boolean isConsumedItem = repair(weapon, weaponTitle, event.getCursor(), cast);

        // Only change back to working weapon if durability changed
        if (!isConsumedItem)
            return;

        ItemStack weaponTemplate = WeaponMechanics.getWeaponHandler().getInfoHandler().generateWeapon(weaponTitle, 1);

        // Weapon no longer exists in config
        if (weaponTemplate == null) {
            WeaponMechanics.debug.debug(event.getWhoClicked() + " has old configuration of weapon '" + weaponTitle + "'");
            return;
        }

        CompatibilityAPI.getNBTCompatibility().copyTagsFromTo(weapon, weaponTemplate, "PublicBukkitValues");
        weapon.setType(weaponTemplate.getType());
        weapon.setItemMeta(weaponTemplate.getItemMeta());
        CustomTag.WEAPON_TITLE.setString(weapon, weaponTitle);
        CustomTag.BROKEN_WEAPON.remove(weapon);
    }

    public boolean repair(ItemStack weapon, String weaponTitle, ItemStack repairItem, CastData cast) {
        Configuration config = WeaponMechanics.getConfigurations();
        CustomDurability customDurability = config.getObject(weaponTitle + ".Shoot.Custom_Durability", CustomDurability.class);

        // We already know that the given event is for a broken weapon, but it
        // is possible that the server-admin deleted the Custom_Durability
        // section of the weapon AFTER somebody obtained a broken weapon
        if (customDurability == null)
            return false;

        // Special repair-kit item
        String repairKitTitle = repairItem.hasItemMeta() ? CustomTag.REPAIR_KIT_TITLE.getString(repairItem) : null;
        if (repairKitTitle != null) {
            RepairKit kit = repairKits.get(repairKitTitle);

            // Config changes AFTER user got item
            if (kit == null) {
                WeaponMechanics.debug.debug("RepairKit '" + repairKitTitle + "' no longer exists.");
                return false;
            }

            if (!kit.canUseWeapon(weaponTitle)) {
                return false;
            }

            int durability = CustomTag.DURABILITY.getInteger(weapon);
            int maxDurability = customDurability.getMaxDurability(weapon);
            int availableRepair = CustomTag.DURABILITY.getInteger(repairItem);

            // RepairKit has more durability than needed
            if (durability + availableRepair > maxDurability) {
                int consume = maxDurability - durability;
                CustomTag.DURABILITY.setInteger(weapon, maxDurability);
                CustomTag.DURABILITY.setInteger(repairItem, availableRepair - consume);
            }

            // RepairKit did not have enough durability
            else {
                CustomTag.DURABILITY.setInteger(weapon, durability + availableRepair);
                repairItem.setAmount(0);
                if (kit.getBreakMechanics() != null) kit.getBreakMechanics().use(cast);
            }

            if (kit.consumeOnUse) {
                repairItem.setAmount(0);
            }

            // When "overrideMaxDurabilityLoss" is -1, it is automatically set
            // to the proper value by this method
            customDurability.modifyMaxDurability(weapon, kit.getOverrideMaxDurabilityLoss());
            return true;
        }

        // Not a valid repair item... setAmount(1) is required to get by key in map.
        int availableItems = repairItem.getAmount();
        repairItem.setAmount(1);
        if (!customDurability.getRepairItems().containsKey(repairItem)) {
            repairItem.setAmount(availableItems);
            return false;
        }

        // Calculate how many items can possibly be consumed in order to
        // max out the weapons durability.
        int repairPerItem = customDurability.getRepairItems().get(repairItem);
        repairItem.setAmount(availableItems);
        int durability = CustomTag.DURABILITY.getInteger(weapon);
        int maxDurability = customDurability.getMaxDurability(weapon);

        // Only allow repairs
        if (maxDurability <= 0 || durability >= maxDurability) {
            if (customDurability.getDenyRepairMechanics() != null)
                customDurability.getDenyRepairMechanics().use(cast);
            return false;
        }

        // Consume items until the durability is maxed out, or until we run
        // out of items to repair with.
        while (availableItems > 0 && durability < maxDurability) {
            durability += repairPerItem;
            availableItems--;
            maxDurability = customDurability.modifyMaxDurability(weapon);
        }

        // Update durability and the repair material amount
        repairItem.setAmount(availableItems);
        CustomTag.DURABILITY.setInteger(weapon, Math.min(maxDurability, durability));
        if (customDurability.getRepairMechanics() != null)
            customDurability.getRepairMechanics().use(cast);

        return true;
    }

    @EventHandler
    public void onExp(PlayerExpChangeEvent event) {
        ItemStack weapon = event.getPlayer().getInventory().getItemInMainHand();
        String weaponTitle = !weapon.hasItemMeta() ? null : CustomTag.WEAPON_TITLE.getString(weapon);

        // Allow offhand repair as well
        if (weaponTitle == null) {
            weapon = event.getPlayer().getInventory().getItemInOffHand();
            weaponTitle = !weapon.hasItemMeta() ? null : CustomTag.WEAPON_TITLE.getString(weapon);

            // Player isn't holding any weapon
            if (weaponTitle == null)
                return;
        }

        Configuration config = WeaponMechanics.getConfigurations();
        CustomDurability customDurability = config.getObject(weaponTitle + ".Shoot.Custom_Durability", CustomDurability.class);

        // Weapon does not use durability.
        if (customDurability == null)
            return;

        int durability = CustomTag.DURABILITY.getInteger(weapon);
        int maxDurability = customDurability.getMaxDurability(weapon);

        int availableExp = event.getAmount();
        while (availableExp > 0 && durability < maxDurability) {
            durability += customDurability.getRepairPerExp();
            availableExp--;
        }

        // Repair the item and consume the experience.
        event.setAmount(availableExp);
        CustomTag.DURABILITY.setInteger(weapon, Math.min(maxDurability, durability));
    }


    /**
     * Repair-Kits are items that can be used to repair multiple different
     * weapons/armors, instead of redefining a repair-item for every weapon.
     */
    public static class RepairKit implements Serializer<RepairKit> {

        private ItemStack item;
        private int totalDurability;
        private int overrideMaxDurabilityLoss;
        private boolean blacklist;
        private Set<String> weapons;
        private Set<String> armors;
        private Mechanics breakMechanics;
        private boolean consumeOnUse;

        /**
         * Default constructor for serializers.
         */
        public RepairKit() {
        }

        public RepairKit(ItemStack item, int totalDurability, int overrideMaxDurabilityLoss, boolean blacklist,
                         Set<String> weapons, Set<String> armors, Mechanics breakMechanics, boolean consumeOnUse) {
            this.item = item;
            this.totalDurability = totalDurability;
            this.overrideMaxDurabilityLoss = overrideMaxDurabilityLoss;
            this.blacklist = blacklist;
            this.weapons = weapons;
            this.armors = armors;
            this.breakMechanics = breakMechanics;
            this.consumeOnUse = consumeOnUse;
        }

        public ItemStack getItem() {
            return item.clone();
        }

        public int getTotalDurability() {
            return totalDurability;
        }

        public int getOverrideMaxDurabilityLoss() {
            return overrideMaxDurabilityLoss;
        }

        public boolean isBlacklist() {
            return blacklist;
        }

        public Set<String> getWeapons() {
            return weapons;
        }

        public Set<String> getArmors() {
            return armors;
        }

        public Mechanics getBreakMechanics() {
            return breakMechanics;
        }

        public boolean isConsumeOnUse() {
            return consumeOnUse;
        }

        public void setConsumeOnUse(boolean consumeOnUse) {
            this.consumeOnUse = consumeOnUse;
        }

        /**
         * Returns <code>true</code> if this repair-kit can be used on a weapon
         * item with the given weapon-title.
         *
         * @param weaponTitle The non-null weapon-title of the weapon.
         * @return true if this kit can repair the weapon.
         */
        public boolean canUseWeapon(String weaponTitle) {
            return isBlacklist() != weapons.contains(weaponTitle);
        }

        /**
         * Returns <code>true</code> if this repair-kit can be used on a piece
         * of armor with the given armor-title.
         *
         * @param armorTitle The non-null armor-title of the armor.
         * @return true if this kit can repair the armor.
         */
        public boolean canUseArmor(String armorTitle) {
            return isBlacklist() != armors.contains(armorTitle);
        }

        @NotNull
        @Override
        public RepairKit serialize(@NotNull SerializeData data) throws SerializerException {
            String repairKitTitle = data.key.split("\\.")[0];

            int totalDurability = data.of("Total_Durability").assertPositive().assertExists().getInt();
            int overrideMaxDurabilityLoss = data.of("Override_Max_Durability_Loss").assertPositive().getInt(-1);
            boolean blacklist = data.of("Blacklist").getBool(false);
            Set<String> weapons = data.ofList("Weapons").addArgument(String.class, true).assertList().stream().map(arr -> arr[0]).collect(Collectors.toSet());
            Set<String> armors = data.ofList("Armors").addArgument(String.class, true).assertList().stream().map(arr -> arr[0]).collect(Collectors.toSet());

            data.of("Item").assertExists();
            Map<String, Object> tags = Map.of(CustomTag.DURABILITY.getKey(), totalDurability, CustomTag.REPAIR_KIT_TITLE.getKey(), repairKitTitle);
            ItemStack item = new ItemSerializer().serializeWithTags(data.move("Item"), tags);

            Mechanics breakMechanics = data.of("Break_Mechanics").serialize(Mechanics.class);
            boolean consumeOnUse = data.of("Consume_On_Use").getBool(false);

            return new RepairKit(item, totalDurability, overrideMaxDurabilityLoss, blacklist, weapons, armors, breakMechanics, consumeOnUse);
        }
    }
}
