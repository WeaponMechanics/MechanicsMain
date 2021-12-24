package me.deecaad.weaponmechanics.weapon.explode.regeneration;

import me.deecaad.core.file.Serializer;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class RegenerationData implements Serializer<RegenerationData> {

    private int ticksBeforeStart;
    private int maxBlocksPerUpdate;
    private int interval;

    /**
     * Empty constructor for serializer
     */
    public RegenerationData() {
    }

    public RegenerationData(int ticksBeforeStart, int maxBlocksPerUpdate, int interval) {
        this.ticksBeforeStart = ticksBeforeStart;
        this.maxBlocksPerUpdate = maxBlocksPerUpdate;
        this.interval = interval;
    }

    public int getTicksBeforeStart() {
        return ticksBeforeStart;
    }

    public void setTicksBeforeStart(int ticksBeforeStart) {
        this.ticksBeforeStart = ticksBeforeStart;
    }

    public int getMaxBlocksPerUpdate() {
        return maxBlocksPerUpdate;
    }

    public void setMaxBlocksPerUpdate(int maxBlocksPerUpdate) {
        this.maxBlocksPerUpdate = maxBlocksPerUpdate;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
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

        debug.validate(ticksBeforeStart >= 0, "Ticks_Before_Start MUST be a positive number!", foundIn);
        debug.validate(maxBlocksPerUpdate > 0, "Max_Blocks_Per_Update MUST be a positive number!", foundIn);
        debug.validate(interval > 0, "Ticks_Between_Updates MUST be a positive number!", foundIn);

        return new RegenerationData(ticksBeforeStart, maxBlocksPerUpdate, interval);
    }
}
