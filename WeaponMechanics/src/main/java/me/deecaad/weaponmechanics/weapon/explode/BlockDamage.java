package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.weapon.damage.BlockDamageData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

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
    public BlockDamage serialize(File file, ConfigurationSection configurationSection, String path) {
        ConfigurationSection config = configurationSection.getConfigurationSection(path);

        boolean isBreakBlocks = config.getBoolean("Break_Blocks", false);
        int damage = config.getInt("Damage_Per_Hit", 1);
        int defaultBlockDurability = config.getInt("Default_Block_Durability", 1);
        boolean isBlacklist = config.getBoolean("Blacklist", false);

        if (damage < 0) {
            debug.error("Block_Damage Damage MUST be positive. Found: " + damage,
                    StringUtil.foundAt(file, path));
            return null;
        }

        if (defaultBlockDurability < 0) {
            debug.error("Block_Damage Default_Block_Durability MUST be positive. Found: " + defaultBlockDurability,
                    StringUtil.foundAt(file, path));
            return null;
        }

        List<String> strings = config.getStringList("Block_List");
        if (!isBlacklist && strings.isEmpty()) {
            debug.warn("No blocks can be broken by the Block_Damage!",
                    "This is most likely a mistake!", StringUtil.foundAt(file, path));
        }

        Map<Material, Integer> blockList = new HashMap<>(strings.size());
        for (String str : strings) {

            // Easy to forget to remove whitespace (or extra blank values). It
            // is not a fatal error, so we can ignore it.
            if (str == null || str.trim().isEmpty()) {
                debug.warn("Found an empty string in Block_List", StringUtil.foundAt(file, path + ".Block_List"));
                continue;
            }

            String[] split = StringUtil.split(str);

            // Will rarely happen, but sometimes people may add extra data. It
            // is not a fatal error, so we can ignore it.
            if (split.length > 2) {
                debug.warn("Found extra data for Block_List in \"" + str + "\". Expected <Material>-<Shots_To_Break>",
                        StringUtil.foundAt(file, path + ".Block_List"));
            }

            List<Material> materials = EnumUtil.parseEnums(Material.class, split[0]);
            int durability;

            try {
                durability = split.length > 1 ? Integer.parseInt(split[1]) : damage;
            } catch (NumberFormatException ex) {
                debug.error("Expected an Integer, Got: " + split[1], StringUtil.foundAt(file, path + ".Block_List"));
                continue;
            }

            // When the list is empty, the parseEnums method failed to
            // find any kind of match for the given material.
            if (materials.isEmpty()) {
                debug.error("Could not find any materials that matched \"" + split[0] + "\"",
                        "Remember that you may use '$' as a wildcard (Try $GLASS)",
                        StringUtil.debugDidYouMean(split[0], Material.class),
                        StringUtil.foundAt(file, path + ".Block_List"));
                continue;
            }

            materials.forEach(material -> blockList.put(material, durability));
        }

        strings = config.getStringList("Shots_To_Break_Blocks");
        Map<Material, Integer> shotsToBreak = new HashMap<>(strings.size());
        if (isBlacklist) {
            for (String str : strings) {

                // Easy to forget to remove whitespace (or extra blank values). It
                // is not a fatal error, so we can ignore it.
                if (str == null || str.trim().isEmpty()) {
                    debug.warn("Found an empty string in Shots_To_Break_Blocks", StringUtil.foundAt(file, path + ".Shots_To_Break_Blocks"));
                    continue;
                }

                String[] split = StringUtil.split(str);

                // Will rarely happen, but sometimes people may add extra data. It
                // is not a fatal error, so we can ignore it.
                if (split.length > 2) {
                    debug.warn("Found extra data for Block_List in \"" + str + "\". Expected <Material>-<Shots_To_Break>",
                            StringUtil.foundAt(file, path + ".Shots_To_Break_Blocks"));
                }

                List<Material> materials = EnumUtil.parseEnums(Material.class, split[0]);
                int durability;

                try {
                    durability = split.length > 1 ? Integer.parseInt(split[1]) : damage;
                } catch (NumberFormatException ex) {
                    debug.error("Expected an Integer, Got: " + split[1], StringUtil.foundAt(file, path + ".Shots_To_Break_Blocks"));
                    continue;
                }

                // When the list is empty, the parseEnums method failed to
                // find any kind of match for the given material.
                if (materials.isEmpty()) {
                    debug.error("Could not find any materials that matched \"" + split[0] + "\"",
                            "Remember that you may use '$' as a wildcard (Try using $GLASS)",
                            StringUtil.debugDidYouMean(split[0], Material.class),
                            StringUtil.foundAt(file, path + ".Block_List"));
                    continue;
                }

                materials.forEach(material -> shotsToBreak.put(material, durability));
            }
        } else if (!strings.isEmpty()) {
            debug.error("Error in Block_Damage!", "You tried to use Shots_To_Break_Blocks with Blacklist: true!",
                    "This doesn't make sense, since all materials/durability should be defined in Block_List",
                    StringUtil.foundAt(file, path));
        }

        return new BlockDamage(isBreakBlocks, damage, defaultBlockDurability, isBlacklist, blockList, shotsToBreak);
    }
}
