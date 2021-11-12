package me.deecaad.weaponmechanics.weapon.explode;

import com.google.common.base.Enums;
import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.weapon.damage.BlockDamageData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class BlockDamage implements Serializer<BlockDamage> {

    private boolean isBreakBlocks;
    private int damage;
    private int defaultBlockDurability;
    private boolean isBlacklist;
    private Map<String, Integer> blockList;
    private Map<String, Integer> shotsToBreak;

    /**
     * Default constructor for serializers
     */
    public BlockDamage() {
    }

    public BlockDamage(boolean isBreakBlocks, int damage, int defaultBlockDurability, boolean isBlacklist, Map<String, Integer> blockList, Map<String, Integer> shotsToBreak) {
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

    public int getDamage() {
        return damage;
    }

    public boolean isBlacklist() {
        return isBlacklist;
    }

    public Map<String, Integer> getBlockList() {
        return blockList;
    }

    public boolean isBlacklisted(Block block) {
        if (CompatibilityAPI.getVersion() < 1.13) {
            String mat = block.getType().name();
            byte data = block.getData();

            return isBlacklist == (blockList.containsKey(mat) || blockList.containsKey(mat + ":" + data));
        } else {
            return isBlacklist == blockList.containsKey(block.getType().name());
        }
    }

    public int getMaxDurability(Block block) {
        if (CompatibilityAPI.getVersion() < 1.13) {
            String mat = block.getType().name();
            byte data = block.getData();

            if (isBlacklist) {
                Integer i = shotsToBreak.get(mat + ":" + data);
                return i == null ? shotsToBreak.getOrDefault(mat, defaultBlockDurability) : i;
            } else {
                Integer i = blockList.get(mat + ":" + data);
                return i == null ? blockList.getOrDefault(mat, defaultBlockDurability) : i;
            }
        } else {
            if (isBlacklist) {
                return shotsToBreak.getOrDefault(block.getType().name(), defaultBlockDurability);
            } else {
                return blockList.getOrDefault(block.getType().name(), defaultBlockDurability);
            }
        }
    }

    public boolean damage(Block block, int regenTicks) {
        if (!isBlacklisted(block) && !BlockDamageData.isBroken(block)) {
            int max = getMaxDurability(block);
            BlockDamageData.damageBlock(block, damage, max, isBreakBlocks, regenTicks);

            return BlockDamageData.isBroken(block);
        }

        return false;
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
        } else if (defaultBlockDurability < 0) {
            debug.error("Block_Damage Default_Block_Durability MUST be positive. Found: " + defaultBlockDurability,
                    StringUtil.foundAt(file, path));
            return null;
        }

        List<String> strings = config.getStringList("Block_List");
        if (!isBlacklist && strings.isEmpty()) {
            debug.warn("No blocks can be broken by the Block_Damage!",
                    "This is most likely a mistake!", StringUtil.foundAt(file, path));
        }

        Map<String, Integer> blockList = new LinkedHashMap<>(strings.size());
        for (String str : strings) {
            String[] split = StringUtil.split(str);

            try {
                String matAndByte = split[0].toUpperCase();
                int durability = split.length > 1 ? Integer.parseInt(split[1]) : damage;

                // We don't need to save this information, just validate that
                // the user input is correct for this bukkit version
                String mat = matAndByte.split(":")[0];
                Material type = Enums.getIfPresent(Material.class, mat).orNull();
                if (type == null) {
                    debug.error("Unknown Material found in config: " + mat,
                            "You can check full material lists for your server version on the wiki (Use /wm wiki)",
                            StringUtil.foundAt(file, path));
                    continue;
                }

                blockList.put(matAndByte, durability);
            } catch (ArrayIndexOutOfBoundsException ex) {
                debug.error("Empty string in config Block_List!", StringUtil.foundAt(file, path));
                debug.log(LogLevel.DEBUG, ex);
            } catch (NumberFormatException ex) {
                debug.error("Invalid integer format: " + ex.getMessage(), StringUtil.foundAt(file, path));
                debug.log(LogLevel.DEBUG, ex);
            }
        }

        strings = config.getStringList("Shots_To_Break_Blocks");
        Map<String, Integer> shotsToBreak = new HashMap<>(strings.size());

        // Shots_To_Break_Blocks should only be used alongside Blacklist: true
        // This is because you cannot define blocks to be broken inside of a blacklist,
        // so all blocks will have the same durability
        if (isBlacklist) {

            for (String str : strings) {
                String[] split = StringUtil.split(str);

                try {
                    String matAndByte = split[0].toUpperCase();
                    int durability = split.length > 1 ? Integer.parseInt(split[1]) : damage;

                    // We don't need to save this information, just validate that
                    // the user input is correct for this bukkit version
                    String mat = matAndByte.split(":")[0];
                    Material type = Enums.getIfPresent(Material.class, mat).orNull();
                    if (type == null) {
                        debug.error("Unknown Material found in config: " + mat,
                                "You can check full material lists for your server version on the wiki (Use /wm wiki)",
                                StringUtil.foundAt(file, path));
                        continue;
                    }

                    shotsToBreak.put(matAndByte, durability);

                } catch (ArrayIndexOutOfBoundsException ex) {
                    debug.error("Empty string in config Block_List!", StringUtil.foundAt(file, path));
                    debug.log(LogLevel.DEBUG, ex);
                } catch (NumberFormatException ex) {
                    debug.error("Invalid integer format: " + ex.getMessage(), StringUtil.foundAt(file, path));
                    debug.log(LogLevel.DEBUG, ex);
                }
            }
        } else {
            debug.error("Error in Block_Damage!", "You tried to use Shots_To_Break_Blocks with Blacklist: true!",
                    "This doesn't make sense, since all materials/durability should be defined in Block_List",
                    StringUtil.foundAt(file, path));
        }

        return new BlockDamage(isBreakBlocks, damage, defaultBlockDurability, isBlacklist, blockList, shotsToBreak);
    }
}
