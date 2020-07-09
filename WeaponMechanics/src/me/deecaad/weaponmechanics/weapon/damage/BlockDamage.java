package me.deecaad.weaponmechanics.weapon.damage;

import com.google.common.base.Enums;
import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class BlockDamage implements Serializer<BlockDamage> {

    private boolean isBreakBlocks;
    private int damage;
    private int regenTicks;
    private boolean isBlacklist;
    private Map<String, Integer> blockList;

    public BlockDamage(boolean isBreakBlocks, int damage, int regenTicks, boolean isBlacklist, Map<String, Integer> blockList) {
        this.isBreakBlocks = isBreakBlocks;
        this.damage = damage;
        this.regenTicks = regenTicks;
        this.isBlacklist = isBlacklist;
        this.blockList = blockList;
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

    public int getRegenTicks() {
        return regenTicks;
    }

    public void setRegenTicks(int regenTicks) {
        this.regenTicks = regenTicks;
    }

    public boolean isBlacklist() {
        return isBlacklist;
    }

    public void setBlacklist(boolean blacklist) {
        isBlacklist = blacklist;
    }

    public Map<String, Integer> getBlockList() {
        return blockList;
    }

    public void setBlockList(Map<String, Integer> blockList) {
        this.blockList = blockList;
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

            Integer i = blockList.get(mat + ":" + data);
            if (i == null) {
                return blockList.getOrDefault(mat, -1);
            } else {
                return i;
            }
        } else {
            return blockList.getOrDefault(block.getType().name(), -1);
        }
    }

    public void damage(Block block) {
        if (isBlacklisted(block)) {
            return;
        }

        int max = getMaxDurability(block);
        if (max == -1) {
            debug.error("blacklist check failed !!!");
        }

        BlockDamageData.damageBlock(block, damage, max, isBreakBlocks, regenTicks);
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
        int regenTicks = config.getInt("Regen_After", 1200); // 1 minute
        boolean isBlacklist = config.getBoolean("Blacklist", false);

        List<String> strings = config.getStringList("Block_List");
        if (!isBlacklist && strings.isEmpty()) {
            debug.warn("No blocks can be broken by the Block_Damage!",
                    "This is most likely a mistake!", StringUtils.foundAt(file, path));
        }

        Map<String, Integer> blockList = new LinkedHashMap<>(strings.size());
        for (String str : strings) {
            String[] split = StringUtils.split(str);

            try {
                String matAndByte = split[0].toUpperCase();
                int durability = split.length > 1 ? Integer.parseInt(split[1]) : damage;

                // We don't need to save this information, just validate that
                // the user input is correct for this bukkit version
                String mat = matAndByte.split(":")[0];
                Material type = Enums.getIfPresent(Material.class, mat).orNull();
                if (type == null) {
                    debug.error("Unknown Material found in config: " + mat);
                    continue;
                }

                blockList.put(matAndByte, durability);

            } catch (ArrayIndexOutOfBoundsException ex) {
                debug.error("Empty string in config Block_List!", StringUtils.foundAt(file, path));
                debug.log(LogLevel.DEBUG, ex);
            } catch (NumberFormatException ex) {
                debug.error("Invalid integer format: " + ex.getMessage(), StringUtils.foundAt(file, path));
                debug.log(LogLevel.DEBUG, ex);
            }
        }

        return new BlockDamage(isBreakBlocks, damage, regenTicks, isBlacklist, blockList);
    }
}
