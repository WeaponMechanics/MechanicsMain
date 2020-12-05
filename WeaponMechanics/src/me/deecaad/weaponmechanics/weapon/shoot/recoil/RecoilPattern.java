package me.deecaad.weaponmechanics.weapon.shoot.recoil;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtils;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class RecoilPattern implements Serializer<RecoilPattern> {

    private boolean repeatPattern;
    private List<ExtraRecoilPatternData> recoilPatternList;

    /**
     * Empty constructor to be used as serializer
     */
    public RecoilPattern() {
    }

    public RecoilPattern(boolean repeatPattern, List<ExtraRecoilPatternData> recoilPatternList) {
        this.repeatPattern = repeatPattern;
        this.recoilPatternList = recoilPatternList;
    }

    /**
     * @return whether the pattern should be reset after reaching end
     */
    public boolean isRepeatPattern() {
        return repeatPattern;
    }

    /**
     * @return the recoil pattern list
     */
    public List<ExtraRecoilPatternData> getRecoilPatternList() {
        return recoilPatternList;
    }

    @Override
    public String getKeyword() {
        return "Recoil_Pattern";
    }

    @Override
    public RecoilPattern serialize(File file, ConfigurationSection configurationSection, String path) {
        List<?> list = configurationSection.getList(path + ".List");
        if (list == null || list.isEmpty()) return null;

        List<ExtraRecoilPatternData> recoilPatternList = new ArrayList<>();
        for (Object data : list) {
            String[] split = StringUtils.split(data.toString());
            if (split.length < 2) {
                debug.log(LogLevel.ERROR,
                        "Found an invalid recoil pattern format in configurations!",
                        "Located at file " + file + " in " + path + ".List" + " (" + data.toString() + ") in configurations",
                        "Correct format is <horizontal recoil>-<vertical recoil> OR <horizontal recoil>-<vertical recoil>-<chance to skip>%");
                continue;
            }

            float horizontalRecoil;
            float verticalRecoil;
            double chanceToSkip = 0;
            try {
                horizontalRecoil = Float.parseFloat(split[0]);
            } catch (NumberFormatException e) {
                debug.log(LogLevel.ERROR,
                        "Found an invalid value in configurations!",
                        "Located at file " + file + " in " + path + ".List (" + data.toString() + ") in configurations",
                        "Tried to get get float from " + split[0] + ", but it wasn't float?");
                continue;
            }
            try {
                verticalRecoil = Float.parseFloat(split[1]);
            } catch (NumberFormatException e) {
                debug.log(LogLevel.ERROR,
                        "Found an invalid value in configurations!",
                        "Located at file " + file + " in " + path + ".List (" + data.toString() + ") in configurations",
                        "Tried to get get float from " + split[1] + ", but it wasn't float?");
                continue;
            }
            if (split.length > 2) {
                try {
                    chanceToSkip = Double.parseDouble(split[2].split("%")[0]);
                } catch (NumberFormatException e) {
                    debug.log(LogLevel.ERROR,
                            "Found an invalid value in configurations!",
                            "Located at file " + file + " in " + path + ".List (" + data.toString() + ") in configurations",
                            "Tried to get get double from " + split[2].split("%")[0] + ", but it wasn't double?");
                    continue;
                }
                if (chanceToSkip > 100 || chanceToSkip < 0) {
                    debug.log(LogLevel.ERROR,
                            "Found an invalid value in configurations!",
                            "Located at file " + file + " in " + path + ".List (" + data.toString() + ") in configurations",
                            "Make sure that chance to skip is between 0 and 100 (" + split[2].split("%")[0] + ")");
                    continue;
                }
                // Convert to 0-1 range
                chanceToSkip *= 0.01;
            }
            recoilPatternList.add(new ExtraRecoilPatternData(horizontalRecoil, verticalRecoil, chanceToSkip));
        }
        if (recoilPatternList.isEmpty()) {
            debug.log(LogLevel.ERROR,
                    "For some reason any value in list wasn't valid!",
                    "Located at file " + file + " in " + path + ".List in configurations");
            return null;
        }

        boolean repeatPattern = configurationSection.getBoolean(path + ".Repeat_Pattern");
        return new RecoilPattern(repeatPattern, recoilPatternList);
    }

    public static class ExtraRecoilPatternData {

        private final float horizontalRecoil;
        private final float verticalRecoil;
        private final double chanceToSkip;

        public ExtraRecoilPatternData(float horizontalRecoil, float verticalRecoil, double chanceToSkip) {
            this.horizontalRecoil = horizontalRecoil;
            this.verticalRecoil = verticalRecoil;
            this.chanceToSkip = chanceToSkip;
        }

        /**
         * @return whether to skip this recoil pattern
         */
        public boolean shouldSkip() {
            return NumberUtils.chance(this.chanceToSkip);
        }

        /**
         * @return the horizontal recoil this should add
         */
        public float getHorizontalRecoil() {
            return horizontalRecoil;
        }

        /**
         * @return the vertical recoil this should add
         */
        public float getVerticalRecoil() {
            return verticalRecoil;
        }
    }
}