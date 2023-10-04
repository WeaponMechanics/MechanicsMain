package me.deecaad.weaponmechanics.weapon.shoot.recoil;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.utils.NumberUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class RecoilPattern implements Serializer<RecoilPattern> {

    private boolean repeatPattern;
    private List<ExtraRecoilPatternData> recoilPatternList;

    /**
     * Default constructor for serializer
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
    @NotNull
    public RecoilPattern serialize(@NotNull SerializeData data) throws SerializerException {
        List<String[]> list = data.ofList("List")
                .addArgument(double.class, true)
                .addArgument(double.class, true)
                .addArgument(String.class, false, true)
                .assertList().assertExists().get();

        List<ExtraRecoilPatternData> recoilPatternList = new ArrayList<>();
        for (String[] split : list) {

            float horizontalRecoil = Float.parseFloat(split[0]);
            float verticalRecoil = Float.parseFloat(split[1]);
            double chanceToSkip = split.length > 2 ? Double.parseDouble(split[2].split("%")[0]) : 0.0;

            if (chanceToSkip > 100 || chanceToSkip < 0) {
                throw data.exception(null, "Chance to skip should be between 0 and 100",
                        SerializerException.forValue(split[2]));
            }

            // Convert to 0-1 range
            chanceToSkip *= 0.01;
            recoilPatternList.add(new ExtraRecoilPatternData(horizontalRecoil, verticalRecoil, chanceToSkip));
        }

        boolean repeatPattern = data.of("Repeat_Pattern").getBool(false);
        return new RecoilPattern(repeatPattern, recoilPatternList);
    }

    public record ExtraRecoilPatternData(float horizontalRecoil, float verticalRecoil, double chanceToSkip) {

        /**
         * @return whether to skip this recoil pattern
         */
        public boolean shouldSkip() {
            return NumberUtil.chance(this.chanceToSkip);
        }
    }
}