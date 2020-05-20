package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class RegenerationData implements Serializer<RegenerationData> {

    private final int ticksBeforeStart;
    private final int maxBlocksPerUpdate;
    private final int interval;

    /**
     * Empty constructor for serializer
     */
    public RegenerationData() {
        ticksBeforeStart = maxBlocksPerUpdate = interval = -1;
    }

    public RegenerationData(int ticksBeforeStart, int maxBlocksPerUpdate, int interval) {
        this.ticksBeforeStart = ticksBeforeStart;
        this.maxBlocksPerUpdate = maxBlocksPerUpdate;
        this.interval = interval;
    }

    public int getTicksBeforeStart() {
        return ticksBeforeStart;
    }

    public int getMaxBlocksPerUpdate() {
        return maxBlocksPerUpdate;
    }

    public int getInterval() {
        return interval;
    }

    public boolean isIgnoreMaxBlocks() {
        return interval == 0;
    }

    @Override
    public String getKeyword() {
        return "Regeneration";
    }

    @Override
    public RegenerationData serialize(File file, ConfigurationSection configurationSection, String path) {
        ConfigurationSection config = configurationSection.getConfigurationSection(path);

        int ticksBeforeStart = config.getInt("Ticks_Before_Start", 1200); // 1 minute, in ticks
        int maxBlocksPerUpdate = config.getInt("Max_Blocks_Per_Update", 1);
        int interval = config.getInt("Ticks_Between_Updates", 1);

        String foundIn = "Found in file " + file + " at path " + path;

        debug.validate(ticksBeforeStart > 0, "Ticks_Before_Start MUST be a positive number!", foundIn);
        debug.validate(maxBlocksPerUpdate > 0, "Max_Blocks_Per_Update MUST be a positive number!", foundIn);
        debug.validate(interval > 0, "Ticks_Between_Updates MUST be a positive number!", foundIn);

        debug.validate(LogLevel.DEBUG, interval == 0, "Interval is 0, ignoring max blocks per update", foundIn);

        return new RegenerationData(ticksBeforeStart, maxBlocksPerUpdate, interval);
    }
}
