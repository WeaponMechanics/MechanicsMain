package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ChanceSerializer;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.weapon.damage.BlockDamageData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class BlockDamage implements Serializer<BlockDamage> {

    private boolean isBreakBlocks;
    private int damage;
    private int defaultBlockDurability;
    private boolean isBlacklist;
    private double dropBlockChance;
    private Material defaultMask;
    private Map<Material, Integer> blockList;
    private Map<Material, Integer> shotsToBreak;
    private Map<Material, Material> maskMap;

    /**
     * Default constructor for serializers
     */
    public BlockDamage() {
    }

    /**
     * @param isBreakBlocks          true breaks blocks, false cracks them.
     * @param damage                 amount of damage done to a block, usually 1.
     * @param defaultBlockDurability default durability of whitelisted blocks
     * @param isBlacklist            true = damage all blocks but the ones in
     *                               blockList. false = damage ONLY the blocks
     *                               in blockList.
     * @param dropBlockChance        Chance 0..1 to drop a broken block as an item.
     * @param defaultMask            Mask material, usually AIR
     * @param blockList              blacklist/whitelist
     * @param shotsToBreak           used in case of blacklist
     * @param maskMap                per block mask type.
     */
    public BlockDamage(boolean isBreakBlocks, int damage, int defaultBlockDurability, boolean isBlacklist, double dropBlockChance,
                       Material defaultMask, Map<Material, Integer> blockList, Map<Material, Integer> shotsToBreak, Map<Material, Material> maskMap) {
        this.isBreakBlocks = isBreakBlocks;
        this.damage = damage;
        this.defaultBlockDurability = defaultBlockDurability;
        this.isBlacklist = isBlacklist;
        this.dropBlockChance = dropBlockChance;
        this.defaultMask = defaultMask == null ? BlockDamageData.MASK : defaultMask;
        this.blockList = blockList;
        this.shotsToBreak = shotsToBreak;
        this.maskMap = maskMap;
    }

    public boolean isBreakBlocks() {
        return isBreakBlocks;
    }

    public void setBreakBlocks(boolean breakBlocks) {
        isBreakBlocks = breakBlocks;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getDefaultBlockDurability() {
        return defaultBlockDurability;
    }

    public void setDefaultBlockDurability(int defaultBlockDurability) {
        this.defaultBlockDurability = defaultBlockDurability;
    }

    public boolean isBlacklist() {
        return isBlacklist;
    }

    public void setBlacklist(boolean blacklist) {
        isBlacklist = blacklist;
    }

    public double getDropBlockChance() {
        return dropBlockChance;
    }

    public void setDropBlockChance(double dropBlockChance) {
        this.dropBlockChance = dropBlockChance;
    }

    @Nonnull
    public Map<Material, Integer> getBlockList() {
        return blockList;
    }

    @Nonnull
    public Map<Material, Integer> getShotsToBreak() {
        return shotsToBreak;
    }

    public Material getMask(Block block) {
        return maskMap.getOrDefault(block.getType(), defaultMask);
    }

    public boolean isBlacklisted(Block block) {
        return isBlacklist == blockList.containsKey(block.getType());
    }

    public int getMaxDurability(Block block) {
        if (isBlacklist) {
            return shotsToBreak.getOrDefault(block.getType(), defaultBlockDurability);
        } else {
            return blockList.getOrDefault(block.getType(), defaultBlockDurability);
        }
    }

    /**
     * Damages the given <code>block</code>. The amount of damage dealt is
     * calculated as a percentage (e.x. 15% damage per hit, 7 hits to break).
     * This damage (obviously) stacks with previously dealt damage.
     *
     * <p>The returned data should be checked to determine if the block was
     * broken, and users of this method MUST handle block regeneration.
     *
     * <blockquote><pre>{@code
     *      BlockDamageData.DamageData data = blockDamage.damage(block, null);
     *      boolean regenerate = true;
     *
     *      if (regenerate) {
     *          if (blockDamage.isBreak() && data.isBroken()) {
     *              new BukkitRunnable() {
     *                  public void run() {
     *                      data.regenerate();
     *                      data.remove();
     *                  }
     *              }.runTaskLater(plugin, 200L); // 10 seconds
     *          }
     *      } else if (data.isBroken()) {
     *          data.remove();
     *      }
     * }</pre></blockquote>
     *
     * @param block  The non-null block to damage.
     * @param player The nullable player who is breaking block, explosions should always give null
     * @return The DamageData associated with the block or null if player couldn't damage the block.
     */
    @Nullable
    public BlockDamageData.DamageData damage(Block block, @Nullable Player player, boolean isRegenerate) {
        if (!isBlacklisted(block) && !BlockDamageData.isBroken(block)) {

            boolean dropItems = true;

            if (player != null) {
                // Allow nullable player since explosions should use EntityExplodeEvent
                // and pass null player for this method
                BlockBreakEvent breakEvent = new BlockBreakEvent(block, player) {
                    @Override
                    public @NotNull String getEventName() {
                        return "WeaponMechanicsBlockDamage";
                    }
                };
                Bukkit.getPluginManager().callEvent(breakEvent);

                // Couldn't damage the block
                if (breakEvent.isCancelled()) return null;

                // Added in 1.12
                if (ReflectionUtil.getMCVersion() >= 12) {
                    dropItems = breakEvent.isDropItems();
                }
            }

            // Calculate dropped blocks BEFORE the block is broken.
            Collection<ItemStack> drops = NumberUtil.chance(dropBlockChance) ? block.getDrops() : null;
            if (block.getState() instanceof InventoryHolder inv && !isRegenerate) {
                if (drops == null)
                    drops = new ArrayList<>();

                if (inv instanceof Chest)
                    Collections.addAll(drops, ((Chest) inv).getBlockInventory().getContents());
                else
                    Collections.addAll(drops, inv.getInventory().getContents());
            }

            int max = getMaxDurability(block);
            BlockDamageData.DamageData data = BlockDamageData.damage(block, (double) damage / (double) max, isBreakBlocks, isRegenerate, getMask(block));
            if (data.isBroken() && drops != null && dropItems) {

                Location location = block.getLocation();
                for (ItemStack item : drops) {
                    if (item != null)
                        block.getWorld().dropItemNaturally(location, item);
                }
            }

            return data;
        }

        return null;
    }

    @Override
    @NotNull
    public BlockDamage serialize(SerializeData data) throws SerializerException {
        boolean isBreakBlocks = data.of("Break_Blocks").getBool(false);
        int damage = data.of("Damage_Per_Hit").assertPositive().getInt(1);
        int defaultBlockDurability = data.of("Default_Block_Durability").assertPositive().getInt(1);
        boolean isBlacklist = data.of("Blacklist").getBool(false);
        Double dropChance = data.of("Drop_Broken_Block_Chance").serializeNonStandardSerializer(new ChanceSerializer());
        Material defaultMask = data.of("Default_Mask").getEnum(Material.class, BlockDamageData.MASK);

        SerializeData.ConfigListAccessor accessor = data.ofList("Block_List")
                .addArgument(Material.class, true);

        // The extra ~<Integer> tag is only useful for when whitelist is used.
        // When a blacklist is used, only the "Shots_To_Break_Blocks" tag can
        // be used to define the extra int tag.
        if (!isBlacklist)
            accessor.addArgument(int.class, false);

        // Only can use block mask when isBreakBlocks
        if (isBreakBlocks)
            accessor.addArgument(Material.class, false);

        // This does the bulk of our validation.
        List<String[]> strings = accessor.assertExists().assertList().get();

        if (!isBlacklist && strings.isEmpty()) {
            throw data.exception(null, "'Block_Damage' found that cannot break any blocks!",
                    "This happens when you use 'Blacklist: false' and an empty 'Block_List'");
        }

        Map<Material, Material> maskMap = new HashMap<>();
        Map<Material, Integer> blockList = new HashMap<>(strings.size());
        for (String[] split : strings) {

            List<Material> materials = EnumUtil.parseEnums(Material.class, split[0]);
            int durability = split.length > 1 ? Integer.parseInt(split[1]) : damage;
            Material mask = split.length > 2 ? Material.valueOf(split[2].toUpperCase(Locale.ROOT)) : null;

            materials.forEach(material -> blockList.put(material, durability));
            if (mask != null) materials.forEach(material -> maskMap.put(material, mask));
        }

        SerializeData.ConfigListAccessor temp = data.ofList("Shots_To_Break_Blocks")
                .addArgument(Material.class, true)
                .addArgument(int.class, true);

        // Only can use block mask when isBreakBlocks
        if (isBreakBlocks)
            temp.addArgument(Material.class, false);

        strings = temp.assertList().get();
        Map<Material, Integer> shotsToBreak = new HashMap<>(strings.size());

        if (isBlacklist) {
            for (String[] split : strings) {

                List<Material> materials = EnumUtil.parseEnums(Material.class, split[0]);
                int durability = Integer.parseInt(split[1]);
                Material mask = split.length > 2 ? Material.valueOf(split[2].toUpperCase(Locale.ROOT)) : null;

                materials.forEach(material -> shotsToBreak.put(material, durability));
                if (mask != null) materials.forEach(material -> maskMap.put(material, mask));
            }
        } else if (!strings.isEmpty()) {
            throw data.exception(null, "Found 'Block_Damage' that uses 'Shots_To_Break_Blocks' when 'Blacklist: false'",
                    "'Shots_To_Break_Blocks' should only be used when 'Blacklist: true'",
                    "Instead, copy and paste your values from 'Shots_To_Break_Blocks' to 'Block_List'");
        }

        if (dropChance == null || NumberUtil.equals(dropChance, 0.0)) {
            dropChance = 0.0;
        }

        if (data.has("Default_Mask") && !isBreakBlocks) {
            throw data.exception("Default_Mask", "Cannot use 'Default_Mask' when 'Break_Blocks: false'",
                    "We have to be able to BREAK the blocks before we can REPLACE them");
        }

        return new BlockDamage(isBreakBlocks, damage, defaultBlockDurability, isBlacklist, dropChance, defaultMask, blockList, shotsToBreak, maskMap);
    }
}
