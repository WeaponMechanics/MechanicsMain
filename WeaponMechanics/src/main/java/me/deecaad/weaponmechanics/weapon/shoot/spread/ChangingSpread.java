package me.deecaad.weaponmechanics.weapon.shoot.spread;

import me.deecaad.core.file.Serializer;
import me.deecaad.weaponmechanics.wrappers.HandData;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;

public class ChangingSpread implements Serializer<ChangingSpread> {

    private double startingAmount;
    private ModifySpreadWhen increaseChangeWhen;
    private Bounds bounds;

    /**
     * Empty constructor to be used as serializer
     */
    public ChangingSpread() { }

    public ChangingSpread(double startingAmount, ModifySpreadWhen increaseChangeWhen, Bounds bounds) {
        this.startingAmount = startingAmount;
        this.increaseChangeWhen = increaseChangeWhen;
        this.bounds = bounds;
    }

    /**
     * Applies all changes based on this changing spread.
     * After changes are applied, also entity wrapper's {@link HandData#getSpreadChange()} is modified
     * based on circumstances. This basically means that changes are always made for NEXT shot, not current.
     *
     * @param entityWrapper the entity wrapper used to check circumstances
     * @param tempSpread the spread
     * @param mainHand whether main hand was used
     * @param updateSpreadChange whether to allow updating current spread change
     * @return the modifier holder with updated horizontal and vertical values
     */
    public double applyChanges(IEntityWrapper entityWrapper, double tempSpread, boolean mainHand, boolean updateSpreadChange) {
        HandData handData = mainHand ? entityWrapper.getMainHandData() : entityWrapper.getOffHandData();

        // Reset if required
        if (handData.shouldReset()) handData.setSpreadChange(startingAmount);

        // Check bounds of spread change
        boolean didReset = false;
        if (bounds != null) didReset = bounds.checkBounds(handData, startingAmount);

        // Add the current spread before doing modifications to it
        tempSpread += handData.getSpreadChange();

        // Modify current changing spread only if its allowed
        // AND
        // If bounds didn't reset it
        if (updateSpreadChange && !didReset) {
            handData.setSpreadChange(increaseChangeWhen.applyChanges(entityWrapper, handData.getSpreadChange()));
        }

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

        double startingAmount = configurationSection.getDouble(path + ".Starting_Amount", 0) * 0.01;
        Bounds bounds = getBounds(configurationSection, path + ".Bounds");
        return new ChangingSpread(startingAmount, increaseChangeWhen, bounds);
    }

    private Bounds getBounds(ConfigurationSection configurationSection, String path) {
        double min = configurationSection.getDouble(path + ".Minimum", -1);
        double max = configurationSection.getDouble(path + ".Maximum", -1);

        if (min == -1 && max == -1) {
            return null;
        }

        // Just giving default values to avoid confusions when checking if value was ever even used
        if (max == -1) {
            max = 15;
        }

        if (min == -1) {
            min = 0;
        }

        if (min < 0.0) {
            min = 0.0;
        }

        boolean resetAfterReachingBound = configurationSection.getBoolean(path + ".Reset_After_Reaching_Bound");
        return new Bounds(resetAfterReachingBound, min * 0.01, max * 0.01);
    }

    public static class Bounds {

        private final boolean resetAfterReachingBound;
        private final double min;
        private final double max;

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
        public boolean checkBounds(HandData handData, double startingAmount) {
            double currentSpreadChange = handData.getSpreadChange();
            if (min != 0.0 && currentSpreadChange < min) {
                if (resetAfterReachingBound) {
                    handData.setSpreadChange(startingAmount);
                    return true;
                }
                handData.setSpreadChange(min);
                return false;
            } else if (max != 0.0 && currentSpreadChange > max) {
                if (resetAfterReachingBound) {
                    handData.setSpreadChange(startingAmount);
                    return true;
                }
                handData.setSpreadChange(max);
                return false;
            }
            return false;
        }
    }
}
