package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.weaponmechanics.weapon.damage.BlockDamageData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockDamage implements Serializer<BlockDamage> {

    private boolean isBreakBlocks;
    private int damage;
    private int defaultBlockDurability;
    private boolean isBlacklist;
    private Map<Material, Integer> blockList;
    private Map<Material, Integer> shotsToBreak;

    /**
     * Default constructor for serializers
     */
    public BlockDamage() {
    }

    /**
     * See parameters.
     *
     * @param isBreakBlocks          <code>true</code> for broken blocks to be
     *                               replaced with air, <code>false</code> for
     *                               broken blocks to appear cracked.
     * @param damage                 The amount of damage to apply to a block.
     *                               Total damage is calculated using
     *                               <code>damage / blockDurability</code>.
     * @param defaultBlockDurability The default durability of each block that
     *                               is not specified in <code>blockList</code>
     *                               or <code>shotsToBreak</code>.
     * @param isBlacklist            <code>true</code> to turn <code>blockList</code>
     *                               into a blacklist, meaning all blocks on the list
     *                               will not be broken.
     * @param blockList              The blacklist or whitelist of blocks.
     * @param shotsToBreak           When <code>blacklist == true</code>, then use
     *                               this parameter to specify block durability.
     */
    public BlockDamage(boolean isBreakBlocks, int damage, int defaultBlockDurability, boolean isBlacklist, Map<Material, Integer> blockList, Map<Material, Integer> shotsToBreak) {
        this.isBreakBlocks = isBreakBlocks;
        this.damage = damage;
        this.defaultBlockDurability = defaultBlockDurability;
        this.isBlacklist = isBlacklist;
        this.blockList = blockList;
        this.shotsToBreak = shotsToBreak;
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

    @Nonnull
    public Map<Material, Integer> getBlockList() {
        return blockList;
    }

    @Nonnull
    public Map<Material, Integer> getShotsToBreak() {
        return shotsToBreak;
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
     *      BlockDamageData.DamageData data = blockDamage.damage(block);
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
     * @param block The non-null block to damage.
     * @return The DamageData associated with the block.
     */
    @Nullable
    public BlockDamageData.DamageData damage(Block block) {
        if (!isBlacklisted(block) && !BlockDamageData.isBroken(block)) {
            int max = getMaxDurability(block);

            return BlockDamageData.damage(block, (double) damage / (double) max, isBreakBlocks);
        }

        return null;
    }

    @Override
    public String getKeyword() {
        return "Block_Damage";
    }

    @Override
    @NotNull
    public BlockDamage serialize(SerializeData data) throws SerializerException {
        boolean isBreakBlocks = data.of("Break_Blocks").getBool(false);
        int damage = data.of("Damage_Per_Hit").assertPositive().getInt(1);
        int defaultBlockDurability = data.of("Default_Block_Durability").assertPositive().getInt(1);
        boolean isBlacklist = data.of("Blacklist").getBool(false);

        SerializeData.ConfigListAccessor accessor = data.ofList("Block_List")
                .addArgument(Material.class, true);

        // The extra ~<Integer> tag is only useful for when whitelist is used.
        // When a blacklist is used, only the "Shots_To_Break_Blocks" tag can
        // be used to define the extra int tag.
        if (!isBlacklist)
            accessor.addArgument(int.class, false);

        // This does the bulk of our validation.
        List<String[]> strings = accessor.assertExists().assertList().get();

        if (!isBlacklist && strings.isEmpty()) {
            throw data.exception(null, "'Block_Damage' found that cannot break any blocks!",
                    "This happens when you use 'Blacklist: false' and an empty 'Block_List'");
        }

        Map<Material, Integer> blockList = new HashMap<>(strings.size());
        for (String[] split : strings) {

            List<Material> materials = EnumUtil.parseEnums(Material.class, split[0]);
            int durability = split.length > 1 ? Integer.parseInt(split[1]) : damage;

            materials.forEach(material -> blockList.put(material, durability));
        }

        strings = data.ofList("Shots_To_Break_Blocks")
                .addArgument(Material.class, true)
                .addArgument(int.class, true)
                .assertList().get();
        Map<Material, Integer> shotsToBreak = new HashMap<>(strings.size());

        if (isBlacklist) {
            for (String[] split : strings) {

                List<Material> materials = EnumUtil.parseEnums(Material.class, split[0]);
                int durability = Integer.parseInt(split[1]);

                materials.forEach(material -> shotsToBreak.put(material, durability));
            }
        } else if (!strings.isEmpty()) {
            throw data.exception(null, "Found 'Block_Damage' that uses 'Shots_To_Break_Blocks' when 'Blacklist: false'",
                    "'Shots_To_Break_Blocks' should only be used when 'Blacklist: true'",
                    "Instead, copy and paste your values from 'Shots_To_Break_Blocks' to 'Block_List'");
        }

        return new BlockDamage(isBreakBlocks, damage, defaultBlockDurability, isBlacklist, blockList, shotsToBreak);
    }
}
