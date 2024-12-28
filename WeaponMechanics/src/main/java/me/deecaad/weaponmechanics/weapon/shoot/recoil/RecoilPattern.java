package me.deecaad.weaponmechanics.weapon.shoot.recoil;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ChanceSerializer;
import me.deecaad.core.file.simple.DoubleSerializer;
import me.deecaad.core.utils.RandomUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    @NotNull public RecoilPattern serialize(@NotNull SerializeData data) throws SerializerException {
        List<List<Optional<Object>>> list = data.ofList("List")
            .addArgument(new DoubleSerializer())
            .addArgument(new DoubleSerializer())
            .addArgument(new ChanceSerializer())
            .assertList();

        List<ExtraRecoilPatternData> recoilPatternList = new ArrayList<>();
        for (List<Optional<Object>> split : list) {

            float horizontalRecoil = ((Number) split.get(0).get()).floatValue();
            float verticalRecoil = ((Number) split.get(1).get()).floatValue();
            double chanceToSkip = (Double) split.get(2).get();

            recoilPatternList.add(new ExtraRecoilPatternData(horizontalRecoil, verticalRecoil, chanceToSkip));
        }

        boolean repeatPattern = data.of("Repeat_Pattern").getBool().orElse(false);
        return new RecoilPattern(repeatPattern, recoilPatternList);
    }

    public record ExtraRecoilPatternData(float horizontalRecoil, float verticalRecoil, double chanceToSkip) {

        /**
         * @return whether to skip this recoil pattern
         */
        public boolean shouldSkip() {
            return RandomUtil.chance(this.chanceToSkip);
        }
    }
}