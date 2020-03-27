package me.deecaad.weaponmechanics.weapon.shoot.spread;

import me.deecaad.core.file.Serializer;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import me.deecaad.weaponmechanics.wrappers.SpreadChange;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;

public class ChangingSpread implements Serializer<ChangingSpread> {

    private long resetAfterMillis;
    private double startingAmount;
    private ModifySpreadWhen increaseChangeWhen;
    private Bounds bounds;

    /**
     * Empty constructor to be used as serializer
     */
    public ChangingSpread() { }

    public ChangingSpread(long resetAfterMillis, double startingAmount, ModifySpreadWhen increaseChangeWhen, Bounds bounds) {
        this.resetAfterMillis = resetAfterMillis;
        this.startingAmount = startingAmount;
        this.increaseChangeWhen = increaseChangeWhen;
        this.bounds = bounds;
    }

    /**
     * Applies all changes based on this changing spread.
     * After changes are applied, also entity wrapper's {@link IEntityWrapper#getSpreadChange()} is modified
     * based on circumstances. This basically means that changes are always made for NEXT shot, not current.
     *
     * @param entityWrapper the entity wrapper used to check circumstances
     * @param tempSpread the spread
     * @return the modifier holder with updated horizontal and vertical values
     */
    public double applyChanges(IEntityWrapper entityWrapper, double tempSpread) {
        SpreadChange spreadChange = entityWrapper.getSpreadChange();

        // Reset if required
        if (spreadChange.shouldReset(resetAfterMillis)) spreadChange.setSpreadChange(startingAmount);

        // Check bounds of spread change
        boolean didReset = false;
        if (bounds != null) didReset = bounds.checkBounds(spreadChange, startingAmount);

        // Add the current spread before doing modifications to it
        tempSpread += spreadChange.getCurrentSpreadChange();

        // Modify current spread if bounds didn't reset it
        if (!didReset) spreadChange.setSpreadChange(increaseChangeWhen.applyChanges(entityWrapper, spreadChange.getCurrentSpreadChange()));

        // Update spread change time
        spreadChange.updateResetTime();

        return tempSpread;
    }

    @Override
    public String getKeyword() {
        return "Changing_Spread";
    }

    @Override
    public ChangingSpread serialize(File file, ConfigurationSection configurationSection, String path) {
        ModifySpreadWhen increaseChangeWhen = new ModifySpreadWhen().serialize(file, configurationSection, path + ".Increase_Change_When");
        if (increaseChangeWhen == null) return null;

        long resetAfterMillis = configurationSection.getLong(path + ".Reset_After_Millis", 1500);
        double startingAmount = configurationSection.getDouble(path + ".Starting_Amount") * 0.1;
        Bounds bounds = getBounds(configurationSection, path + ".Bounds");
        return new ChangingSpread(resetAfterMillis, startingAmount, increaseChangeWhen, bounds);
    }

    private Bounds getBounds(ConfigurationSection configurationSection, String path) {
        double min = configurationSection.getDouble(path + ".Minimum_Spread") * 0.1;
        double max = configurationSection.getDouble(path + ".Maximum_Spread") * 0.1;

        if (min == 0.0 && max == 0.0) {
            return null;
        }

        // Just giving default values to avoid confusions when checking if value was ever even used
        if (min != 0.0 && max == 0.0) {
            max = 50;
        } else if (max != 0.0 && min == 0.0) {
            min = 0.00001;
        }

        if (min < 0.0) {
            min = 0.00001;
        }

        boolean resetAfterReachingBound = configurationSection.getBoolean(path + ".Reset_After_Reaching_Bound");
        return new Bounds(resetAfterReachingBound, min, max);
    }

    public static class Bounds {

        private boolean resetAfterReachingBound;
        private double min;
        private double max;

        public Bounds(boolean resetAfterReachingBound, double min, double max) {
            this.resetAfterReachingBound = resetAfterReachingBound;
            this.min = min;
            this.max = max;
        }

        /**
         * Checks bounds of spread
         *
         * @return whether or not the spread change was reset
         */
        public boolean checkBounds(SpreadChange spreadChange, double startingAmount) {
            double currentSpreadChange = spreadChange.getCurrentSpreadChange();
            if (min != 0.0 && currentSpreadChange < min) {
                if (resetAfterReachingBound) {
                    spreadChange.setSpreadChange(startingAmount);
                    return true;
                }
                spreadChange.setSpreadChange(min);
                return false;
            } else if (max != 0.0 && currentSpreadChange > max) {
                if (resetAfterReachingBound) {
                    spreadChange.setSpreadChange(startingAmount);
                    return true;
                }
                spreadChange.setSpreadChange(max);
                return false;
            }
            return false;
        }
    }
}
