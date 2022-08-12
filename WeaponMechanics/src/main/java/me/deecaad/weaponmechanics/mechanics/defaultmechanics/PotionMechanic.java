package me.deecaad.weaponmechanics.mechanics.defaultmechanics;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.SerializerOptionsException;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.IMechanic;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PotionMechanic implements IMechanic<PotionMechanic> {

    private List<PotionEffect> potionEffectList;

    /**
     * Empty constructor to be used as serializer
     */
    public PotionMechanic() {
        if (Mechanics.hasMechanic(getKeyword())) return;
        Mechanics.registerMechanic(WeaponMechanics.getPlugin(), this);
    }

    public PotionMechanic(List<PotionEffect> potionEffectList) {
        this.potionEffectList = potionEffectList;
    }

    @Override
    public void use(CastData castData) {
        castData.getCaster().addPotionEffects(this.potionEffectList);
    }

    @Override
    public boolean requireEntity() {
        return true;
    }

    @Override
    public String getKeyword() {
        return "Potion_Effects";
    }

    @Override
    public boolean shouldSerialize(SerializeData data) {
        // Let Mechanics handle auto serializer stuff
        return false;
    }

    @Override
    @Nonnull
    public PotionMechanic serialize(SerializeData data) throws SerializerException {

        // Uses the format: <PotionEffectType>-<Duration>-<Amplifier>-<Ambient>-<Hide>-<Icon>
        List<String[]> stringPotionList = data.ofList()
                .addArgument(PotionEffectType.class, true, true)
                .addArgument(int.class, true).assertArgumentPositive()
                .addArgument(int.class, true).assertArgumentPositive()
                .addArgument(boolean.class, false)
                .addArgument(boolean.class, false)
                .addArgument(boolean.class, false)
                .assertList().assertExists().get();

        List<PotionEffect> potionEffectList = new ArrayList<>();

        for (String[] split : stringPotionList) {

            PotionEffectType potionEffectType = PotionEffectType.getByName(split[0]);
            if (potionEffectType == null) {
                throw new SerializerOptionsException(this, "Potion Effect", Arrays.stream(PotionEffectType.values()).map(Object::toString).collect(Collectors.toList()), split[0], data.of().getLocation());
            }

            int duration = Integer.parseInt(split[1]);
            int amplifier = Integer.parseInt(split[2]) - 1; // subtract one since 0 is potion level 1

            if (amplifier < 1) amplifier = 1;

            boolean allowParticles = split.length <= 3 || Boolean.parseBoolean(split[3]);
            boolean produceMoreParticles = split.length > 4 && Boolean.parseBoolean(split[4]);
            boolean icon = split.length > 5 && Boolean.parseBoolean(split[5]);

            if (CompatibilityAPI.getVersion() < 1.14)
                potionEffectList.add(new PotionEffect(potionEffectType, duration, amplifier, produceMoreParticles, allowParticles));
            else
                potionEffectList.add(new PotionEffect(potionEffectType, duration, amplifier, produceMoreParticles, allowParticles, icon));
        }

        return new PotionMechanic(potionEffectList);
    }
}